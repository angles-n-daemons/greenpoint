package greenpoint.parser

import greenpoint.scanner.Token
import greenpoint.scanner.TokenType
import greenpoint.grammar.Expr
import greenpoint.grammar.Stmt

class ParseError(message: String): RuntimeException(message)

class Parser(val tokens: List<Token>){
    var current = 0
    val errors = mutableListOf<ParseError>()

    fun parse(): List<Stmt> {
        val statements = mutableListOf<Stmt>()
        while(!isAtEnd()) {
            statements.add(statement())
        }
        return statements
    }

    fun parseExpression(): Expr {
        val expr = expression()
        if (current < tokens.size) {
            throw ParseError("Finished parsing before reaching end of input")
        }
        return expr
    }

    private fun statement(): Stmt {
        if (match(TokenType.PRINT)) return printStmt()
        return expressionStmt()
    }

    private fun printStmt(): Stmt {
        val expr = expression()
        consume(TokenType.SEMICOLON, "Expecting ';' after value")
        return Stmt.Print(expr)
    }

    private fun expressionStmt(): Stmt {
        val expr = expression()
        consume(TokenType.SEMICOLON, "Expecting ';' after expression")
        return Stmt.Expression(expr)
    }

    private fun expression(): Expr {
        return comma()
    }

    private fun comma(): Expr {
        var expressions = mutableListOf<Expr>(ternary())

        while (match(TokenType.COMMA)) {
            expressions.add(ternary())
        }

        return if (expressions.size == 1) expressions.first() else Expr.ExprList(expressions)
    }

    private fun ternary(): Expr {
        var expr = equality()

        if (match(TokenType.QUESTION)) {
            val left = equality()
            consume(TokenType.COLON, "Missing colon in ternary expression")
            val right = equality()
            expr = Expr.Ternary(expr, left, right)
        }

        return expr
    }

    private fun equality(): Expr {
        var expr = comparison()
        while (match(TokenType.BANG_EQUAL, TokenType.EQUAL_EQUAL)) {
            val op = previous()
            val right = comparison()
            expr = Expr.Binary(expr, op, right)
        }

        return expr
    }

    private fun comparison(): Expr {
        var expr = term()

        while (match(
            TokenType.GREATER,
            TokenType.GREATER_EQUAL,
            TokenType.LESS,
            TokenType.LESS_EQUAL,
        )) {
            val op = previous()
            val right = term()
            expr = Expr.Binary(expr, op, right)
        }

        return expr
    }

    private fun term(): Expr {
        var expr = factor()

        while (match(TokenType.PLUS, TokenType.MINUS)) {
            val op = previous()
            val right = factor()
            expr = Expr.Binary(expr, op, right)
        }

        return expr
    }

    private fun factor(): Expr {
        var expr = unary()

        while (match(TokenType.SLASH, TokenType.STAR)) {
            val op = previous()
            val right = unary()
            expr = Expr.Binary(expr, op, right)
        }

        return expr
    }

    private fun unary(): Expr {
        if (match(TokenType.BANG, TokenType.MINUS)) {
            val op = previous()
            val right = primary()
            return Expr.Unary(op, right)
        }

        return primary()
    }

    private fun primary(): Expr {
        if (match(TokenType.FALSE)) return Expr.Literal(false)
        if (match(TokenType.TRUE)) return Expr.Literal(true)
        if (match(TokenType.NIL)) return Expr.Literal(null)

        if (match(TokenType.NUMBER, TokenType.STRING)) {
            return Expr.Literal(previous().literal)
        }

        if (match(TokenType.LEFT_PAREN)) {
            val expr = expression()
            consume(TokenType.RIGHT_PAREN, "Expect ')' after expression.")
            return Expr.Group(expr)
        }

        if (isAtEnd()) {
            throw ParseError("Unexpected end of tokens while parsing")
        }
        throw ParseError("Unexpected primary parse token ${peek().type}")
    }

    private fun consume(type: TokenType, message: String): Token {
        if(isAtEnd()) throw ParseError("Reached EOF while searching for $type")
        if(check(type)) return advance()
        throw ParseError("$message - ${peek().type}")
    }
    
    private fun match(vararg types: TokenType): Boolean {
        for (type in types) {
            if (check(type)) {
                advance()
                return true
            }
        }

        return false
    }

    private fun check(type: TokenType): Boolean {
        if(isAtEnd()) return false
        return peek().type == type
    }

    private fun advance(): Token {
        if(!isAtEnd()) current++
        return previous()
    }

    private fun isAtEnd(): Boolean {
        if (current >= tokens.size) return true
        return peek().type == TokenType.EOF
    }

    private fun peek(): Token {
        return tokens.get(current);
    }

    private fun previous(): Token {
        return tokens.get(current-1)
    }

    private fun syncrhonize() {
        advance()
        while (!isAtEnd()) {
            if (previous().type == TokenType.SEMICOLON) return

            when(peek().type) {
                TokenType.CLASS, TokenType.FUN, TokenType.VAR,
                TokenType.FOR, TokenType.IF, TokenType.WHILE,
                TokenType.PRINT, TokenType.RETURN -> return
                else -> {}
            }
            advance()
        }
    }
}
