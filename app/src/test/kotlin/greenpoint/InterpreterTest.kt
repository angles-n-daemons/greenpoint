package greenpoint

import kotlin.test.Test
import kotlin.test.assertEquals

class InterpreterTest {
    @Test fun testInterpreterRun() {
        val interpreter = Interpreter()
        assertEquals(
            "!!{}",
            interpreter.run("!!{}"),
            "verify interpreter just returns expression"
        )
    }
}
