package tvinke.bulletproof.transform
import static org.codehaus.groovy.ast.ClassHelper.make
import static org.codehaus.groovy.ast.ClassHelper.makeWithoutCaching
import static org.codehaus.groovy.ast.tools.GeneralUtils.getInstanceProperties

import org.codehaus.groovy.GroovyBugError
import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.ast.AnnotatedNode
import org.codehaus.groovy.ast.AnnotationNode
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.ConstructorNode
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.PropertyNode
import org.codehaus.groovy.ast.builder.AstBuilder
import org.codehaus.groovy.ast.expr.ArgumentListExpression
import org.codehaus.groovy.ast.expr.ConstantExpression
import org.codehaus.groovy.ast.expr.MethodCallExpression
import org.codehaus.groovy.ast.expr.VariableExpression
import org.codehaus.groovy.ast.stmt.BlockStatement
import org.codehaus.groovy.ast.stmt.ExpressionStatement
import org.codehaus.groovy.ast.stmt.Statement
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.transform.AbstractASTTransformation
import org.codehaus.groovy.transform.GroovyASTTransformation
import org.codehaus.groovy.transform.ImmutableASTTransformation

import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
/**
 * 
 * Handles generation of code for the {@code NonNull} annotation.
 * 
 * @author Ted Vinke
 *
 */
@CompileStatic
@GroovyASTTransformation(phase = CompilePhase.INSTRUCTION_SELECTION)
public class NonNullASTTransformation extends AbstractASTTransformation {
    
    static final Class MY_CLASS = NonNullASTTransformation.class
    static final ClassNode MY_TYPE = make(MY_CLASS)
    private static final ClassNode SELF_TYPE = make(ImmutableASTTransformation.class)
    private static final ClassNode HASHMAP_TYPE = makeWithoutCaching(HashMap.class, false)
    private static final ClassNode MAP_TYPE = makeWithoutCaching(Map.class, false)
    private static final String UBER_CHECKER_METHOD_NAME = 'checkAllProperties'
    private static final String NON_NULL_PROPERTY_NODE_METADATA_KEY = 'NON_NULL_PROPERTY_NODE_METADATA_KEY'
    
    @Override
    public void visit(ASTNode[] nodes, SourceUnit source) {
        init(nodes, source);
        AnnotatedNode parent = (AnnotatedNode) nodes[1];
        AnnotationNode annotation = (AnnotationNode) nodes[0];
        if (parent instanceof ClassNode) {
            modifyClass(parent);
        } else {
            addError("@NonNull may only be applied on the class level", annotation);
        }
    }
    
    protected void init(ASTNode[] nodes, SourceUnit sourceUnit) {
        if (nodes == null || nodes.length != 2 || !(nodes[0] instanceof AnnotationNode) || !(nodes[1] instanceof AnnotatedNode)) {
            throw new GroovyBugError("Internal error: expecting [AnnotationNode, AnnotatedNode] but got: " + (nodes == null ? null : Arrays.asList(nodes)));
        }
        this.sourceUnit = sourceUnit;
    }
    
    public void modifyClass(ClassNode classNode) {
        boolean force = true;
        // no processing if existing constructors found
        
        List<ConstructorNode> constructors = classNode.getDeclaredConstructors();
        
        if (constructors.size() > 1 && !force) return;
        boolean foundEmpty = constructors.size() == 1 && constructors.get(0).getFirstStatement() == null;
        
        // proceed by adjusting class
        final List<PropertyNode> properties = getInstanceProperties(classNode);
        boolean specialHashMapCase = properties.size() == 1 && properties.get(0).getField().getType().equals(HASHMAP_TYPE);
        
        final List<MethodNode> addedMethods = createCheckMethods(properties)
        addUberCheckMethodToClass(classNode, createUberCheckerMethod(addedMethods))
        addCheckMethodsToClass(classNode, addedMethods)
        
        if (constructors) {
            haveConstructorsCallUberChecker(classNode, createUberCheckerStatement())
        }
    }
    
