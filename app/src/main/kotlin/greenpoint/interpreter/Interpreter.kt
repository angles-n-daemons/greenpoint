package greenpoint.interpreter

import greenpoint.scanner.Scanner
import greenpoint.scanner.TokenType

import greenpoint.parser.Parser

import greenpoint.grammar.Expression

class RuntimeError(message: String): RuntimeException(message)

class Interpreter: Expression.Visitor<Any?> {
	fun run(source: String): Any? {
        val scanner = Scanner(source)
        val tokens = scanner.scanTokens()
        val parser = Parser(tokens)
        val expr = parser.parse()
        if (expr == null) {
            return expr
        }
        return evaluate(expr)
	}

    override fun visitBinary(binary: Expression.Binary): Any? {
        val left = evaluate(binary.left)
        val right = evaluate(binary.right)
        when(binary.op.type) {
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
            else -> throw RuntimeError("Unknown operator or comparator $binary.op.type")
        }
    }

    override fun visitUnary(unary: Expression.Unary): Any? {
        val right = evaluate(unary.expr)

        when(unary.op.type) {
            TokenType.MINUS -> return -toNumber(right)
            TokenType.BANG -> return !isTruthy(right)
            else -> throw RuntimeError("Unknown prefix operator $unary.op.type")
        }
    }

    override fun visitLiteral(literal: Expression.Literal): Any? {
        return literal.value
    }

    override fun visitGroup(group: Expression.Group): Any? {
        return evaluate(group.expr)
    }

    override fun visitExpressionList(expressionList: Expression.ExpressionList): Any? {
        val values = mutableListOf<Any?>()
        for (expression in expressionList.expressions) {
            values.add(evaluate(expression))
        }
        return values
    }
    override fun visitTernary(ternary: Expression.Ternary): Any? {
        val condition = evaluate(ternary.condition)

        if (isTruthy(condition)) {
            return evaluate(ternary.left)
        } else {
            return evaluate(ternary.right)
        }
    }

    private fun evaluate(expr: Expression): Any? {
        return expr.accept(this)
    }

    private fun plus(a: Any?, b: Any?): Any? {
        if (a is Double && b is Double) {
            return a + b
        } else if (a is String || b is String) {
            var aStr = when(a) {
                is String -> a
                is Boolean, is Double -> a.toString()
                null -> "nil"
                else -> throw RuntimeException("Cannot convert $a to string for concatenation")
            }
            var bStr = when(b) {
                is String -> b
                is Boolean, is Double -> b.toString()
                null -> "nil"
                else -> throw RuntimeException("Cannot convert $b to string for concatenation")
            }
            
            return aStr + bStr
        }

        throw RuntimeError("Cannot add $a.javaClass.kotlin.qualifiedName and $b.javaClass.kotlin.qualifiedName")
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
        else throw RuntimeError("Cannot evaluate truthiness on type: $value.javaClass.kotlin.qualifiedName")
    }
}
