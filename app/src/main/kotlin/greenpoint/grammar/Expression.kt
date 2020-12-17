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
        fun visitLogicAndOrExpr(expr: LogicAndOr): R
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

    class ExprList(val expressions: List<Expr>): Expr() {
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

    class Variable(val name: Token): Expr() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visitVariableExpr(this)
        }
    }

    class Assign(
        val name: Token,
        val value: Expr,
    ): Expr() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visitAssignExpr(this)
        }
    }

    class LogicAndOr(
        val left: Expr,
        val op: Token,
        val right: Expr,
    ): Expr() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visitLogicAndOrExpr(this)
        }
    }
}
