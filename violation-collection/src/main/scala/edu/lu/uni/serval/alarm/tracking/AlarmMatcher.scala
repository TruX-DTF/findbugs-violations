package edu.lu.uni.serval.alarm.tracking

import edu.lu.uni.serval.alarm.tracking.entity.AlarmLineage
import edu.lu.uni.serval.alarm.tracking.entity.AlarmsInCommit
import org.apache.commons.io.FilenameUtils
import org.eclipse.jgit.diff.DiffEntry
import edu.lu.uni.serval.alarm.tracking.entity.ChunkAlarm
import edu.lu.uni.serval.alarm.tracking.entity.FieldAlarm
import edu.lu.uni.serval.alarm.tracking.entity.ClassAlarm
import edu.lu.uni.serval.alarm.tracking.entity.AlarmEntity
import edu.lu.uni.serval.alarm.tracking.entity.MethodAlarm
import edu.lu.uni.serval.alarm.tracking.entity.RangelessAlarm
import edu.lu.uni.serval.alarm.tracking.TrackingUtils._
import edu.lu.uni.serval.scm.git.GitProxy
import org.eclipse.jgit.revwalk.RevCommit
import org.eclipse.jgit.diff.DiffEntry.ChangeType
import scala.collection.mutable._
import com.typesafe.scalalogging.LazyLogging
import org.eclipse.jgit.diff.Edit
import edu.lu.uni.serval.alarm.util.db.graph.neo4j.VioDBFacade


object AlarmMatcher extends LazyLogging
{
	val MATCHING_THRESHOLD = 3
	
