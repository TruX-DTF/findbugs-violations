package edu.lu.uni.serval.scm.git

import org.junit.Test
import org.junit.Assert._
import org.eclipse.jgit.diff.DiffFormatter
import org.eclipse.jgit.util.io.DisabledOutputStream
import scala.collection.JavaConverters._

class GitProxyTest 
{
	@Test
	def testGetFileContent() =
	{
		val proxy = new GitProxy()
		proxy.setURI("/Users/darkrsw/git/repo-iterator/.git")
		proxy.connect()
		
		val path = "src/main/scala/edu/lu/uni/serval/scm/git/GitProxy.scala"
		val content = proxy.getFileContent("HEAD", path)
		
		
		Console.println("File content of " + path)
		if( content == null )
		{
			Console.println("File not found")
			fail()
		}
		else
			Console.println(content)
	}
	
	@Test
	def testGetChangedFiles()
	{
		val proxy = new GitProxy()
		proxy.setURI("/Users/darkrsw/git/repo-iterator/.git")
		proxy.connect()
		
		val commit = "49cb50e"
		val parent = commit + "^"
		
		val c = proxy.getCommitByHash(commit)
		val p = proxy.getCommitByHash(parent)
		
		val diffs = proxy.getChangedFiles(c, p).toList
		
		val df = new DiffFormatter(DisabledOutputStream.INSTANCE)
		df.setRepository(proxy.getRepository())
			
		
		for(d <- diffs)
		{
			Console.println("OLD: " + d.getOldPath)
			Console.println("NEW: " + d.getNewPath)
			Console.println("CHANGE_TYPE: " + d.getChangeType)			
			
			val header = df.toFileHeader(d)
			
			for( e <- header.toEditList().asScala )
			{
				Console.println(e.toString())
			}
		}
	}	
}