    @CompileStatic(TypeCheckingMode.SKIP)
    private boolean hasImmutableAnnotation(ClassNode classNode) {
        hasAnnotation(classNode, ImmutableASTTransformation.MY_TYPE)
    }
    
    private void haveConstructorsCallUberChecker(ClassNode classNode, Statement uberCheckerCallingStatement) {
        for (ConstructorNode constructor : classNode.getDeclaredConstructors()) {
            def code = constructor.getCode()
            if (constructor.code instanceof BlockStatement) {
                List<Statement> existingStatements = ((BlockStatement)constructor.code).getStatements();
                existingStatements.add uberCheckerCallingStatement
            } else if (constructor.code instanceof ExpressionStatement) {
                // no-op
            } else {
                addError("@NonNull fails to determine the correct constructor", classNode)
            }
        }
    }
    
    private static void addUberCheckMethodToClass(ClassNode classNode, MethodNode methodNode) {
        classNode.addMethod(methodNode)
    }
    
    private static void addCheckMethodsToClass(ClassNode classNode, List<MethodNode> methods) {
        // will not add duplicate methods
        methods.each { method ->
            classNode.addMethod(method)
        }
    }
    
    private static MethodNode createUberCheckerMethod(List<MethodNode> checkerMethods) {
        String methodName = UBER_CHECKER_METHOD_NAME
        StringBuilder sb = new StringBuilder()
        sb.with {
            append "void ${methodName}() {\n"
            for (MethodNode method : checkerMethods) {
                PropertyNode property = method.getNodeMetaData(NON_NULL_PROPERTY_NODE_METADATA_KEY)
                append "this.${method.name}(this.${property.name})\n"
            }
            append "}\n"
        }
        buildMethodFromString(methodName, sb.toString())
    }
    
    private static List<MethodNode> createCheckMethods(List<PropertyNode> properties) {
        properties.collect { prop ->
            createCheckMethod(prop)
        }
    }
    
    private static MethodNode createCheckMethod(PropertyNode propertyNode) {
        String propName = propertyNode.getName()
        String propNameCap = propName.capitalize()
        String propType = propertyNode.getType().getName()
        String methodName = "checkNonNull${propNameCap}"
        
        MethodNode methodNode = buildMethodFromString(methodName, """
                void ${methodName}($propType value) {
                    if (value == null) {
                        throw new IllegalArgumentException(\"$propNameCap can not be null\")
                    }
                }
        """)
        methodNode.setNodeMetaData(NON_NULL_PROPERTY_NODE_METADATA_KEY, propertyNode)
        return methodNode
    }
    
    private static MethodNode buildMethodFromString(String methodName, String source) {
        // make sure there's a class node, the method is added to
        final StringBuilder sb = new StringBuilder()
        sb.with {
            append "class ${methodName}Class {"
            append source
            append "}"
        }
        final def nodes = new AstBuilder().buildFromString(sb.toString())
        MethodNode createdMethod = (MethodNode) ((ClassNode)nodes[1]).methods[0].find { MethodNode mn -> mn.name == methodName }
        createdMethod
    }
    
    private static Statement createUberCheckerStatement() {
        new ExpressionStatement(
            new MethodCallExpression(
                new VariableExpression("this"),
                new ConstantExpression(UBER_CHECKER_METHOD_NAME),
                ArgumentListExpression.EMPTY_ARGUMENTS
            )
        )
    }
    
    private static Statement createPrintlnAst(String message) {
        new ExpressionStatement(
            new MethodCallExpression(
                new VariableExpression("this"),
                new ConstantExpression("println"),
                new ArgumentListExpression(
                    new ConstantExpression(message)
                )
            )
        )
    }
    
    private static Statement createPrintlnAstFromCode(String message) {
        new AstBuilder().buildFromString("""
            println message
        """) [0] as BlockStatement
    }
    
    
}
