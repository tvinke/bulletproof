package tvinke.bulletproof.grails.validation

import groovy.transform.CompileStatic
import org.codehaus.groovy.ast.*
import org.codehaus.groovy.ast.builder.AstBuilder
import org.codehaus.groovy.ast.stmt.BlockStatement
import org.codehaus.groovy.ast.stmt.ExpressionStatement
import org.codehaus.groovy.ast.stmt.Statement
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.transform.AbstractASTTransformation
import org.codehaus.groovy.transform.GroovyASTTransformation
import org.codehaus.groovy.transform.TupleConstructorASTTransformation

/**
 * 
 * Handles generation of code for the {@code Valid} annotation.
 * 
 * @author Ted Vinke
 *
 */
@CompileStatic
@GroovyASTTransformation(phase = CompilePhase.INSTRUCTION_SELECTION)
class ValidASTTransformation extends AbstractASTTransformation {
    
    @Override
    void visit(ASTNode[] nodes, SourceUnit source) {
        init(nodes, source)
        AnnotatedNode parent = (AnnotatedNode) nodes[1]
        AnnotationNode annotation = (AnnotationNode) nodes[0]
        if (parent instanceof ClassNode) {
            modifyClass(parent as ClassNode)
        } else {
            addError("@Valid may only be applied on the class level", annotation)
        }
    }

    void modifyClass(ClassNode classNode) {

        List<ConstructorNode> constructors = classNode.getDeclaredConstructors()

        if (constructors.isEmpty()) {
            addMapConstructors(classNode)
        }

        haveConstructorsCallValidate(classNode, constructors, createValidateCallingStatement())
    }
    
    private void haveConstructorsCallValidate(ClassNode classNode, List<ConstructorNode> constructors, Statement validateCallingStatement) {
        for (ConstructorNode constructor : constructors) {
            def code = constructor.getCode()
            if (code instanceof BlockStatement) {
                List<Statement> existingStatements = ((BlockStatement)constructor.code).getStatements()
                existingStatements.add validateCallingStatement
            } else if (code instanceof ExpressionStatement) {
                // no-op
            } else {
                addError("@Valid fails to determine the correct constructor", classNode)
            }
        }
    }

    private static Statement createValidateCallingStatement() {
        new AstBuilder().buildFromString("""
            if (!validate()) {
                throw new grails.validation.ValidationException(getErrors().errorCount + " errors", getErrors())
            }
        """) [0] as Statement
    }

    private static void addMapConstructors(ClassNode classNode) {
        TupleConstructorASTTransformation.addMapConstructors(classNode, false,
                "The class " + classNode.name + " was incorrectly initialized via the map constructor with null.")
    }

}
