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

    data class Binary( 
        val left: Expr,
        val op: Token,
        val right: Expr,
    ): Expr() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visitBinaryExpr(this)
        }
    }

    data class Unary(
        val op: Token,
        val expr: Expr,
    ): Expr() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visitUnaryExpr(this)
        }
    }

    data class Group(val expr: Expr): Expr() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visitGroupExpr(this)
        }
    }

    data class ExprList(val expressions: List<Expr>): Expr() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visitExprListExpr(this)
        }
    }

    data class Ternary(
        val condition: Expr,
        val left: Expr,
        val right: Expr,
    ): Expr() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visitTernaryExpr(this)
        }
    }

    data class Literal(val value: Any?): Expr() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visitLiteralExpr(this)
        }
    }

    data class Variable(val name: Token): Expr() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visitVariableExpr(this)
        }
    }

    data class Assign(
        val name: Token,
        val value: Expr,
    ): Expr() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visitAssignExpr(this)
        }
    }

    data class LogicAndOr(
        val left: Expr,
        val op: Token,
        val right: Expr,
    ): Expr() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visitLogicAndOrExpr(this)
        }
    }
}
