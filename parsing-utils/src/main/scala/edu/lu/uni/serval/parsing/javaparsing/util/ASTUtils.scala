package edu.lu.uni.serval.parsing.javaparsing.util

import org.eclipse.jdt.core.dom.CompilationUnit

/**
  * Created by darkrsw on 2016/July/29.
  */
object ASTUtils
{
  def getLineNumber(cu: CompilationUnit, pos: Int) =
  {
    cu.getLineNumber(pos) - 1
  }

  def getLineRange(cu: CompilationUnit, pos: (Int, Int)): (Int, Int) =
  {
    val startLine = getLineNumber(cu, pos._1)
    val endLine = getLineNumber(cu, pos._1 + pos._2)

    (startLine, endLine)
  }
}
