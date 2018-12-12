package edu.lu.uni.serval.alarm.tracking

import java.io.File
import scala.io.Source
import edu.lu.uni.serval.alarm.util.TrackingHelperUtils
import edu.lu.uni.serval.alarm.tracking.TrackingUtils._
import edu.lu.uni.serval.alarm.util.db.graph.neo4j.VioDBFacade

import scala.collection.JavaConverters._
import scala.collection.mutable._
import org.neo4j.driver.v1.Value
import org.eclipse.jgit.treewalk.TreeWalk
import org.apache.commons.io.FileUtils

object UnfixedAlarmCollectorByVType 
{
  def main(args: Array[String]): Unit =
  {
    doTask("top-50-fixed-violation-types.csv")
  }
  
  def doTask(topFixedVtypesPath: String): Unit =
  {
    val vtypeListFlie = new File(topFixedVtypesPath)
    val vtypeList = Source.fromFile(vtypeListFlie, "UTF-8").getLines()
        
    // prepare project to pack map
		val prj2PackMap = TrackingHelperUtils.readProject2PackMap("/home/darkrsw/repo/false-alarm-study/prj-pack.map")
		println("prj2pack map size: " + prj2PackMap.size)
  	val repoPathTemplate = "/mnt/archive1/data/violations/repos/repos-%s/%s/.git"
  	
  	
  	vtypeList.foreach( vtype => {

  	  //val vtype = "NP_PARAMETER_MUST_BE_NONNULL_BUT_MARKED_AS_NULLABLE"
    	println("VType: Processing "+vtype)
    	
    	// TODO
    	val outFile = new File(s"vtype-unfixed/$vtype.list")
      
      // collect vtype of fixed alarms in the project
    	VioDBFacade.init()
    	
    	val results = VioDBFacade.session.run(
          s"""match (n:Violation {vtype: '$vtype'}) 
  						where NOT (n)-[:CHILD]->(:Violation)
  						return n""")
  						
  		val dlist = results.list().asScala.toList
     	println("Neo4J query completed: "+dlist.size)
     	
    	// reaarange commit and files
    	val projectCommitFileMap = Map[String, Map[String, Map[String, Set[Value]]]]()
  
    	var counter = 0
    	
    	dlist.foreach(line => {
  		  try {
  		    
  		    val node = line.get("n")
  		    
  		    val projectName = node.get("project").asString()
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
  												 		
  												 		
  				val projectMap = if(projectCommitFileMap.contains(projectName))
  				{
  				  projectCommitFileMap(projectName)
  				}
  				else
  				{
  				  val newMap = Map[String, Map[String, Set[Value]]]()
  				  projectCommitFileMap.put(projectName, newMap)
  				  newMap
  				}
  		    
  		    val fileValueMap = if( projectMap.contains(commitHash))
  				{
  				  projectMap(commitHash)
  				}
  				else 
  				{ 
  					val newMap = Map[String, Set[Value]]()
  					projectMap.put(commitHash,newMap)
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
  		    
  		    if( resolution != "fixed")
          {
            counter+=1
    		    valueSet.add(node)
          }
  		    
  	  	} catch {
  		  	case e: Throwable => println("error: "+e.getMessage)
  		  }
      })
      println("#Node after filtering: "+counter)
      
      projectCommitFileMap.foreach( line =>
      {
        try{
          val projectName = line._1
          val commitFileMap = line._2
          
          println("Project: Processing "+projectName)
          
          val pack = prj2PackMap(projectName)
        	val repoPath = repoPathTemplate.format(pack,projectName)
        	val tracker = new AlarmTracker(projectName)
          tracker.initGitRepo(repoPath)
      		val gitproxy = tracker.repoProxy.get
          
          commitFileMap.foreach( entry =>
          {
            try {
              val commitHash = entry._1
              val fileValueMap = entry._2
              
              println("Commit: Processing "+commitHash)
              
              val commit = gitproxy.getCommitByHash(commitHash)
      				
              val treeWalk = new TreeWalk( gitproxy.getRepository() )
              val tree = commit.getTree()
              treeWalk.addTree(tree);
              treeWalk.setRecursive(true);
              
              while( treeWalk.next() ) {
                
                try {
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
        	              
        	              FileUtils.write(outFile, s"$vtype,$projectName,$commitHash,$path,$sLine,$eLine\n", "UTF-8", true)
                    	})
                    }
                  }
                } 
                catch
                {
                  case e: Throwable => println(e.getMessage)
                }
              }
          
              treeWalk.close();
            } 
            catch 
            {
              case e: Throwable => println(e.getMessage)
            }            
          })
          
        }
        catch
        {
          case e: Throwable => println(e.getMessage)
        }
        })
      
      println("Original Neo4J query completed: "+dlist.size)
      println("#Node after filtering: "+counter)
      VioDBFacade.close()
  	  
  	})
  }
}