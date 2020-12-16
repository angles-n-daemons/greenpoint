package greenpoint.parser

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

import greenpoint.grammar.Expr
import greenpoint.scanner.Token
import greenpoint.scanner.Scanner
import greenpoint.grammar.ASTPrinter

class ParserTest {
    @Test fun testSimple() {
        val scanner = Scanner("!(5 + 8 * (3 / 1) == 4) ? \"hello\" : \"goodbye\", 5 + 9;")
        val parser = Parser(scanner.scanTokens())
        val printer = ASTPrinter()
        val stmts = parser.parse()
        if (stmts.size == 0) {
            throw Exception("parser parsed no statements")
        }
        assertEquals(
            "(list (ternary (! (group (== (+ 5.0 (* 8.0 (group (/ 3.0 1.0)))) 4.0))) hello goodbye) (+ 5.0 9.0))",
            printer.print(stmts[0]),
        )
    }

    @Test fun testErrorConditions() {
        val tests = listOf<String>(
            "+(3 + 4)",
            "(5 - 8",
            "3-4)",
        )
        for (test in tests) {
            val scanner = Scanner(test)
            val parser = Parser(scanner.scanTokens())
            var raisedError = false
            try {
                parser.parse()
            } catch (e: Exception) {
                raisedError = true
            }

            assertTrue(raisedError)
        }
    }
}
