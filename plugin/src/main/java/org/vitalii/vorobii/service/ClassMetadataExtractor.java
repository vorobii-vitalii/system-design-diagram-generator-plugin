package org.vitalii.vorobii.service;

import javax.lang.model.element.TypeElement;

import com.sun.source.util.Trees;
import org.vitalii.vorobii.dto.ClassMetadata;
import org.vitalii.vorobii.dto.ClassMethod;
import org.vitalii.vorobii.dto.ClassName;
import org.vitalii.vorobii.utils.ConstructorsScanner;
import org.vitalii.vorobii.utils.FieldTypeScanner;
import org.vitalii.vorobii.utils.MethodsScanner;

import java.util.stream.Collectors;

public class ClassMetadataExtractor {

	public ClassMetadata getClassMetadata(TypeElement typeElement, Trees trees) {
		var fieldTypeByFieldName = new FieldTypeScanner().scan(typeElement);
		var methods = new MethodsScanner(trees, fieldTypeByFieldName).scan(typeElement);
		var constructors = new ConstructorsScanner().scan(typeElement);
		return new ClassMetadata(
				new ClassName(typeElement.asType().toString()),
				methods.stream().collect(Collectors.toMap(ClassMethod::name, e -> e)),
				constructors,
				fieldTypeByFieldName
		);
	}

}
