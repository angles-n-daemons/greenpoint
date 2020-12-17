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
        env.define(tokenGuy("1"), null)
        env.define(tokenGuy("2"), 5.0)
        env.define(tokenGuy("3"), 5.0)
        env.define(tokenGuy("3"), "i was changed")

        assertEquals(env.get(tokenGuy("1")), null)
        assertEquals(env.get(tokenGuy("2")), 5.0)
        assertEquals(env.get(tokenGuy("3")), "i was changed")

        env.assign(tokenGuy("3"), "i was changed again!")
        assertEquals(env.get(tokenGuy("3")), "i was changed again!")
    }

    @Test fun testAssignUndefined() {
        val env = Environment()

        var errored = false
        try {
            env.assign(tokenGuy("1"), "i was changed again!")
        } catch(e: Exception) {
            errored = true
        }
        assertTrue(errored)
    }

    @Test fun testGetUndefined() {
        val env = Environment()

        var errored = false
        try {
            env.get(tokenGuy("1"))
        } catch(e: Exception) {
            errored = true
        }
        assertTrue(errored)
    }

    @Test fun testEnvironmentAssign() {
        val outerEnv = Environment()
        val innerEnv = Environment(outerEnv)

        outerEnv.define(tokenGuy("1"), 5.0)
        assertEquals(innerEnv.get(tokenGuy("1")), 5.0)
    }
}
