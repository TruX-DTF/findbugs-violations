package edu.lu.uni.serval.alarm.tracking

import scala.annotation.tailrec
import scala.collection.mutable.ListBuffer
import scala.collection.mutable.Set
import scala.collection.JavaConverters._

import org.eclipse.jgit.diff.DiffEntry
import org.eclipse.jgit.revwalk.RevCommit

import com.typesafe.scalalogging.LazyLogging

import edu.lu.uni.serval.scm.git.GitProxy
import edu.lu.uni.serval.alarm.tracking.entity.AlarmsInCommit
import edu.lu.uni.serval.parsing.javaparsing.InMemoryParser
import org.eclipse.jdt.core.dom.ASTVisitor
import org.eclipse.jdt.core.dom.PackageDeclaration
import edu.lu.uni.serval.alarm.tracking.entity.AlarmEntity
import com.github.gumtreediff.gen.jdt.JdtTreeGenerator
import com.github.gumtreediff.matchers.Matcher
import com.github.gumtreediff.matchers.Matchers
import com.github.gumtreediff.actions.ActionGenerator
import com.github.gumtreediff.actions.model.Action
import org.eclipse.jdt.core.dom.CompilationUnit
import edu.lu.uni.serval.parsing.javaparsing.visitor.ElementListVisitor
import edu.lu.uni.serval.alarm.tracking.entity._
import org.eclipse.jgit.diff.Edit
import java.util.Scanner
import com.google.common.hash.Hashing
import java.nio.charset.StandardCharsets
import java.io.File
import org.apache.commons.io.FilenameUtils

object TrackingUtils extends LazyLogging
{
	val HASH_SIZE = 100
	
	val hashFunction = Hashing.sha1()
	//byte[] encodedhash = digest.digest(
  //originalString.getBytes(StandardCharsets.UTF_8));
	
	def hashFirstTokens(n: Int, tokens: Array[String]): String =
	{
		val head = takeFirstTokens(n, tokens)
		//val inBytes = head.getBytes
		return hashFunction.hashString(head, StandardCharsets.UTF_8).toString()
	}
	
	def hashLastTokens(n: Int, tokens: Array[String]): String =
	{
		val tail = takeLastTokens(n, tokens)
		//val inBytes = tail.getBytes
		return hashFunction.hashString(tail, StandardCharsets.UTF_8).toString()
	}
	
	def takeFirstTokens(n: Int, tokens: Array[String]): String =
	{
		tokens.slice(0, n).mkString(" ") // first tokens.
	}
	
	def takeLastTokens(n: Int, tokens: Array[String]): String =
	{
		tokens.slice( tokens.length-n, tokens.length ).mkString(" ")
	}
	
	
	def takeLineRange(startLine: Int, endLine: Int, source: String): String =
	{
		var counter = 1
		val sb = new StringBuilder
		
		val scanner = new Scanner(source);
		
		while (scanner.hasNextLine() && counter <= endLine) 
		{
			if( startLine <= counter)
  			sb.append(scanner.nextLine() + "\n")
			else
				scanner.nextLine() // skip
			
			counter += 1;
		}
		
		scanner.close()
		
		sb.mkString
	}
	
	def hasEditedParent(startLine: Int, endLine: Int, edits: List[Edit]): Boolean =
  {
		edits.exists( x => isOverlappedParent(startLine, endLine, x) )
  }
	
	def hasEditedChild(startLine: Int, endLine: Int, edits: List[Edit]): Boolean =
  {
		edits.exists( x => isOverlappedChild(startLine, endLine, x) )
  }
	
	def getOverlappingEditsParent(startLine: Int, endLine: Int, edits: List[Edit]): List[Edit] =
	{
		edits.filter( x => isOverlappedParent(startLine, endLine, x) )
	}
	
	def getOverlappingEditsChild(startLine: Int, endLine: Int, edits: List[Edit]): List[Edit] =
	{
		edits.filter( x => isOverlappedChild(startLine, endLine, x) )
	}
	
