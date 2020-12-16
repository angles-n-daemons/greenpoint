package greenpoint.grammar

class ASTPrinter: Expression.Visitor<String> {
    fun print(expr: Expression): String {
        return expr.accept(this)
    }

    override fun visitBinary(binary: Expression.Binary): String {
        return parenthesize(
            binary.op.lexeme,
            binary.left,
            binary.right,
        )
    }

    override fun visitUnary(unary: Expression.Unary): String {
        return parenthesize(
            unary.op.lexeme,
            unary.expr,
        )
    }

    override fun visitGroup(group: Expression.Group): String {
        return parenthesize("group", group.expr)
    }

    override fun visitLiteral(literal: Expression.Literal): String {
        return literal.value?.toString() ?: "nil"
    }

    override fun visitExpressionList(expressionList: Expression.ExpressionList): String {
        return parenthesize("list", *expressionList.expressions.toTypedArray())
    }

    override fun visitTernary(ternary: Expression.Ternary): String {
        return parenthesize("ternary", ternary.condition, ternary.left, ternary.right)
    }

    private fun parenthesize(name: String, vararg exprs: Expression): String {
        val builder = StringBuilder();
        builder.append("(")
        builder.append(name)

        for (expr in exprs) {
            builder.append(" ")
            builder.append(expr.accept(this))
        }
        builder.append(")")
        
        return builder.toString()
    }
}
