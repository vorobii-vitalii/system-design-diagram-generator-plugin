package org.vitalii.vorobii.dto;

import java.util.List;
import java.util.Map;

public record ClassMetadata(
        ClassName className,
        // TODO: Take into consideration overloading
        Map<String, ClassMethod> methods,
        List<ClassConstructor> constructors,
        Map<String, ClassName> fields
) {
}