	def isOverlapped(startLine: Int, endLine: Int, up: Int, bottom: Int ) =
	{
		// 1. edit is before violation?
		if( startLine > up && startLine > bottom )
			false
		// 2. edit is after violation?
		else if( endLine < up && endLine < bottom )
			false
		else
			true
		
		// vice versa...?
		/*
		// 1. upper overlapping
		if( startLine > up && startLine < bottom && endLine > bottom && endLine > up )
			true
		// 2. bottom overlapping
		else if( startLine < up && startLine < bottom && endLine > up && endLine < bottom )
			true
		// 3. inclusion
		else if( startLine < up && startLine < bottom && endLine > up && endLine > bottom )
			true
		// 4. surrounding
		else if( startLine > up && startLine < bottom && endLine > up && endLine < bottom )
			true
		else
			false	
		*/
	}
	
	
	def isOverlappedParent(startLine: Int, endLine: Int, e: Edit ) =
	{
		val up = e.getBeginA
		val bottom = e.getEndA
		
		isOverlapped(startLine, endLine, up, bottom)
	}
	
	def isOverlappedChild(startLine: Int, endLine: Int, e: Edit ) =
	{
		val up = e.getBeginB
		val bottom = e.getBeginB
		
		isOverlapped(startLine, endLine, up, bottom)
	}
  
	// TODO refactor	
  def methodExistsInNodeList(methodName: String, pos: Int, nodeList: ElementListVisitor): Boolean =
  {
  	nodeList.methodList.exists( x => methodName == x._1 && x._2 == pos)
  }
  
  def fieldExistsInNodeList(fieldName: String, pos: Int, nodeList: ElementListVisitor): Boolean =
  {
  	nodeList.fieldList.exists( x => fieldName == x._1 && x._2._1 == pos)
  }

	def getAlarmClass(a: AlarmEntity, nodeList: ElementListVisitor): AlarmClass = 
  {
  	// for class?
  	if( a.fieldName == "NO_FIELD" && a.methodName == "NO_METHOD" )
  		return ClassAlarm
  	
  	// for field?
  	if( a.fieldName != "NO_FIELD" && a.methodName == "NO_METHOD" )
  		return FieldAlarm
  		
  	if( a.startLine == -1 && a.endLine == -1 )
  		return RangelessAlarm
  		
  	// for method?
		{
  		val methodName1 = a.methodName.split("\\(")(0)
  		val className = a.className.split("\\$").last.split("\\.").last
  		
  		val methodName = // if it is a constructor, replace it with its class name
  			if(methodName1 == "<init>") className else methodName1
  		
  		val startLine = a.startLine
  		
  		if( nodeList.methodList.exists(m => m._1 == methodName && m._2 == startLine) )
  				return MethodAlarm
		}
  	
  	return ChunkAlarm
  }
	
	def inThisFile(a: AlarmEntity, fullClassPath: String): Boolean =
  {
  	if(a.className == fullClassPath || a.className.startsWith(fullClassPath + "$")) true
  	else false
  }
	
	def findMatchingLineage(lineageSet: Set[AlarmLineage], a: AlarmEntity): AlarmLineage =
  {
  	for(l <- lineageSet)
  	{
  		if(l.contains(a)) return l;
  	}
  	
  	logger.error("No matching alarms in lineages: " + a.toString())
  	return null; // can happen?
  }
	
