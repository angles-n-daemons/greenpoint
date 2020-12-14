package greenpoint.scanner

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.test.assertFalse

class ScannerTest {
    @Test fun testScannerInit() {
        Scanner("")
    }

    @Test fun testScannerIsAtEnd() {
        val scanner = Scanner("12345")
        scanner.current = 4
        assertEquals(false, scanner.isAtEnd(), "current at last token")
        scanner.current = 5
        assertEquals(true, scanner.isAtEnd(), "current equal to length of source")
        scanner.current = 6
        assertEquals(true, scanner.isAtEnd(), "current greater than length of source")
    }

    @Test fun testScannerNewToken() {
        val scanner = Scanner("and")
        scanner.start = 0
        scanner.current = 3
        assertEquals(
            scanner.newToken(TokenType.AND),
            Token(TokenType.AND, "and", null, 1),
        )
    }

    @Test fun testScannerAdvance() {
        val scanner = Scanner("12345")
        assertEquals(
            '1',
            scanner.advance(),
        )
        assertEquals(
            1,
            scanner.current,
        )

        scanner.current = 3
        assertEquals(
            '4',
            scanner.advance(),
        )
    }

    @Test fun testScannerMatch() {
        val scanner = Scanner("12345")
        assertTrue(scanner.match('1'))
        assertEquals(1, scanner.current)
        assertFalse(scanner.match('1'))
        assertEquals(1, scanner.current)

        // end of string case
        scanner.current = 5
        assertFalse(scanner.match('5'))
        assertEquals(5, scanner.current)
    }

    @Test fun testScannerScanTokens() {
        val scanner = Scanner("(){},.+-;*")
        val expected = mutableListOf<Token>(
            Token(TokenType.LEFT_PAREN, "(", null, 1),
            Token(TokenType.RIGHT_PAREN, ")", null, 1),
            Token(TokenType.LEFT_BRACE, "{", null, 1),
            Token(TokenType.RIGHT_BRACE, "}", null, 1),
            Token(TokenType.COMMA, ",", null, 1),
            Token(TokenType.DOT, ".", null, 1),
            Token(TokenType.PLUS, "+", null, 1),
            Token(TokenType.MINUS, "-", null, 1),
            Token(TokenType.SEMICOLON, ";", null, 1),
            Token(TokenType.STAR, "*", null, 1),
        )
        val result = scanner.scanTokens()
        assertEquals(
            expected,
            result
        )
    }

    @Test fun testScannerScanTokenSingleChar() {
        val scanner = Scanner("(){},.+-;*")
        assertEquals(
            Token(TokenType.LEFT_PAREN, "(", null, 1),
            scanner.scanTokenMaybe(),
        )
        assertEquals(
            Token(TokenType.RIGHT_PAREN, ")", null, 1),
            scanner.scanTokenMaybe(),
        )
        assertEquals(
            Token(TokenType.LEFT_BRACE, "{", null, 1),
            scanner.scanTokenMaybe(),
        )
        assertEquals(
            Token(TokenType.RIGHT_BRACE, "}", null, 1),
            scanner.scanTokenMaybe(),
        )
        assertEquals(
            Token(TokenType.COMMA, ",", null, 1),
            scanner.scanTokenMaybe(),
        )
        assertEquals(
            Token(TokenType.DOT, ".", null, 1),
            scanner.scanTokenMaybe(),
        )
        assertEquals(
            Token(TokenType.PLUS, "+", null, 1),
            scanner.scanTokenMaybe(),
        )
        assertEquals(
            Token(TokenType.MINUS, "-", null, 1),
            scanner.scanTokenMaybe(),
        )
        assertEquals(
            Token(TokenType.SEMICOLON, ";", null, 1),
            scanner.scanTokenMaybe(),
        )
        assertEquals(
            Token(TokenType.STAR, "*", null, 1),
            scanner.scanTokenMaybe(),
        )
    }


    @Test fun testScannerScanTokenOneOrTwoChar() {
        var scanner = Scanner("!!=")
        assertEquals(
            Token(TokenType.BANG, "!", null, 1),
            scanner.scanTokenMaybe(),
        )
        assertEquals(
            Token(TokenType.BANG_EQUAL, "!=", null, 1),
            scanner.scanTokenMaybe(),
        )
        scanner = Scanner("===")
        assertEquals(
            Token(TokenType.EQUAL_EQUAL, "==", null, 1),
            scanner.scanTokenMaybe(),
        )
        assertEquals(
            Token(TokenType.EQUAL, "=", null, 1),
            scanner.scanTokenMaybe(),
        )
        scanner = Scanner(">>=")
        assertEquals(
            Token(TokenType.GREATER, ">", null, 1),
            scanner.scanTokenMaybe(),
        )
        assertEquals(
            Token(TokenType.GREATER_EQUAL, ">=", null, 1),
            scanner.scanTokenMaybe(),
        )
        scanner = Scanner("<<=")
        assertEquals(
            Token(TokenType.LESS, "<", null, 1),
            scanner.scanTokenMaybe(),
        )
        assertEquals(
            Token(TokenType.LESS_EQUAL, "<=", null, 1),
            scanner.scanTokenMaybe(),
        )
    }

