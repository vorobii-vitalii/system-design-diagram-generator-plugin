package org.vitalii.vorobii.processor;

import com.google.auto.service.AutoService;
import com.sun.source.util.Trees;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vitalii.vorobii.annotation.Component;
import org.vitalii.vorobii.annotation.ComponentAction;
import org.vitalii.vorobii.annotation.GenerateSequence;
import org.vitalii.vorobii.dto.CallStatement;
import org.vitalii.vorobii.dto.ClassMetadata;
import org.vitalii.vorobii.dto.ClassMethod;
import org.vitalii.vorobii.dto.ClassName;
import org.vitalii.vorobii.service.ClassDescriptionSerializer;
import org.vitalii.vorobii.service.ClassMetadataExtractor;
import org.vitalii.vorobii.utils.CalledClassesScanner;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import static org.vitalii.vorobii.utils.AncestorUtils.getAncestors;

@SupportedAnnotationTypes("org.vitalii.vorobii.annotation.Component")
@AutoService(Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_19)
public class GeneratePlantUMLDiagramProcessor extends AbstractProcessor {
	public static final String BACKWARD = "-->";
	private static final String SMART_UML = "@startuml\n";
	private static final String END_UML = "\n@enduml";
	private static final Logger LOGGER = LoggerFactory.getLogger(GeneratePlantUMLDiagramProcessor.class);

	private static final String MODULE_AND_PKG = "";
	public static final String FORWARD = " -> ";
	public static final String COMMENT_DELIMITER = " : ";

	private final ClassMetadataExtractor classMetadataExtractor = new ClassMetadataExtractor();
	private final ClassDescriptionSerializer classDescriptionSerializer = new ClassDescriptionSerializer();

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		LOGGER.info("Annotation processor to generate diagrams started!");
		var annotatedElements = roundEnv.getElementsAnnotatedWith(Component.class);
		if (annotatedElements.isEmpty()) {
			return false;
		}
		var classes = getAncestors(annotatedElements, TypeElement.class);
		var classMetadataByClassName = new HashMap<ClassName, ClassMetadata>();
		generateComponentDiagram(classes, classMetadataByClassName);