	def matchChildParent(parent: AlarmsInCommit, child: AlarmsInCommit, project:String, 
			gitproxy: GitProxy): List[AlarmEntity] /* TODO: OK? */ =
  {
		val parentAlarms = collection.mutable.Set(parent.alarms:_*)
		val childAlarms = collection.mutable.Set(child.alarms:_*)

		// TODO this is just for debugging.
		/*if( parentAlarms.size > 10 || childAlarms.size > 10 )
		{
			logger.error(s"Incorrect number of alarms: ${parentAlarms.size}, ${childAlarms.size}")
			logger.error(s"\t at ${parent.commitHash} ==> ${child.commitHash}.")
		}*/
		
  	val parentTracked = Set[AlarmEntity]() // should be a set? // tracked parent alarms; start with empty set
  	val childTracked = Set[AlarmEntity]() // should be a set?  // tracked child alarms; start with empty set
  	
  	val trackingMap = Map[AlarmEntity, AlarmEntity]() // tracked alarms pairs. (aParent -> aChild)
  	
  	//val gitproxy = this.repoProxy.get
  	val parentCommit = gitproxy.getCommitByHash(parent.commitHash)
  	val childCommit = gitproxy.getCommitByHash(child.commitHash)
  	
  	// find changed set
  	val diffs = gitproxy.getChangedFiles(childCommit, parentCommit).toList
  	val sourceChanges = getChangedSourceFiles(diffs)
  	val (parentChangedPaths, childChangedPaths, diffMap) = 
  			transformFilesToPackagePaths(sourceChanges, parentCommit, childCommit, gitproxy)
  	
  	//================== inline functions
  	def recordSuccessMatch(pa: AlarmEntity, 
  			matchedAlarms: Set[AlarmEntity], mainClassName: String, matchedby: String): Boolean =
		{
			if( matchedAlarms.size > 1 ) // if there are multiple matches, log it and take the first one.
	  	{
				println()
				logger.error("Multiple matches! " + matchedby)
	  		logger.error("Multiple matches:")
	  		logger.error("\t Parent:" + pa + " in " + parent.commitHash)
	  		matchedAlarms.foreach( x => logger.error("\t Child:" + x + " in " + child.commitHash) )
	  	}
			
			// TODO take which is not matched already instead of simple head.
			val untrackedAlarms = (matchedAlarms -- childTracked)
			
			if( untrackedAlarms.isEmpty )
			{
				println()
	  		logger.error("All candidates are already matched! " + matchedby)
	  		logger.error("All matched! \n" + pa + " is in a changed source: " + mainClassName)
	  		
	  		false
			}
	  	else
	  	{
		  	// Check out whether it is already in tracked alarms. (If it is, exceptional case.)
		  	if( untrackedAlarms.size != matchedAlarms.size )
		  	{
		  		println()
		  		logger.error("Some already matched! " + matchedby)
		  		logger.error("Some already matched! \n" + pa + " is in a changed source: " + mainClassName)
		  	}

	  		
				val matchedChild = untrackedAlarms.head // assuming there are one single match.

  	  	// record tracked
  	  	parentTracked += pa
  	  	childTracked += matchedChild
  	  	trackingMap += (pa -> matchedChild) // finally matched in unchanged files.
  	  	
  	  	// record into Neo4J
  	  	val parentKey = "%s:%s:%s:%s:%d:%d".format(project, parent.commitHash, pa.className, 
  	  			pa.vType, pa.startLine, pa.endLine)
  	  	val childKey = "%s:%s:%s:%s:%d:%d".format(project, child.commitHash, matchedChild.className, 
  	  			matchedChild.vType, matchedChild.startLine, matchedChild.endLine)
  	  	
  	  	//VioDBFacade.init()
				//id: String, pid: String, commit: String, sLine: Int, eLine: Int
				VioDBFacade.connect2Parent(
						parentKey, childKey, child.commitHash, matchedby,
						matchedChild.startLine, matchedChild.endLine)
				//VioDBFacade.close()
  	  	
  	  	true
	  	}
			// :matched:
		}
  			
    def findExactMatching(pa: AlarmEntity, mainClassName: String): Boolean =
		{
	  	val matchedChildAlarms = findExactlyMatchingAlarm( pa, childAlarms )
	  	
	  	if( matchedChildAlarms.size == 0 ) // if no matching (even if parent is in unchanged set), log it and do nothing
	  	{
	  		println()
	  		logger.error("No match! even though a parent alram " + pa + " is in unchanged source: " + mainClassName)
	  		false
	  	}
	  	else
	  	{
	  		recordSuccessMatch(pa, matchedChildAlarms, mainClassName, "exact")
	  	}
		}
		////////////////////////////////////// end: inline functions
  			
		// for-loop ======================================================
  	for( pa <- parentAlarms )
  	{
  		// 0. find out whether it is in changed set.
  		//val mainCName = takeClassName(pa.className) // to find out file name.
  		//val packageName = takePackage
  	  val mainClassPath = if(pa.className.contains("$"))
											 		{
											 			val tokens2 = pa.className.split("\\$")
											 			tokens2(0)
											 		}
											 		else pa.className


 	  	val successAny = if( ! parentChangedPaths.contains(mainClassPath) ) // if not in changed files
  	  {
 	  		// 1. Try to find matching alarms in child alarm set 
	  		// (only one method available since it is in the unchanged set).
	  		// exact matching in non-changed set.
	  		findExactMatching( pa, mainClassPath )
  	  }
  	  // if not, in changed files
 	  	else
 	  	{
				// 2. try to find exactly matching alarms in changed files.
				val exactMatches = findExactlyMatchingAlarm( pa, childAlarms )
				
				val successExact = if(exactMatches.size > 0)
				{
					recordSuccessMatch(pa, exactMatches, mainClassPath, "exact")
				}
				else
					false
					
				val successLoc = if( ! successExact )
				{
					// 3. location-based matching.
					val d = diffMap(mainClassPath)
					val edits = gitproxy.getEditList(d)
					
					val locMatchingAlarms = findLocBasedMatchingAlarms( pa, childAlarms, edits )
					
					if( locMatchingAlarms.size > 0 )
					{
						recordSuccessMatch(pa, locMatchingAlarms, mainClassPath, "location")							
					}
					else
						false
				}
				else
					true
					
				val successSnippet = if( ! successLoc )
				{
					// 3. snippet-based matching: in case of moving
					val d = diffMap(mainClassPath)
					
					if(d.getChangeType == DiffEntry.ChangeType.DELETE)
					{
						false
					}
					else
					{
						val d = diffMap(mainClassPath)
						val edits = gitproxy.getEditList(d)
						val parentMatchingEdits = getOverlappingEditsParent( pa.startLine, pa.endLine, edits )
						
						val parentSnippet = TrackingUtils.takeLineRange(pa.startLine, pa.endLine, 
							gitproxy.getFileContent(parent.commitHash, d.getOldPath) )
						
						val childSource = gitproxy.getFileContent(child.commitHash, d.getNewPath)
						val candidates = childAlarms.filter( c => isSameType(pa, c) )
													
						if( parentMatchingEdits.size > 0 ) // if parent is in edit ranges.
						{
							val matchedAlarms = candidates.filter( x => {
								val childMatchingEdits = getOverlappingEditsChild( x.startLine, x.endLine, parentMatchingEdits )
							
								if(childMatchingEdits.size > 0)
								{
									val childSnippet = TrackingUtils.takeLineRange( x.startLine, x.endLine, childSource)
								
									(parentSnippet == childSnippet)
								}
								else
									false
							})
							
							if( matchedAlarms.size > 0 )
							recordSuccessMatch(pa, matchedAlarms, mainClassPath, "snippet")
							else
								false
						}
						else // if parent is *NOT* in edit ranges
						{
							val matchedAlarms = candidates.filter( x => {
									val childMatchingEdits = getOverlappingEditsChild( x.startLine, x.endLine, parentMatchingEdits )
								
									if(childMatchingEdits.size == 0)
									{
										val childSnippet = TrackingUtils.takeLineRange( x.startLine, x.endLine, childSource)
										
										(parentSnippet == childSnippet)
									}
									else
										false
							})
							
							if( matchedAlarms.size > 0 )
								recordSuccessMatch(pa, matchedAlarms, mainClassPath, "snippet")
							else
								false
						}
					}
				}
				else
					true
					
				val successHash = if( ! successSnippet )
				{
					val d = diffMap(mainClassPath)
					
					if(d.getChangeType == DiffEntry.ChangeType.DELETE)
					{
						false
					}
					else
					{
						val parentSnippet = TrackingUtils.takeLineRange(pa.startLine, pa.endLine, 
								gitproxy.getFileContent(parent.commitHash, d.getOldPath) )
								
						val candidates = childAlarms.filter( c => isSameType(pa, c) )
						val childSource = gitproxy.getFileContent(child.commitHash, d.getNewPath)
						
						val parentTokens = parentSnippet.split("\\s+")
						
						if( parentTokens.length <= 100 )
						{
							false // not possible to apply this heuristic
						}
						else
						{
							val parentHeadHash = TrackingUtils.hashFirstTokens(TrackingUtils.HASH_SIZE, parentTokens) 						
							val parentTailHash = TrackingUtils.hashLastTokens(TrackingUtils.HASH_SIZE, parentTokens)
							
							val matchedAlarms = candidates.filter( x => {
								val childSnippet = TrackingUtils.takeLineRange( x.startLine, x.endLine, childSource)
								val childTokens = childSnippet.split("\\s+")
								
								val childHeadHash = TrackingUtils.hashFirstTokens(TrackingUtils.HASH_SIZE, childTokens)
								val childTailHash = TrackingUtils.hashLastTokens(TrackingUtils.HASH_SIZE, childTokens)
								
								if( (parentHeadHash == childHeadHash) || // parent head hash == child head hash
										(parentTailHash == childTailHash) )  // OR tail hash matches.
								{
									true
								}
								else
								{
									false
								}
							})
							
							if( matchedAlarms.size > 0 )
								recordSuccessMatch(pa, matchedAlarms, mainClassPath, "hash")
							else
								false	
						}
					}
				}
				else
					true
				
				successHash
  	  }

  	  //
  	  if( ! successAny ) // i.e., // all matching failed
  	  {
  	  	// uid
  	  	val key = "%s:%s:%s:%s:%d:%d".format(project, parent.commitHash, pa.className, pa.vType, pa.startLine, pa.endLine)
  	  	
  	  	// resolution
  	  	val resolution = 
  	  		if( diffMap.contains(mainClassPath) )
	  	  	{
	  	  		val diff = diffMap(mainClassPath)
	  	  		
	  	  		if(diff.getChangeType == DiffEntry.ChangeType.DELETE)
	  	  			"disappeared"
	  	  		else
	  	  			"fixed"
	  	  			
	  	  		// TODO deal with "RENAME"
	  	  	}
  	  		else
  	  			"unknown"
  	  	
  	  	// pa should be a terminal.
  	  	parentTracked += pa
  	  	//VioDBFacade.init()
				VioDBFacade.setTerminal(key, resolution, child.commitHash)
				//VioDBFacade.close()
  	  }
  	  
  	  // :end for-loop
  	}
  	
  	
  	// *** untracked child alarms ==> new origins ==> register to Neo4J
  	val untrackedChildAlarms = (childAlarms -- childTracked)
  	
  	untrackedChildAlarms.foreach( e => {
  			val key = "%s:%s:%s:%s:%d:%d".format(project, e.baseCommit.commitHash, 
  			e.className, e.vType, e.startLine, e.endLine)
  	
		  	//VioDBFacade.init()
				VioDBFacade.addNewOriginViolation(key, project, 
						e.baseCommit.commitHash, e.vType, e.category, e.startLine, e.endLine)
				//VioDBFacade.close()
  		})
  	
  		
  	// *** untracked parent alarms ==> unknown???
  	val untrackedParentAlarms = (parentAlarms -- parentTracked)
  	
  	untrackedParentAlarms.foreach( e => {
  			val key = "%s:%s:%s:%s:%d:%d".format(project, e.baseCommit.commitHash, 
  			e.className, e.vType, e.startLine, e.endLine)
  			
  			//VioDBFacade.init()
				VioDBFacade.setTerminal(key, "Unknown", child.commitHash)
				//VioDBFacade.close()
	  	})
  	
  	
  	return List[AlarmEntity]() // TODO temporary
  }
	
  	  
  def findLocBasedMatchingAlarms( pa: AlarmEntity, childAlarms: Set[AlarmEntity], edits: List[Edit] ): Set[AlarmEntity] =
  {
  	val candidate1 = childAlarms.filter( child => isSameButDiffLoc( pa, child ) )
  	
  	val matchingEdits = getOverlappingEditsParent( pa.startLine, pa.endLine, edits )
  	val candidate2 = candidate1.filter( x => hasEditedChild( x.startLine, x.endLine, matchingEdits) )
  	
  	//val parentDelta = Math.abs(pa.startLine - )
  	
  	candidate2.filter( x => {
  			val childEdits = getOverlappingEditsChild( x.startLine, x.endLine, matchingEdits )
  			
  			childEdits.exists( y => { 
  				Math.abs( Math.abs(pa.startLine - y.getBeginA) - Math.abs(x.startLine - y.getBeginB) ) <= MATCHING_THRESHOLD } )
  		}
  	)
  }
	
