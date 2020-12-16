package greenpoint.parser

import greenpoint.scanner.Token
import greenpoint.scanner.TokenType
import greenpoint.grammar.Expression

class ParseError(message: String): RuntimeException(message)

class Parser(val tokens: List<Token>){
    var current = 0
    val errors = mutableListOf<ParseError>()

    fun parse(): Expression? {
        val expr =  expression()
        if (current < tokens.size) {
            throw ParseError("Parse did not read all tokens")
        }
        return expr
    }

    private fun expression(): Expression {
        return comma()
    }

    private fun comma(): Expression {
        var expressions = mutableListOf<Expression>(ternary())

        while (match(TokenType.COMMA)) {
            expressions.add(ternary())
        }

        return if (expressions.size == 1) expressions.first() else Expression.ExpressionList(expressions)
    }

    private fun ternary(): Expression {
        var expr = equality()

        if (match(TokenType.QUESTION)) {
            val left = equality()
            consume(TokenType.COLON, "Missing colon in ternary expression")
            val right = equality()
            expr = Expression.Ternary(expr, left, right)
        }

        return expr
    }

    private fun equality(): Expression {
        var expr = comparison()
        while (match(TokenType.BANG_EQUAL, TokenType.EQUAL_EQUAL)) {
            val op = previous()
            val right = comparison()
            expr = Expression.Binary(expr, op, right)
        }

        return expr
    }

    private fun comparison(): Expression {
        var expr = term()

        while (match(
            TokenType.GREATER,
            TokenType.GREATER_EQUAL,
            TokenType.LESS,
            TokenType.LESS_EQUAL,
        )) {
            val op = previous()
            val right = term()
            expr = Expression.Binary(expr, op, right)
        }

        return expr
    }

    private fun term(): Expression {
        var expr = factor()

        while (match(TokenType.PLUS, TokenType.MINUS)) {
            val op = previous()
            val right = factor()
            expr = Expression.Binary(expr, op, right)
        }

        return expr
    }

    private fun factor(): Expression {
        var expr = unary()

        while (match(TokenType.SLASH, TokenType.STAR)) {
            val op = previous()
            val right = unary()
            expr = Expression.Binary(expr, op, right)
        }

        return expr
    }

    private fun unary(): Expression {
        if (match(TokenType.BANG, TokenType.MINUS)) {
            val op = previous()
            val right = primary()
            return Expression.Unary(op, right)
        }

        return primary()
    }

    private fun primary(): Expression {
        if (match(TokenType.FALSE)) return Expression.Literal(false)
        if (match(TokenType.TRUE)) return Expression.Literal(true)
        if (match(TokenType.NIL)) return Expression.Literal(null)

        if (match(TokenType.NUMBER, TokenType.STRING)) {
            return Expression.Literal(previous().literal)
        }

        if (match(TokenType.LEFT_PAREN)) {
            val expr = expression()
            consume(TokenType.RIGHT_PAREN, "Expect ')' after expression.")
            return Expression.Group(expr)
        }

        if (isAtEnd()) {
            throw ParseError("Unexpected end of tokens while parsing")
        }
        throw ParseError("Unexpected primary parse token ${peek().type}")
    }

    private fun consume(type: TokenType, message: String): Token {
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
