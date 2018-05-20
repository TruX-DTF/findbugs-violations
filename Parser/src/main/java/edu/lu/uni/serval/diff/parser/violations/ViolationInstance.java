package edu.lu.uni.serval.diff.parser.violations;

import java.util.ArrayList;
import java.util.List;

import edu.lu.uni.serval.diffentry.DiffEntryHunk;
import edu.lu.uni.serval.gumtree.regroup.HierarchicalActionSet;

public class ViolationInstance implements Comparable<ViolationInstance> {
	
	private String fileName = "";
	private Integer startLineNum;
	private int endLineNum;
	private int bugStartLineNum;
	private int bugEndLineNum;
	private int fixStartLineNum = 0;
	private int fixEndLineNum;
	private String violationType;
	private List<DiffEntryHunk> hunks = new ArrayList<>();
	private List<HierarchicalActionSet> actionSets;
	private int bugFixStartLineNum = 0; // the heuristic matched fix start line of a violation
	private int bugFixEndLineNum = 0; // the heuristic matched fix end line of a violation

	public ViolationInstance(Integer startLineNum, int endLineNum, String violationType) {
		super();
		this.startLineNum = startLineNum;
		this.endLineNum = endLineNum;
		this.violationType = violationType;
		this.actionSets = new ArrayList<>();
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public Integer getStartLineNum() {
		return startLineNum;
	}

	public int getEndLineNum() {
		return endLineNum;
	}

	public int getBugStartLineNum() {
		return bugStartLineNum;
	}

	public void setBugStartLineNum(int bugStartLineNum) {
		this.bugStartLineNum = bugStartLineNum;
	}

	public int getBugEndLineNum() {
		return bugEndLineNum;
	}

	public void setBugEndLineNum(int bugEndLineNum) {
		this.bugEndLineNum = bugEndLineNum;
	}

	public int getFixStartLineNum() {
		return fixStartLineNum;
	}

	public void setFixStartLineNum(int fixStartLineNum) {
		this.fixStartLineNum = fixStartLineNum;
	}

	public int getFixEndLineNum() {
		return fixEndLineNum;
	}

	public void setFixEndLineNum(int fixEndLineNum) {
		this.fixEndLineNum = fixEndLineNum;
	}

	public List<DiffEntryHunk> getHunks() {
		return hunks;
	}

	public void setHunks(List<DiffEntryHunk> hunks) {
		this.hunks = hunks;
	}

	public String getViolationType() {
		return violationType;
	}

	public List<HierarchicalActionSet> getActionSets() {
		return actionSets;
	}

	public int getBugFixStartLineNum() {
		return bugFixStartLineNum;
	}

	public void setBugFixStartLineNum(int bugFixStartLineNum) {
		this.bugFixStartLineNum = bugFixStartLineNum;
	}

	public int getBugFixEndLineNum() {
		return bugFixEndLineNum;
	}

	public void setBugFixEndLineNum(int bugFixEndLineNum) {
		this.bugFixEndLineNum = bugFixEndLineNum;
	}

	@Override
	public int compareTo(ViolationInstance v) {
		return this.startLineNum.compareTo(v.startLineNum);
	}
	
	@Override
	public String toString() {
		return this.startLineNum + " : " + this.endLineNum + " : " + this.violationType;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ViolationInstance) {
			ViolationInstance v = (ViolationInstance) obj;
			if (this.fileName.equals(v.fileName) && this.violationType.equals(v.violationType) && this.startLineNum == v.startLineNum && this.endLineNum == v.endLineNum) {
				return true;
			}
		}
		return false;
	}
	
}
