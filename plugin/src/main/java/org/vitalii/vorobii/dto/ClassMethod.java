package org.vitalii.vorobii.dto;

import java.util.List;

public record ClassMethod(String name, List<Parameter> methodParameters, ClassName returnType) {
}
