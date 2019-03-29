package edu.lu.uni.serval.iter

import edu.lu.uni.serval.scm.git.GitProxy
import edu.lu.uni.serval.iter.filter.CommitFilter
import com.typesafe.scalalogging.LazyLogging
import java.io.File
import org.apache.commons.io.FileUtils
import scala.collection.mutable.HashSet
import scala.collection.JavaConversions

class BackwardIterator(proxy: GitProxy) extends LazyLogging 
{
	private var resultMap = new HashSet[String];
	
	private var preFilter: CommitFilter = null;
	private var task: CommitTask = null;
	private var logWriter: File = null; // NOTE: this should be File rather than StringBuffer since it must preserve output as much as possible.  
	
	// This defines its explicit setter. 
	def setPreFilter(v: CommitFilter): Unit = { this.preFilter = v; }
	def setTask(t: CommitTask): Unit = { this.task = t;}
	def setLogWriter(p: File): Unit = {	this.logWriter = p;	}
	def setResultMap(p: HashSet[String]) = {this.resultMap = p;}
	
	def logTaskResult(msg: String): Unit =
	{
		FileUtils.write(logWriter, msg, true);
	}
	
	// Iterate backward (reverse chronological direction)
  def iterateBackward(): Boolean = 
  {
  	if(proxy != null || !proxy.testConnection())
  	{
  		val commits = proxy.getLogAll()
  		
  		for( commit <- commits)
  		{
  			val sha1 = commit.getName
  			
  			try{
	  			val preskip = if(preFilter != null) {	preFilter.filterCommit(commit)} else {	false	}
	  			val continue = if(!preskip) {task.doTask(commit)} else {false}
	  			val success = if(continue && !preskip) {
	  				// NOTE need to be idempotent and flush all the time.
	  				logTaskResult(sha1 + " SUCCESS " + task.getResultMsg() + "\n");
	  				
	  				true 
	  			} else {
	  				if(preskip)
	  				{ if(!resultMap.contains(sha1)) 
	  						logTaskResult(sha1 + " SKIPPED\n"); 
	  				}
	  				else
	  				{	logTaskResult(sha1 + " FAILED " + task.getResultMsg() + "\n") }
	  				
	  				false
	  			}
  			}
  			catch
  			{	case e : Exception => logTaskResult(sha1 + " ERROR " + e.getMessage + "\n")	}
  		}
  	}
  	else
  	{	logger.error("Repository proxy is unavailable.")	}
  	
  	return true;
  }
}