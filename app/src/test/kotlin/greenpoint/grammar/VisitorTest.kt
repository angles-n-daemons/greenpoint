package greenpoint.grammar

import kotlin.test.Test
import kotlin.test.assertEquals

import greenpoint.scanner.Token
import greenpoint.scanner.TokenType

class VisitorTest {
    @Test fun testASTVisitor() {
        val expression = Binary(
            Unary(
                Token(TokenType.MINUS, "-", null, 1),
                Literal(123),
            ),
            Token(TokenType.STAR, "*", null, 1),
            Group(Literal(45.67)),
        )
            
        val printer = ASTPrinter()
        assertEquals(
            "(* (- 123) (group 45.67))",
            printer.print(expression),
        )
    }

    @Test fun testRPNVisitor() {
        val expression = Binary(
            Group(Binary(
                Literal(1),
                Token(TokenType.PLUS, "+", null, 0),
                Literal(2),
            )),
            Token(TokenType.STAR, "*", null, 0),
            Group(Binary(
                Literal(4),
                Token(TokenType.MINUS, "-", null, 0),
                Literal(3),
            )),
        )
            
        val printer = RPNPrinter()
        assertEquals(
            "   1 2 +   4 3 - *",
            printer.print(expression),
        )
    }
}
