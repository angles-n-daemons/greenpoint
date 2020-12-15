package greenpoint.interpreter

import kotlin.test.Test
import kotlin.test.assertEquals

data class ITest(
    val input: String,
    val expected: Any?,
    val raisesError: Boolean=false,
)


class InterpreterTest {
    @Test fun testInterpreterEvaluate() {
        val tests = listOf<ITest>(
            // literals
            ITest("nil", null),
            ITest("\"coffee\"", "coffee"),
            ITest("3", 3.0),
            ITest("3.0", 3.0),
            ITest("false", false),

            // unary operators
            ITest("!true", false),
            ITest("-(3 + 4)", -7.0),
            ITest("-4.0", -4.0),
            ITest("!\"\"", true),
            ITest("!\"a string\"", false),
            ITest("!0", true),
            ITest("!1", false),
            ITest("!nil", true),
            // fails for invalid types
            ITest("-\"johnson\"", null, true),
            ITest("-nil", null, true),
            ITest("-true", null, true),

            // binary operations
            ITest("5 + 6", 11.0),
            ITest("\"Hello\" + \"World\"", "HelloWorld"),
            ITest("10 / 2", 5.0),
            ITest("3 - 4", -1.0),
            ITest("5 + 6", 11.0),
            // cannot add different types
            ITest("5 + \"john\"", null, true),
            ITest("nil + \"john\"", null, true),

            // comparators
            ITest("6 > 3", true),
            ITest("6 < 3", false),
            ITest("3 <= 3", true),
            ITest("3 >= 3", true),
            ITest("4 <= 3", false),
            ITest("4 >= 3", true),
            ITest("1 <= 3", true),
            ITest("1 >= 3", false),

            // equality checks
            ITest("3 == 3", true),
            ITest("4 == 3", false),
            ITest("\"3\" == 3", false),
            ITest("3 != 3", false),
            ITest("4 != 3", true),
            ITest("\"3\" != 3", true),

            // test ternary operations
            ITest("true ? 1 : 2", 1.0),
            ITest("false ? 1 : 2", 2.0),
        )
        val interpreter = Interpreter()

        for (test in tests) {
            var result: Any? = null
            var raisedError = false

            try {
                result = interpreter.run(test.input)
            } catch(e: Exception) {
                raisedError = true
            }

            assertEquals(test.raisesError, raisedError)
            assertEquals(test.expected, result)
        }
    }
}
