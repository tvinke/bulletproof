package tvinke.bulletproof.grails.validation

import grails.validation.ValidationException
import org.springframework.validation.Errors
import tvinke.bulletproof.transform.GroovyShellSpec

class ValidSpec extends GroovyShellSpec {

    def "@Valid should not fail creation valid object"() {

        when: "valid object is created"
        def person = evaluate("""
            import grails.validation.Validateable
            import tvinke.bulletproof.grails.validation.Valid
            @Valid
            class Person implements Validateable {
                String name
            }
            new Person(name: 'Tim')
        """)

        then:
        person.name == 'Tim'
    }

    def "@Valid should fail creation invalid object using default constructor and constraints"() {

        when: "invalid object is created"
        evaluate("""
            import grails.validation.Validateable
            import tvinke.bulletproof.grails.validation.Valid
            @Valid
            class Person implements Validateable {
                String name
                // without 'constraints' block, all properties are 'nullable: false' by default in Grails
            }
            new Person()
        """)

        then:
        ValidationException ex = thrown()
        Errors errors = ex.errors
        errors['name'].code == 'nullable'
        errors['name'].defaultMessage == 'Property [{0}] of class [{1}] cannot be null'

        when: "invalid object is created"
        evaluate("""
            import grails.validation.Validateable
            import tvinke.bulletproof.grails.validation.Valid
            @Valid
            class User implements Validateable {
                String login
                String password
                String email
                Integer age
                
                static constraints = {
                    login size: 5..15, blank: false, unique: true
                    password size: 5..15, blank: false
                    email email: true, blank: false
                    age min: 18
                }
            }
            new User(login: 'tim123', 
                password: 'ssht', 
                age: 15, 
                email: 'invalid')
        """)

        then:
        ex = thrown()
        ex.errors.errorCount == 3
        ex.errors['password'].code == 'size.toosmall'
        ex.errors['email'].code == 'email.invalid'
        ex.errors['age'].code == 'min.notmet'
    }

    def "@Valid should fail creation invalid object using custom Map constructor"() {

        when: "invalid object is created"
        evaluate("""
            import grails.validation.Validateable
            import tvinke.bulletproof.grails.validation.Valid
            @Valid
            class Person implements Validateable {
                String name
                Person(Map props) { this.name = props?.name }
            }
            new Person(name: 'tim') // ok
            new Person() // fails
        """)

        then:
        ValidationException ex = thrown()
        def errors = ex.errors
        errors['name'].code == 'nullable'
    }


    def "@Valid should fail creation invalid object using custom constructor"() {

        when: "invalid object is created"
        evaluate("""
            import grails.validation.Validateable
            import tvinke.bulletproof.grails.validation.Valid
            @Valid
            class Person implements Validateable {
                String name
                Person(String name) { this.name = name }
            }
            new Person('tim') // ok
            new Person() // fails
        """)

        then:
        ValidationException ex = thrown()
        def errors = ex.errors
        errors['name'].code == 'nullable'
    }

    def "@Valid should validate at the end of any existing constructors"() {

        when: "valid object is created"
        def person = evaluate("""
            import grails.validation.Validateable
            import tvinke.bulletproof.grails.validation.Valid
            @Valid
            class Person implements Validateable {
                String name
                Person() { this.name = 'Tim' }
            }
            new Person()
        """)

        then:
        person.name == 'Tim'
    }

    def "@Valid and @TupleConstructor should work together and fail creation invalid object using created constructors"() {

        when: "invalid object is created"
        evaluate("""
            import groovy.transform.TupleConstructor
            import grails.validation.Validateable
            import tvinke.bulletproof.grails.validation.Valid
            @Valid
            @TupleConstructor
            class Person implements Validateable {
                String first, last
            }
            new Person('Tim') // missing 'last', fails
        """)

        then:
        ValidationException ex = thrown()
        def errors = ex.errors
        errors['last'].code == 'nullable'
    }
}
