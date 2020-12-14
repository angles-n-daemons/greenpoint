package greenpoint.grammar

import greenpoint.scanner.Token

sealed class Expression {
    abstract fun <R> accept(visitor: Visitor<R>): R
}

class Binary( 
    val left: Expression,
    val op: Token,
    val right: Expression,
): Expression() {
    override fun <R> accept(visitor: Visitor<R>): R {
        return visitor.visitBinary(this)
    }
}

class Unary(
    val op: Token,
    val expr: Expression,
): Expression() {
    override fun <R> accept(visitor: Visitor<R>): R {
        return visitor.visitUnary(this)
    }
}

class Group(val expr: Expression): Expression() {
    override fun <R> accept(visitor: Visitor<R>): R {
        return visitor.visitGroup(this)
    }
}

class Literal(val value: Any?): Expression() {
    override fun <R> accept(visitor: Visitor<R>): R {
        return visitor.visitLiteral(this)
    }
}
