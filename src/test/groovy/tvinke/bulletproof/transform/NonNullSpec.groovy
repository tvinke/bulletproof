package tvinke.bulletproof.transform

import org.codehaus.groovy.control.MultipleCompilationErrorsException

import spock.lang.*

class NonNullSpec extends GroovyShellSpec {
    
    def "@NonNull may only be applied on the class level"() {
        when:
        def person = evaluate("""
            import tvinke.bulletproof.transform.NonNull
            import groovy.transform.*
            
            class Person {

                @NonNull // wrong place
                String name
            }
            new Person('Ted')
        """)

        then:
        thrown(MultipleCompilationErrorsException)
    }
    
    @See('@NonNull runs in a AST phase before @Immutable')
    def "@NonNull and @Immutable annotation order on class should not matter "() {
        when:
        def person1 = evaluate("""
            import tvinke.bulletproof.transform.NonNull
            import groovy.transform.*
            @Immutable // 1st
            @NonNull  // 2nd
            class Person {
                String name
            }
            new Person()
        """)

        then:
        IllegalArgumentException ex1 = thrown()
        ex1.message == 'Name can not be null'
        
        when:
        def person2 = evaluate("""
            import tvinke.bulletproof.transform.NonNull
            import groovy.transform.*
            @NonNull  // 1st
            @Immutable // 2nd
            class Person {
                String name
            }
            new Person()
        """)

        then:
        IllegalArgumentException ex2 = thrown()
        ex2.message == 'Name can not be null'
    }
    
    def "@NonNull and @Immutable should fail on null value"() {
        when:
        def person = evaluate("""
            import tvinke.bulletproof.transform.NonNull
            import groovy.transform.*
            @Immutable
            @NonNull
            class Person {
                String name
            }
            new Person()
        """)

        then:
        IllegalArgumentException ex = thrown()
        ex.message == 'Name can not be null'
    }
    
    def "@NonNull and @Immutable tuple constructor should fail on multiple null values"() {
        when:
        def person = evaluate("""
            import tvinke.bulletproof.transform.NonNull
            import groovy.transform.*
            @Immutable
            @NonNull
            class Person {
                String name
                Integer age
            }
            new Person('Ted', null) /* null age */
        """)

        then:
        IllegalArgumentException ex = thrown()
        ex.message == 'Age can not be null'
    }
    
    def "@NonNull and @Immutable Map constructor should fail on first null values"() {
        when:
        def person = evaluate("""
            import tvinke.bulletproof.transform.NonNull
            import groovy.transform.*
            @Immutable
            @NonNull
            class Person {
                String name
                Integer age
            }
            new Person(name: 'Ted') /* null age */
        """)

        then:
        IllegalArgumentException ex = thrown()
        ex.message == 'Age can not be null'
    }
    
    def "@NonNull should still allow @Immutable Map constructor to fail on unknown property name"() {
        when:
        def person = evaluate("""
            import tvinke.bulletproof.transform.NonNull
            import groovy.transform.*
            @Immutable
            @NonNull
            class Person {
                String name
                Integer age
            }
            new Person(name: 'Ted', age2: null) /* mispelled  age2*/
        """)

        then:
        thrown(MissingPropertyException)
    }

    def "@NonNull and default Map constructor should keep working as-is with non-null value"() {
        when:
        def person = evaluate("""
            import tvinke.bulletproof.transform.NonNull
            @NonNull
            class Person {
                String name
            }
            new Person(name: 'Ted')
        """)

        then:
        person.name == 'Ted'
    }
    
    def "@NonNull and default Map constructor should work as-is and is not bullet-proof"() {
        when:
        def person = evaluate("""
            import tvinke.bulletproof.transform.NonNull
            @NonNull
            class Person {
                String name
                //void setName(String value) {
                //    throw new NullPointerException("Oops setName")
                //}
                //void setProperty(String prop, Object value) {
                //    throw new NullPointerException("Oops setProperty")
                //}
            }
            new Person(name: null)
        """)

        then:
        !person.name
    }
    
    def "@NonNull and @TupleConstructor should keep working as-is with non-null value"() {
        when:
        def person = evaluate("""
            import tvinke.bulletproof.transform.NonNull
            import groovy.transform.*
            @NonNull
            @TupleConstructor
            class Person {
                String name
            }
            new Person('Ted')
        """)

        then:
        person.name == 'Ted'
    }
    
    def "@NonNull and custom constructor should keep working as-is with non-null value"() {
        when:
        def person = evaluate("""
            import tvinke.bulletproof.transform.NonNull
            @NonNull
            class Person {
                String name
                Person(String name) {
                    this.name = name
                }
            }
            new Person('Ted')
        """)

        then:
        //person.getClass().getDeclaredMethods().each { println it }
        person.name == 'Ted'
    }

    def "@NonNull and custom constructor should fail on null value"() {
        when:
        def person = evaluate("""
            import tvinke.bulletproof.transform.NonNull
            @NonNull
            class Person {
                String name
                Person(String name) {
                    this.name = name
                }
            }
            new Person(null)
        """)

        then:
        IllegalArgumentException ex = thrown()
        ex.message == 'Name can not be null'
    }
    
}
