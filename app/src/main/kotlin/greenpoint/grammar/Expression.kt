package greenpoint.grammar

import greenpoint.scanner.Token

sealed class Expr {
    abstract fun <R> accept(visitor: Visitor<R>): R

    interface Visitor<R> {
        fun visitBinaryExpr(expr: Binary): R
        fun visitUnaryExpr(expr: Unary): R
        fun visitLiteralExpr(expr: Literal): R
        fun visitGroupExpr(expr: Group): R
        fun visitExprListExpr(expr: ExprList): R
        fun visitTernaryExpr(expr: Ternary): R
        fun visitVariableExpr(expr: Variable): R
        fun visitAssignExpr(expr: Assign): R
        fun visitLogicalExpr(expr: Logical): R
        fun visitCallExpr(expr: Call): R
        fun visitFuncExpr(expr: Func): R
        fun visitGetExpr(expr: Get): R
        fun visitSetExpr(expr: Set): R
        fun visitThisExpr(expr: This): R
    }

    class Binary(
        val left: Expr,
        val op: Token,
        val right: Expr,
    ): Expr() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visitBinaryExpr(this)
        }
    }

    class Unary(
        val op: Token,
        val expr: Expr,
    ): Expr() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visitUnaryExpr(this)
        }
    }

    class Group(val expr: Expr): Expr() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visitGroupExpr(this)
        }
    }

    class ExprList(val expressions: MutableList<Expr>): Expr() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visitExprListExpr(this)
        }
    }

    class Ternary(
        val condition: Expr,
        val left: Expr,
        val right: Expr,
    ): Expr() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visitTernaryExpr(this)
        }
    }

    class Literal(val value: Any?): Expr() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visitLiteralExpr(this)
        }
    }

    // INTENTIONALLY NOT DATA CLASS SO CAN BE MAP KEY
    class Variable(val name: Token): Expr() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visitVariableExpr(this)
        }
    }

    // INTENTIONALLY NOT DATA CLASS SO CAN BE MAP KEY
    class Assign(
        val name: Token,
        val value: Expr,
    ): Expr() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visitAssignExpr(this)
        }
    }

    class Logical(
        val left: Expr,
        val op: Token,
        val right: Expr,
    ): Expr() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visitLogicalExpr(this)
        }
    }

    class Call(
        val callee: Expr,
        val paren: Token,
        val args: List<Expr>,
    ): Expr() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visitCallExpr(this)
        }
    }

    class Func(
        val params: List<Token>,
        val body: List<Stmt>,
    ): Expr() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visitFuncExpr(this)
        }
    }

    class Get(
        val obj: Expr,
        val name: Token,
    ): Expr() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visitGetExpr(this)
        }
    }

    class Set(
        val obj: Expr,
        val name: Token,
        val value: Expr,
    ): Expr() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visitSetExpr(this)
        }
    }

    class This(
        val keyword: Token,
    ): Expr() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visitThisExpr(this)
        }
    }
}
