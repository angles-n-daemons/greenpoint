package greenpoint.resolver

import java.util.ArrayDeque

import greenpoint.scanner.Token
import greenpoint.interpreter.Interpreter
import greenpoint.grammar.Expr
import greenpoint.grammar.Stmt


class ResolverError(message: String): RuntimeException(message)


enum class FunctionType {
    NONE,
    FUNCTION,
    METHOD,
    ANONYMOUS,
}


class Resolver (
    val interpreter: Interpreter,
): Stmt.Visitor<Unit>, Expr.Visitor<Unit> {
    val scopes = ArrayDeque<MutableMap<String, Boolean>>()
    private var currentFunction = FunctionType.NONE

    fun resolve(statements: List<Stmt>) {
        for (statement in statements) {
            resolve(statement)
        }
    }

    fun resolve(statement: Stmt) {
        statement.accept(this)
    }

    fun resolve(expression: Expr) {
        expression.accept(this)
    }

    private fun beginScope() {
        scopes.push(mutableMapOf<String, Boolean>())
    }

    private fun endScope() {
        scopes.pop()
    }

    private fun declare(name: Token) {
        if (scopes.isEmpty()) return
        val scope = scopes.peek()
        scope.put(name.lexeme, false)
    }

    private fun define(name: Token) {
        if (scopes.isEmpty()) return
        val scope = scopes.peek()
        scope.put(name.lexeme, true)
    }

    private fun resolveLocal(expr: Expr, name: Token) {
        var dist = 0
        // iterate from inner most scope outward
        for (scope in scopes) {
            if (scope.containsKey(name.lexeme)) {
                interpreter.resolve(expr, dist)
                return
            }
            dist++
        }
    }

    private fun resolveFunction(
        params: List<Token>,
        body: List<Stmt>,
        type: FunctionType,
    ) {
        val enclosingFunction = currentFunction
        currentFunction = type
        try {
            beginScope()

            for (param in params) {
                declare(param)
                define(param)
            }

            resolve(body)
            endScope()
        } finally {
            currentFunction = enclosingFunction
        }
    }

    override fun visitBlockStmt(stmt: Stmt.Block) {
        beginScope()
        resolve(stmt.statements)
        endScope()
    }

    override fun visitVarStmt(stmt: Stmt.Var) {
        declare(stmt.name)
        if (stmt.initializer != null) {
            resolve(stmt.initializer)
        }
        define(stmt.name)
    }
    
    override fun visitVariableExpr(expr: Expr.Variable) {
        if (!scopes.isEmpty()) {
            val varBinding = scopes.peek().get(expr.name.lexeme)
            if (varBinding == false) {
                throw ResolverError("Can't read local variable in its own intializer")
            }
        }

        resolveLocal(expr, expr.name)
    }

    override fun visitAssignExpr(expr: Expr.Assign) {
        resolve(expr.value)
        resolveLocal(expr, expr.name)
    }

    override fun visitClassStmt(stmt: Stmt.Class) {
        declare(stmt.name)
        define(stmt.name)

        for (method in stmt.methods) {
            val declaration = FunctionType.METHOD
            resolveFunction(method.params, method.body, declaration)
        }
    }

    override fun visitFuncStmt(stmt: Stmt.Func) {
        declare(stmt.name)
        define(stmt.name)
        resolveFunction(stmt.params, stmt.body, FunctionType.FUNCTION)
    }

    override fun visitExpressionStmt(stmt: Stmt.Expression) {
        resolve(stmt.expr)
    }

    override fun visitIfStmt(stmt: Stmt.If) {
        resolve(stmt.condition)
        resolve(stmt.thenStmt)
        if (stmt.elseStmt != null) resolve(stmt.elseStmt)
    }

    override fun visitPrintStmt(stmt: Stmt.Print) {
        resolve(stmt.expr)
    }

    override fun visitReturnStmt(stmt: Stmt.Return) {
        if (currentFunction == FunctionType.NONE) {
            throw ResolverError("Can't return from top-level code")
        }
        if (stmt.value != null) {
            resolve(stmt.value)
        }
    }
    
    override fun visitWhileStmt(stmt: Stmt.While) {
        resolve(stmt.condition)
        resolve(stmt.body)
    }

    override fun visitBinaryExpr(expr: Expr.Binary) {
        resolve(expr.left)
        resolve(expr.right)
    }

    override fun visitCallExpr(expr: Expr.Call) {
        resolve(expr.callee)
        
        for (arg in expr.args) {
            resolve(arg)
        }
    }

    override fun visitGroupExpr(expr: Expr.Group) {
        resolve(expr.expr)
    }

    override fun visitLiteralExpr(expr: Expr.Literal) {}

    override fun visitLogicalExpr(expr: Expr.Logical) {
        resolve(expr.left)
        resolve(expr.right)
    }

    override fun visitUnaryExpr(expr: Expr.Unary) {
        resolve(expr.expr)
    }

    override fun visitExprListExpr(expr: Expr.ExprList) {
        for (expression in expr.expressions) {
            resolve(expression)
        }
    }

    override fun visitTernaryExpr(expr: Expr.Ternary) {
        resolve(expr.condition)
        resolve(expr.left)
        resolve(expr.right)
    }

    override fun visitFuncExpr(expr: Expr.Func) {
        resolveFunction(expr.params, expr.body, FunctionType.ANONYMOUS)
    }

    override fun visitGetExpr(expr: Expr.Get) {
        resolve(expr.obj)
    }

    override fun visitSetExpr(expr: Expr.Set) {
        resolve(expr.value)
        resolve(expr.obj)
    }
}
