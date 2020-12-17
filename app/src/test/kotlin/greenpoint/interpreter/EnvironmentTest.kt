package greenpoint.interpreter

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

import greenpoint.scanner.Token
import greenpoint.scanner.TokenType

fun tokenGuy(name: String): Token {
    return Token(TokenType.IDENTIFIER, name, null, 0)
}

class EnvironmentTest {
    @Test fun testEnvironmentDefine() {
        val env = Environment()
        env.define(tokenGuy("a"), null)
        env.define(tokenGuy("b"), 5.0)
        env.define(tokenGuy("c"), 5.0)
        env.define(tokenGuy("c"), "i was changed")

        assertEquals(env.get(tokenGuy("a")), null)
        assertEquals(env.get(tokenGuy("b")), 5.0)
        assertEquals(env.get(tokenGuy("c")), "i was changed")

        env.assign(tokenGuy("c"), "i was changed again!")
        assertEquals(env.get(tokenGuy("c")), "i was changed again!")
    }

    @Test fun testEnvironmentWrongTokenType() {
        val tests = mutableListOf<TokenType>(
            TokenType.AND,
            TokenType.OR,
            TokenType.BANG,
            TokenType.EQUAL_EQUAL,
            TokenType.STRING,
            TokenType.LEFT_BRACE,
            TokenType.LEFT_PAREN,
        )
        for (test in tests) {
            var errored = false

            val environment = Environment()
            try {
                environment.define(Token(test, "test", null, 0), true)
            } catch(e: Exception) {
                errored = true
            }
            assertTrue(errored)

            errored = false
            environment.define(Token(TokenType.IDENTIFIER, "test", null, 0), true)
            try {
                environment.get(Token(test, "test", null, 0))
            } catch(e: Exception) {
                errored = true
            }
            assertTrue(errored)

            errored = false
            try {
                environment.assign(Token(test, "test", null, 0), true)
            } catch(e: Exception) {
                errored = true
            }
            assertTrue(errored)
        }
    }

    @Test fun testAssignUndefined() {
        val env = Environment()

        var errored = false
        try {
            env.assign(tokenGuy("a"), "i was changed again!")
        } catch(e: Exception) {
            errored = true
        }
        assertTrue(errored)
    }

    @Test fun testGetUndefined() {
        val env = Environment()

        var errored = false
        try {
            env.get(tokenGuy("a"))
        } catch(e: Exception) {
            errored = true
        }
        assertTrue(errored)
    }

    @Test fun testEnvironmentEnclosing() {
        val outerEnv = Environment()
        val innerEnv = Environment(outerEnv)

        outerEnv.define(tokenGuy("a"), 5)
        assertEquals(innerEnv.get(tokenGuy("a")), 5)

        // assignment bubbles down if scope not defined
        innerEnv.assign(tokenGuy("a"), 10)
        assertEquals(outerEnv.get(tokenGuy("a")), 10)

        // inner definition doesn't bubble downward
        innerEnv.define(tokenGuy("b"), 3)
        var errored = false
        try {
            outerEnv.get(tokenGuy("b"))
        } catch(e: Exception) {
            errored = true
        }
        assertTrue(errored)

        // inner assignment doesn't bubble downward if key exists
        outerEnv.define(tokenGuy("c"), false)
        innerEnv.define(tokenGuy("c"), false)
        innerEnv.assign(tokenGuy("c"), true)

        assertEquals(outerEnv.get(tokenGuy("c")), false)
        assertEquals(innerEnv.get(tokenGuy("c")), true)
    }
}
