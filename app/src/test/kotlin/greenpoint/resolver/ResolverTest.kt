package greenpoint.grammar

import kotlin.test.Test

import greenpoint.interpreter.Interpreter
import greenpoint.resolver.Resolver

class ResolverTest {
    @Test fun testConstructors() {
        Resolver(Interpreter())
    }
}
