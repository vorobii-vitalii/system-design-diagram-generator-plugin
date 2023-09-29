package org.vitalii.vorobii.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.util.ElementScanner14;

import org.vitalii.vorobii.dto.ClassConstructor;
import org.vitalii.vorobii.dto.ClassName;
import org.vitalii.vorobii.dto.Parameter;

public class ConstructorsScanner extends ElementScanner14<List<ClassConstructor>, Void> {

	public ConstructorsScanner() {
		super(new ArrayList<>());
	}

	@Override
	public List<ClassConstructor> visitExecutable(ExecutableElement e, Void unused) {
		if (e.getKind() == ElementKind.CONSTRUCTOR) {
			DEFAULT_VALUE.add(
					new ClassConstructor(e.getParameters().stream()
							.map(v -> new Parameter(v.getSimpleName().toString(), new ClassName(v.asType().toString())))
							.toList()
					));
		}
		return super.visitExecutable(e, unused);
	}

}
