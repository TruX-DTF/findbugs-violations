package edu.lu.uni.serval.alarm.tracking

import java.io.File
import edu.lu.uni.serval.alarm.util.db.graph.neo4j.VioDBFacade
import com.typesafe.scalalogging.LazyLogging

object TrackingExecutorByProject extends LazyLogging
{
	def main(args: Array[String]): Unit =
	{
		try{
			internal(args(0), args(1), args(2))
		} catch
		{
			case e: Throwable => logger.error("Unknown Error", e)
			Runtime.getRuntime.exit(127)
		}
		finally
		{
			Runtime.getRuntime.exit(0)
		}
	}
	
	def internal(projectName: String, alarmRootPath: String, repoRootPath: String): Unit =
	{
		// guard
		val alarmPath = alarmRootPath + File.separator + projectName
		val repoPath = repoRootPath + File.separator + projectName + File.separator + ".git"
		
		val alarmDir = new File(alarmPath)
		val repoDir = new File(repoPath)
		
		if( ! alarmDir.exists() )
		{
			// no alarm files
			Console.err.println("No alarm directory exists!: " + alarmPath )
			Runtime.getRuntime.exit(1)
			return
		}
		
		if( ! repoDir.exists() )
		{
			Console.err.println("No git repo directory exists!:" + repoPath)
			Runtime.getRuntime.exit(1)
			return
		}
		
		// Neo4J connection initialization
		VioDBFacade.init()
				
		val reader = new AlarmDataReader
		val list = reader.readAlarmList4AllCommits(alarmPath)
		
		Console.println("# of commits: " + list.size)
		
		val totalAlarms = list.map(_.alarms.size).sum
		Console.println(s"# of total alarms: $totalAlarms")
		
		
		val tracker = new AlarmTracker(projectName)
		
		// 1. init git repo
		tracker.initGitRepo(repoPath)
		
		// 2. sort commits
		val roots = TrackingUtils.sortCommits(list, tracker.repoProxy.get)
		
		//roots.foreach(TrackingUtils.traverseAndPrintAllChildren(_))
		
		// 3. start tracking
		tracker.trackAlarms(roots)
		
		// close Neo4J connection
		VioDBFacade.close()
	}
}