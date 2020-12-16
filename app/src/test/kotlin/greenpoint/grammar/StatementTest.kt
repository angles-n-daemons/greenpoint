package greenpoint.grammar

import kotlin.test.Test
import kotlin.test.assertEquals

import greenpoint.scanner.Token
import greenpoint.scanner.TokenType

class StmtTest {
    @Test fun testStmtInit() {
        Stmt.Expression(Expr.Literal(5.0))
        Stmt.Print(Expr.Literal(1.0))
    }
}
