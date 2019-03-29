package edu.lu.uni.serval.iter.filter

import org.eclipse.jgit.revwalk.RevCommit

/**
 * @author darkrsw
 */
trait CommitFilter 
{
  def filterCommit(commit: RevCommit): Boolean // true => skip; false => do task
}