package edu.lu.uni.serval.alarm.tracking

import org.junit.Test
import org.junit.Assert._
import edu.lu.uni.serval.alarm.util.db.graph.neo4j.VioDBFacade
import scala.collection.JavaConverters._
import scala.collection.mutable._
import org.apache.commons.io.FileUtils
import java.io.File
import com.github.tototoshi.csv.CSVReader
import edu.lu.uni.serval.alarm.util.TrackingHelperUtils
import edu.lu.uni.serval.scm.git.GitProxy
import org.eclipse.jgit.treewalk.TreeWalk
import org.eclipse.jgit.revwalk.RevCommit
import edu.lu.uni.serval.alarm.tracking.TrackingUtils._
import org.neo4j.driver.v1.Record
import org.neo4j.driver.v1.Value

class StatCollectorTest 
{
  @Test
  def testProjectVtypeCollect() =
  {
    val outFile = new File("per-project-vtype.csv")    
    
    VioDBFacade.init()
    
    val results = VioDBFacade.session.run(
        """match (n:Violation) return n.project as prj, n.vtype as vtype, count(n.id) as cnt""")
    	
		val dlist = results.list().asScala.toList
		
		dlist.foreach(line => {
		  
		  try {
  		  
  		  val project = line.get("prj").asString()
  		  val vtype = line.get("vtype").asString()
  		  val count = line.get("cnt").asInt()
  		  
  		  FileUtils.write(outFile, s"$project,$vtype,$count\n", "UTF-8", true)
  		  
		  } catch {
		    case e: Throwable => println("error: "+e.getMessage)
		  }
		  
		})
		
    
    VioDBFacade.close()
  }
  
  @Test
  def testProjectDistinctVtypeCollect() =
  {
    val outFile = new File("distinct-per-project-vtype.csv")    
    
    VioDBFacade.init()
    
    val results = VioDBFacade.session.run(
        """match (n:Violation {class: 'origin'}) return n.project as prj, n.vtype as vtype, count(n.id) as cnt""")
    	
		val dlist = results.list().asScala.toList
		
		dlist.foreach(line => {
		  
		  try {
  		  
  		  val project = line.get("prj").asString()
  		  val vtype = line.get("vtype").asString()
  		  val count = line.get("cnt").asInt()
  		  
  		  FileUtils.write(outFile, s"$project,$vtype,$count\n", "UTF-8", true)
  		  
		  } catch {
		    case e: Throwable => println("error: "+e.getMessage)
		  }
		  
		})
		
    
    VioDBFacade.close()
  }
  
  @Test
  def testCollectAllLeafNodePerProjectVtype() =
  { 
    val outFile = new File("all-leafnodes-per-project-vtype.csv")
    
    VioDBFacade.init()
    
    val results = VioDBFacade.session.run(
        """match (n:Violation) 
            where NOT (n)-[:CHILD]->(:Violation)
             return n.project as prj, n.vtype as vtype, count(n.id) as cnt""")
    	
		val dlist = results.list().asScala.toList
		
		dlist.foreach(line => {
		  
		  try {
  		  
  		  val project = line.get("prj").asString()
  		  val vtype = line.get("vtype").asString()
  		  val count = line.get("cnt").asInt()
  		  
  		  FileUtils.write(outFile, s"$project,$vtype,$count\n", "UTF-8", true)
  		  
		  } catch {
		    case e: Throwable => println("error: "+e.getMessage)
		  }
		  
		})
		
    
    VioDBFacade.close()
  }
  
  @Test
  def testCollectDistinctFixedViolation() =
  {
    val outFile = new File("distinct-fixed-summary-per-project-vtype.csv")
    
    VioDBFacade.init()
    
    val results = VioDBFacade.session.run(
        """match (n:Violation {resolution: 'fixed'}) 
             return n.project as prj, n.vtype as vtype, count(n.id) as cnt""")
    	
		val dlist = results.list().asScala.toList
		
		dlist.foreach(line => {
		  
		  try {
  		  
  		  val project = line.get("prj").asString()
  		  val vtype = line.get("vtype").asString()
  		  val count = line.get("cnt").asInt()
  		  
  		  FileUtils.write(outFile, s"$project,$vtype,$count\n", "UTF-8", true)
  		  
		  } catch {
		    case e: Throwable => println("error: "+e.getMessage)
		  }
		  
		})
		
    
    VioDBFacade.close()
  }
  
  
  @Test
  def testCollectProjectVtypeUnfixed() =
  {
  	val summaryFile = new File("fixed-summary-project-vtype.csv")    
  	val summaryList = CSVReader.open(summaryFile).all()
  	
  	
  	// prepare project to pack map
		val prj2PackMap = TrackingHelperUtils.readProject2PackMap("/home/darkrsw/repo/false-alarm-study/prj-pack.map")
		println("prj2pack map size: " + prj2PackMap.size)
  	val repoPathTemplate = "/mnt/archive1/data/violations/repos/repos-%s/%s/.git"
		
  	val project = "Activiti-Activiti"
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
  		fail()
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
   	println("Neo4J query completed.")
   	
  	// reaarange commit and files
  	val commitFileMap = Map[String, Map[String, Value]]()

  	// collect commits and files.
    dlist.foreach(line => {
		  try { 
		    val node = line.get("n")
  		  val commitHash = node.get("commit").asString()
  		  
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
				else { val newMap = Map[String, Value](); commitFileMap.put(commitHash,newMap); newMap  }
		    
		    fileValueMap.put(mainClassPath, node)
		  } catch {
		  	case e: Throwable => println("error: "+e.getMessage)
		  }
    })
    
    commitFileMap.foreach( entry =>
      {
        val commitHash = entry._1
        val fileValueMap = entry._2
        
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
              val node = fileValueMap(targetPath)
              val vtype = node.get("vtype").asString()
        		  val commitHash = node.get("commit").asString()
        		  val sLine = node.get("sLine").asInt()
        		  val eLine = node.get("eLine").asInt()
        		  val resolution = if(node.get("resolution").isNull()) "" else node.get("resolution").asString()
              
              if( resolution != "fixed" && vtypeSet.contains(vtype))
		            FileUtils.write(outFile, s"$vtype,$project,$commitHash,path,$sLine,$eLine\n", "UTF-8", true)
		  
            }
          }
        }
    
        treeWalk.close();
		    
		    //val sourcePath = findSourcePath(gitproxy, commit, mainClassPath)
  		  
//		    if( sourcePath == "")
//		      println("ERROR: can't find file path: " + mainClassPath)
		    
  		 
        
      })
    
    
    VioDBFacade.close()
  }
  
  
  def findSourcePath(proxy: GitProxy, commit: RevCommit, mainClassPath: String): String =
  {
    val treeWalk = new TreeWalk( proxy.getRepository() )
    val tree = commit.getTree()
    treeWalk.addTree(tree);
    treeWalk.setRecursive(true);
    
    while( treeWalk.next() ) {
      val path = treeWalk.getPathString()
      //println(path)
      if( path.toLowerCase().endsWith(".java") )
      {      
        val source = getSourceText( commit, path, proxy )
        val thisPackagePath = parseAndExtractPackagePath( source )
        val className = takeFileName(path)
        val targetPath = thisPackagePath+"."+className
        
        if(targetPath == mainClassPath)
          return path
      }
    }
    
    treeWalk.close();
    
    return ""
  }
}