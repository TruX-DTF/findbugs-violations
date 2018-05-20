package edu.lu.uni.serval.diff.parser.utils;

public class NodeChecker {
	
	public static boolean isExpression(String astNode) {
		if ("BooleanLiteral".equals(astNode)) return true;
		if ("CharacterLiteral".equals(astNode)) return true;
		if ("NullLiteral".equals(astNode)) return true;
		if ("NumberLiteral".equals(astNode)) return true;
		if ("StringLiteral".equals(astNode)) return true;
		if ("ThisExpression".equals(astNode)) return true;
		if ("SimpleName".equals(astNode)) return true;  // Name
		if ("QualifiedName".equals(astNode)) return true;
		if ("NormalAnnotation".equals(astNode)) return true; // Annotation
		if ("MarkerAnnotation".equals(astNode)) return true;
		if ("SingleMemberAnnotation".equals(astNode)) return true;
		return isComplexExpression(astNode);
	}
	
	public static boolean isComplexExpression(String astNode) {
		if ("ArrayAccess".equals(astNode)) return true;
		if ("ArrayCreation".equals(astNode)) return true;
		if ("ArrayInitializer".equals(astNode)) return true;
		if ("Assignment".equals(astNode)) return true;
		if ("CastExpression".equals(astNode)) return true;
		if ("ClassInstanceCreation".equals(astNode)) return true;
		if ("ConditionalExpression".equals(astNode)) return true;
		if ("CreationReference".equals(astNode)) return true;
		if ("ExpressionMethodReference".equals(astNode)) return true;
		if ("FieldAccess".equals(astNode)) return true;
		if ("InfixExpression".equals(astNode)) return true;
		if ("InstanceofExpression".equals(astNode)) return true;
		if ("LambdaExpression".equals(astNode)) return true;
		if ("MethodInvocation".equals(astNode)) return true;
		if ("MethodReference".equals(astNode)) return true;
		if ("ParenthesizedExpression".equals(astNode)) return true;
		if ("PostfixExpression".equals(astNode)) return true;
		if ("PrefixExpression".equals(astNode)) return true;
		if ("SuperFieldAccess".equals(astNode)) return true;
		if ("SuperMethodInvocation".equals(astNode)) return true;
		if ("SuperMethodReference".equals(astNode)) return true;
		if ("TypeLiteral".equals(astNode)) return true;
		if ("TypeMethodReference".equals(astNode)) return true;
		if ("VariableDeclarationExpression".equals(astNode)) return true;
		return false;
	}
	
	public static boolean isStatement(int type) {//EmptyStatement
		if (type == 6)  return true; // AssertStatement
		if (type == 10) return true; // BreakStatement
		if (type == 17) return true; // ConstructorInvocation
		if (type == 18) return true; // ContinueStatement
		if (type == 21) return true; // ExpressionStatement
		if (type == 41) return true; // ReturnStatement
		if (type == 46) return true; // SuperConstructorInvocation
		if (type == 49) return true; // SwitchCase
		if (type == 53) return true; // ThrowStatement
		if (type == 56) return true; // TypeDeclarationStatement
		if (type == 60) return true; // VariableDeclarationStatement
		return withBlockStatement(type);
	}
	
	public static boolean isStatement2(int type) {
		if (type == 8)  return true; // block
		if (type == 12) return true; // CatchClause
		return isStatement(type);
	}
	
	public static boolean containsBlockStatement(String statementType) {
		if ("DoStatement".equals(statementType)) return true;
		if ("EnhancedForStatement".equals(statementType)) return true;
		if ("ForStatement".equals(statementType)) return true;
		if ("IfStatement".equals(statementType)) return true;
		if ("LabeledStatement".equals(statementType)) return true;
		if ("SynchronizedStatement".equals(statementType)) return true;
		if ("SwitchStatement".equals(statementType)) return true;
		if ("TryStatement".equals(statementType)) return true;
		if ("WhileStatement".equals(statementType)) return true;
		return false;
	}
	
	public static boolean withBlockStatement(int type) {
		if (type == 19) return true; // DoStatement
		if (type == 24) return true; // ForStatement
		if (type == 25) return true; // IfStatement
		if (type == 30) return true; // LabeledStatement
		if (type == 50) return true; // SwitchStatement
		if (type == 51) return true; // SynchronizedStatement
		if (type == 54) return true; // TryStatement
		if (type == 61) return true; // WhileStatement
		if (type == 70) return true; // EnhancedForStatement
		return false;
	}
}
