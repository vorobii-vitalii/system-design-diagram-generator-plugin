package org.vitalii.vorobii.processor;

import com.google.auto.service.AutoService;
import com.sun.source.util.Trees;
import guru.nidi.graphviz.attribute.Color;
import guru.nidi.graphviz.attribute.Label;
import guru.nidi.graphviz.attribute.Style;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.MutableGraph;
import guru.nidi.graphviz.model.MutableNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vitalii.vorobii.annotation.Component;
import org.vitalii.vorobii.dto.ClassMetadata;
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
import java.util.HashMap;
import java.util.Set;

import static guru.nidi.graphviz.model.Factory.*;
import static org.vitalii.vorobii.utils.AncestorUtils.getAncestors;

//@SupportedAnnotationTypes("org.vitalii.vorobii.annotation.Component")
@AutoService(Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_19)
public class CreateDiagramProcessor extends AbstractProcessor {
	private static final Logger LOGGER = LoggerFactory.getLogger(CreateDiagramProcessor.class);

	private static final String MODULE_AND_PKG = "";
	private static final Color LINKS_COLOR = Color.RED;
	private static final Color COMPONE_GRAPH_BACKGROUND_COLOR = Color.BLACK;
	private static final Color COMPONENT_GRAPH_FONT_COLOR = Color.WHITE;
	private static final Color COMPONENT_GRAPH_NODES_COLOR = Color.WHITE;
	public static final String SYSTEM_DIAGRAM_GRAPH_NAME = "Diagram";

	private final ClassMetadataExtractor classMetadataExtractor = new ClassMetadataExtractor();
	private final ClassDescriptionSerializer classDescriptionSerializer = new ClassDescriptionSerializer();

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		var trees = Trees.instance(this.processingEnv);
		var annotatedElements = roundEnv.getElementsAnnotatedWith(Component.class);
		if (annotatedElements.isEmpty()) {
			LOGGER.info("No annotated classes found, skipping processor.");
			return true;
		}
		var classes = getAncestors(annotatedElements, TypeElement.class);
		LOGGER.info("Processing {} classes", classes.size());
		var contextByClass = new HashMap<ClassName, String>();
		var classMetadataByClassName = new HashMap<ClassName, ClassMetadata>();
		var nodeByClass = new HashMap<ClassName, MutableNode>();
		var graphByComponent = new HashMap<String, MutableGraph>();

		for (var typeElement : classes) {
			var classMetadata = classMetadataExtractor.getClassMetadata(typeElement);
			var componentName = typeElement.getAnnotation(Component.class).name();
			LOGGER.info("Class {} belongs to component {}", classMetadata.className().fullName(), componentName);
			var contextGraph = graphByComponent.computeIfAbsent(componentName, this::createComponentGraph);
			var classNode = createClassNode(classMetadata);
			contextByClass.put(classMetadata.className(), componentName);
			contextGraph.add(classNode);
			nodeByClass.put(classMetadata.className(), classNode);
			classMetadataByClassName.put(classMetadata.className(), classMetadata);
		}

		for (var typeElement : classes) {
			var fullClassName = new ClassName(typeElement.asType().toString());
			var classMetadata = classMetadataByClassName.get(fullClassName);
			var connectedTypes = new CalledClassesScanner(classMetadata.fields(), trees).scan(typeElement);
			var sourceClassNode = nodeByClass.get(fullClassName);
			for (var connectedType : connectedTypes) {
				if (contextByClass.containsKey(connectedType.className())) {
					LOGGER.info("Adding link between {} and {}", fullClassName, connectedType);
					sourceClassNode.addLink(sourceClassNode.linkTo(nodeByClass.get(connectedType.className()))
							.with(LINKS_COLOR)
							.with(Label.lines(Label.Justification.MIDDLE, connectedType.description())));
				}
			}
		}
		saveDiagram(graphByComponent);
		return true;
	}

	private MutableNode createClassNode(ClassMetadata classMetadata) {
		return mutNode(Label.htmlLines(Label.Justification.MIDDLE, serializeClassDescription(classMetadata)))
				.add(Color.WHITE);
	}

	private String[] serializeClassDescription(ClassMetadata classMetadata) {
		return classDescriptionSerializer.serializeClassDescription(classMetadata).toArray(String[]::new);
	}

	private void saveDiagram(HashMap<String, MutableGraph> graphByComponent) {
		try {
			var diagramFileObject = generateDiagramFileObject();
			try (var stream = diagramFileObject.openOutputStream()) {
				Graphviz.fromGraph(graph(SYSTEM_DIAGRAM_GRAPH_NAME)
								.with(graphByComponent.values().stream().toList()))
						.render(Format.PNG)
						.toOutputStream(stream);
			}
		}
		catch (IOException e) {
			System.err.println("Error on generation of diagram!!!!");
			throw new UncheckedIOException(e);
		}
	}

	private FileObject generateDiagramFileObject() throws IOException {
		return processingEnv.getFiler()
				.createResource(StandardLocation.CLASS_OUTPUT, MODULE_AND_PKG, generateDiagramFileName());
	}

	private static String generateDiagramFileName() {
		return "diagram" + System.currentTimeMillis() + ".png";
	}

	private MutableGraph createComponentGraph(String s) {
		var newContextGraph = mutGraph(s).setCluster(true);
		newContextGraph.nodeAttrs().add(Style.FILLED);
		newContextGraph.nodeAttrs().add(COMPONENT_GRAPH_NODES_COLOR);
		newContextGraph.graphAttrs().add(Style.FILLED);
		newContextGraph.graphAttrs().add(COMPONE_GRAPH_BACKGROUND_COLOR.background());
		newContextGraph.graphAttrs().add(COMPONENT_GRAPH_FONT_COLOR.font());
		newContextGraph.graphAttrs().add(Label.of(s));
		return newContextGraph;
	}

}

