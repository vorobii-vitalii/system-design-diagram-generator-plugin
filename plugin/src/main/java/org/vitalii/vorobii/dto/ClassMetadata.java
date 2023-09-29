package org.vitalii.vorobii.dto;

import java.util.List;
import java.util.Map;

public record ClassMetadata(ClassName className, List<ClassMethod> methods, List<ClassConstructor> constructors, Map<String, ClassName> fields) {
}
