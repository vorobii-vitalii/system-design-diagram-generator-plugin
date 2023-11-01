package org.vitalii.vorobii.dto;

import javax.lang.model.element.ExecutableElement;
import java.util.List;

public record ClassMethod(
        String name,
        List<Parameter> methodParameters,
        ClassName returnType,
        List<CallStatement> callStatements,
        ExecutableElement element
) {
}
