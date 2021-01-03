package greenpoint.grammar

import greenpoint.scanner.Token

sealed class Stmt {
    abstract fun <R> accept(visitor: Visitor<R>): R

    interface Visitor<R> {
        fun visitExpressionStmt(stmt: Expression): R
        fun visitFuncStmt(stmt: Func): R
        fun visitPrintStmt(stmt: Print): R
        fun visitVarStmt(stmt: Var): R
        fun visitBlockStmt(stmt: Block): R
        fun visitIfStmt(stmt: If): R
        fun visitWhileStmt(stmt: While): R
        fun visitReturnStmt(stmt: Return): R
        fun visitClassStmt(stmt: Class): R
    }

    class Expression(
        val expr: Expr,
    ): Stmt() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visitExpressionStmt(this)
        }
    }

    class Func(
        val name: Token,
        val params: List<Token>,
        val body: List<Stmt>,
    ): Stmt() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visitFuncStmt(this)
        }
    }

    class Print(
        val expr: Expr,
    ): Stmt() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visitPrintStmt(this)
        }
    }

    class Return(
        val keyword: Token,
        val value: Expr?,
    ): Stmt() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visitReturnStmt(this)
        }
    }

    class Var(
        val name: Token,
        val initializer: Expr?,
    ): Stmt() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visitVarStmt(this)
        }
    }

    class Block(val statements: List<Stmt>): Stmt() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visitBlockStmt(this)
        }
    }

    class If(
        val condition: Expr,
        val thenStmt: Stmt,
        val elseStmt: Stmt?,
    ): Stmt() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visitIfStmt(this)
        }
    }

    class While(
        val condition: Expr,
        val body: Stmt,
    ): Stmt() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visitWhileStmt(this)
        }
    }

    class Class (
        val name: Token,
        val methods: List<Stmt.Func>,
    ): Stmt() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visitClassStmt(this)
        }
    }
}
