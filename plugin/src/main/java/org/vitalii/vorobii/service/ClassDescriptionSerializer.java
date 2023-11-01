package org.vitalii.vorobii.service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.vitalii.vorobii.dto.ClassMetadata;
import org.vitalii.vorobii.dto.Parameter;

public class ClassDescriptionSerializer {

	public String serializeParameters(List<Parameter> parameters) {
		return parameters.stream()
				.map(e -> e.parameterType().fullName() + " " + e.parameterName())
				.collect(Collectors.joining(", ", "(", ")"));
	}

	public List<String> serializeClassDescription(ClassMetadata classMetadata) {
		var className = classMetadata.className().fullName();
		return combine(
				Stream.of("<b>" + className + "</b>"),
				Stream.of("<b>Fields</b>"),
				classMetadata.fields().entrySet()
						.stream()
						.map(e -> "<i>" + e.getValue().fullName() + " " + e.getKey() + "</i>"),
				Stream.of("<b>Methods</b>"),
				classMetadata.methods()
						.values()
						.stream()
						.map(method -> method.returnType().fullName() + " " + serializeParameters(method.methodParameters())),
				Stream.of("<b>Constructors</b>"),
				classMetadata.constructors()
						.stream()
						.map(e -> className + serializeParameters(e.parameters()))
		).toList();
	}

	@SafeVarargs
	private <T> Stream<T> combine(Stream<T>... streams) {
		return Arrays.stream(streams).reduce(Stream.empty(), Stream::concat);
	}

}
