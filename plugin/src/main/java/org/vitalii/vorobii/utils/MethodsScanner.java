package org.vitalii.vorobii.utils;

import com.sun.source.util.Trees;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vitalii.vorobii.dto.CallStatement;
import org.vitalii.vorobii.dto.ClassMethod;
import org.vitalii.vorobii.dto.ClassName;
import org.vitalii.vorobii.dto.Parameter;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.util.ElementScanner14;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MethodsScanner extends ElementScanner14<List<ClassMethod>, Void> {
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodsScanner.class);

	private final Trees trees;
	private final Map<String, ClassName> typeByFieldName;
	private final ClassName currentClass;

	public MethodsScanner(Trees trees, Map<String, ClassName> typeByFieldName, ClassName currentClass) {
		super(new ArrayList<>());
		this.trees = trees;
		this.typeByFieldName = typeByFieldName;
		this.currentClass = currentClass;
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

		List<CallStatement> allCalls = tree.getBody().getStatements().stream()
				.flatMap(e -> e.accept(new AllCallStatementsScanner(trees, currentClass, typeByFieldName), null).stream())
				.filter(e -> e.className() != null && e.className().fullName() != null)
				.toList();

		LOGGER.info(executableElement.getSimpleName() + " calls = " + allCalls);

		return allCalls;
	}

}
