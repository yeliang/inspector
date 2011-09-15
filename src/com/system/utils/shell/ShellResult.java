package com.system.utils.shell;

public class ShellResult implements java.io.Serializable 
{
	private static final long serialVersionUID = 1L;
	
	private String errorResult;
	private String standardResult;
	
	public ShellResult() {
		
	}
	
	public String getErrorResult() {
		return errorResult;
	}
	
	public String getStandardResult() {
		return standardResult;
	}
	
	public void setErrorResult(String value) {
		errorResult = value;
	}
	
	public void setStandardResult(String value) {
		standardResult = value;
	}
}
