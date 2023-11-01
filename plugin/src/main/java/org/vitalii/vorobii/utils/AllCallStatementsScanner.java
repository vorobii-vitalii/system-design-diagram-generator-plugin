package org.vitalii.vorobii.utils;

import com.sun.source.tree.*;
import com.sun.source.util.SimpleTreeVisitor;
import com.sun.source.util.Trees;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vitalii.vorobii.dto.CallStatement;
import org.vitalii.vorobii.dto.ClassCalled;
import org.vitalii.vorobii.dto.ClassName;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AllCallStatementsScanner extends SimpleTreeVisitor<List<CallStatement>, Void> {
    private static final Logger LOGGER = LoggerFactory.getLogger(AllCallStatementsScanner.class);
    private final Map<String, ClassName> typeByFieldName;
    private final Trees trees;

    public AllCallStatementsScanner(Map<String, ClassName> typeByFieldName, Trees trees) {
        super(new ArrayList<>());
        this.typeByFieldName = typeByFieldName;
        this.trees = trees;
    }

    @Override
    public List<CallStatement> visitReturn(ReturnTree node, Void unused) {
        node.getExpression().accept(this, null);
        return super.visitReturn(node, unused);
    }

    @Override
    public List<CallStatement> visitMethod(MethodTree node, Void unused) {
        node.getBody().getStatements().forEach(statementTree -> {
            statementTree.accept(this, null);
        });
        return super.visitMethod(node, unused);
    }

    @Override
    public List<CallStatement> visitMethodInvocation(MethodInvocationTree node, Void unused) {
        var methodSelect = node.getMethodSelect();
        if (methodSelect instanceof IdentifierTree identifierTree) {
            var type = typeByFieldName.get(identifierTree.getName().toString());
            if (type != null) {
//                DEFAULT_VALUE.add(new ClassCalled(
//                        type,
//                        "METHOD_CALL",
//                        identifierTree.getName().toString() + "#" + memberSelectTree.getIdentifier()
//                ));
            }
        }
        // Field access
        else if (methodSelect instanceof MemberSelectTree selectTree) {
            var fieldName = selectTree.getIdentifier().toString();
            var type = typeByFieldName.get(fieldName);
            if (type != null) {
                LOGGER.info("Selected tree expression = {}", selectTree.getExpression());
                String s = fieldName + "#" + selectTree.getIdentifier();
                DEFAULT_VALUE.add(new CallStatement(type, selectTree.getIdentifier().toString()));
            }
        }
        return super.visitMethodInvocation(node, unused);
    }
}
