package edu.lu.uni.serval.parsing.javaparsing.requestor

import com.typesafe.scalalogging.LazyLogging
import org.eclipse.jdt.core.dom.{ASTVisitor, CompilationUnit, FileASTRequestor}

/**
  * Created by darkrsw on 2016/August/15.
  */
class MyParserRequestor(visitor: ASTVisitor) extends FileASTRequestor with LazyLogging
{
  override def acceptAST(path: String, ast: CompilationUnit): Unit =
  {
    MyParserRequestor.currentFilePath = path

    ast.accept(visitor)
    super.acceptAST(path, ast)
  }
}

object MyParserRequestor
{
  var currentFilePath = ""
}