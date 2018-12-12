package edu.lu.uni.serval.alarm.tracking

import edu.lu.uni.serval.alarm.util.db.graph.neo4j.VioDBFacade
import edu.lu.uni.serval.alarm.util.TrackingHelperUtils
import org.neo4j.driver.v1.Value
import java.io.File
import com.github.tototoshi.csv.CSVReader
import org.eclipse.jgit.treewalk.TreeWalk
import org.apache.commons.io.FileUtils
import scala.collection.mutable._
import scala.collection.JavaConverters._
import edu.lu.uni.serval.alarm.tracking.TrackingUtils._



object UnfixedAlarmCollector 
{
  def main(args: Array[String]): Unit =
  {
    doTask("fixed-summary-project-vtype.csv", args(0))
  }
  
  def doTask(fixSummaryPath: String, project: String): Unit =
  {
    val summaryFile = new File(fixSummaryPath)    
  	val summaryList = CSVReader.open(summaryFile).all()
  	
  	
  	// prepare project to pack map
		val prj2PackMap = TrackingHelperUtils.readProject2PackMap("/home/darkrsw/repo/false-alarm-study/prj-pack.map")
		println("prj2pack map size: " + prj2PackMap.size)
  	val repoPathTemplate = "/mnt/archive1/data/violations/repos/repos-%s/%s/.git"
		
  	//val project = "Activiti-Activiti"
  	val outFile = new File(s"unfixed-$project.csv")
  	
  	val pack = prj2PackMap(project)
  	val repoPath = repoPathTemplate.format(pack,project)
  	val tracker = new AlarmTracker(project)
    tracker.initGitRepo(repoPath)
		val gitproxy = tracker.repoProxy.get
		
  	
  	val perProject = summaryList.filter(entry => entry(0) == project)
  	
  	if( perProject.size < 1 )
  	{
  		println(s"No alarms in $project?") 
  		return
  	}
  		
  	// collect vtype of fixed alarms in the project
  	val vtypeSet = Set[String]()  	
  	perProject.foreach( entry => vtypeSet += entry(1) ) 
  	
    VioDBFacade.init()

    val results = VioDBFacade.session.run(
        s"""match (n:Violation {project: '$project'}) 
						where NOT (n)-[:CHILD]->(:Violation)
						return n""")
    /*
    val results = VioDBFacade.session.run(
        s"""match (n:Violation {id: 'Activiti-Activiti:a8e456784456f93a5c808d241b156cf79b0985ce:org.activiti.engine.impl.bpmn.behavior.IntermediateThrowCompensationEventActivityBehavior:BC_UNCONFIRMED_CAST:41:41'}) 
						return n""")
    */
   	val dlist = results.list().asScala.toList
   	println("Neo4J query completed: "+dlist.size)
   	
  	// reaarange commit and files
  	val commitFileMap = Map[String, Map[String, Set[Value]]]()

  	var counter = 0
  	// collect commits and files.
    dlist.foreach(line => {
		  try { 
		    val node = line.get("n")
		    
  		  val commitHash = node.get("commit").asString()
  		  val resolution = if(node.get("resolution").isNull()) "" else node.get("resolution").asString()
        val vtype = node.get("vtype").asString()
        
  		  val infoTokens = node.get("id").asString().split(":")
				val packagePath = infoTokens(2)
				
				val mainClassPath = if(packagePath.contains("$"))
				 		{
				 			val tokens2 = packagePath.split("\\$")
				 			tokens2(0)
				 		}
				 		else packagePath
												 	
				val fileValueMap = if( commitFileMap.contains(commitHash))
				{
				  commitFileMap(commitHash)
				}
				else 
				{ 
					val newMap = Map[String, Set[Value]]()
					commitFileMap.put(commitHash,newMap)
					newMap  
				}
		    
		    val valueSet = if( fileValueMap.contains(mainClassPath) )
		    {
		    	fileValueMap(mainClassPath)
		    }
		    else
		    {
		    	val newSet = Set[Value]()
		    	fileValueMap.put(mainClassPath, newSet)
		    	newSet
		    }
		        
        if( resolution != "fixed" && vtypeSet.contains(vtype))
        {
          counter+=1
  		    valueSet.add(node)
        }
		  } catch {
		  	case e: Throwable => println("error: "+e.getMessage)
		  }
    })

    println("#Node after filtering: "+counter)
    
    commitFileMap.foreach( entry =>
      {
        val commitHash = entry._1
        val fileValueMap = entry._2
        
        println("Processing "+commitHash)
        
        val commit = gitproxy.getCommitByHash(commitHash)
				
        val treeWalk = new TreeWalk( gitproxy.getRepository() )
        val tree = commit.getTree()
        treeWalk.addTree(tree);
        treeWalk.setRecursive(true);
        
        while( treeWalk.next() ) {
          val path = treeWalk.getPathString()
          //println(path)
          if( path.toLowerCase().endsWith(".java") )
          {      
            val source = getSourceText( commit, path, gitproxy )
            val thisPackagePath = parseAndExtractPackagePath( source )
            val className = takeFileName(path)
            val targetPath = thisPackagePath+"."+className
            
            if(fileValueMap.contains(targetPath))
            {
            	val valueSet = fileValueMap(targetPath)
            	
            	valueSet.foreach( node => {
	              val vtype = node.get("vtype").asString()
	        		  val commitHash = node.get("commit").asString()
	        		  val sLine = node.get("sLine").asInt()
	        		  val eLine = node.get("eLine").asInt()
	        		  val resolution = if(node.get("resolution").isNull()) "" else node.get("resolution").asString()
	              
	              FileUtils.write(outFile, s"$vtype,$project,$commitHash,$path,$sLine,$eLine\n", "UTF-8", true)
            	})
            }
          }
        }
    
        treeWalk.close();
      })
    
    println("Original Neo4J query completed: "+dlist.size)
    println("#Node after filtering: "+counter)
    VioDBFacade.close()
  }
}