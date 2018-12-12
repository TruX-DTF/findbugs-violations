package edu.lu.uni.serval.alarm.tracking

import scala.collection.mutable.Map
import scala.collection.mutable.Set

import com.typesafe.scalalogging.LazyLogging

import edu.lu.uni.serval.alarm.tracking.entity.AlarmLineage
import edu.lu.uni.serval.alarm.tracking.entity.AlarmsInCommit
import edu.lu.uni.serval.scm.git.GitProxy
import edu.lu.uni.serval.alarm.tracking.entity.AlarmEntity
import edu.lu.uni.serval.alarm.tracking.TrackingUtils._

import org.eclipse.jgit.diff.DiffEntry.ChangeType._
import org.apache.commons.io.FilenameUtils
import org.eclipse.jgit.diff.DiffEntry
import org.eclipse.jgit.diff.Edit
import org.eclipse.jgit.diff.Edit.Type._
import org.eclipse.jgit.diff.DiffEntry.ChangeType
import edu.lu.uni.serval.alarm.tracking.entity.AlarmClass
import edu.lu.uni.serval.alarm.tracking.entity._
import edu.lu.uni.serval.alarm.tracking.TrackingUtils._

import org.eclipse.jdt.core.dom.CompilationUnit
import edu.lu.uni.serval.parsing.javaparsing.visitor.ElementListVisitor
import edu.lu.uni.serval.parsing.javaparsing.util.ASTUtils
import com.github.gumtreediff.actions.model.Action
import com.github.gumtreediff.actions.model.Update
import org.eclipse.jdt.core.dom.ASTNode
import edu.lu.uni.serval.alarm.util.db.graph.neo4j.VioDBFacade

class AlarmTracker(projectName: String) extends LazyLogging   {
	val allLineages = Set[AlarmLineage]()
	val lineages4Commit = Map[AlarmsInCommit, Set[AlarmLineage]]()
	var repoPath = ""
	var repoProxy: Option[GitProxy] = None 
	
	val matchedPairs = Set[String]()
	
	
	def getFileInfo(path: String, commit: AlarmsInCommit): String =
  {
  	val proxy = this.repoProxy.get
  	
  	val fileSource = TrackingUtils.getSourceText(commit.commitHash, path, proxy)
  	val packageName = TrackingUtils.parseAndExtractPackagePath(fileSource)
  	val className = FilenameUtils.getBaseName(path)
  	
  	return packageName + "." + className
  }
	
  def trackAlarms(rootCommits: Set[AlarmsInCommit]) =
  {
  	rootCommits.foreach( x => startTracking(x) )
  } 
  
  def createAlarmOrigin(e: AlarmEntity)
  {
  	val key = "%s:%s:%s:%s:%d:%d".format(projectName, e.baseCommit.commitHash, 
  			e.className, e.vType, e.startLine, e.endLine)
  	
  	//VioDBFacade.init()
		VioDBFacade.addNewOriginViolation(key, projectName, 
				e.baseCommit.commitHash, e.vType, e.category, e.startLine, e.endLine)
		//VioDBFacade.close()
  }

	def initGitRepo(repoPath: String): Boolean =
	{
		val proxy = new GitProxy
		proxy.setURI(repoPath)
		if(!proxy.connect())
		{
			Console.println("repo connection error: " + repoPath)
			return false
		}
		
		this.repoPath = repoPath
		repoProxy = Some(proxy)
		return true
	}
	
