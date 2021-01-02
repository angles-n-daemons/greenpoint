package greenpoint.grammar

class ASTPrinter: Expr.Visitor<String>, Stmt.Visitor<String> {
    fun print(stmt: Stmt): String {
        return stmt.accept(this)
    }

    override fun visitExpressionStmt(stmt: Stmt.Expression): String {
        return stmt.expr.accept(this)
    }

    override fun visitFuncStmt(stmt: Stmt.Func): String {
        return "dont do funcs"
    }

    override fun visitFuncExpr(expr: Expr.Func): String {
        return "dont do funcs"
    }

    override fun visitReturnStmt(stmt: Stmt.Return): String {
        return "dont do funcs"
    }

    override fun visitPrintStmt(stmt: Stmt.Print): String {
        return stmt.expr.accept(this)
    }

    override fun visitVarStmt(stmt: Stmt.Var): String {
        if (stmt.initializer == null) {
            return stmt.name.lexeme
        } else {
            return parenthesize(
                "${stmt.name.lexeme} =",
                stmt.initializer,
            )
        }
    }

    override fun visitBlockStmt(stmt: Stmt.Block): String {
        return "I Dont do blocks"
    }

    override fun visitIfStmt(stmt: Stmt.If): String {
        return "nor do i do ifs"
    }

    override fun visitWhileStmt(stmt: Stmt.While): String {
        return "dont do while"
    }

    override fun visitCallExpr(expr: Expr.Call): String {
        return "dont do call"
    }

    override fun visitClassStmt(stmt: Stmt.Class): String {
        return "dont do class"
    }

    override fun visitBinaryExpr(expr: Expr.Binary): String {
        return parenthesize(
            expr.op.lexeme,
            expr.left,
            expr.right,
        )
    }

    override fun visitUnaryExpr(expr: Expr.Unary): String {
        return parenthesize(
            expr.op.lexeme,
            expr.expr,
        )
    }

    override fun visitGroupExpr(expr: Expr.Group): String {
        return parenthesize("group", expr.expr)
    }

    override fun visitLiteralExpr(expr: Expr.Literal): String {
        return expr.value?.toString() ?: "nil"
    }

    override fun visitExprListExpr(expr: Expr.ExprList): String {
        return parenthesize("list", *expr.expressions.toTypedArray())
    }

    override fun visitTernaryExpr(expr: Expr.Ternary): String {
        return parenthesize("ternary", expr.condition, expr.left, expr.right)
    }

    override fun visitVariableExpr(expr: Expr.Variable): String {
        return expr.name.lexeme
    }

    override fun visitAssignExpr(expr: Expr.Assign): String {
        return parenthesize(expr.name.lexeme, expr.value)
    }

    override fun visitLogicalExpr(expr: Expr.Logical): String {
        return parenthesize(expr.op.lexeme, expr.left, expr.right)
    }

    private fun parenthesize(name: String, vararg exprs: Expr): String {
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
