package tvinke.bulletproof.transform;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.codehaus.groovy.transform.GroovyASTTransformationClass;

/**
 * Class annotation to assist in creating objects which should not accept {@code null} values
 * when they're constructed.
 * 
 * <p>This AST transformation is inspired by Project Lombok's
 * <a href="https://projectlombok.org/features/NonNull.html">@NonNull</a> annotation.
 * 
 * <p>Any existing constructor is modified (except for the default Map constructor for now) to
 * perform null-checks.
 * 
 * <p>The null-check (for a property e.g. {@code name}) looks like 
 * <code><pre>
 * if (this.name == null) { throw new IllegalArgumentException("Name can not be null") }
 * </pre></code>
 * 
 * @author Ted Vinke
 *
 */
@java.lang.annotation.Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@GroovyASTTransformationClass("tvinke.bulletproof.transform.NonNullASTTransformation")
public @interface NonNull {

}