		for (var classMetadata : classMetadataByClassName.values()) {
			for (var method : classMetadata.methods().values()) {
				var generateSequence = method.element().getAnnotation(GenerateSequence.class);
				if (generateSequence != null) {
					var sequenceName = generateSequence.sequenceName();
					var builder = new StringBuilder(SMART_UML);
					if (!generateSequence.sequenceDescription().isEmpty()) {
						builder.append("mainframe ").append(generateSequence.sequenceDescription()).append('\n');
					}
					Set<String> visitedMethods = new HashSet<>();
					solve(visitedMethods, classMetadataByClassName, builder, classMetadata.className(), method);

					// generate
					builder.append(END_UML);
					try {
						var diagramFileObject = generateSequenceDiagramFile(sequenceName);
						try (var stream = diagramFileObject.openOutputStream()) {
							stream.write(builder.toString().getBytes(StandardCharsets.UTF_8));
						}
					}
					catch (IOException e) {
						System.err.println("Error on generation of diagram!!!!");
						throw new UncheckedIOException(e);
					}
				}
			}
		}
		LOGGER.info("Annotation processor to generate diagrams finished!");
		return true;
	}

	private void solve(
			Set<String> visitedMethods,
			Map<ClassName, ClassMetadata> classMetadataByClassName,
			StringBuilder builder,
			ClassName className,
			ClassMethod method
	) {
		String methodId = className.fullName() + "#" + method.name();
		if (visitedMethods.contains(methodId)) {
			return;
		}
		visitedMethods.add(methodId);
		var refClassLabel = className.fullName();
		for (var callStatement : method.callStatements()) {
			var calledClass = callStatement.className();
			var calledClassMetadata = classMetadataByClassName.get(calledClass);
			if (calledClassMetadata == null) {
				builder.append(refClassLabel)
						.append(FORWARD)
						.append(calledClass.fullName())
						.append(COMMENT_DELIMITER)
						.append(callStatement.methodName())
						.append('\n');
			}
			else {
				var calledMethod = calledClassMetadata.methods().get(callStatement.methodName());
				var componentAction = calledMethod.element().getAnnotation(ComponentAction.class);
				builder.append(refClassLabel)
						.append(FORWARD)
						.append(calledClass.fullName())
						.append(COMMENT_DELIMITER)
						.append(componentAction != null ? componentAction.requestDescription() : callStatement.methodName())
						.append('\n');
				solve(visitedMethods, classMetadataByClassName, builder, calledClass, calledMethod);
				if (!calledClass.equals(className)) {
					var response =
							componentAction != null && !componentAction.responseDescription().isEmpty()
									? componentAction.responseDescription()
									: calledMethod.returnType().fullName();
					builder.append(calledClass.fullName())
							.append(BACKWARD)
							.append(refClassLabel)
							.append(COMMENT_DELIMITER)
							.append(response)
							.append('\n');
				}
			}
		}
		visitedMethods.remove(methodId);
	}

	private void generateComponentDiagram(
			List<TypeElement> classes,
			Map<ClassName, ClassMetadata> classMetadataByClassName
	) {
		var contextByClass = new HashMap<ClassName, String>();
		var builder = new StringBuilder(SMART_UML);
		addClassesToDiagram(classes, contextByClass, classMetadataByClassName, builder);
		addRelationsToDiagram(classes, contextByClass, classMetadataByClassName, builder);
		builder.append(END_UML);
		try {
			var diagramFileObject = generateComponentDiagramFile();
			try (var stream = diagramFileObject.openOutputStream()) {
				stream.write(builder.toString().getBytes(StandardCharsets.UTF_8));
			}
		}
		catch (IOException e) {
			System.err.println("Error on generation of diagram!!!!");
			throw new UncheckedIOException(e);
		}
	}

	private void addRelationsToDiagram(
			List<TypeElement> classes,
			Map<ClassName, String> contextByClass,
			Map<ClassName, ClassMetadata> classMetadataByClassName,
			StringBuilder builder
	) {
		var trees = Trees.instance(this.processingEnv);
		for (var typeElement : classes) {
			var fullClassName = new ClassName(typeElement.asType().toString());
			var classMetadata = classMetadataByClassName.get(fullClassName);
			var connectedTypes =
					new CalledClassesScanner(classMetadata.fields(), trees).scan(typeElement);
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
	}

	private void addClassesToDiagram(
			List<TypeElement> classes,
			Map<ClassName, String> contextByClass,
			Map<ClassName, ClassMetadata> classMetadataByClassName,
			StringBuilder builder
	) {
		var trees = Trees.instance(this.processingEnv);
		for (var entry : classes.stream()
				.collect(Collectors.groupingBy(e -> e.getAnnotation(Component.class).name()))
				.entrySet()
		) {
			var componentName = entry.getKey();
			builder.append("\n package \"")
					.append(componentName)
					.append("\" {\n");
			// Serialize the classes inside
			for (var typeElement : entry.getValue()) {
				var classMetadata = classMetadataExtractor.getClassMetadata(typeElement, trees);
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
				for (ClassMethod method : classMetadata.methods().values()) {
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
	}

	private FileObject generateSequenceDiagramFile(String sequenceName) throws IOException {
		return processingEnv.getFiler()
				.createResource(StandardLocation.SOURCE_OUTPUT, MODULE_AND_PKG, sequenceName + ".puml");
	}

	private FileObject generateComponentDiagramFile() throws IOException {
		return processingEnv.getFiler()
				.createResource(StandardLocation.SOURCE_OUTPUT, MODULE_AND_PKG, generateDiagramFileName());
	}

	private static String generateDiagramFileName() {
		return "diagram_uml" + System.currentTimeMillis() + ".puml";
	}

}
