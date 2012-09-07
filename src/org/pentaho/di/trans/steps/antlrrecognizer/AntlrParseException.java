package org.pentaho.di.trans.steps.antlrrecognizer;

public class AntlrParseException extends RuntimeException {

	/**
	 * Serial ID
	 */
	private static final long serialVersionUID = 1L;

	public AntlrParseException() {
		
	}

	public AntlrParseException(String message) {
		super(message);
		
	}

	public AntlrParseException(Throwable cause) {
		super(cause);
		
	}

	public AntlrParseException(String message, Throwable cause) {
		super(message, cause);
		
	}

}