	def sortCommits(l: List[AlarmsInCommit], gitproxy: GitProxy): Set[AlarmsInCommit] =
  {
  	val hashCommitMap = (for(c <- l) yield (c.commitHash -> c)).toMap
  	
  	println("Commits to sort #: " + hashCommitMap.size)
  	
  	val rootCommits = Set[AlarmsInCommit]()
 		
  	def findParentCommit(parentCommit: RevCommit): Set[AlarmsInCommit] =
  	{
			val ancestors = Set[AlarmsInCommit]()
			val pHash = parentCommit.getName
			
			if(hashCommitMap.contains(pHash)) // if there is any commit analyzed, collect and stop traversing.
			{
				// collect
				val parentCommit = hashCommitMap(pHash)
				ancestors += parentCommit
			}
			else // recursive
			{
				val grandParents = parentCommit.getParents
				if( grandParents != null ) 
				{
					grandParents.foreach( g => ancestors ++= findParentCommit(g) )
					 // else, there is no preceding commits analyzed.
						//else /* TODO TESTING */ Console.println("!!!!!!!!!!! empty grand parents !!!!!!!!!!!!!!")
				}
			}

			return ancestors
  	}
  	
  	def collectRelations(child: AlarmsInCommit, parentList: Set[AlarmsInCommit]): Unit =
  	{
  		if(parentList.isEmpty)
  		{
  			// TODO only for testing.
  			//Console.println("############ " + child.commitHash + " has no parent!")
  			rootCommits += child
  		}
  		else
  		{
  			parentList.foreach(child.parents += _)
  			parentList.foreach( p => p.children += child )
  		}

  		/*for(p <- parentList)
  		{
  			// TODO only for testing.
  			//Console.print("   " + p.commitHash)
  			//counter += 1
  			
  			p.children += child
  		}*/
  	}
  	
  	for( (hash, alarmsInCommit) <- hashCommitMap )
  	{
  		val commit = gitproxy.getCommitByHash(hash)
  		val parentCommits = commit.getParents
  		
  		val parentList = Set[AlarmsInCommit]()
  		
  		//val parentList = findParentCommit(parentCommits.toList, List[AlarmsInCommit]())
  		parentCommits.foreach( p => parentList ++= findParentCommit(p) )
  		
  		collectRelations(alarmsInCommit, parentList)
  	}
  	
  	return rootCommits;
  }
  
  def hasChangedSourceFiles(entries: List[DiffEntry]): Boolean =
  {
  	if(entries.isEmpty)
  		return false
  	else
  	{
  		@tailrec
  		def findJavaFiles(in: List[DiffEntry], has: Boolean): Boolean =
  		{
  			in match {
  				case Nil => false
  				case x :: tail => if( x.getNewPath.toLowerCase().endsWith(".java") ) true 
  													else findJavaFiles(tail, has | false) // "has" has meaning? 
  			}
  		}
  		
  		findJavaFiles(entries, false)
  	}
  }
  
  // This returns List[DiffEntry] of source code changes
  def getChangedSourceFiles(entries: List[DiffEntry]): List[DiffEntry] =
  {
  	val retList = ListBuffer[DiffEntry]() // empty list
  	
		for( diff <- entries )
		{
			val isSource = if( diff.getOldPath.toLowerCase().endsWith(".java") ) true else false
			
			if(isSource)
				retList += diff
		}
		
  	return retList.toList
  }
  
  def getSourceText(commit: RevCommit, path: String, proxy: GitProxy): String =
  {
  	proxy.getFileContent(commit, path)
  }
  
  def getSourceText(commit: String, path: String, proxy: GitProxy): String =
  {
  	proxy.getFileContent(commit, path)
  }
  
  // how about multiple matches?
  def findExactMatch(s: AlarmEntity, aSet: List[AlarmEntity]): Option[AlarmEntity] =
  {
  	for(t <- aSet)
  	{
  		if( isSameAlarm( s, t ) )	return Some(t)
  	}
  	
  	return None;
  }
  
  def isSameAlarm( p: AlarmEntity, c: AlarmEntity ): Boolean =
  {
  	if(c.className == p.className &&
  				c.methodName == p.methodName &&
  				c.fieldName  == p.fieldName &&
  				c.vType      == p.vType &&
  				c.startLine  == p.startLine &&
  				c.endLine    == p.endLine)
  		return true
  	else
  		return false
  }
  
  def isSameButDiffLoc( p: AlarmEntity, c: AlarmEntity ): Boolean =
  {
  	if(c.className == p.className &&
  				c.methodName == p.methodName &&
  				c.fieldName  == p.fieldName &&
  				c.vType      == p.vType)
  		return true
  	else
  		return false
  }
  
  /*
  def isSameFileAndType( p: AlarmEntity, c: AlarmEntity ): Boolean =
  {
  	if(c.className == p.className &&
  				c.vType      == p.vType)
  		return true
  	else
  		return false
  }*/
  
  def isSameType( p: AlarmEntity, c: AlarmEntity ): Boolean =
  {
  	if(c.vType == p.vType)
  		return true
  	else
  		return false
  }
  
  def isMergingCommit(a: AlarmsInCommit): Boolean =
  {
  	if(a.parents.size > 1)
  		true
  	else
  		false
  }
  
