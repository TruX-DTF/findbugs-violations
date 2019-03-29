package edu.lu.uni.serval.iter

import org.eclipse.jgit.revwalk.RevCommit

/**
 * @author darkrsw
 */
class DoNothingTask extends CommitTask  {
  def doTask(commit: RevCommit): Boolean =
  {
  	return true;
  }

  def getResultMsg(): String = {
    "NOTHING"
  }
  
  
}