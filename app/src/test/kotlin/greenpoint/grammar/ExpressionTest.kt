package greenpoint.grammar

import kotlin.test.Test
import kotlin.test.assertEquals

import greenpoint.scanner.Token
import greenpoint.scanner.TokenType

class ExpressionTest {
    @Test fun testConstructors() {
        val plus = Token(TokenType.PLUS, "+", null, 0)
        val l1 = Literal(12.0)
        val l2 = Literal(15.0)
        val bang = Token(TokenType.BANG, "!", null, 0)
        Binary(l1, plus, l2)
        Unary(bang, l1)
    }
}
