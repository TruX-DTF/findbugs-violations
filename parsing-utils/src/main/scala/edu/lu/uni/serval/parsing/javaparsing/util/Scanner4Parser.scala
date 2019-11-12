package edu.lu.uni.serval.parsing.javaparsing.util

import java.io.File

/**
  * Created by darkrsw on 2017/February/27.
  */
class Scanner4Parser
{
  def getJavaFilesRecursively(root: File): Array[String] =
  {
    if(root.exists()) collectJavaFiles(root.getCanonicalPath).map(_.getCanonicalPath) else Array[String]()
  }

  def getJarFilesRecursively(root: File): Array[String] =
  {
    if(root.exists()) collectJarFiles(root.getCanonicalPath).map(_.getCanonicalPath) else Array[String]()
  }

  def findAllFiles(root: File): Array[File] =
  {
    val these = root.listFiles
    these ++ these.filter(_.isDirectory).flatMap(findAllFiles)
  }

  def collectFiles(rootPath: String, ext: String) = findAllFiles(new File(rootPath)).filter(_.getName.toLowerCase.endsWith(ext))

  def collectClassFiles(rootPath: String) = collectFiles(rootPath, ".class")

  def collectJavaFiles(rootPath: String) = collectFiles(rootPath, ".java")

  def collectJarFiles(rootPath: String) = collectFiles(rootPath, ".jar")
}
