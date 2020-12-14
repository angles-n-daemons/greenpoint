package greenpoint.parser

import kotlin.test.Test
import kotlin.test.assertEquals

import greenpoint.scanner.Token
import greenpoint.scanner.Scanner
import greenpoint.grammar.ASTPrinter

class ParserTest {
    @Test fun testSimple() {
        val scanner = Scanner("!(5 + 8 * (3 / 1) == 4), 5 + 9")
        val parser = Parser(scanner.scanTokens())
        val printer = ASTPrinter()
        val expr = parser.parse()
        if (expr == null) {
            throw Exception("parser failed")
        }
        assertEquals(
            "(, (! (group (== (+ 5.0 (* 8.0 (group (/ 3.0 1.0)))) 4.0))) (+ 5.0 9.0))",
            printer.print(expr),
        )
    }
}
