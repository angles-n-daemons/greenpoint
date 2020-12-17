package greenpoint.interpreter

import kotlin.test.Test
import kotlin.test.assertEquals

import greenpoint.scanner.Token
import greenpoint.scanner.TokenType

fun tokenGuy(name: String): Token {
    return Token(TokenType.IDENTIFIER, name, null, 0)
}

class EnvironmentTest {
    @Test fun testEnvironmentDefine() {
        val env = Environment()
        env.define(tokenGuy("1"), null)
        env.define(tokenGuy("2"), 5.0)
        env.define(tokenGuy("3"), 5.0)
        env.define(tokenGuy("3"), "i was changed")

        assertEquals(env.get(tokenGuy("1")), null)
        assertEquals(env.get(tokenGuy("2")), 5.0)
        assertEquals(env.get(tokenGuy("3")), "i was changed")
    }
}