  def isTerminalCommit(a : AlarmsInCommit): Boolean =
  {
  	if(a.children.size == 0) true
  	else false
  }
  
  def isBranchingCommit(a : AlarmsInCommit): Boolean =
  {
  	if(a.children.size > 1) true
  	else false
  }
  
  // Assumes the input is like "org.apache.hadoop.io.TestGenericWritable$Bar" and take outer class.
 	def takeClassName(in: String): String =
 	{
 		val tokens = in.split("\\.")
 		val classSignature = tokens(tokens.size-1)
 		
 		val outerClass = if(classSignature.contains("$"))
 		{
 			val tokens2 = classSignature.split("\\$")
 			tokens2(0)
 		}
 		else classSignature
 			
 		return outerClass 		
 	}
 	
 	def takeFileName(path: String): String =
 	{
 		//val tfile = new File(path)
 		
 		return FilenameUtils.getBaseName(path)
 	}
 	
 	// take a source code file (.java) and extract full class path (main class only? => yes because a main class name is file name) 
 	def parseAndExtractPackagePath(source: String): String =
 	{
 		val cu = getCU(source)
 		
 		val packageVisitor = new ASTVisitor() 
 		{
 			var packageName = ""
 	    override def visit(node: PackageDeclaration): Boolean =
      {
        packageName = node.getName.toString()
        return super.visit(node)
      }
 			def getPackageName() = this.packageName
 		}
 		
 		cu.accept(packageVisitor) 		
 		return packageVisitor.getPackageName
	}
 	
 	def getCU(source: String): CompilationUnit = InMemoryParser.parse(source)
 	
  def getElementLists(source: String): ElementListVisitor =
  {
 		val cu = getCU(source)
 		
 		return getElementLists(cu)
  }
 	
 	def getElementLists(cu: CompilationUnit) =
 	{
 		val elemVisitor = new ElementListVisitor
 		cu.accept(elemVisitor)
 		
 		elemVisitor
 	}
 	
 	def getGumTreeDiff(oldSource: String, newSource: String): List[Action] =
 	{
 		val oldTC = new JdtTreeGenerator().generateFromString(oldSource)
 		val newTC = new JdtTreeGenerator().generateFromString(newSource)
 		
 		val oldTree = oldTC.getRoot
 		val newTree = newTC.getRoot
 		
 		val matcher = Matchers.getInstance.getMatcher(oldTree, newTree)
 		matcher.`match`()
 		
 		val ag = new ActionGenerator(oldTree, newTree, matcher.getMappings)
 		ag.generate()
 		
 		val actions = ag.getActions
 		
 		return actions.asScala.toList;
 	}
 	
  
  // TODO This is for printing.
  def traverseAndPrintAllChildren(root: AlarmsInCommit) =
  {
  	Console.println("Root: " + root.commitHash)
  	
  	val visitedSet = Set[String]()
  	
  	def recurAllChildren(parent: AlarmsInCommit, in: Set[AlarmsInCommit], counter: Int): Unit = 
  	{
  		for(child <- in)
  		{
  			val key = "%s->%s".format(parent.commitHash, child.commitHash)
  			
  			if( ! visitedSet.contains(key) )
  			{
  				Console.println("   " + counter + " " + child.commitHash)
  				visitedSet.add(key)
  				recurAllChildren(child, child.children, counter + 1)
  			}
  		}
  	}
  	
  	recurAllChildren(root, root.children, 1)
  }
  
  // This is just for testing.
	def main(args: Array[String]): Unit =
	{
		val alarmRootPath = args(0)
		val repoRootPath = args(1)
		
		val reader = new AlarmDataReader
		val list = reader.readAlarmList4AllCommits(alarmRootPath)
		
		Console.println("# of commits: " + list.size)
		
		val totalAlarms = list.map(_.alarms.size).sum
		Console.println(s"# of total alarms: $totalAlarms")
		
		val proxy = new GitProxy
		proxy.setURI(repoRootPath)
		if(!proxy.connect())
		{
			Console.println("repo connection error")
			return
		}
		
		val roots = sortCommits(list, proxy)
		
		roots.foreach(traverseAndPrintAllChildren(_))
		
		//Console.println("\n\n\ntotal relation counts: " + counter)
	}
}