  def startTracking(root: AlarmsInCommit) = 
  {
  	// set up roots (origins) of a set of lineage
  	val initSet = Set[AlarmLineage]()
    	
    for(e <- root.alarms)
    {
    	val aLineage = new AlarmLineage(e)
    	
    	initSet += aLineage    	

    	// register to Neo4J
    	createAlarmOrigin(e)
    }
  	
  	this.lineages4Commit += (root -> initSet)  	
  	this.allLineages ++= initSet
  	
  	def recurChildren(parent: AlarmsInCommit): Unit =
  	{
  		//////////////////////////////////////////////////
  		val children = parent.children
  		
  		for( child <- children )
  		{
  			//matchChildParent(parent, child)
  			val pairString = "%s->%s".format(parent.commitHash, child.commitHash)
  			
  			if( matchedPairs.contains(pairString) )
  			{
  				println()
  				logger.info(s"$pairString has already been matched.")
  				
  				return
  			}
  			else
  			{
	  			AlarmMatcher.matchChildParent(parent, child, projectName, repoProxy.get)
	  			println("\nTracking alarms completed: %s -> %s".format(parent.commitHash, child.commitHash))  			
	  			matchedPairs.add(pairString)
  			}
	  			
  			recurChildren(child)
  		}
  		//////////////////////////////////////////////////
  	}
  	
  	recurChildren(root)
  }
  
  @deprecated 
  def matchChildParent(parent: AlarmsInCommit, child: AlarmsInCommit): Boolean =
  {
  	/* necessary?
  	val checkChild = this.lineages4Commit.get(child) match
  	{
  		case None => true // sane state
  		case Some(_) => false // already tracked
  	}
  	*/
  	
  	val parentLineageSet = this.lineages4Commit.apply(parent)
  	val childLineageSet = Set[AlarmLineage]() // start with empty set
  	val childAlarms = child.alarms
  	val parentAlarms = parent.alarms
  	val tracked = Set[AlarmsInCommit]() // start with empty set
  	
  	val gitproxy = this.repoProxy.get
  	val childCommit = gitproxy.getCommitByHash(child.commitHash)
  	val parentCommit = gitproxy.getCommitByHash(parent.commitHash)
  	
  	val diffs = gitproxy.getChangedFiles(childCommit, parentCommit).toList
  	
  	// commit level
  	if(!TrackingUtils.hasChangedSourceFiles(diffs))
  	{
  		// Nothing changed -> inherit all alarms of lineage
 			for( p <- parentAlarms ) // every alarm in parent commit should have one to one match.
 			{
 				val aAlarm = findExactMatch(p, childAlarms)
 				if( aAlarm == None )
 				{
 					println()
 					logger.error(s"EXACT MATCH: Can't find match for $p from " + child.commitHash)
 				}
 				else
 				{
 					val l = findMatchingLineage(parentLineageSet, p)
 					l.attachAfter(p, aAlarm.get)
 					childLineageSet += l
 				}
 			}
  		
  		this.lineages4Commit += (child -> childLineageSet)
  		
  		return true;
  	}
  	
  	// ==> here: if any source file changed
  	
  	// file level
  	// Some source files have been changed.
  	val changeSourceFiles = TrackingUtils.getChangedSourceFiles(diffs)
  	
  	for( d <- diffs )
  	{
  		d.getChangeType match 
  		{
  			case ChangeType.ADD       => createNewLineages(d.getNewPath, child) // new file added ==> a set of new lineages starts.
  			case ChangeType.DELETE    => finalizeLineages(d.getOldPath, parent) // file deleted, so all Lineages should be finalized.
  			case ChangeType.COPY      => createNewLineages(d.getNewPath, child) // copied but not different from new file added.
  			case ChangeType.RENAME | 																						// if modified, 
  					 ChangeType.MODIFY    => connectRenamedOrModified(d, parent, child) // renamed or moved with, perhaps, modification.
  		}
  	}
  	
  	def connectRenamedOrModified(d: DiffEntry, parent: AlarmsInCommit, child: AlarmsInCommit): Unit = 
	  {
	  	val proxy = this.repoProxy.get
	  	val oldPath = d.getOldPath
	  	val newPath = d.getNewPath
	  	
	  	val oldFileSource = TrackingUtils.getSourceText(parent.commitHash, oldPath, proxy)
	  	val oldPackageName = TrackingUtils.parseAndExtractPackagePath(oldFileSource)
	  	val oldFileCU = TrackingUtils.getCU(oldFileSource)
	  	val oldClassName = FilenameUtils.getBaseName(oldPath)
	  	val oldClassPath = oldPackageName + "." + oldClassName
	  	
	  	val newFileSource = TrackingUtils.getSourceText(child.commitHash, newPath, proxy)
	  	val newPackageName = TrackingUtils.parseAndExtractPackagePath(newFileSource)
	  	val newFileCU = TrackingUtils.getCU(newFileSource)
	  	val newClassName = FilenameUtils.getBaseName(newPath)
	  	val newClassPath = newPackageName + "." + newClassName
	  	
	  	//val parentLineageSet = this.lineages4Commit.apply(parent)
	  	//val childLineageSet = Set[AlarmLineage]() // start with an empty set
	  	//val childAlarms = child.alarms
	  	//val parentAlarms = parent.alarms
	  	//val trackedParent = Set[AlarmsInCommit]() // start with an empty set => parentAlarms - trackedParent = finalized lineages
	  	//val trackedChild = Set[AlarmsInCommit]()  // start with an empty set => childAlarms - trackedChild = new lineages
	  	
	  	val edits = this.repoProxy.get.getEditList(d)
	  	val actions = TrackingUtils.getGumTreeDiff(oldFileSource, newFileSource)
	  	
	  	val oldNodeList = getElementLists(oldFileSource)
	  	val newNodeList = getElementLists(newFileSource)
	  	
	  	for( p <- parent.alarms )
	  	{
	  		// check membership of old class file first
	  		if(inThisFile(p, oldPackageName))
	  		{
	  			val matchingAlarm = getAlarmClass(p, oldNodeList) match {
		  		// if p is associated with a class?
	  				case ClassAlarm => { // find a matching alarm in child alarms
	  					findMatchingClassAlarm(p, child.alarms, oldNodeList, newNodeList, oldFileCU, edits, actions)
	  				}
		  		// if p is associated with a method?
	  				case MethodAlarm => { // find a matching alarm in child alarms
	  					findMatchingMethodAlarm(p, child.alarms, oldNodeList, newNodeList, oldFileCU, edits, actions)
	  				}
		  		// if p is associated with a field?
	  				case FieldAlarm => {
	  					findMatchingFieldAlarm(p, child.alarms, oldNodeList, newNodeList, oldFileCU, edits, actions)
	  				}
		  		// if p is associated with rangeless alarm.
	  				case RangelessAlarm => child.alarms.filter( c => p.vType == c.vType && 
	  						p.className == c.className && p.fieldName == c.fieldName && 
	  						p.methodName == c.methodName).head
	  			// if p is associated with a line or a block?
	  				case ChunkAlarm => { // this must be in the middle of method (or static) block.
	  					findMatchingChunkAlarm(p, child.alarms, oldNodeList, newNodeList, oldFileCU, edits, actions)
	  				}
	  			}
	  		}
				// 2) find a lineage matching the alarm in parent.
				val oldLineage = findMatchingLineage(parentLineageSet, p)
				// TODO not yet completed
				//oldLineage.attachAfter(p, aAlarm.get)
				//childLineageSet += l
	  	}
		}
  	
  	return false // TODO temporary
  }
  
