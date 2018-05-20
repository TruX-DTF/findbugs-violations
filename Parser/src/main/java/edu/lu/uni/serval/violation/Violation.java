package edu.lu.uni.serval.violation;

import java.util.List;
import java.util.Map;

public class Violation {

	private String buggyCommitId;
	private String buggyFileName;
	private String fixedCommitId;
	private String fixedFileName;
	@Deprecated
	private Map<Integer, String> violationTypes;
	@Deprecated
	private Map<Integer, Integer> positions;
	private List<String> violationTypesAndPositions;
	
	public Violation(String buggyCommitId, String buggyFileName, String fixedCommitId, String fixedFileName) {
		super();
		this.buggyCommitId = buggyCommitId;
		this.buggyFileName = buggyFileName;
		this.fixedCommitId = fixedCommitId;
		this.fixedFileName = fixedFileName;
	}

	public String getBuggyCommitId() {
		return buggyCommitId;
	}

	public String getBuggyFileName() {
		return buggyFileName;
	}

	public String getFixedCommitId() {
		return fixedCommitId;
	}

	public String getFixedFileName() {
		return fixedFileName;
	}

	@Deprecated
	public Map<Integer, String> getViolationTypes() {
		return violationTypes;
	}

	@Deprecated
	public void setViolationTypes(Map<Integer, String> violationTypes) {
		this.violationTypes = violationTypes;
	}

	@Deprecated
	public Map<Integer, Integer> getPositions() {
		return positions;
	}

	@Deprecated
	public void setPositions(Map<Integer, Integer> positions) {
		this.positions = positions;
	}

	public List<String> getViolationTypesAndPositions() {
		return violationTypesAndPositions;
	}

	public void setViolationTypesAndPositions(List<String> violationTypesAndPositions) {
		this.violationTypesAndPositions = violationTypesAndPositions;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Violation) {
			Violation alarm = (Violation) obj;
			if (alarm.buggyCommitId.equals(this.buggyCommitId) && alarm.buggyFileName.equals(this.buggyFileName)
					&& alarm.fixedCommitId.equals(this.fixedCommitId) && alarm.fixedFileName.equals(this.fixedFileName)) {
				return true;
			}
		}
		return false;
	}

}