	def findExactlyMatchingAlarm( pa: AlarmEntity, childAlarms: Set[AlarmEntity] ): Set[AlarmEntity] =
	{
		childAlarms.filter( child => isSameAlarm( pa, child ) )
	}
	
	def transformFilesToPackagePaths(diffs: List[DiffEntry], parentCommit: RevCommit, childCommit: RevCommit,
			gitproxy: GitProxy): (Set[String], Set[String], Map[String, DiffEntry]) =  // IN: diffs / parent / child / gitproxy
	{
		val oldPaths = Set[String]()
		val newPaths = Set[String]()
		val diffMap = Map[String, DiffEntry]()
		
		for( d <- diffs )
		{
			val oldSource = getSourceText( parentCommit, d.getOldPath, gitproxy )
			val oldPackage = parseAndExtractPackagePath( oldSource )
			val oldClassName = takeFileName(d.getOldPath)
			
			val newSource = if( d.getChangeType == DiffEntry.ChangeType.DELETE )
											{
												"" // empty since it is deleted
											}
											else	
											{
												getSourceText( childCommit, d.getNewPath, gitproxy )
											}
			
			val (newPackage, newClassName) = 
				if( d.getChangeType == DiffEntry.ChangeType.DELETE )
				{
					(oldPackage, oldClassName)
				}
				else
				{
					val np = parseAndExtractPackagePath( newSource )
					val nc = takeFileName(d.getNewPath)
					(np, nc)
				}
			
			val oldPackagePath = oldPackage+"."+oldClassName
			val newPackagePath = newPackage+"."+newClassName
			oldPaths.add(oldPackagePath)		
			newPaths.add(newPackagePath)
			
			diffMap += (oldPackagePath -> d)
		}
		
		return (oldPaths, newPaths, diffMap)
	}
}





////////////////////////////////////////////////////////////////////////////////////////////////////////
// backup

/*
  	// commit level
  	if(!TrackingUtils.hasChangedSourceFiles(diffs))
  	{
  		// Nothing changed -> inherit all alarms of lineage
 			for( p <- parentAlarms ) // every alarm in parent commit should have one to one match.
 			{
 				val aAlarm = TrackingUtils.findExactMatch(p, childAlarms)
 				if( aAlarm == None )
 					logger.error(s"EXACT MATCH: Can't find match for $p from " + child.commitHash)
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
  	*/