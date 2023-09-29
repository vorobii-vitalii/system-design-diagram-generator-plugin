package org.vitalii.vorobii.service;

import javax.lang.model.element.TypeElement;

import org.vitalii.vorobii.dto.ClassMetadata;
import org.vitalii.vorobii.dto.ClassName;
import org.vitalii.vorobii.utils.ConstructorsScanner;
import org.vitalii.vorobii.utils.FieldTypeScanner;
import org.vitalii.vorobii.utils.MethodsScanner;

public class ClassMetadataExtractor {

	public ClassMetadata getClassMetadata(TypeElement typeElement) {
		var fieldTypeByFieldName = new FieldTypeScanner().scan(typeElement);
		var methods = new MethodsScanner().scan(typeElement);
		var constructors = new ConstructorsScanner().scan(typeElement);
		return new ClassMetadata(new ClassName(typeElement.asType().toString()), methods, constructors, fieldTypeByFieldName);
	}

}
