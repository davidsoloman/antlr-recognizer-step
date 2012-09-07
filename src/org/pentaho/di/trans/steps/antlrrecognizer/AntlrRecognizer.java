package org.pentaho.di.trans.steps.antlrrecognizer;

import java.io.File;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

import org.antlr.Tool;
import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CharStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.Lexer;
import org.antlr.runtime.Parser;
import org.antlr.runtime.TokenSource;
import org.antlr.runtime.TokenStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;


public class AntlrRecognizer extends BaseStep {
	
	private static Class<?> PKG = AntlrRecognizer.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	public static final String PLUGIN_BASE_LOC = "plugins/steps/AntlrRecognizer/";
	public static final String GRAMMAR_BASE_LOC = PLUGIN_BASE_LOC+"grammars/";
	
	private AntlrRecognizerMeta meta;

	private AntlrRecognizerData data;
	
	private Class<? extends Lexer> lexerClass;
	private Class<? extends Parser> parserClass;
	
	public AntlrRecognizer(StepMeta stepMeta,
			StepDataInterface stepDataInterface, int copyNr,
			TransMeta transMeta, Trans trans) {
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean init(StepMetaInterface smi, StepDataInterface sdi) {
		meta = (AntlrRecognizerMeta) smi;
		data = (AntlrRecognizerData) sdi;
		
		if (super.init(smi, sdi)) {
			
			try {
			
				String filename = environmentSubstitute(meta.getGrammarFilename());

				if (Const.isEmpty(filename)) {
					logError(BaseMessages.getString(PKG, "AntlrRecognizer.MissingFilename.Message")); //$NON-NLS-1$
					return false;
				}

				if(!Const.isEmpty(filename))
	        	{
	        		data.file=KettleVFS.getFileObject(filename, getTransMeta());
	        	
	        		// Check if file
	        		if(data.file.exists()) {
	        			
	        			String grammarName = FilenameUtils.getBaseName(filename);
	        			String grammarDirName = (GRAMMAR_BASE_LOC + grammarName + File.separatorChar).replace('/', File.separatorChar);
	        			String lexerClassName = grammarName + "Lexer";
	        			String parserClassName = grammarName + "Parser";
	        			File parserFile = new File(grammarDirName + parserClassName + ".java");
	        			
	        			// Check to see if the grammar has changed since last code generation, use the Parser filename
	        			if(!parserFile.exists() || FileUtils.isFileNewer(new File(filename), parserFile)) {
	        				
		        			// Regenerate Java source for grammar
		        			Tool antlrTool = new Tool(new String[]{"-o",grammarDirName,filename});
		        			antlrTool.process();
		        			
		        			// Use javac if present, because of Janino bug JANINO-147
	        				String command = "javac -cp "+PLUGIN_BASE_LOC+"lib/antlrworks-1.4.3.jar;"+PLUGIN_BASE_LOC+"ANTLRRecognizer.jar -d "+grammarDirName+" "+grammarDirName+"*.java";
        					Process p = Runtime.getRuntime().exec(command.replace('/',File.separatorChar));
        					if(p.waitFor() != 0) {
        						StringWriter writer = new StringWriter();
        						IOUtils.copy(p.getErrorStream(), writer, "UTF-8");
        						String theString = writer.toString();
        						throw new Exception("Error during compilation: "+theString);
        					}
	        					
	        				// Remove annotations from generated code (Janino doesn't support annotations)
		        			/*for(File javaSourceFile : (Collection<File>)FileUtils.listFiles(new File(grammarDirName), new String[]{"java"}, true)) {
		        				String content = IOUtils.toString(new FileInputStream(javaSourceFile));
		        				content = content.replaceAll("\\s*@.*", "");
		        				IOUtils.write(content, new FileOutputStream(javaSourceFile));
		        				
			        			
		        			}
	        			
	        				// Compile ANTLR-generated Java files using Janino
		        			try {
		        				cl = new JavaSourceClassLoader(
		        				    this.getClass().getClassLoader(),  // parentClassLoader
		        				    new File[] { new File(grammarDirName) }, // optionalSourcePath
		        				    (String) null,                     // optionalCharacterEncoding
		        				    DebuggingInformation.NONE          // debuggingInformation
		        				);
		        			}
		        			catch(Exception e) {
		        				log.logError("Janino error", e);
		        			}*/
		        			
	        			}
	        			
	        			URLClassLoader ucl = new URLClassLoader(
	        				new URL[]{(new File(grammarDirName)).toURI().toURL(),
	        					new File(grammarDirName + lexerClassName+".class").toURI().toURL(),
	        					new File(grammarDirName + parserClassName+".class").toURI().toURL()}, getClass().getClassLoader());
	        			lexerClass = (Class<? extends Lexer>) ucl.loadClass(lexerClassName);
    					parserClass = (Class<? extends Parser>) ucl.loadClass(parserClassName);
	        		}
	           	}
			}
			catch(Exception e) {
				log.logError("Couldn't initialize ANTLR Recognizer!", e);
				return false;
			}
			return true;
		}
		return false;
		
	}
	
	@Override
	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException {
		
		meta = (AntlrRecognizerMeta) smi;
		data = (AntlrRecognizerData) sdi;

		Object[] row = getRow();

		if (row == null) // no more input to be expected...
		{
			setOutputDone();
			return false;
		}

		try {
			if (first) // we just got started
			{
				first = false;
				
				// get the RowMeta
				data.previousRowMeta = getInputRowMeta().clone();
				data.NrPrevFields=data.previousRowMeta.size();
				data.outputRowMeta = data.previousRowMeta;
				meta.getFields(data.outputRowMeta, getStepname(), null, null, this);
			}
			
			
			Object[] outputRow = RowDataUtil.allocateRowData(data.outputRowMeta.size());
    		for (int i = 0; i < data.NrPrevFields; i++)
    		{
    			outputRow[i] = row[i];
    		}
    		
    		String contentField = data.previousRowMeta.getString(row, data.previousRowMeta.indexOfValue(meta.getContentFieldname()));
    		CharStream input = new ANTLRStringStream(contentField);
    		Constructor<?> lexerCtor = null;
    		for(Constructor<?> ctor : lexerClass.getConstructors()) {
    			Class<?>[] params = ctor.getParameterTypes();
    			if(params.length == 1 && params[0].getName().equals(CharStream.class.getName())) {
    				lexerCtor = ctor;
    			}
    		}
    		
    		Object lexer = lexerCtor.newInstance(CharStream.class.cast(input));
    		CommonTokenStream tokens = new CommonTokenStream((TokenSource) lexer);
    		
    		Constructor<?> parserCtor = null;
    		for(Constructor<?> ctor : parserClass.getConstructors()) {
    			Class<?>[] params = ctor.getParameterTypes();
    			if(params.length == 1 && params[0].getName().equals(TokenStream.class.getName())) {
    				parserCtor = ctor;
    			}
    		}
    		
    		Object parser = parserCtor.newInstance(tokens);
    		
    		/*StringTemplateGroup templates = new StringTemplateGroup(new FileReader("Java.stg"),
				    AngleBracketTemplateLexer.class);
    		//parser.setTemplateLib(templates);
    		parserClass.getMethod("setTemplateLib", StringTemplateGroup.class).invoke(parser, templates);
    		*/
    		
    		Method entryPoint = parserClass.getMethod(meta.getRuleName());
    		
    		boolean valid = false;
    		try {
    			entryPoint.invoke(parser, (Object[])null);
    			valid = true;
    		}
    		catch(Exception e) {
    			if(e.getCause() instanceof AntlrParseException) {
    				//TODO valid = false handling
    			}
    		}
    		//System.out.println(r.getTemplate().toString());
        	
    		// Add result field to input stream
    		outputRow[data.NrPrevFields]= valid;
    		//int rowIndex=data.NrPrevFields;
    		//rowIndex++;

			 //	add new values to the row.
	        putRow(data.outputRowMeta, outputRow);  // copy row to output rowset(s);
			
		}
		catch(Exception e)
        {
			boolean sendToErrorRow = false;
			String errorMessage = null;

        	if (getStepMeta().isDoingErrorHandling())
        	{
                  sendToErrorRow = true;
                  errorMessage = e.toString();
        	}
        	else
        	{
	            logError(BaseMessages.getString(PKG, "AntlrRecognizer.ErrorInStepRunning"),e); //$NON-NLS-1$
	            setErrors(1);
	            stopAll();
	            setOutputDone();  // signal end to receiver(s)
	            return false;
        	}
        	if (sendToErrorRow)
        	{
        	   // Simply add this row to the error row
        	   putError(getInputRowMeta(), row, 1, errorMessage, meta.getResultFieldName(), "AntlrRecognizerO01");
        	}
        }
            
        return true;
    }
		
	@Override
	public void dispose(StepMetaInterface smi, StepDataInterface sdi) {
		super.dispose(smi, sdi);
	}
}
