package tvinke.bulletproof.transform

import spock.lang.*

class ValueObjectSpec extends GroovyShellSpec {

    def "@ValueObject uses @NonNull to prevent object instantiated with a null value"() {
        when:
        def money = evaluate("""
            import tvinke.bulletproof.transform.ValueObject
            @ValueObject
            class Money {
                BigDecimal amount
            }
            new Money()
        """)

        then:
        IllegalArgumentException ex = thrown()
        ex.message == 'Amount can not be null'
    }
    
    def "@ValueObject uses @Immutable to prevent mutating an object after creation"() {
        given:
        def money = evaluate("""
            import tvinke.bulletproof.transform.ValueObject
            @ValueObject
            class Money {
                BigDecimal amount
            }
            new Money(2.95)
        """)

        when:
        money.amount = 3.0
        
        then:
        thrown(ReadOnlyPropertyException)
    }
    
    def "@ValueObject can have settings passed to @Immutable"() {
        when:
        def address = evaluate("""
            import tvinke.bulletproof.transform.ValueObject
            @ValueObject(copyWith=true)
            class Address {
                String street
                String number
            }
            new Address('Main Street', '8')
        """)

        then:
        def newAddress = address.copyWith(number: '11')
        newAddress.number == '11'
    }
}