  def findMatchingChunkAlarm(parentAlarm: AlarmEntity, childAlarms: List[AlarmEntity],
  		oldNodeList: ElementListVisitor, newNodeList: ElementListVisitor,
  		oldCU: CompilationUnit, edits: List[Edit], actions: List[Action]): Option[AlarmEntity] =
  {
  	// try to find moved
  	// try to find exact moved
  	// check out whether this is edited set
  	
  	
  	// try exact matching
  	for( c <- childAlarms )
  	{
  		getAlarmClass(c, newNodeList) match {
  			case ChunkAlarm => {
  				if( c.className == parentAlarm.className && c.vType == parentAlarm.vType &&
  						c.methodName == parentAlarm.methodName && c.fieldName == parentAlarm.fieldName &&
  						c.startLine == parentAlarm.startLine && c.endLine == parentAlarm.endLine )
  					return Some(c)
  			}
  		}
  	}
  	
  	return None // TODO temporary
  }
  
  def findMatchingFieldAlarm(parentAlarm: AlarmEntity, childAlarms: List[AlarmEntity],
  		oldNodeList: ElementListVisitor, newNodeList: ElementListVisitor,
  		oldCU: CompilationUnit, edits: List[Edit], actions: List[Action]): Option[AlarmEntity] =
  {
  	// obvious case
  	for( c <- childAlarms )
  	{
  		getAlarmClass(c, newNodeList) match {
  			case FieldAlarm => {
  				if(c.fieldName == parentAlarm.fieldName && c.vType == parentAlarm.vType) return Some(c)
  			}
  		}
  	}
  	
  	val fieldName = parentAlarm.fieldName
  	// try to find renamed
  	val matchingActions = actions.filter{
  		case x: Update => x.getNode.getType == ASTNode.SIMPLE_NAME &&
  											x.getNode.getLabel == fieldName &&
  											fieldExistsInNodeList(fieldName, x.getNode.getPos, oldNodeList) &&
  											hasEditedParent( ASTUtils.getLineNumber(oldCU, x.getNode.getPos), ASTUtils.getLineNumber(oldCU, x.getNode.getPos), edits)
  	}
  	
  	if(!matchingActions.isEmpty)
  	{
  		// renamed!
  		val newFieldName = matchingActions.head.asInstanceOf[Update].getValue
  		val matchingChildAlarms = childAlarms.filter( x => x.vType == parentAlarm.vType &&
  				x.fieldName == newFieldName)
  		
  		// found!
  		if(!matchingChildAlarms.isEmpty)
  			return Some(matchingChildAlarms.head)
  	}
  	
  	return None
  }
  