    @Test fun testScannerScanTokenComment() {
        var scanner = Scanner(".//blah i am a comment\n,")
        var expected = mutableListOf<Token>(
            Token(TokenType.DOT, ".", null, 1),
            Token(TokenType.COMMA, ",", null, 2),
        )
        var result = scanner.scanTokens()
        assertEquals(
            expected,
            result,
        )

        scanner = Scanner(".//blah i am a comment")
        expected = mutableListOf<Token>(
            Token(TokenType.DOT, ".", null, 1),
        )
        result = scanner.scanTokens()
        assertEquals(
            expected,
            result,
        )
    }

    @Test fun testScannerScanTokenMultilineComment() {
        var scanner = Scanner("./*blah i am a comment\n-*/,")
        var expected = mutableListOf<Token>(
            Token(TokenType.DOT, ".", null, 1),
            Token(TokenType.COMMA, ",", null, 2),
        )
        var result = scanner.scanTokens()
        assertEquals(
            expected,
            result,
        )

        scanner = Scanner("./*blah i am a comment\n-,")
        var raised = false
        try {
            // should fail since comment isn't terminated
            scanner.scanTokens()
        } catch(e: Exception) {
            raised = true
        }
        assertTrue(raised)
    }

    @Test fun testScannerScanTokenNewline() {
        val scanner = Scanner("\n\n.\n\n")
        assertEquals(scanner.line, 1)

        assertNull(scanner.scanTokenMaybe())
        assertEquals(scanner.line, 2)

        assertNull(scanner.scanTokenMaybe())
        assertEquals(scanner.line, 3)

        assertEquals(Token(TokenType.DOT, ".", null, 3), scanner.scanTokenMaybe())
        assertEquals(scanner.line, 3)

        assertNull(scanner.scanTokenMaybe())
        assertEquals(scanner.line, 4)

        assertNull(scanner.scanTokenMaybe())
        assertEquals(scanner.line, 5)
    }

    @Test fun testScannerScanTokenString() {
        val scanner = Scanner("., \"tony\"\n \"man\" ")
        var expected = mutableListOf<Token>(
            Token(TokenType.DOT, ".", null, 1),
            Token(TokenType.COMMA, ",", null, 1),
            Token(TokenType.STRING, "\"tony\"", "tony", 1),
            Token(TokenType.STRING, "\"man\"", "man", 2),
        )
        assertEquals(
            expected,
            scanner.scanTokens(),
        )
    }

    @Test fun testScannerScanTokenNumber() {
        val scanner = Scanner("15.28 0 160. .5")
        var expected = mutableListOf<Token>(
            Token(TokenType.NUMBER, "15.28", 15.28, 1),
            Token(TokenType.NUMBER, "0", 0.0, 1),
            Token(TokenType.NUMBER, "160", 160.0, 1),
            Token(TokenType.DOT, ".", null, 1),
            Token(TokenType.DOT, ".", null, 1),
            Token(TokenType.NUMBER, "5", 5.0, 1),
        )
        assertEquals(
            expected,
            scanner.scanTokens(),
        )
    }

    @Test fun testScannerScanTokenKeywords() {
        val scanner = Scanner("and class else false for fun if nil or print return super this true var while")
        var expected = mutableListOf<Token>(
            Token(TokenType.AND, "and", null, 1),
            Token(TokenType.CLASS, "class", null, 1),
            Token(TokenType.ELSE, "else", null, 1),
            Token(TokenType.FALSE, "false", null, 1),
            Token(TokenType.FOR, "for", null, 1),
            Token(TokenType.FUN, "fun", null, 1),
            Token(TokenType.IF, "if", null, 1),
            Token(TokenType.NIL, "nil", null, 1),
            Token(TokenType.OR, "or", null, 1),
            Token(TokenType.PRINT, "print", null, 1),
            Token(TokenType.RETURN, "return", null, 1),
            Token(TokenType.SUPER, "super", null, 1),
            Token(TokenType.THIS, "this", null, 1),
            Token(TokenType.TRUE, "true", null, 1),
            Token(TokenType.VAR, "var", null, 1),
            Token(TokenType.WHILE, "while", null, 1),
        )
        assertEquals(
            expected,
            scanner.scanTokens(),
        )
    }

    @Test fun testScannerScanTokenIdentifier() {
        val scanner = Scanner("mary had a lit_tle lam3b")
        var expected = mutableListOf<Token>(
            Token(TokenType.IDENTIFIER, "mary", null, 1),
            Token(TokenType.IDENTIFIER, "had", null, 1),
            Token(TokenType.IDENTIFIER, "a", null, 1),
            Token(TokenType.IDENTIFIER, "lit_tle", null, 1),
            Token(TokenType.IDENTIFIER, "lam3b", null, 1),
        )
        assertEquals(
            expected,
            scanner.scanTokens(),
        )
    }
}
