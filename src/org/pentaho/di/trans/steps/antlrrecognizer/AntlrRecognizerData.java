package org.pentaho.di.trans.steps.antlrrecognizer;

import org.apache.commons.vfs.FileObject;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

public class AntlrRecognizerData extends BaseStepData implements
		StepDataInterface {

	public RowMetaInterface outputRowMeta;
	public RowMetaInterface previousRowMeta;
	public FileObject	file;
	public int NrPrevFields;
	
	/**
	 * Constructor
	 */
	public AntlrRecognizerData()
	{
		super();
		file=null;
	}
}