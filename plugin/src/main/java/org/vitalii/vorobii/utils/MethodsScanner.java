package org.vitalii.vorobii.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.ElementScanner14;

import org.vitalii.vorobii.dto.ClassMethod;
import org.vitalii.vorobii.dto.ClassName;
import org.vitalii.vorobii.dto.Parameter;

public class MethodsScanner extends ElementScanner14<List<ClassMethod>, Void> {

	public MethodsScanner() {
		super(new ArrayList<>());
	}

	@Override
	public List<ClassMethod> visitExecutable(ExecutableElement e, Void unused) {
		if (e.getKind() == ElementKind.METHOD) {
			DEFAULT_VALUE.add(new ClassMethod(
					e.getSimpleName().toString(),
					e.getParameters().stream()
							.map(v -> new Parameter(v.getSimpleName().toString(), new ClassName(v.asType().toString())))
							.toList(),
					new ClassName(e.getReturnType().toString())
			));
		}
		return super.visitExecutable(e, unused);
	}

}
