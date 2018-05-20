package edu.lu.uni.serval.detected.violations;

import java.util.Date;
import java.util.List;

public class ViolationCollection implements Comparable<ViolationCollection> {

	private String projectName;
	private Date releasedTime;
	private List<ViolationInstance> bugInstances;

	public String getProjectName() {
		return projectName;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

	public Date getReleasedTime() {
		return releasedTime;
	}

	public void setReleasedTime(Date releasedTime) {
		this.releasedTime = releasedTime;
	}

	public List<ViolationInstance> getBugInstances() {
		return bugInstances;
	}

	public void setBugInstances(List<ViolationInstance> bugInstances) {
		this.bugInstances = bugInstances;
	}
	
	public ViolationCollection(String projectName, List<ViolationInstance> bugInstances) {
		super();
		this.projectName = projectName;
		this.bugInstances = bugInstances;
	}

	public ViolationCollection(String projectName, Date releasedTime, List<ViolationInstance> bugInstances) {
		super();
		this.projectName = projectName;
		this.releasedTime = releasedTime;
		this.bugInstances = bugInstances;
	}

	@Override
	public int compareTo(ViolationCollection bugCollection) {
		return this.projectName.compareTo(bugCollection.projectName);
	}
	
	@Override
	public String toString() {
		return this.projectName;
	}
}
