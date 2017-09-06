package tvinke.bulletproof.grails.validation;

import org.codehaus.groovy.transform.GroovyASTTransformationClass;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Class annotation to assist in creating objects which should validate according to Grails'
 * {@link grails.validation.Validateable} constraints when they're constructed.
 * 
 * <p>Any existing constructor is modified to perform the validation as the last step.
 * 
 * <p>If the instance is not valid after construction, Grails's own {@link grails.validation.ValidationException} will be
 * thrown with the errors.
 *
 * <pre>
 * &#64;Valid
 * class Person implements grails.validation.Validateable {
 *    String name // 'nullable: false' by default in Grails
 * }
 * def person = new Person() // fails
 * // grails.validation.ValidationException: 1 errors:
 * // - Field error in object 'Person' on field 'name': rejected value [null]; codes [Person.name.... etc
 * </pre>
 *
 * @author Ted Vinke
 *
 */
@java.lang.annotation.Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@GroovyASTTransformationClass("tvinke.bulletproof.grails.validation.ValidASTTransformation")
public @interface Valid {

}
