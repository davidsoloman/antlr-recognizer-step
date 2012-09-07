package org.pentaho.di.trans.steps.antlrrecognizer;

import java.util.List;
import java.util.Map;

import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Counter;
import org.pentaho.di.core.annotations.Step;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.w3c.dom.Node;

@Step(
		id = "AntlrRecognizer",
		name = "AntlrRecognizer.Step.Name",
		description = "AntlrRecognizer.Step.Description",
		image = "antlr32x32.png",//"AntlrRecognizer.png",
		categoryDescription="i18n:org.pentaho.di.trans.step:BaseStep.Category.Transform",
		i18nPackageName="org.pentaho.di.trans.steps.antlrrecognizer"
	)
public class AntlrRecognizerMeta extends BaseStepMeta implements
		StepMetaInterface {
	
	private static Class<?> PKG = AntlrRecognizerMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private String grammarFilename;
	
	private String ruleName;
	
	private String contentFieldname;
	
	/** function result: new value name */
    private String       resultFieldName;
	
	public String getResultFieldName() {
		return resultFieldName;
	}

	public void setResultFieldName(String resultfieldname) {
		this.resultFieldName = resultfieldname;
	}

	public AntlrRecognizerMeta() {
		super(); // allocate BaseStepMeta
	}
	
	@Override
	public void setDefault() {
		 resultFieldName = "result"; //$NON-NLS-1$
		 ruleName = "";
		 contentFieldname="";
	}

	private void readData(Node stepnode) throws KettleXMLException
	{
		try
		{
			grammarFilename = XMLHandler.getTagValue(stepnode, "grammarFilename");
			ruleName = XMLHandler.getTagValue(stepnode, "rulename");
			contentFieldname = XMLHandler.getTagValue(stepnode, "contentfieldname");
			resultFieldName = XMLHandler.getTagValue(stepnode, "resultfieldname");
			
		}
		catch (Exception e)
		{
			throw new KettleXMLException("Unable to load step info from XML", e);
		}
	}
	
	@Override
	public String getXML()
	{
		StringBuffer retval = new StringBuffer(500);
		
		retval.append("    ").append(XMLHandler.addTagValue("grammarfilename", grammarFilename));
		retval.append("    ").append(XMLHandler.addTagValue("rulename", ruleName));
		retval.append("    ").append(XMLHandler.addTagValue("contentfieldname", contentFieldname));
		retval.append("    ").append(XMLHandler.addTagValue("resultfieldname", resultFieldName));
		
		return retval.toString();
	}
	
	@Override
	public void loadXML(Node stepnode, List<DatabaseMeta> databases, Map<String, Counter> counters) 
			throws KettleXMLException {
		readData(stepnode);
	}

	@Override
	public void saveRep(Repository rep, ObjectId id_transformation, ObjectId id_step) throws KettleException {
		try
		{
			rep.saveStepAttribute(id_transformation, id_step, "grammarfilename", grammarFilename);
			rep.saveStepAttribute(id_transformation, id_step, "rulename", ruleName);
			rep.saveStepAttribute(id_transformation, id_step, "resultfieldname", resultFieldName);
		}
		catch (Exception e)
		{
			throw new KettleException("Unable to save step information to the repository for id_step=" + id_step, e);
		}

	}

	@Override
	public void readRep(Repository rep, ObjectId id_step, List<DatabaseMeta> databases, Map<String, Counter> counters)
			throws KettleException {
		try
		{
			grammarFilename = rep.getStepAttributeString(id_step, "grammarfilename");
			ruleName = rep.getStepAttributeString(id_step, "rulename");
			contentFieldname = rep.getStepAttributeString(id_step, "contentfieldname");
			resultFieldName = rep.getStepAttributeString(id_step, "resultfieldname"); //$NON-NLS-1$
		}
		catch (Exception e)
		{
			throw new KettleException("Unexpected error reading step information from the repository", e);
		}
	}

	@Override
	public void check(List<CheckResultInterface> remarks, TransMeta transMeta,
			StepMeta stepMeta, RowMetaInterface prev, String[] input,
			String[] output, RowMetaInterface info) {
		// TODO Auto-generated method stub

	}

	@Override
	public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans) {
		return new AntlrRecognizer(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}

	@Override
	public StepDataInterface getStepData() {
		
		return new AntlrRecognizerData();
	}
	
	@Override
	public void getFields(RowMetaInterface inputRowMeta, String name, RowMetaInterface[] info, StepMeta nextStep, VariableSpace space) throws KettleStepException
	{
		// Output fields (String)
		 if (!Const.isEmpty(resultFieldName))
	     {
			 ValueMetaInterface v = new ValueMeta(space.environmentSubstitute(resultFieldName), ValueMeta.TYPE_BOOLEAN);
			 v.setOrigin(name);
			 inputRowMeta.addValueMeta(v);
	     }
		
	}

	public String getGrammarFilename() {
		return grammarFilename;
	}

	public void setGrammarFilename(String grammarFilename) {
		this.grammarFilename = grammarFilename;
	}

	public String getRuleName() {
		return ruleName;
	}

	public void setRuleName(String ruleName) {
		this.ruleName = ruleName;
	}

	public String getContentFieldname() {
		return contentFieldname;
	}

	public void setContentFieldname(String contentFieldname) {
		this.contentFieldname = contentFieldname;
	}

}
