package edu.lu.uni.serval.parsing.javaparsing.visitor

import org.eclipse.jdt.core.dom._

import scala.collection.mutable._

/**
  * Created by darkrsw on 2016/July/28.
  */
class ElementListVisitor extends ASTVisitor
{
  // TypeName -> (startPos, length)
  val classTypes = Map[String, (Int, Int)]()
  val fieldList = Map[String, (Int, Int)]()
  val methodList = ListBuffer[(String, Int, Int)]() // due to overloading, e.g., method1:22

  override def visit(node: TypeDeclaration) = { processNode(node); super.visit(node) }
  override def visit(node: EnumDeclaration) = { processNode(node); super.visit(node) }
  override def visit(node: FieldDeclaration) = { processNode(node); super.visit(node) }
  override def visit(node: MethodDeclaration) = { processNode(node); super.visit(node) }

  def processNode(node: ASTNode): Unit =
  {
    node match {
      case e: TypeDeclaration =>
        classTypes += (e.getName.getIdentifier -> (e.getStartPosition, e.getLength))
      case e: EnumDeclaration =>
        classTypes += (e.getName.getIdentifier -> (e.getStartPosition, e.getLength))
      case e: FieldDeclaration => {
        val fieldCollector = new FieldVariableCollector()
        e.accept(fieldCollector)
        fieldList ++ fieldCollector.fieldNames
      }
      case e: MethodDeclaration =>
        methodList += ((e.getName.getIdentifier, e.getStartPosition, e.getLength))
      //case _ => _
    }
  }

  class FieldVariableCollector extends ASTVisitor
  {
    val fieldNames = Map[String, (Int, Int)]()

    override def visit(node: VariableDeclarationFragment) =
    {
      fieldNames += (node.getName.getIdentifier -> (node.getStartPosition, node.getLength))
      super.visit(node)
    }
  }
}
