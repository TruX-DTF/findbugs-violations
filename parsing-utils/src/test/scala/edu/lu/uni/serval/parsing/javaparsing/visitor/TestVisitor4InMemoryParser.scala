package edu.lu.uni.serval.parsing.javaparsing.visitor

import org.eclipse.jdt.core.dom._

/**
  * Created by darkrsw on 2016/August/15.
  */
class TestVisitor4InMemoryParser extends ASTVisitor
{
  override def visit(node: TypeDeclaration) = { processNode(node); super.visit(node) }
  override def visit(node: EnumDeclaration) = { processNode(node); super.visit(node) }
  override def visit(node: FieldDeclaration) = { processNode(node); super.visit(node) }
  override def visit(node: MethodDeclaration) = { processNode(node); super.visit(node) }

  def processNode(node: ASTNode) =
  {
    Console.println(node.toString)
  }
}
