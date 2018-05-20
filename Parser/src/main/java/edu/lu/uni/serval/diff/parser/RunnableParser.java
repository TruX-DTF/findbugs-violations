package edu.lu.uni.serval.diff.parser;

import java.io.File;

public class RunnableParser implements Runnable {

	private File prevFile;
	private File revFile;
	private File diffentryFile;
	private Parser parser;
	
	public RunnableParser(File prevFile, File revFile, File diffentryFile, Parser parser) {
		this.prevFile = prevFile;
		this.revFile = revFile;
		this.diffentryFile = diffentryFile;
		this.parser = parser;
	}

	@Override
	public void run() {
		parser.parseCodeChangeDiffs(prevFile, revFile, diffentryFile);
	}
}
