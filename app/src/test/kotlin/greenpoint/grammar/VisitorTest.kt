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
}
