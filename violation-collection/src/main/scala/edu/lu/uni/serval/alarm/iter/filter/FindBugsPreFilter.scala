package edu.lu.uni.serval.alarm.iter.filter

import edu.lu.uni.serval.iter.filter.CommitFilter
import org.eclipse.jgit.revwalk.RevCommit
import edu.lu.uni.serval.iter.filter.MergeFilter
import org.apache.commons.io.FileUtils
import java.io.File
import scala.collection.mutable.HashSet
import scala.collection.JavaConversions

/**
 * @author darkrsw
 */
class FindBugsPreFilter(resultMap: HashSet[String]) extends CommitFilter 
{
	//var reportsRoot: String = "";
	//def setReportsRoot(p: String) = {this.reportsRoot = p}
	
  def filterCommit(commit: RevCommit): Boolean = // true => skip; false => do task
  {
  	val sha1 = commit.getName
  	if(resultMap.contains(sha1))
  	{
  		true;
  	}	else false;
  }
}