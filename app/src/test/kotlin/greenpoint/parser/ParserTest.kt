package greenpoint.parser

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

import greenpoint.grammar.Expr
import greenpoint.grammar.Stmt
import greenpoint.scanner.Token
import greenpoint.scanner.TokenType
import greenpoint.scanner.Scanner
import greenpoint.grammar.ASTPrinter

class ParserTest {
    @Test fun testSimple() {
        val scanner = Scanner("var wallet = !(5 + johnson * (3 / 1) == 4) ? \"hello\" : \"goodbye\", 5 + 9;")
        val parser = Parser(scanner.scanTokens())
        val printer = ASTPrinter()
        val stmts = parser.parse()
        if (stmts.size == 0) {
            throw Exception("parser parsed no statements")
        }
        assertEquals(
            "(wallet = (list (ternary (! (group (== (+ 5.0 (* johnson (group (/ 3.0 1.0)))) 4.0))) hello goodbye) (+ 5.0 9.0)))",
            printer.print(stmts[0]),
        )
    }

    @Test fun testErrorConditions() {
        val tests = listOf<String>(
            "+(3 + 4);",
            "(5 - 8;",
            "3-4);",
        )
        for (test in tests) {
            val scanner = Scanner(test)
            val parser = Parser(scanner.scanTokens())
            parser.parse()
            assertTrue(parser.errors.size > 0)
        }
    }

    @Test fun testBlock() {
        val stmts = Parser(Scanner("""{
            var a = 3;
            print 4;
        }""").scanTokens()).parse()
        val expected = mutableListOf<Stmt>(
            Stmt.Block(mutableListOf<Stmt>(
                Stmt.Var(Token(TokenType.IDENTIFIER, "a", null, 2), Expr.Literal(3.0)),
                Stmt.Print(Expr.Literal(4.0)),
            )),
        )
        assertEquals(
            expected,
            stmts,
        )
    }

    @Test fun testIf() {
        val stmts = Parser(Scanner("""
        if (a > 0) {
            b = a;
        }""").scanTokens()).parse()
        val expected = mutableListOf<Stmt>(Stmt.If(
            Expr.Binary(
                Expr.Variable(Token(TokenType.IDENTIFIER, "a", null, 2)),
                Token(TokenType.GREATER, ">", null, 2),
                Expr.Literal(0.0),
            ),
            Stmt.Block(mutableListOf<Stmt>(
                Stmt.Expression(Expr.Assign(
                    Token(TokenType.IDENTIFIER, "b", null, 3),
                    Expr.Variable(Token(TokenType.IDENTIFIER, "a", null, 3)),
                )),
            )),
            null,
        ))
        assertEquals(
            expected,
            stmts,
        )
    }

    @Test fun testIfElse() {
        val stmts = Parser(Scanner("""
        if (a > 0) {
            b = a;
        } else {
            b = 0;
        }""").scanTokens()).parse()
        val expected = mutableListOf<Stmt>(Stmt.If(
            Expr.Binary(
                Expr.Variable(Token(TokenType.IDENTIFIER, "a", null, 2)),
                Token(TokenType.GREATER, ">", null, 2),
                Expr.Literal(0.0),
            ),
            Stmt.Block(mutableListOf<Stmt>(
                Stmt.Expression(Expr.Assign(
                    Token(TokenType.IDENTIFIER, "b", null, 3),
                    Expr.Variable(Token(TokenType.IDENTIFIER, "a", null, 3)),
                )),
            )),
            Stmt.Block(mutableListOf<Stmt>(
                Stmt.Expression(Expr.Assign(
                    Token(TokenType.IDENTIFIER, "b", null, 5),
                    Expr.Literal(0.0),
                )),
            )),
        ))
        assertEquals(
            expected,
            stmts,
        )
    }

    @Test fun testLogical() {
        val stmts = Parser(Scanner("""true or false and true;""").scanTokens()).parse()
        val expected = mutableListOf<Stmt>(Stmt.Expression(
            Expr.Logical(
                Expr.Literal(true),
                Token(TokenType.OR, "or", null, 1),
                Expr.Logical(
                    Expr.Literal(false),
                    Token(TokenType.AND, "and", null, 1),
                    Expr.Literal(true),
                ),
            ),
        ))
        assertEquals(
            expected,
            stmts,
        )
    }

    @Test fun testWhile() {
        val stmts = Parser(Scanner("""
            while(a < 3) { a = a + 1; }
        """).scanTokens()).parse()
        val expected = mutableListOf<Stmt>(Stmt.While(
            Expr.Binary(
                Expr.Variable(Token(TokenType.IDENTIFIER, "a", null, 2)),
                Token(TokenType.LESS, "<", null, 2),
                Expr.Literal(3.0),
            ),
            Stmt.Block(mutableListOf<Stmt>(
                Stmt.Expression(Expr.Assign(
                    Token(TokenType.IDENTIFIER, "a", null, 2),
                    Expr.Binary(
                        Expr.Variable(Token(TokenType.IDENTIFIER, "a", null, 2)),
                        Token(TokenType.PLUS, "+", null, 2),
                        Expr.Literal(1.0),
                    ),
                )),
            )),
        ))
        assertEquals(
            expected,
            stmts,
        )
    }
}
