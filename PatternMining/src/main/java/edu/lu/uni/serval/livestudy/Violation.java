package edu.lu.uni.serval.livestudy;

public class Violation {

	private String type;
	private String projectName;
	private String fileName;
	private int startLine;
	private int endLine;
	private String sourceCode;
	
	public Violation(String type, String projectName, String fileName, int startLine, int endLine, String sourceCode) {
		super();
		this.type = type;
		this.projectName = projectName;
		this.fileName = fileName;
		this.startLine = startLine;
		this.endLine = endLine;
		this.sourceCode = sourceCode;
	}

	public String getType() {
		return type;
	}

	public String getProjectName() {
		return projectName;
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
	
	public String getSourceCode() {
		return sourceCode;
	}
	
}
