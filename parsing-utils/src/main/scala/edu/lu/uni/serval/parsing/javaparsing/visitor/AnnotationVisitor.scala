package edu.lu.uni.serval.parsing.javaparsing.visitor

import org.eclipse.jdt.core.dom._

/**
  * Created by darkrsw on 2016/August/15.
  */
class AnnotationVisitor extends ASTVisitor
{
  override def visit(node: SingleMemberAnnotation) = {processNode(node); super.visit(node)}
  override def visit(node: NormalAnnotation) = {processNode(node); super.visit(node)}
  override def visit(node: MarkerAnnotation) = {processNode(node); super.visit(node)}

  def processNode(node: ASTNode) =
  {
    node match {
      case x: Annotation => Console.println(x.getClass.toString + ": " + x.resolveTypeBinding().getQualifiedName)
      //case x: NormalAnnotation => Console.println("NormalAnnotation: " + x.getTypeName.getFullyQualifiedName)
      //case x: MarkerAnnotation => Console.println("MarkerAnnotation: " + x.getTypeName.getFullyQualifiedName)
    }
  }
}
