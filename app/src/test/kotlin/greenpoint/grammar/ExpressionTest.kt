package greenpoint.grammar

import kotlin.test.Test
import kotlin.test.assertEquals

import greenpoint.scanner.Token
import greenpoint.scanner.TokenType

class ExprTest {
    @Test fun testConstructors() {
        val plus = Token(TokenType.PLUS, "+", null, 0)
        val l1 = Expr.Literal(12.0)
        val l2 = Expr.Literal(15.0)
        val bang = Token(TokenType.BANG, "!", null, 0)
        Expr.Binary(l1, plus, l2)
        Expr.Unary(bang, l1)
    }
}
