package greenpoint.grammar

import kotlin.test.Test
import kotlin.test.assertEquals

import greenpoint.scanner.Token
import greenpoint.scanner.TokenType

class ExpressionTest {
    @Test fun testConstructors() {
        val plus = Token(TokenType.PLUS, "+", null, 0)
        val l1 = Expression.Literal(12.0)
        val l2 = Expression.Literal(15.0)
        val bang = Token(TokenType.BANG, "!", null, 0)
        Expression.Binary(l1, plus, l2)
        Expression.Unary(bang, l1)
    }
}