  def findMatchingMethodAlarm(parentAlarm: AlarmEntity, childAlarms: List[AlarmEntity],
  		oldNodeList: ElementListVisitor, newNodeList: ElementListVisitor,
  		oldCU: CompilationUnit, edits: List[Edit], actions: List[Action]): Option[AlarmEntity] =
  {
  	// obvious case
  	for( c <- childAlarms )
  	{
  		getAlarmClass(c, newNodeList) match {
  			case MethodAlarm => {
  				if(c.methodName == parentAlarm.methodName && c.vType == parentAlarm.vType) return Some(c)
  			}
  		}
  	}
  	
    val methodName = parentAlarm.methodName.split("\\(").head
  	// try to find renamed
  	val matchingActions = actions.filter{
    		case x: Update => x.getNode.getType == ASTNode.SIMPLE_NAME && 
    											x.getNode.getLabel == methodName &&
    											methodExistsInNodeList(methodName, x.getNode.getPos, oldNodeList) &&
    										  hasEditedParent( 
    										  		ASTUtils.getLineNumber(oldCU, x.getNode.getPos), ASTUtils.getLineNumber(oldCU, x.getNode.getPos), edits )
    										
  	}
    
    if(!matchingActions.isEmpty)
    {
    	// renamed!
  		val newMethodName = matchingActions.head.asInstanceOf[Update].getValue
  		val matchingChildAlarms = childAlarms.filter( x => x.vType == parentAlarm.vType &&
  				x.methodName.split("\\(").head == newMethodName)
  		
  		// found!
  		if(!matchingChildAlarms.isEmpty)
  			return Some(matchingChildAlarms.head)
    }
    return None
  }
  
  
  def findMatchingClassAlarm(parentAlarm: AlarmEntity, childAlarms: List[AlarmEntity], 
  		oldNodeList: ElementListVisitor, newNodeList: ElementListVisitor,
  		oldCU: CompilationUnit, edits: List[Edit], actions: List[Action]): Option[AlarmEntity] =
  {
  	// obvious case
  	for(c <- childAlarms)
  	{
  		getAlarmClass(c, newNodeList) match {
  			case ClassAlarm => {
  				if(c.className == parentAlarm.className && c.vType == parentAlarm.vType) return Some(c)
  			}
  		}
  	}
  	
  	// try to find renamed
  	// TODO: can be more concise?
  	// 1) take the target class def line
  	val classTokens = parentAlarm.className.split("\\$")
  	val classIdentifier = classTokens.last.split("\\.").last
  	
  	// 2) compute line range
  	val (startPos, endPos) = newNodeList.classTypes(classIdentifier)
  	val (startLine, endLine) = ASTUtils.getLineRange(oldCU, (startPos, endPos))
  	
  	// 3) check out the start line is edited
  	val matchingEdits = edits.filter( 
  			x => (x.getType == REPLACE && x.getBeginA <= startLine && x.getEndA > startLine) )
  	
    // 4) if there is any edit for class definition
    val matchingActions = if(!matchingEdits.isEmpty)
    {
    // 5) find out there is about UPDATE
    	actions.filter{
    		case x: Update => x.getNode.getType == ASTNode.SIMPLE_NAME && x.getNode.getLabel == classIdentifier
    	}
    } 
    else List[Action]()
  	
    
  	if(!matchingActions.isEmpty)
  	{
  		// renamed!
  		val newClassName = matchingActions.head.asInstanceOf[Update].getValue
  		val matchingChildAlarms = childAlarms.filter( x => x.vType == parentAlarm.vType && 
  				x.className.split("\\$").last.split("\\.").last == newClassName)
  		
  		// found!
  		if(!matchingChildAlarms.isEmpty)
  			return Some(matchingChildAlarms.head)
  	}
  			
  	return None
  }
  
