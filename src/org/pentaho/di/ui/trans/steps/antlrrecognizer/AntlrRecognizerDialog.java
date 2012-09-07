/*******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2012 by Pentaho : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/
package org.pentaho.di.ui.trans.steps.antlrrecognizer;


import java.io.File;
import java.io.StringWriter;

import org.antlr.works.IDE;
import org.apache.commons.io.IOUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.TransPreviewFactory;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.steps.antlrrecognizer.AntlrRecognizer;
import org.pentaho.di.trans.steps.antlrrecognizer.AntlrRecognizerMeta;
import org.pentaho.di.ui.core.dialog.EnterNumberDialog;
import org.pentaho.di.ui.core.dialog.EnterTextDialog;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.dialog.PreviewRowsDialog;
import org.pentaho.di.ui.core.dialog.ShowMessageDialog;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.trans.dialog.TransPreviewProgressDialog;
import org.pentaho.di.ui.trans.step.BaseStepDialog;

public class AntlrRecognizerDialog extends BaseStepDialog implements StepDialogInterface
{
	private static Class<?> PKG = AntlrRecognizer.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private AntlrRecognizerMeta inputMeta;
	
	private TextVar      wFilename;
	private Button       wbbFilename; // Browse for a file
	private Label		 wlRulename;
	private TextVar      wRulename;
	private Label        wlContent;
	private CCombo       wContentField;
	private FormData     fdlContent, fdContent;
	private Label        wlResult;
	private TextVar      wResult;
	private FormData     fdlResult, fdResult;
	

	private Button wEdit;

	private Listener lsEdit;
	
	public AntlrRecognizerDialog(Shell parent, Object in, TransMeta tr, String sname)
	{
		super(parent, (BaseStepMeta)in, tr, sname);
		inputMeta=(AntlrRecognizerMeta)in;
	}

	public String open()
	{
		Shell parent = getParent();
		Display display = parent.getDisplay();

		shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MIN | SWT.MAX);
 		props.setLook(shell);
 		setShellImage(shell, inputMeta);
        
		ModifyListener lsMod = new ModifyListener() 
		{
			public void modifyText(ModifyEvent e) 
			{
				inputMeta.setChanged();
			}
		};
		changed = inputMeta.hasChanged();

		FormLayout formLayout = new FormLayout ();
		formLayout.marginWidth  = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;

		shell.setLayout(formLayout);
		shell.setText(BaseMessages.getString(PKG, "AntlrRecognizerDialog.Shell.Title")); //$NON-NLS-1$
		
		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;

		// Step name line
		//
		wlStepname=new Label(shell, SWT.RIGHT);
		wlStepname.setText(BaseMessages.getString(PKG, "AntlrRecognizerDialog.Stepname.Label")); //$NON-NLS-1$
 		props.setLook(wlStepname);
		fdlStepname=new FormData();
		fdlStepname.left = new FormAttachment(0, 0);
		fdlStepname.right= new FormAttachment(middle, -margin);
		fdlStepname.top  = new FormAttachment(0, margin);
		wlStepname.setLayoutData(fdlStepname);
		wStepname=new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wStepname);
		wStepname.addModifyListener(lsMod);
		fdStepname=new FormData();
		fdStepname.left = new FormAttachment(middle, 0);
		fdStepname.top  = new FormAttachment(0, margin);
		fdStepname.right= new FormAttachment(100, 0);
		wStepname.setLayoutData(fdStepname);
		Control lastControl = wStepname;
		
		
		// Filename...
		//
		// The filename browse button
		//
        wbbFilename=new Button(shell, SWT.PUSH| SWT.CENTER);
        props.setLook(wbbFilename);
        wbbFilename.setText(BaseMessages.getString(PKG, "System.Button.Browse"));
        wbbFilename.setToolTipText(BaseMessages.getString(PKG, "System.Tooltip.BrowseForFileOrDirAndAdd"));
        FormData fdbFilename = new FormData();
        fdbFilename.top  = new FormAttachment(lastControl, margin);
        fdbFilename.right= new FormAttachment(100, 0);
        wbbFilename.setLayoutData(fdbFilename);

        // The field itself...
        //
		Label wlFilename = new Label(shell, SWT.RIGHT);
		wlFilename.setText(BaseMessages.getString(PKG, "AntlrRecognizerDialog.FileName.Label")); //$NON-NLS-1$
 		props.setLook(wlFilename);
		FormData fdlFilename = new FormData();
		fdlFilename.top  = new FormAttachment(lastControl, margin);
		fdlFilename.left = new FormAttachment(0, 0);
		fdlFilename.right= new FormAttachment(middle, -margin);
		wlFilename.setLayoutData(fdlFilename);
		wFilename=new TextVar(transMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wFilename);
		wFilename.addModifyListener(lsMod);
		FormData fdFilename = new FormData();
		fdFilename.top  = new FormAttachment(lastControl, margin);
		fdFilename.left = new FormAttachment(middle, 0);
		fdFilename.right= new FormAttachment(wbbFilename, -margin);
		wFilename.setLayoutData(fdFilename);
		lastControl = wFilename;
		
		// The content field
		RowMetaInterface previousFields;
		try {
			previousFields = transMeta.getPrevStepFields(stepMeta);
		}
		catch(KettleStepException e) {
			new ErrorDialog(shell, BaseMessages.getString(PKG, "AntlrRecognizerDialog.ErrorDialog.UnableToGetInputFields.Title"), BaseMessages.getString(PKG, "AntlrRecognizerDialog.ErrorDialog.UnableToGetInputFields.Message"), e);
			previousFields = new RowMeta();
		}
		
		
		wlContent = new Label(shell, SWT.RIGHT);
		wlContent.setText(BaseMessages.getString(PKG, "AntlrRecognizerDialog.Content.Label")); //$NON-NLS-1$
 		props.setLook(wlContent);
		fdlContent = new FormData();
		fdlContent.top  = new FormAttachment(lastControl, margin);
		fdlContent.left = new FormAttachment(0, 0);
		fdlContent.right= new FormAttachment(middle, -margin);
		wlContent.setLayoutData(fdlContent);
		wContentField=new CCombo(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wContentField.setItems(previousFields.getFieldNames());
 		props.setLook(wContentField);
 		wContentField.addModifyListener(lsMod);
		fdContent = new FormData();
		fdContent.top  = new FormAttachment(lastControl, margin);
		fdContent.left = new FormAttachment(middle, 0);
		fdContent.right= new FormAttachment(100, 0);
		wContentField.setLayoutData(fdContent);
		lastControl = wContentField;
		
		
		// The rule namefield itself...
        //
		wlRulename = new Label(shell, SWT.RIGHT);
		wlRulename.setText(BaseMessages.getString(PKG, "AntlrRecognizerDialog.RuleName.Label")); //$NON-NLS-1$
 		props.setLook(wlRulename);
		FormData fdlRulename = new FormData();
		fdlRulename.top  = new FormAttachment(lastControl, margin);
		fdlRulename.left = new FormAttachment(0, 0);
		fdlRulename.right= new FormAttachment(middle, -margin);
		wlRulename.setLayoutData(fdlRulename);
		wRulename=new TextVar(transMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);//new CCombo(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		props.setLook(wRulename);
 		wRulename.addModifyListener(lsMod);
		FormData fdRulename = new FormData();
		fdRulename.top  = new FormAttachment(lastControl, margin);
		fdRulename.left = new FormAttachment(middle, 0);
		fdRulename.right= new FormAttachment(100, 0);
		wRulename.setLayoutData(fdRulename);
		lastControl = wRulename;
		
		// Result fieldname ...
		wlResult=new Label(shell, SWT.RIGHT);
		wlResult.setText(BaseMessages.getString(PKG, "AntlrRecognizerDialog.ResultField.Label")); //$NON-NLS-1$
 		props.setLook(wlResult);
		fdlResult=new FormData();
		fdlResult.left = new FormAttachment(0, 0);
		fdlResult.right= new FormAttachment(middle, -margin);
		fdlResult.top  = new FormAttachment(lastControl, margin*2);
		wlResult.setLayoutData(fdlResult);

		wResult=new TextVar(transMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wResult.setToolTipText(BaseMessages.getString(PKG, "AntlrRecognizerDialog.ResultField.Tooltip"));
 		props.setLook(wResult);
		wResult.addModifyListener(lsMod);
		fdResult=new FormData();
		fdResult.left = new FormAttachment(middle, 0);
		fdResult.top  = new FormAttachment(lastControl, margin*2);
		fdResult.right= new FormAttachment(100, 0);
		wResult.setLayoutData(fdResult);
		lastControl = wResult;
		
		// Some buttons first, so that the dialog scales nicely...
		//
		wOK=new Button(shell, SWT.PUSH);
		wOK.setText(BaseMessages.getString(PKG, "System.Button.OK")); //$NON-NLS-1$
		wCancel=new Button(shell, SWT.PUSH);
		wCancel.setText(BaseMessages.getString(PKG, "System.Button.Cancel")); //$NON-NLS-1$
		//wPreview=new Button(shell, SWT.PUSH);
		wEdit=new Button(shell, SWT.PUSH);
		wEdit.setText(BaseMessages.getString(PKG,"AntlrRecognizerDialog.Button.Edit"));
		//wPreview.setText(BaseMessages.getString(PKG, "System.Button.Preview")); //$NON-NLS-1$
		//wPreview.setEnabled(true);
		
		setButtonPositions(new Button[] { wOK, /*wPreview,*/ wEdit, wCancel }, margin, null);
      
		// Add listeners
		lsCancel   = new Listener() { public void handleEvent(Event e) { cancel(); } };
		lsOK       = new Listener() { public void handleEvent(Event e) { ok();     } };
		//lsPreview  = new Listener() { public void handleEvent(Event e) { preview(); } };
		lsEdit     = new Listener() { public void handleEvent(Event e) { edit(); } };
		
		wCancel.addListener (SWT.Selection, lsCancel );
		wOK.addListener     (SWT.Selection, lsOK     );
		//wPreview.addListener(SWT.Selection, lsPreview);
		wEdit.addListener   (SWT.Selection, lsEdit   );
		
		lsDef=new SelectionAdapter() { public void widgetDefaultSelected(SelectionEvent e) { ok(); } };
		
		wStepname.addSelectionListener( lsDef );
		if (wFilename!=null) wFilename.addSelectionListener( lsDef );
		
		
		if (wbbFilename!=null) {
			// Listen to the browse button next to the file name
			wbbFilename.addSelectionListener(
					new SelectionAdapter()
					{
						public void widgetSelected(SelectionEvent e) 
						{
							FileDialog dialog = new FileDialog(shell, SWT.OPEN);
							dialog.setFilterExtensions(new String[] {"*.g", "*"});
							if (wFilename.getText()!=null)
							{
								String fname = transMeta.environmentSubstitute(wFilename.getText());
								dialog.setFileName( fname );
							}
							
							dialog.setFilterNames(new String[] {BaseMessages.getString(PKG, "System.FileType.ANTLRGrammarFiles")+", ", BaseMessages.getString(PKG, "System.FileType.AllFiles")});
							
							if (dialog.open()!=null)
							{
								String str = dialog.getFilterPath()+System.getProperty("file.separator")+dialog.getFileName();
								wFilename.setText(str);
								//wRulename.setItems(getRuleNames(str));
							}
						}
					}
				);
		}

		
		// Detect X or ALT-F4 or something that kills this window...
		shell.addShellListener(	new ShellAdapter() { public void shellClosed(ShellEvent e) { cancel(); } } );


		// Set the shell size, based upon previous time...
		setSize();
		
		getData();
		inputMeta.setChanged(changed);
	
		shell.open();
		while (!shell.isDisposed())
		{
				if (!display.readAndDispatch()) display.sleep();
		}
		return stepname;
	}
	
	
  	public void getData()
	{
		getData(inputMeta, true);
	}
	/**
	 * Copy information from the meta-data input to the dialog fields.
	 */ 
	public void getData(AntlrRecognizerMeta inputMeta, boolean copyStepname)
	{
	  if (copyStepname) {
		wStepname.setText(stepname);
	  }
	  
		wFilename.setText(Const.NVL(inputMeta.getGrammarFilename(), ""));
		wRulename.setText(Const.NVL(inputMeta.getRuleName(), ""));
		wContentField.setText(Const.NVL(inputMeta.getContentFieldname(),""));
		wResult.setText(Const.NVL(inputMeta.getResultFieldName(), ""));
		
		wStepname.selectAll();
	}
	
	private void cancel()
	{
		stepname=null;
		inputMeta.setChanged(changed);
		dispose();
	}
	
	protected void edit() {
		String filename;
		filename = wFilename.getText();
		if(new File(filename).exists()) {
			try {
				String command = "java -cp "+AntlrRecognizer.PLUGIN_BASE_LOC+"lib/antlrworks-1.4.3.jar org.antlr.works.IDE -f "+filename;
				Process p = Runtime.getRuntime().exec(command.replace('/',File.separatorChar));
				if(p.waitFor() != 0) {
					StringWriter writer = new StringWriter();
					IOUtils.copy(p.getErrorStream(), writer, "UTF-8");
					String theString = writer.toString();
					throw new Exception("Error during compilation: "+theString);
				}
				//IDE.main(new String[] {"-f", filename});
			}
			catch(Exception e) {
				log.logError(e.getMessage(),e);
			}
		}
		else {
			ShowMessageDialog msgDialog 
				= new ShowMessageDialog(
						shell, 
						SWT.ICON_ERROR | SWT.OK, 
						BaseMessages.getString(PKG, "AntlrRecognizer.Dialog.CouldNotEditTitle"), 
						BaseMessages.getString(PKG, "AntlrRecognizer.Dialog.CouldNotEdit"), 
						false); //$NON-NLS-1$
		    msgDialog.open();
		}
	}
	
	private void getInfo(AntlrRecognizerMeta inputMeta) {
		
		inputMeta.setGrammarFilename(wFilename.getText());
		inputMeta.setRuleName(wRulename.getText());
		inputMeta.setContentFieldname(wContentField.getText());
		inputMeta.setResultFieldName(wResult.getText() );
		inputMeta.setChanged();
	}
	
	private void ok()
	{
		if (Const.isEmpty(wStepname.getText())) return;

		getInfo(inputMeta);
		stepname = wStepname.getText();
		dispose();
	}
	
	
	// Preview the data
    private void preview()
    {
        // Create the XML input step
        AntlrRecognizerMeta oneMeta = new AntlrRecognizerMeta();
        getInfo(oneMeta);
        
        TransMeta previewMeta = TransPreviewFactory.generatePreviewTransformation(transMeta, oneMeta, wStepname.getText());
        transMeta.getVariable("Internal.Transformation.Filename.Directory");
        previewMeta.getVariable("Internal.Transformation.Filename.Directory");
        
        EnterNumberDialog numberDialog = new EnterNumberDialog(shell, props.getDefaultPreviewSize(), BaseMessages.getString(PKG, "AntlrRecognizerDialog.PreviewSize.DialogTitle"), BaseMessages.getString(PKG, "AntlrRecognizerDialog.PreviewSize.DialogMessage"));
        int previewSize = numberDialog.open();
        if (previewSize>0)
        {
            TransPreviewProgressDialog progressDialog = new TransPreviewProgressDialog(shell, previewMeta, new String[] { wStepname.getText() }, new int[] { previewSize } );
            progressDialog.open();

            Trans trans = progressDialog.getTrans();
            String loggingText = progressDialog.getLoggingText();

            if (!progressDialog.isCancelled())
            {
                if (trans.getResult()!=null && trans.getResult().getNrErrors()>0)
                {
                	EnterTextDialog etd = new EnterTextDialog(shell, BaseMessages.getString(PKG, "System.Dialog.PreviewError.Title"),  
                			BaseMessages.getString(PKG, "System.Dialog.PreviewError.Message"), loggingText, true );
                	etd.setReadOnly();
                	etd.open();
                }
            }
            
            PreviewRowsDialog prd =new PreviewRowsDialog(shell, transMeta, SWT.NONE, wStepname.getText(), progressDialog.getPreviewRowsMeta(wStepname.getText()), progressDialog.getPreviewRows(wStepname.getText()), loggingText);
            prd.open();
        }
    }
    
    /*private String[] getRuleNames(String str) {
    	String[] ruleNames = null;
    	
    	// Fill rule combo with rule names
    	Tool antlrTool = new Tool(new String[]{str});
		try {
			
			Grammar grammar = new Grammar(antlrTool);//antlrTool.getRootGrammar(str);
			log.logBasic("Root Grammar = "+grammar.getFileName());
			int numRules = grammar.getRules().size();
			log.logBasic("Number of rules = "+numRules);
			ruleNames = new String[numRules];
			for(int i=0;i<numRules;i++) {
				System.out.println("Found rule: "+grammar.getRuleName(i));
				ruleNames[i] = grammar.getRuleName(i);
			}
			
		} catch (Exception ex) {
			// TODO Auto-generated catch block
			ex.printStackTrace();
		}
		
		return ruleNames;
    }*/

}
