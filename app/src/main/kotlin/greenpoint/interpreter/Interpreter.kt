package greenpoint.interpreter

import greenpoint.scanner.Scanner
import greenpoint.scanner.Token
import greenpoint.scanner.TokenType

import greenpoint.parser.Parser

import greenpoint.grammar.Expr
import greenpoint.grammar.Stmt

import greenpoint.interpreter.classes.Class
import greenpoint.interpreter.classes.Instance
import greenpoint.interpreter.function.Callable
import greenpoint.interpreter.function.Func
import greenpoint.interpreter.function.Return
import greenpoint.interpreter.function.seedEnvironmentWithNatives

import greenpoint.resolver.Resolver


class RuntimeError(message: String): RuntimeException(message)


class Interpreter(
    private val printer: (message: Any?) -> Unit = ::println,
): Expr.Visitor<Any?>, Stmt.Visitor<Any?> {
    val globals = Environment()
    private var environment = globals
    private var locals = mutableMapOf<Expr, Int>()

    init {
        seedEnvironmentWithNatives(globals)
    }

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

        val resolver = Resolver(this)
        resolver.resolve(statements)

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

    fun resolve(expr: Expr, dist: Int) {
        locals.put(expr, dist)
    }

    private fun execute(stmt: Stmt): Any? {
        return stmt.accept(this)
    }

    override fun visitExpressionStmt(stmt: Stmt.Expression): Any? {
        return evaluate(stmt.expr)
    }

    override fun visitClassStmt(stmt: Stmt.Class): Any? {
        environment.define(stmt.name, null)

        val methods = mutableMapOf<String, Func>()
        for (method in stmt.methods) {
            methods.put(
                method.name.lexeme,
                Func(method, environment),
            )
        }

        environment.assign(
            stmt.name,
            Class(stmt.name.lexeme, methods),
        )
        return null
    }

    override fun visitFuncStmt(stmt: Stmt.Func): Any? {
        val f = Func(stmt, environment)
        environment.define(stmt.name, f)
        return null
    }

    override fun visitReturnStmt(stmt: Stmt.Return): Any? {
        var value: Any? = null
        if (stmt.value != null) value = evaluate(stmt.value)
        throw Return(value)
    }

    override fun visitPrintStmt(stmt: Stmt.Print): Any? {
        printer(stringify(evaluate(stmt.expr)))
        return null
    }

    override fun visitWhileStmt(stmt: Stmt.While): Any? {
        while(isTruthy(evaluate(stmt.condition))) {
            execute(stmt.body)
        }
        return null
    }

    override fun visitIfStmt(stmt: Stmt.If): Any? {
        if (isTruthy(evaluate(stmt.condition))) {
            execute(stmt.thenStmt)
        } else if (stmt.elseStmt != null) {
            execute(stmt.elseStmt)
        }
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
        executeBlock(stmt.statements, Environment(environment))
        return null
    }

    fun executeBlock(
        statements: List<Stmt>,
        environment: Environment,
    ) {
        val previous = this.environment
        try {
            this.environment = environment
            
            for (statement in statements) {
                execute(statement)
            }
        } finally {
            this.environment = previous
        }
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

    override fun visitLogicalExpr(expr: Expr.Logical): Any? {
        val left = isTruthy(evaluate(expr.left))
        // short circuit conditions
        if (left && expr.op.type == TokenType.OR) {
            return true
        }
        if (!left && expr.op.type == TokenType.AND) {
            return false
        }

        val right = isTruthy(evaluate(expr.right))
        when (expr.op.type) {
            TokenType.AND -> return left && right
            TokenType.OR -> return left || right
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

    override fun visitCallExpr(expr: Expr.Call): Any? {
        val callee = evaluate(expr.callee)
        val argVals = mutableListOf<Any?>()
        for (arg in expr.args) {
            argVals.add(evaluate(arg))
        }

        if (callee is Callable) {
            if (argVals.size != callee.arity()) {
                throw RuntimeError("Expected ${callee.arity()} arguments but got ${argVals.size}")
            }
            return callee.call(this, argVals)
        } else {
            throw RuntimeError("Can only call functions and classes")
        }
    }

    override fun visitFuncExpr(expr: Expr.Func): Any? {
        // Wrap function for usage later
        return Func(Stmt.Func(
            Token(TokenType.IDENTIFIER, "anon func", null, -1),
            expr.params,
            expr.body,
        ), environment)
    }

    override fun visitGetExpr(expr: Expr.Get): Any? {
        val obj = evaluate(expr.obj)
        if (obj is Instance) {
            return obj.get(expr.name)
        }
        throw RuntimeError("Only instances have properties")
    }

    override fun visitSetExpr(expr: Expr.Set): Any? {
        val obj = evaluate(expr.obj)
        if (obj is Instance) {
            val value = evaluate(expr.value)
            return obj.set(expr.name, value)
        }
        throw RuntimeError("Only instances have fields")
    }

    override fun visitLiteralExpr(expr: Expr.Literal): Any? {
        return expr.value
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
        return lookupVariable(expr.name, expr)
    }
    
    override fun visitAssignExpr(expr: Expr.Assign): Any? {
        val value = evaluate(expr.value)
        val dist = locals.get(expr)
        if (dist != null) {
            environment.assignAt(dist, expr.name, value)
        } else {
            globals.assign(expr.name, value)
        }
        return value
    }

    private fun lookupVariable(name: Token, expr: Expr): Any? {
        val dist = locals.get(expr)
        if (dist != null) {
            return environment.getAt(dist, name)
        } else {
            return globals.get(name)
        }
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
