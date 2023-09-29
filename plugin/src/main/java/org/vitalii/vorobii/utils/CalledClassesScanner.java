package org.vitalii.vorobii.utils;

import static org.vitalii.vorobii.utils.AncestorUtils.getAncestors;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.util.ElementScanner14;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vitalii.vorobii.dto.ClassCalled;
import org.vitalii.vorobii.dto.ClassName;

import com.sun.source.tree.ExpressionStatementTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.util.Trees;

public class CalledClassesScanner extends ElementScanner14<Set<ClassCalled>, Void> {
	private static final Logger LOGGER = LoggerFactory.getLogger(CalledClassesScanner.class);

	private final Map<String, ClassName> typeByFieldName;
	private final Trees trees;

	public CalledClassesScanner(Map<String, ClassName> typeByFieldName, Trees trees) {
		super(new HashSet<>());
		this.typeByFieldName = typeByFieldName;
		this.trees = trees;
	}

	@Override
	public Set<ClassCalled> visitExecutable(ExecutableElement e, Void unused) {
		var tree = trees.getTree(e);
		if (tree != null) {
			var expressionStatements = getAncestors(tree.getBody().getStatements(), ExpressionStatementTree.class);
			var methodInvocations = getAncestors(expressionStatements.stream()
					.map(ExpressionStatementTree::getExpression)
					.toList(), MethodInvocationTree.class);
			var methodSelectors = methodInvocations.stream().map(MethodInvocationTree::getMethodSelect).toList();
			var methodSelectTrees = getAncestors(methodSelectors, MemberSelectTree.class);
			methodSelectTrees.forEach(memberSelectTree -> {
				var expression = memberSelectTree.getExpression();
				LOGGER.info("Expression type = {}", expression.getClass());
				if (expression instanceof IdentifierTree identifierTree) {
					var type = typeByFieldName.get(identifierTree.getName().toString());
					if (type != null) {
						DEFAULT_VALUE.add(new ClassCalled(
								type,
								"METHOD_CALL",
								identifierTree.getName().toString() + "#" + memberSelectTree.getIdentifier()));
					}
				} else if (expression instanceof MemberSelectTree selectTree) {
					var type = typeByFieldName.get(selectTree.getIdentifier().toString());
					if (type != null) {
						LOGGER.info("Selected tree expression = {}", selectTree.getExpression());
						DEFAULT_VALUE.add(new ClassCalled(
								type,
								"METHOD_CALL",
								selectTree.getIdentifier().toString() + "#" + memberSelectTree.getIdentifier()));
					}
				}

			});
		}

		return super.visitExecutable(e, unused);
	}

}
