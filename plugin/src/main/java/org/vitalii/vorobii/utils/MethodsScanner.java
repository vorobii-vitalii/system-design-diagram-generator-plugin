package org.vitalii.vorobii.utils;

import com.sun.source.tree.*;
import com.sun.source.util.Trees;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vitalii.vorobii.dto.*;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.ElementScanner14;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.vitalii.vorobii.utils.AncestorUtils.getAncestors;

public class MethodsScanner extends ElementScanner14<List<ClassMethod>, Void> {
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodsScanner.class);

	private final Trees trees;
	private final Map<String, ClassName> typeByFieldName;

	public MethodsScanner(Trees trees, Map<String, ClassName> typeByFieldName) {
		super(new ArrayList<>());
		this.trees = trees;
		this.typeByFieldName = typeByFieldName;
	}

	@Override
	public List<ClassMethod> visitExecutable(ExecutableElement e, Void unused) {
		if (e.getKind() == ElementKind.METHOD) {
			var returnType = new ClassName(e.getReturnType().toString());
			var methodParameters = e.getParameters().stream()
					.map(v -> new Parameter(
							v.getSimpleName().toString(),
							new ClassName(v.asType().toString())
					))
					.toList();
			var methodName = e.getSimpleName().toString();
			List<CallStatement> callStatements = getCallStatements(e);

			DEFAULT_VALUE.add(new ClassMethod(methodName, methodParameters, returnType, callStatements, e));
		}
		return super.visitExecutable(e, unused);
	}

	private List<CallStatement> getCallStatements(ExecutableElement executableElement) {
		var tree = trees.getTree(executableElement);
		var expressionStatements =
				getAncestors(tree.getBody().getStatements(), ExpressionStatementTree.class);
		var methodInvocations =
				getAncestors(
						expressionStatements.stream()
								.map(ExpressionStatementTree::getExpression)
								.toList(),
						MethodInvocationTree.class
				);
		LOGGER.info("Getting call statements from method {}", executableElement);
		LOGGER.info("All statements {}", tree.getBody().getStatements());
		LOGGER.info("All statements types {}", tree.getBody().getStatements().stream().map(Tree::getKind).collect(Collectors.toList()));
		LOGGER.info("Expression statements = {}", expressionStatements);
		LOGGER.info("Expression statements types = {}", expressionStatements.stream().map(ExpressionStatementTree::getClass).collect(Collectors.toList()));
		LOGGER.info("Method invocations = {}", methodInvocations);
		var methodSelectors =
				methodInvocations.stream().map(MethodInvocationTree::getMethodSelect).toList();
		var methodSelectTrees = getAncestors(methodSelectors, MemberSelectTree.class);
		List<CallStatement> callStatements = new ArrayList<>();
		methodSelectTrees.forEach(memberSelectTree -> {
			var expression = memberSelectTree.getExpression();
			var methodName = memberSelectTree.getIdentifier().toString();
			LOGGER.info("METHOD NAME = {}", methodName);
			if (expression instanceof IdentifierTree identifierTree) {
				LOGGER.info("identifier tree");
				var type = typeByFieldName.get(identifierTree.getName().toString());
				if (type != null) {
					callStatements.add(new CallStatement(type, methodName));
				}
			}
			// Field access
			else if (expression instanceof MemberSelectTree selectTree) {
				LOGGER.info("member select tree");
				var type = typeByFieldName.get(selectTree.getIdentifier().toString());
				if (type != null) {
					callStatements.add(new CallStatement(type, methodName));
				}
			}
			else {
				LOGGER.warn("Undefined expression type {}", expression);
			}
		});
		return callStatements;
	}

}
