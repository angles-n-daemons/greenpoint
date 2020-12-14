package greenpoint.grammar

interface Visitor<R> {
    fun visitBinary(binary: Binary): R
    fun visitUnary(unary: Unary): R
    fun visitLiteral(literal: Literal): R
    fun visitGroup(group: Group): R
}

class ASTPrinter: Visitor<String> {
    fun print(expr: Expression): String {
        return expr.accept(this)
    }

    override fun visitBinary(binary: Binary): String {
        return parenthesize(
            binary.op.lexeme,
            binary.left,
            binary.right,
        )
    }

    override fun visitUnary(unary: Unary): String {
        return parenthesize(
            unary.op.lexeme,
            unary.expr,
        )
    }

    override fun visitGroup(group: Group): String {
        return parenthesize("group", group.expr)
    }

    override fun visitLiteral(literal: Literal): String {
        return literal.value?.toString() ?: "nil"
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

class RPNPrinter: Visitor<String> {
    fun print(expr: Expression): String {
        return expr.accept(this)
    }

    override fun visitBinary(binary: Binary): String {
        return push(
            binary.op.lexeme,
            binary.left,
            binary.right,
        )
    }

    override fun visitUnary(unary: Unary): String {
        return push(
            unary.op.lexeme,
            unary.expr,
        )
    }

    override fun visitGroup(group: Group): String {
        return push("", group.expr)
    }

    override fun visitLiteral(literal: Literal): String {
        return literal.value?.toString() ?: "nil"
    }

    private fun push(name: String, vararg exprs: Expression): String {
        val builder = StringBuilder();

        for (expr in exprs) {
            builder.append(" ")
            builder.append(expr.accept(this))
        }

        if (name != "") {
            builder.append(" ")
            builder.append(name)
        }
        
        return builder.toString()
    }
}
