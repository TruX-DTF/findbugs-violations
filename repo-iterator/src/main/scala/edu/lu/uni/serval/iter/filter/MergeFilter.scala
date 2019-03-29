package edu.lu.uni.serval.iter.filter

import org.eclipse.jgit.revwalk.RevCommit

/**
 * @author darkrsw
 */
class MergeFilter extends CommitFilter 
{
  def filterCommit(commit: RevCommit): Boolean =// true => skip; false => do task
	{
		val ret = if(commit.getParentCount() > 1 
				|| commit.getShortMessage().startsWith("Merge ")
				|| commit.getShortMessage().startsWith("Merged ") ) true;
		else
			false;
		
		return ret;
	}
}