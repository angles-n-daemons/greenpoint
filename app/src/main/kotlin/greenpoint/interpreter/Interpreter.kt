package greenpoint.interpreter

import greenpoint.scanner.Scanner
import greenpoint.scanner.TokenType

import greenpoint.parser.Parser

import greenpoint.grammar.Expr
import greenpoint.grammar.Stmt

class RuntimeError(message: String): RuntimeException(message)

class Interpreter(
    private val printer: (message: Any?) -> Unit = ::println,
): Expr.Visitor<Any?>, Stmt.Visitor<Any?> {
    private val environment = Environment()

	fun run(source: String): Any? {
        val scanner = Scanner(source)
        val tokens = scanner.scanTokens()
        if (scanner.hasErrors()) {
            scanner.printErrors()
            return null
        }

        val parser = Parser(tokens)
        val statements = parser.parse()
        if (parser.hasErrors()) {
            parser.printErrors()
            return null
        }

        var result: Any? = null
        for (statement in statements) {
            result = execute(statement)
        }
        return result
	}

    fun runExpression(source: String): Any? {
        val scanner = Scanner(source)
        val tokens = scanner.scanTokens()
        val parser = Parser(tokens)
        return evaluate(parser.parseExpression())
    }

    fun runStatement(source: String): Any? {
        val scanner = Scanner(source)
        val tokens = scanner.scanTokens()
        val parser = Parser(tokens)
        return execute(parser.parseStatement())
    }

    private fun execute(stmt: Stmt): Any? {
        return stmt.accept(this)
    }

    override fun visitExpressionStmt(stmt: Stmt.Expression): Any? {
        return evaluate(stmt.expr)
    }

    override fun visitPrintStmt(stmt: Stmt.Print): Any? {
        printer(stringify(evaluate(stmt.expr)))
        return null
    }

    override fun visitVarStmt(stmt: Stmt.Var): Any? {
        var value: Any? = null
        if (stmt.initializer != null) {
            value = evaluate(stmt.initializer)
        }

        environment.define(stmt.name, value)
        return null
    }

    override fun visitBlockStmt(stmt: Stmt.Block): Any? {
        throw RuntimeError("not yet able to handle block statements")
    }

    override fun visitBinaryExpr(expr: Expr.Binary): Any? {
        val left = evaluate(expr.left)
        val right = evaluate(expr.right)
        when(expr.op.type) {
            TokenType.MINUS -> return toNumber(left) - toNumber(right)
            TokenType.SLASH -> return toNumber(left) / toNumber(right)
            TokenType.STAR -> return toNumber(left) * toNumber(right)
            TokenType.PLUS -> return plus(left, right)
            TokenType.GREATER -> return toNumber(left) > toNumber(right)
            TokenType.GREATER_EQUAL -> return toNumber(left) >= toNumber(right)
            TokenType.LESS -> return toNumber(left) < toNumber(right)
            TokenType.LESS_EQUAL -> return toNumber(left) <= toNumber(right)
            TokenType.BANG_EQUAL -> return !isEqual(left, right)
            TokenType.EQUAL_EQUAL -> return isEqual(left, right)
            else -> throw RuntimeError("Unknown operator or comparator $expr.op.type")
        }
    }

    override fun visitUnaryExpr(expr: Expr.Unary): Any? {
        val right = evaluate(expr.expr)

        when(expr.op.type) {
            TokenType.MINUS -> return -toNumber(right)
            TokenType.BANG -> return !isTruthy(right)
            else -> throw RuntimeError("Unknown prefix operator $expr.op.type")
        }
    }

    override fun visitLiteralExpr(expr: Expr.Literal): Any? {
        return expr.value
    }

    override fun visitLogicAndOrExpr(expr: Expr.LogicAndOr): Any? {
        val left = evaluate(expr.left)
        val right = evaluate(expr.right)
        when(expr.op.type) {
            TokenType.AND -> return isTruthy(left) && isTruthy(right)
            TokenType.OR -> return isTruthy(left) || isTruthy(right)
            else -> throw RuntimeError("Parse error cannot do logical and/or with ${expr.op.type}")
        }
    }

    override fun visitGroupExpr(expr: Expr.Group): Any? {
        return evaluate(expr.expr)
    }

    override fun visitExprListExpr(expr: Expr.ExprList): Any? {
        val values = mutableListOf<Any?>()
        for (expression in expr.expressions) {
            values.add(evaluate(expression))
        }
        return values
    }

    override fun visitTernaryExpr(expr: Expr.Ternary): Any? {
        val condition = evaluate(expr.condition)

        if (isTruthy(condition)) {
            return evaluate(expr.left)
        } else {
            return evaluate(expr.right)
        }
    }
    
    override fun visitVariableExpr(expr: Expr.Variable): Any? {
        return environment.get(expr.name)
    }
    
    override fun visitAssignExpr(expr: Expr.Assign): Any? {
        val value = evaluate(expr.value)
        environment.assign(expr.name, value)
        return value
    }

    private fun evaluate(expr: Expr): Any? {
        return expr.accept(this)
    }

    private fun plus(a: Any?, b: Any?): Any? {
        if (a is Double && b is Double) {
            return a + b
        } else if (a is String || b is String) {
            return stringify(a) + stringify(b)
        }

        throw RuntimeError("Cannot add $a and $b")
    }

    private fun isEqual(a: Any?, b: Any?): Boolean {
        if (a == null && b == null) return true
        else if (a == null) return false
        else return a.equals(b)
    }

    private fun toNumber(value: Any?): Double {
        if (value is Double) return value
        else throw RuntimeError("$value cannot be cast to number")
    }

    private fun isTruthy(value: Any?): Boolean {
        if (value == null) return false
        else if (value is String) return value.length > 0
        else if (value is Number) return value != 0.0
        else if (value is Boolean) return value
        else throw RuntimeError("Cannot evaluate truthiness on type: $value")
    }

    fun stringify(value: Any?): String {
        if (value == null) return "nil"
        if (value is String) return value;
        return value.toString()
    }
}
