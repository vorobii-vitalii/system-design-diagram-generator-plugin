package org.vitalii.vorobii.utils;

import com.sun.source.tree.*;
import com.sun.source.util.SimpleTreeVisitor;
import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vitalii.vorobii.dto.CallStatement;
import org.vitalii.vorobii.dto.ClassName;

import javax.lang.model.element.ExecutableElement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AllCallStatementsScanner extends SimpleTreeVisitor<List<CallStatement>, Void> {
    private static final Logger LOGGER = LoggerFactory.getLogger(AllCallStatementsScanner.class);
    private final Trees trees;
    private final ClassName rootClass;
    private final Map<String, ClassName> fields;

    public AllCallStatementsScanner(Trees trees, ClassName rootClass, Map<String, ClassName> fields) {
        super(new ArrayList<>());
        this.trees = trees;
        this.rootClass = rootClass;
        this.fields = fields;
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
    public List<CallStatement> visitAssignment(AssignmentTree node, Void unused) {
        node.getExpression().accept(this, null);
        return super.visitAssignment(node, unused);
    }

    @Override
    public List<CallStatement> visitExpressionStatement(ExpressionStatementTree node, Void unused) {
        node.getExpression().accept(this, null);
        return super.visitExpressionStatement(node, unused);
    }

    @Override
    public List<CallStatement> visitForLoop(ForLoopTree node, Void unused) {
        node.getInitializer().forEach(e -> e.accept(this, null));
        node.getCondition().accept(this, null);
        node.getStatement().accept(this, null);
        node.getUpdate().forEach(e -> e.accept(this, null));
        return super.visitForLoop(node, unused);
    }

    @Override
    public List<CallStatement> visitBlock(BlockTree node, Void unused) {
        for (StatementTree statement : node.getStatements()) {
            statement.accept(this, null);
        }
        return super.visitBlock(node, unused);
    }

    @Override
    public List<CallStatement> visitEnhancedForLoop(EnhancedForLoopTree node, Void unused) {
        node.getExpression().accept(this, null);
        node.getStatement().accept(this, null);
        return super.visitEnhancedForLoop(node, unused);
    }

    @Override
    public List<CallStatement> visitWhileLoop(WhileLoopTree node, Void unused) {
        node.getStatement().accept(this, null);
        return super.visitWhileLoop(node, unused);
    }

    @Override
    public List<CallStatement> visitConditionalExpression(ConditionalExpressionTree node, Void unused) {
        node.getCondition().accept(this, null);
        node.getTrueExpression().accept(this, null);
        node.getFalseExpression().accept(this, null);
        return super.visitConditionalExpression(node, unused);
    }

    @Override
    public List<CallStatement> visitMethodInvocation(MethodInvocationTree node, Void unused) {
        node.getArguments()
                .forEach(e -> e.accept(this, null));
        DEFAULT_VALUE.add(new CallStatement(
                getClassName(node),
                getMethodName(node)
        ));
        return super.visitMethodInvocation(node, unused);
    }

    private ClassName getClassName(MethodInvocationTree tree) {
        ExpressionTree methodSelect = tree.getMethodSelect();
        if (methodSelect.getKind() == Tree.Kind.MEMBER_SELECT) {
            MemberSelectTree mtree = (MemberSelectTree) methodSelect;
            return fields.get(mtree.getExpression().toString());
        } else {
            LOGGER.info("DETECTED SELF CALL");
            return rootClass;
        }
    }

    private String getMethodName(MethodInvocationTree tree) {
        ExpressionTree methodSelect = tree.getMethodSelect();
        if (methodSelect.getKind() == Tree.Kind.MEMBER_SELECT) {
            MemberSelectTree mtree = (MemberSelectTree) methodSelect;
            return mtree.getIdentifier().toString();
        } else {
            IdentifierTree itree = (IdentifierTree) methodSelect;
            return itree.getName().toString();
        }
    }

}
