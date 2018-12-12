package edu.lu.uni.serval.alarm.tracking

import java.io.File
import edu.lu.uni.serval.alarm.util.db.graph.neo4j.VioDBFacade
import scala.collection.mutable._
import scala.collection.JavaConverters._
import org.neo4j.driver.v1.Value
import edu.lu.uni.serval.alarm.util.TrackingHelperUtils
import com.typesafe.scalalogging.LazyLogging
import org.apache.commons.io.FileUtils
import org.reactormonk.Counter

object FixedAlarmCollector extends LazyLogging
{
	def main(args: Array[String]): Unit =
	{
		collectFixedAlarms("fixed-alarms.list", 
				"/mnt/archive1/data/violations/repos/repos-%s/%s/.git",
				"/home/darkrsw/repo/false-alarm-study/prj-pack.map",
				"vtype-stat.csv",
				"summary-project-vtype.csv"
				)
				
	  /*attachFixerCommit(
	      "/mnt/archive1/data/violations/repos/repos-%s/%s/.git",
	      "/mnt/exp2/data/violation/working-%s/reports/%s",
	      "/home/darkrsw/repo/false-alarm-study/prj-pack.map"
      )*/
	}
	
	def collectFixedAlarms(outputPath: String, 
	    repoPathTemplate: String, mapFilePath: String,
	    vtypePath: String,
	    summaryPath: String) =
	{
		// result output file
		val outputFile = new File(outputPath)
		
		// summary file
		val summaryFile = new File(summaryPath)
		val summaryMap = Map[String, Counter[String,Int]]()
		
		// vtype stat file
		val vtypeStatFile = new File(vtypePath)
		
		
		// project to Violations map
		val prjNodeMap = Map[String, Set[Value]]() // prj -> Value
		
		// prepare project to pack map
		val prj2PackMap = TrackingHelperUtils.readProject2PackMap(mapFilePath)
		println("prj2pack map size: " + prj2PackMap.size)
		
		// for vtype stat
		val vtypeMap = Map[String, Set[Value]]() // vtype -> Value
		
		
		VioDBFacade.init()
		////////////////////////////////////////
		
		// get fixed alarms
		val fixedList = VioDBFacade.searchFixedAlarms()
		println("#fixed alarms: " + fixedList.size)
		
		// sort fixed alarms.
		fixedList.foreach(x=>{
			
			val node = x.get("f")
			val prj = node.get("project").asString()
			
			if(prjNodeMap.contains(prj))
				prjNodeMap(prj).add(node)
			else
			{
				val valueSet = Set[Value]()
				valueSet.add(node)
				
				prjNodeMap += (prj->valueSet)
			}
		})
		
		println("#projects: "+prjNodeMap.size)
		
		prjNodeMap.keySet.foreach( prj => {
			try{
				println(s"\nStarting $prj")
				
				val pack = prj2PackMap(prj)
				val repoPath = repoPathTemplate.format(pack,prj)
				
				val tracker = new AlarmTracker(prj)
				tracker.initGitRepo(repoPath)
				val gitproxy = tracker.repoProxy.get
				
				val fixedAlarmSet = prjNodeMap(prj)
				println(s"#alarms in $prj: "+fixedAlarmSet.size)
				
				fixedAlarmSet.foreach( alarmNode => {
					val prj = alarmNode.get("project").asString()
					val buggycommit = alarmNode.get("commit").asString()
					val vtype = alarmNode.get("vtype").asString()
					val fixer = alarmNode.get("fixer").asString()
					val sLine = alarmNode.get("sLine").asInt()
					val eLine = alarmNode.get("eLine").asInt()
					
					addVtypeValue2Set(vtypeMap, vtype, alarmNode)
					
					val infoTokens = alarmNode.get("id").asString().split(":")
					val buggyPackagePath = infoTokens(2)
					
					//val gitproxy = this.repoProxy.get
			  	val parentCommit = gitproxy.getCommitByHash(buggycommit)
			  	val childCommit = gitproxy.getCommitByHash(fixer)
			  	
			  	// find changed set
			  	val diffs = gitproxy.getChangedFiles(childCommit, parentCommit).toList
			  	val sourceChanges = TrackingUtils.getChangedSourceFiles(diffs)
			  	val (parentChangedPaths, childChangedPaths, diffMap) = 
			  			AlarmMatcher.transformFilesToPackagePaths(sourceChanges, parentCommit, childCommit, gitproxy)
					
			  	// 0. find out whether it is in changed set.
		  	  val mainBuggyClassPath = if(buggyPackagePath.contains("$"))
													 		{
													 			val tokens2 = buggyPackagePath.split("\\$")
													 			tokens2(0)
													 		}
													 		else buggyPackagePath
													 		
					println(s"package name: $mainBuggyClassPath")
								
					if( ! parentChangedPaths.contains(mainBuggyClassPath) )
					{
						// something wrong
						println()
						logger.error(s"Something went wrong: ${alarmNode.get("id")}==>$fixer")
					}
					else
					{
						val d = diffMap(mainBuggyClassPath)
						val resultString = s"$vtype:$prj:$buggycommit:${d.getOldPath}:$sLine:$eLine=>$prj:$fixer:${d.getNewPath}\n"
						FileUtils.write(outputFile, resultString, "UTF-8", true)
						
						// add to summary
						add2summaryMap(summaryMap, prj, vtype)
						
						print(".")
					}
				})
				
				println()
			} 
			catch
			{
				case e: Throwable => println(s"Error in $prj: \n"+e.getMessage)
			}
		})
		
		// print vtype stat
		//vtypeMap.foreach( n => println(s"${n._1}=>${n._2.size}") )
		vtypeMap.foreach( n => 
		  FileUtils.write(vtypeStatFile, s"${n._1}=>${n._2.size}\n", "UTF-8", true ) )
		
		summaryMap.foreach( prjPair =>
		  {
		    val prj = prjPair._1
		    val counter = prjPair._2.toMap()
		    
		    counter.foreach( counterPair => { 
		      val vtype = counterPair._1
		      val count = counterPair._2
		      FileUtils.write(summaryFile, s"$prj,$vtype,$count\n", "UTF-8", true)
		    })
		  })
		  
		
		////////////////////////////////////////
		VioDBFacade.close()
		
		/*rlist.foreach( x => {
			
		})
		summaryFile
		val root = new JsonObject()
    root.addProperty("type", "task")
    root.addProperty("repo.path", repoName)
    root.addProperty("repo.name", repoName)*/
	}
	 
