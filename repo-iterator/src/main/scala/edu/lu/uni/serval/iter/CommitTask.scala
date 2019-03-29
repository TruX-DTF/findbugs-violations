package edu.lu.uni.serval.iter

import org.eclipse.jgit.revwalk.RevCommit

/**
 * @author darkrsw
 */
trait CommitTask 
{
	def doTask(commit: RevCommit): Boolean
	def getResultMsg(): String
}