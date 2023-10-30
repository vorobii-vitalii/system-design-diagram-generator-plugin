package org.vitalii.vorobii.processor;

import static org.vitalii.vorobii.utils.AncestorUtils.getAncestors;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vitalii.vorobii.annotation.Component;
import org.vitalii.vorobii.dto.ClassMetadata;
import org.vitalii.vorobii.dto.ClassMethod;
import org.vitalii.vorobii.dto.ClassName;
import org.vitalii.vorobii.service.ClassDescriptionSerializer;
import org.vitalii.vorobii.service.ClassMetadataExtractor;
import org.vitalii.vorobii.utils.CalledClassesScanner;

import com.google.auto.service.AutoService;
import com.sun.source.util.Trees;

import guru.nidi.graphviz.model.MutableGraph;

@SupportedAnnotationTypes("org.vitalii.vorobii.annotation.Component")
@AutoService(Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_19)
public class GeneratePlantUMLDiagramProcessor extends AbstractProcessor {
	private static final String SMART_UML = "@startuml\n";
	private static final String END_UML = "\n@enduml";
	private static final Logger LOGGER = LoggerFactory.getLogger(GeneratePlantUMLDiagramProcessor.class);

	private static final String MODULE_AND_PKG = "";

	private final ClassMetadataExtractor classMetadataExtractor = new ClassMetadataExtractor();
	private final ClassDescriptionSerializer classDescriptionSerializer = new ClassDescriptionSerializer();

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		System.out.println("Annotation processor found!");
		var trees = Trees.instance(this.processingEnv);
		var annotatedElements = roundEnv.getElementsAnnotatedWith(Component.class);

		var classes = getAncestors(annotatedElements, TypeElement.class);

		Map<ClassName, String> contextByClass = new HashMap<>();
		Map<ClassName, ClassMetadata> classMetadataByClassName = new HashMap<>();

		Map<String, MutableGraph> graphByComponent = new HashMap<>();

		StringBuilder builder = new StringBuilder(SMART_UML);

		var elementsByComponentName =
				classes.stream().collect(Collectors.groupingBy(e -> e.getAnnotation(Component.class).name()));

		for (var entry : elementsByComponentName.entrySet()) {
			var componentName = entry.getKey();
			builder.append("\n package \"")
					.append(componentName)
					.append("\" {\n");
			// Serialize the classes inside
			for (var typeElement : entry.getValue()) {
				var classMetadata = classMetadataExtractor.getClassMetadata(typeElement);
				var elementDescription = typeElement.getAnnotation(Component.class).elementDescription();
				var className = classMetadata.className().fullName();
				builder.append("class ").append(className).append(" {\n");
				for (var constructor : classMetadata.constructors()) {
					builder.append(className).append(' ')
							.append(classDescriptionSerializer.serializeParameters(constructor.parameters()))
							.append('\n');
				}
				for (var field : classMetadata.fields().entrySet()) {
					builder.append(field.getKey())
							.append(": ")
							.append(field.getValue().fullName())
							.append("\n");
				}
				for (ClassMethod method : classMetadata.methods()) {
					builder.append(method.returnType().fullName())
							.append(' ')
							.append(method.name())
							.append(classDescriptionSerializer.serializeParameters(method.methodParameters()))
							.append('\n');
				}
				builder.append("}\n");
				if (!elementDescription.isEmpty()) {
					builder.append("note top of  ").append(className).append(':').append(elementDescription).append('\n');
				}
				contextByClass.put(classMetadata.className(), componentName);
				classMetadataByClassName.put(classMetadata.className(), classMetadata);
			}
			builder.append('}');
		}

		for (var typeElement : classes) {
			var fullClassName = new ClassName(typeElement.asType().toString());
			var classMetadata = classMetadataByClassName.get(fullClassName);
			var connectedTypes = new CalledClassesScanner(classMetadata.fields(), trees).scan(typeElement);
			for (var connectedType : connectedTypes) {
				if (contextByClass.containsKey(connectedType.className())) {
					LOGGER.info("Adding link between {} and {}", fullClassName, connectedType);
					builder.append('\n')
							.append(classMetadata.className().fullName())
							.append(" --> ")
							.append(connectedType.className().fullName())
							.append(" : ")
							.append(connectedType.description());
				}
			}
		}
		builder.append(END_UML);
		try {
			var diagramFileObject = generateDiagramFile();
			try (var stream = diagramFileObject.openOutputStream()) {
				stream.write(builder.toString().getBytes(StandardCharsets.UTF_8));
			}
		}
		catch (IOException e) {
			System.err.println("Error on generation of diagram!!!!");
			throw new UncheckedIOException(e);
		}
		return true;
	}

	private FileObject generateDiagramFile() throws IOException {
		return processingEnv.getFiler()
				.createResource(StandardLocation.CLASS_OUTPUT, MODULE_AND_PKG, generateDiagramFileName());
	}

	private static String generateDiagramFileName() {
		return "diagram_uml" + System.currentTimeMillis() + ".puml";
	}

}