	def add2summaryMap(sMap: Map[String, Counter[String, Int]], prj: String, vtype: String) =
	{
	  if(sMap.contains(prj))
	  {
	    val counter = sMap(prj)
	    sMap.put(prj, counter+vtype)
	  }
	  else
	  {
	    val counter = Counter[String,Int]()
	    sMap.put(prj, counter+vtype)
	  }
	}
	
	
	
	def attachFixerCommit(repoPathTemplate: String, reportPathTemplate: String, mapFilePath: String) =
	{
		val prjNodeMap = Map[String, Set[Value]]() // prj -> Value
		val prj2PackMap = TrackingHelperUtils.readProject2PackMap(mapFilePath)
		println("prj2pack map size: " + prj2PackMap.size)
		
		VioDBFacade.init()
		////////////////////////////////////////
		
		val nofixerList = VioDBFacade.findFixedWithoutFixer()
		
		nofixerList.foreach( x=> {
			val node = x.get("f")
			val prj = node.get("project").asString()
			
			if(prjNodeMap.contains(prj))
				prjNodeMap(prj).add(node)
			else
			{
				val valueSet = Set[Value]()
				valueSet.add(node)
				
				prjNodeMap += (prj->valueSet)
			}
		})
		
		// foreach prj, find repo, find reports, sort commits,
		prjNodeMap.keySet.foreach( prj => {
		  println()
			val pack = prj2PackMap(prj)
			val repoPath = repoPathTemplate.format(pack,prj)
			val reportPath = reportPathTemplate.format(pack,prj)
			
			val tracker = new AlarmTracker(prj)
			tracker.initGitRepo(repoPath)
			
			val reader = new AlarmDataReader
			val list = reader.readAlarmList4AllCommits(reportPath)
			
			val commitMap = Map(list.map{ s => (s.commitHash, s)}: _*)
			
			val roots = TrackingUtils.sortCommits(list, tracker.repoProxy.get)
			
			val fixedAlarmSet = prjNodeMap(prj)

			// foreach Value, find commit and find child(ren) and set fixer.
			fixedAlarmSet.foreach( alarmNode => {
				val nodeCommit: String = alarmNode.get("commit").asString()
				//val commits = list.filter(a => a.commitHash == nodeCommit)

				// TODO need to deal with multiple children (fixers)
				if(commitMap.contains(nodeCommit))
				{
					val commit = commitMap(nodeCommit)
					VioDBFacade.setFixer2Node(alarmNode.get("id").asString(), commit.children.head.commitHash)
				}
				else
				{
					logger.error(s"No matching commit in $prj: $nodeCommit")
				}
			})
			
			println()
		})
		
		
		
		////////////////////////////////////////
		VioDBFacade.close()
	}
	
	def addVtypeValue2Set(tMap: Map[String, Set[Value]], key: String, v: Value) =
	{
		if(tMap.contains(key))
		{
			val vSet = tMap(key)
			vSet += v
		}
		else
		{
			val newSet = Set[Value]()
			newSet += v
			
			tMap+=(key -> newSet)
		}
	}
}
