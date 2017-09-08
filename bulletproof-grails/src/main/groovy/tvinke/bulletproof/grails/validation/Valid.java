package tvinke.bulletproof.grails.validation;

import org.codehaus.groovy.transform.GroovyASTTransformationClass;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Class annotation to assist in creating objects which should validate according to Grails'
 * <a href="http://docs.grails.org/latest/guide/validation.html#constraints">constraints</a> when they're constructed. This
 * AST transformation makes sure you can not create an instance without passing all constraints.
 *
 * <p><em>Constraints provide Grails with a declarative DSL for defining validation rules, schema generation and CRUD
 * generation meta data. See the Grails chapter about <a href="http://docs.grails.org/latest/guide/validation.html">Validation</a>.</em></p>
 *
 * <p>Any existing constructor is modified by this AST transformation to perform the validation as the last step,
 * by invoking <code>validate()</code> and inspecting the results. If the instance is not valid, Grails's own
 * <code>grails.validation.ValidationException</code> will be thrown with the errors.
 *
 * This is what this AST transformation basically appends:
 * <pre>
 * if (!validate()) {
 *    throw new grails.validation.ValidationException(getErrors().errorCount + " errors", getErrors())
 * }
 * </pre>
 *
 * Example with implicit constraints where every property is 'nullable: false' by default in Grails.
 * <pre>
 * &#64;Valid
 * class Person implements Validateable {
 *    String name
 * }
 * def person = new Person()
 * // fails with grails.validation.ValidationException: 1 errors:
 * // - Field error in object 'Person' on field 'name': rejected value [null]; codes [Person.name.... etc
 * </pre>
 *
 * Another example where you can not create an instance without passing all constraints:
 * <pre>
 * &#64;Valid
 * class User implements Validateable {
 *    String login
 *    String password
 *    String email
 *    Integer age
 *
 *    static constraints = {
 *       login size: 5..15, blank: false, unique: true
 *       password size: 5..15, blank: false
 *       email email: true, blank: false
 *       age min: 18
 *    }
 * }
 * def user = new User(login: 'tim123', password: 'ssht', age: 15, email: 'invalid')
 * // fails with grails.validation.ValidationException: 3 errors:
 * // - Field error in object 'User' on field 'password': rejected value [ssht]; ... etc
 * // - Field error in object 'User' on field 'email': rejected value [invalid]; ... etc
 * // - Field error in object 'User' on field 'age': rejected value [15]; codes ... etc
 *
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
