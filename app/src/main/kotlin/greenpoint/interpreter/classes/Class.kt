package greenpoint.interpreter.classes

import greenpoint.scanner.Token
import greenpoint.interpreter.Interpreter
import greenpoint.interpreter.RuntimeError
import greenpoint.interpreter.function.Callable
import greenpoint.interpreter.function.Func

class Class(
    val name: String,
    val methods: MutableMap<String, Func>,
): Callable {
    override fun arity(): Int {
        return 0
    }

    override fun call(
        interpreter: Interpreter,
        args: List<Any?>,
    ): Any? {
        return Instance(this)
    }

    fun findMethod(name: String): Func? {
        if (methods.containsKey(name)) {
            return methods.get(name)
        }
        return null
    }

    override fun toString(): String {
        return name
    }
}

class Instance(
    val klass: Class,
) {
    private val fields = mutableMapOf<String, Any?>()

    fun get(name: Token): Any? {

        if (fields.contains(name.lexeme)) {
            return fields.get(name.lexeme)
        }

        val method = klass.findMethod(name.lexeme)
        if (method != null) return method

        throw RuntimeError("Undefined property ${name.lexeme}.")
    }

    fun set(name: Token, obj: Any?) {
        fields.put(name.lexeme, obj)
    }

    override fun toString(): String {
        return "${klass.name} instance"
    }
}
