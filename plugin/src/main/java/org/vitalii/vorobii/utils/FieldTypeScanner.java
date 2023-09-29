package org.vitalii.vorobii.utils;

import java.util.HashMap;
import java.util.Map;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.ElementScanner14;

import org.vitalii.vorobii.dto.ClassName;

public class FieldTypeScanner extends ElementScanner14<Map<String, ClassName>, Void> {

	public FieldTypeScanner() {
		super(new HashMap<>());
	}

	@Override
	public Map<String, ClassName> visitVariable(VariableElement e, Void fieldTypeByFieldName) {
		if (e.getKind() == ElementKind.FIELD) {
			DEFAULT_VALUE.put(e.getSimpleName().toString(), new ClassName(e.asType().toString()));
		}
		return super.visitVariable(e, fieldTypeByFieldName);
	}

}
