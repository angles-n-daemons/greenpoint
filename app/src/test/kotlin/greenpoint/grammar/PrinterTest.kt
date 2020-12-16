package greenpoint.grammar

import kotlin.test.Test
import kotlin.test.assertEquals

import greenpoint.scanner.Token
import greenpoint.scanner.TokenType

class PrinterTest {
    @Test fun testASTPrinter() {
        val stmt = Stmt.Expression(Expr.Binary(
            Expr.Unary(
                Token(TokenType.MINUS, "-", null, 1),
                Expr.Literal(123),
            ),
            Token(TokenType.STAR, "*", null, 1),
            Expr.Group(Expr.Literal(45.67)),
        ))
            
        val printer = ASTPrinter()
        assertEquals(
            "(* (- 123) (group 45.67))",
            printer.print(stmt),
        )
    }
}
