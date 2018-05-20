package edu.lu.uni.serval.mining.pattern.violation;

public class Violation {

	private String violationType;
	private String fileName;
	private String tokens;
	private int startLine;
	private int endLine;
	private String sourceCode;
	private String feature;

	public Violation(String violationType, String fileName, String tokens, int startLine, int endLine) {
		super();
		this.violationType = violationType;
		this.fileName = fileName;
		this.tokens = tokens;
		this.startLine = startLine;
		this.endLine = endLine;
	}

	public String getViolationType() {
		return violationType;
	}

	public String getFileName() {
		return fileName;
	}

	public String getTokens() {
		return tokens;
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

	public void setSourceCode(String sourceCode) {
		this.sourceCode = sourceCode;
	}

	public String getFeature() {
		return feature;
	}

	public void setFeature(String feature) {
		this.feature = feature;
	}
	
}
