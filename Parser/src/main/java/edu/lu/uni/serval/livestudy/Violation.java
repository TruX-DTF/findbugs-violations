package edu.lu.uni.serval.livestudy;

public class Violation {
	private String violationType;
	private String fileName;
	private int startLine;
	private int endLine;

	public String getViolationType() {
		return violationType;
	}

	public String getFileName() {
		return fileName;
	}

	public int getStartLine() {
		return startLine;
	}

	public int getEndLine() {
		return endLine;
	}

	public Violation(String violationType, String fileName, int startLine, int endLine) {
		super();
		this.violationType = violationType;
		this.fileName = fileName;
		this.startLine = startLine;
		this.endLine = endLine;
	}
	
}