  def finalizeLineages(oldPath: String, parent: AlarmsInCommit): Unit = // return?
  {
  	val fullPackage = getFileInfo(oldPath, parent)
  	
  	val finSet = Set[AlarmEntity]()
  	
  	for(a <- parent.alarms)
  	{
  		if(inThisFile(a, fullPackage))
  		{
  			finSet += a
  		}
  	}
  	
  	// TODO return?
  }
  
  def createNewLineages(newPath: String, child: AlarmsInCommit): Unit =
  {
  	val fullPackage = getFileInfo(newPath, child)
  	
  	val initSet = Set[AlarmLineage]()
  	
  	for(a <- child.alarms)
  	{
  		if(inThisFile(a, fullPackage)) // class and inner classes
  		{
  			val aLineage = new AlarmLineage(a)
    	
    		initSet += aLineage
  		}
  	}
  	
  	this.allLineages ++= initSet
  }
  
  /*def findMatchingChildAlarm(p: AlarmEntity, 
  													 childAlarms: List[AlarmEntity], 
  													 oldClassPath: String,
  													 newClassPath: String,
  													 edits: List[Edit]): AlarmEntity =
  {
  	// 1) find relevant alarms in parent
		if( inThisFile(p, oldClassPath) )
		{
			for( c <- childAlarms )
			{
				// 2) in changed pair? (it might be renamed.)
				if( inThisFile(c, newClassPath) )
				{
					// 3) changed?
					for( e <- edits )
					{
						// matched = whether the child alarm is associated with the parent
						val matched = e.getType match {
							// if inserted, need to identify a matching child
							case Edit.Type.INSERT  => insertedAlarmAtChild(p, c, e)
							case Edit.Type.DELETE  => deletedAlarmAtParent(p, c, e)
							case Edit.Type.REPLACE => retainedAtReplace(p, c, e)
							case _                 => false
						}
						
						if( matched ) return c;
					}
				}
			}
		}
		
		return null;
  }*/
  
  /*def insertedAlarmAtChild(parent: AlarmEntity, child: AlarmEntity, e: Edit): Boolean =
  {
  	// inside the inserted lines and it is a new alarm
  	if( child.startLine > e.getBeginB && child.endLine <= e.getEndB )
  	{
  		// TODO start a new lineage
  		return false
  	}
  	
  	// overlapping with the tail of a child alarm
  	if( child.startLine < e.getBeginB && child.endLine >= e.getBeginB && child.endLine <= e.getEndB )
  	{
  		// see if 
  	}
  }*/
}