package greenpoint.parser

import greenpoint.scanner.Token
import greenpoint.scanner.TokenType
import greenpoint.grammar.Expr
import greenpoint.grammar.Stmt

class ParseError(message: String): RuntimeException(message)

class Parser(val tokens: List<Token>){
    var current = 0
    val errors = mutableListOf<Exception>()

    fun parse(): List<Stmt> {
        val statements = mutableListOf<Stmt>()
        while(!isAtEnd()) {
            try {
                statements.add(declaration())
            } catch (e: Exception) {
                errors.add(e)
                synchronize()
            }
        }
        return statements
    }

    fun parseExpression(): Expr {
        return expression()
    }

    fun parseStatement(): Stmt {
        return declaration()
    }

    fun hasErrors(): Boolean {
        return errors.size > 0
    }

    fun printErrors() {
        for (err in errors) {
            println(err)
        }
    }

    private fun declaration(): Stmt {
        if (match(TokenType.VAR)) return varDeclaration()
        return statement()
    }

    private fun statement(): Stmt {
        if (match(TokenType.FOR)) return forStmt()
        if (match(TokenType.IF)) return ifStmt()
        if (match(TokenType.PRINT)) return printStmt()
        if (match(TokenType.WHILE)) return whileStmt()
        if (match(TokenType.LEFT_BRACE)) return block()
        return expressionStmt()
    }

    private fun whileStmt(): Stmt {
        consume(TokenType.LEFT_PAREN, "Expect '(' after 'while'")
        val condition = expression()
        consume(TokenType.RIGHT_PAREN, "Expect closing ')' after 'while' condition")
        val body = statement()
        return Stmt.While(condition, body)
    }

    private fun forStmt(): Stmt {
        consume(TokenType.LEFT_PAREN, "Expect '(' after 'for'")

        var initializer: Stmt? = null
        if (match(TokenType.SEMICOLON)) {
            // do nothing
        } else if (match(TokenType.VAR)) {
            initializer = varDeclaration();
        } else {
            initializer = expressionStmt()
        }

        // default true if no condition present
        var condition: Expr = Expr.Literal(true)
        if (!check(TokenType.SEMICOLON)) {
            condition = expression()
        }
        consume(TokenType.SEMICOLON, "Expect ';' after loop condition")

        var increment: Expr? = null
        if (!check(TokenType.RIGHT_PAREN)) {
            increment = expression()
        }
        consume(TokenType.RIGHT_PAREN, "Expect closing ')' after 'for' clauses")

        var body = statement()
        if (increment != null) {
            body = Stmt.Block(mutableListOf<Stmt>(body, Stmt.Expression(increment)))
        }

        body = Stmt.While(condition, body)

        if (initializer != null) {
            body = Stmt.Block(mutableListOf<Stmt>(initializer, body))
        }
        return body
    }

    private fun ifStmt(): Stmt {
        consume(TokenType.LEFT_PAREN, "Expecting '(' after 'if'")
        val condition = expression()
        consume(TokenType.RIGHT_PAREN, "Expecting closing ')' after 'if' condition")
        
        val thenStmt = statement()
        var elseStmt: Stmt? = null
        if (match(TokenType.ELSE)) {
            elseStmt = statement()
        }

        return Stmt.If(condition, thenStmt, elseStmt)
    }

    private fun printStmt(): Stmt {
        val expr = expression()
        consume(TokenType.SEMICOLON, "Expecting ';' after print statement")
        return Stmt.Print(expr)
    }

    private fun block(): Stmt {
        val statements = mutableListOf<Stmt>()
        while (!check(TokenType.RIGHT_BRACE) && !isAtEnd()) {
            statements.add(declaration())
        }

        consume(TokenType.RIGHT_BRACE, "Expected '}' to close block")
        return Stmt.Block(statements)
    }

    private fun expressionStmt(): Stmt {
        val expr = expression()
        consume(TokenType.SEMICOLON, "Expecting ';' after statement")
        return Stmt.Expression(expr)
    }

    private fun varDeclaration(): Stmt {
        val name = consume(TokenType.IDENTIFIER, "Expect variable name")

        var initializer: Expr? = null
        if (match(TokenType.EQUAL)) {
            initializer = expression()
        }

        consume(TokenType.SEMICOLON, "Expect ';' after variable declaration.")
        return Stmt.Var(name, initializer)
    }

    private fun expression(): Expr {
        return comma()
    }

    private fun comma(): Expr {
        var expressions = mutableListOf<Expr>(assignment())

        while (match(TokenType.COMMA)) {
            expressions.add(assignment())
        }

        return if (expressions.size == 1) expressions.first() else Expr.ExprList(expressions)
    }

    private fun assignment(): Expr {
        val expr = ternary()

        if (match(TokenType.EQUAL)) {
            val value = assignment()
            if (expr is Expr.Variable) {
                val name = expr.name
                return Expr.Assign(name, value)
            }

            throw ParseError("Invalid assignment target ${expr.javaClass.kotlin.qualifiedName}")
        }

        return expr
    }

    private fun ternary(): Expr {
        var expr = logicOr()

        if (match(TokenType.QUESTION)) {
            val left = logicOr()
            consume(TokenType.COLON, "Missing colon in ternary expression")
            val right = logicOr()
            expr = Expr.Ternary(expr, left, right)
        }

        return expr
    }

    private fun logicOr(): Expr {
        var expr = logicAnd()
        if (match(TokenType.OR)) {
            expr = Expr.Logical(expr, previous(), logicAnd())
        }
        return expr
    }

    private fun logicAnd(): Expr {
        var expr = equality()
        if (match(TokenType.AND)) {
            expr = Expr.Logical(expr, previous(), equality())
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

        return call()
    }

    private fun call(): Expr {
        var expr = primary()

        while (true) {
            if (match(TokenType.LEFT_PAREN)) {
                expr = finishCall(expr)
            }
            else {
                break
            }
        }

        return expr
    }

    private fun finishCall(callee: Expr): Expr {
        var args = mutableListOf<Expr>()
        if (!check(TokenType.RIGHT_PAREN)) {
            val nextExpr = expression()
            if (nextExpr is Expr.ExprList) {
                args = nextExpr.expressions
            } else {
                args.add(nextExpr)
            }
        }

        if (args.size > 255) {
            throw ParseError("Too many arguments, maximum allowed is 255")
        }

        val paren = consume(TokenType.RIGHT_PAREN, "Expect ')' after arguments")
        return Expr.Call(callee, paren, args)
    }

    private fun primary(): Expr {
        if (match(TokenType.FALSE)) return Expr.Literal(false)
        if (match(TokenType.TRUE)) return Expr.Literal(true)
        if (match(TokenType.NIL)) return Expr.Literal(null)
        if (match(TokenType.IDENTIFIER)) return Expr.Variable(previous())

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

    private fun synchronize() {
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
