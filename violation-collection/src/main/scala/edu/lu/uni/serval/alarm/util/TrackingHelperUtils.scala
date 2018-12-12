package edu.lu.uni.serval.alarm.util

import java.io.File
import org.apache.commons.io.FileUtils
import scala.io.Source
import scala.collection.mutable._
import org.apache.commons.io.FilenameUtils

object TrackingHelperUtils 
{
	def main(args: Array[String]): Unit =
	{
		mapProject2Pack("prj-pack.map", "/mnt/exp2/data/violation/working-%s/reports")
	}
	
	def mapProject2Pack(mapFilePath: String, pathTemplate: String) =
	{
		val mapFile = new File(mapFilePath)
		
		val index = 'a'
		
		0 to 20 foreach { x => 
			val indexChar = (index + x).toChar 
			println(indexChar)
			
			val reportRootDir = new File( pathTemplate.format(indexChar) )
			
			val prjList = reportRootDir.listFiles().filter(_.isDirectory())
			prjList.foreach( x => FileUtils.write(mapFile, FilenameUtils.getBaseName(x.getCanonicalPath) + 
			    ":repo-"+indexChar + "\n", "UTF-8", true) )
		}
	}
	
	def readProject2PackMap(mapFilePath: String) =
	{
		val mapFile = new File(mapFilePath)
		val aMap = Map[String,String]()
		
		val mList = Source.fromFile(mapFile).getLines().toList
		println("#lines: "+mList.size)
		
		mList.foreach( line => {
			val tokens = line.split(":")
			val repotokens = tokens(1).split("-")
			val prj = tokens(0)
			val packid = repotokens(1)
			
			/*if(aMap.contains(prj))
			  println(s"$prj has been added already.")
			else  */
			  aMap += (prj->packid)
		})
		
		aMap
	}
}