package greenpoint.interpreter

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue


data class ITest(
    val input: String,
    val expected: Any?,
    val raisesError: Boolean=false,
)


class InterpreterTest {
    @Test fun testInterpreterEvaluate() {
        val tests = listOf<ITest>(
            // literals
            ITest("nil", null),
            ITest("\"coffee\"", "coffee"),
            ITest("3", 3.0),
            ITest("3.0", 3.0),
            ITest("false", false),

            // unary operators
            ITest("!true", false),
            ITest("-(3 + 4)", -7.0),
            ITest("-4.0", -4.0),
            ITest("!\"\"", true),
            ITest("!\"a string\"", false),
            ITest("!0", true),
            ITest("!1", false),
            ITest("!nil", true),
            // fails for invalid types
            ITest("-\"johnson\"", null, true),
            ITest("-nil", null, true),
            ITest("-true", null, true),

            // binary operations
            ITest("5 + 6", 11.0),
            ITest("\"Hello\" + \"World\"", "HelloWorld"),
            ITest("10 / 2", 5.0),
            ITest("3 - 4", -1.0),
            ITest("5 + 6", 11.0),
            // cannot add different types
            ITest("5 + \"john\"", "5.0john"),
            ITest("nil + \"john\"", "niljohn"),

            // comparators
            ITest("6 > 3", true),
            ITest("6 < 3", false),
            ITest("3 <= 3", true),
            ITest("3 >= 3", true),
            ITest("4 <= 3", false),
            ITest("4 >= 3", true),
            ITest("1 <= 3", true),
            ITest("1 >= 3", false),

            // equality checks
            ITest("3 == 3", true),
            ITest("4 == 3", false),
            ITest("\"3\" == 3", false),
            ITest("3 != 3", false),
            ITest("4 != 3", true),
            ITest("\"3\" != 3", true),

            // logical and or
            ITest("true or false", true),
            ITest("false or true", true),
            ITest("true or true", true),
            ITest("false or false", false),
            ITest("true and false", false),
            ITest("false and true", false),
            ITest("true and true", true),
            ITest("false and false", false),
            ITest("\"\" or false", false),
            ITest("\"ello\" or false", true),
            ITest("false or 6 == \" something\"", false),

            // test ternary operations
            ITest("true ? 1 : 2", 1.0),
            ITest("false ? 1 : 2", 2.0),
        )
        val interpreter = Interpreter()

        for (test in tests) {
            var result: Any? = null
            var raisedError = false

            try {
                result = interpreter.runExpression(test.input)
            } catch(e: Exception) {
                raisedError = true
            }

            assertEquals(test.raisesError, raisedError)
            assertEquals(test.expected, result)
        }
    }

    @Test fun testInterpreterStmt() {
        val tests = listOf<ITest>(
            // variable examples
            ITest("var tree = 8;", null),
            // no semi colon
            ITest("var tree = 8", null, true),
            
            ITest("print 4 / 6", null, true),

            // expression examples
            ITest("5 * 3;", 15.0),
            ITest("5 * 3", null, true),

            ITest("{var snack = 4; print 4;", null, true),
        )
        val interpreter = Interpreter()

        for (test in tests) {
            var result: Any? = null
            var raisedError = false

            try {
                result = interpreter.runStatement(test.input)
            } catch(e: Exception) {
                raisedError = true
            }

            assertEquals(test.raisesError, raisedError)
            assertEquals(test.expected, result)
        }
    }

    @Test fun testInterpreterPrinter() {
        var printedMessage: Any? = ""
        fun fakePrint(message: Any?): Unit {
            printedMessage = message
        }

        val interpreter = Interpreter(::fakePrint)
        interpreter.run("print 3;")
        assertEquals(
            "3.0",
            printedMessage,
        )
    }

    @Test fun testInterpreterBlock() {
        val printedMessages = mutableListOf<Any?>()
        fun fakePrint(message: Any?): Unit {
            printedMessages.add(message)
        }

        val interpreter = Interpreter(::fakePrint)
        interpreter.run("""
			var a = "global a";
			var b = "global b";
			var c = "global c";
			{
			  var a = "outer a";
			  var b = "outer b";
			  {
			    var a = "inner a";
			    print a;
			    print b;
			    print c;
			  }
			  print a;
			  print b;
			  print c;
			}
			print a;
			print b;
			print c;
        """)
        assertEquals(
            mutableListOf<Any?>(
                "inner a",
                "outer b",
                "global c",
                "outer a",
                "outer b",
                "global c",
                "global a",
                "global b",
                "global c",
            ),
            printedMessages,
        )
    }

    @Test fun testInterpreterIf() {
        val printedMessages = mutableListOf<Any?>()
        fun fakePrint(message: Any?): Unit {
            printedMessages.add(message)
        }
        
        Interpreter(::fakePrint).run("""
            var a = 4;
            if (a > 3) {
                print "a";
            } else {
                print "b";
            }

            if (a < 3) {
                print "c";
            } else {
                print "d";
            }
        """)

        assertEquals(
            mutableListOf<Any?>("a", "d"),
            printedMessages,
        )
    }

    @Test fun testInterpreterLogical() {
        var printedMessage: Any? = null
        fun fakePrint(message: Any?): Unit {
            printedMessage = message
        }

        Interpreter(::fakePrint).run("""
            print(true and false or true and false);
        """)

        assertEquals(
            "false",
            printedMessage,
        )
    }

    @Test fun testInterpreterWhile() {
        val printedMessages = mutableListOf<Any?>()
        fun fakePrint(message: Any?): Unit {
            printedMessages.add(message)
        }

        Interpreter(::fakePrint).run("""
            var x = 0;
            while (x < 3) {
                print x;
                x = x + 1;
            }
        """)

        assertEquals(
            mutableListOf<Any?>("0.0", "1.0", "2.0"),
            printedMessages,
        )
    }

    @Test fun testInterpreterFor() {
        val printedMessages = mutableListOf<Any?>()
        fun fakePrint(message: Any?): Unit {
            printedMessages.add(message)
        }

        Interpreter(::fakePrint).run("""
            for (var i = 0; i < 3; i = i + 1) {
                print i;
            }
        """)

        assertEquals(
            mutableListOf<Any?>("0.0", "1.0", "2.0"),
            printedMessages,
        )
    }

    @Test fun testInterpreterSimpleFunction() {
        var printedMessage:Any? = null
        fun fakePrint(message: Any?): Unit {
            printedMessage = message
        }

        Interpreter(::fakePrint).run("""
            fun test() {
                print "hello world";
            }

            test();
        """)

        assertEquals(
            "hello world",
            printedMessage,
        )
    }

    @Test fun testInterpreterFunctionReturn() {
        var printedMessage:Any? = null
        fun fakePrint(message: Any?): Unit {
            printedMessage = message
        }

        Interpreter(::fakePrint).run("""
            fun sum(a, b) {
                return a + b;
            }

            print sum(3, 4);
        """)

        assertEquals(
            "7.0",
            printedMessage,
        )
    }

    @Test fun testInterpreterCallNonFunction() {
        var errored = false;
        try {
            Interpreter().run("""
                var test = "hello world";
                test();
            """)
        } catch(e: RuntimeError) {
            errored = true
        }

        assertTrue(errored)
    }

    @Test fun testInterpreterFunctionArityCheck() {
        var errored = false;
        try {
            Interpreter().run("""
                fun test(a, b) {
                    print("hi");
                }
                test(1);
            """)
        } catch(e: RuntimeError) {
            errored = true
        }

        assertTrue(errored)

        errored = false;
        try {
            Interpreter().run("""
                fun test(a, b) {
                    print("hi");
                }
                test(1, 2, 3);
            """)
        } catch(e: RuntimeError) {
            errored = true
        }

        assertTrue(errored)
    }

    @Test fun testInterpreterFunctionAsReference() {
        var printedMessage:Any? = null
        fun fakePrint(message: Any?): Unit {
            printedMessage = message
        }

        Interpreter(::fakePrint).run("""
            fun a(f) {
                f();
            }

            fun b() {
                print "hello world";
            }

            a(b);
        """)

        assertEquals(
            "hello world",
            printedMessage,
        )
    }

    @Test fun testFunctionEnvironments() {
        var printedMessages = mutableListOf<Any?>()
        fun fakePrint(message: Any?): Unit {
            printedMessages.add(message)
        }

        Interpreter(::fakePrint).run("""
            fun count(n) {
                if (n > 1) count(n - 1);
                print n;
            }
            count(3);
        """)

        assertEquals(
            mutableListOf<Any?>("1.0", "2.0", "3.0"),
            printedMessages,
        )
    }

    @Test fun testFunctionClosures() {
        var printedMessages = mutableListOf<Any?>()
        fun fakePrint(message: Any?): Unit {
            printedMessages.add(message)
        }

        Interpreter(::fakePrint).run("""
            fun makeCounter() {
                var i = 0;
                fun count() {
                    i = i + 1;
                    print i;
                }

                return count;
            }

            var counter = makeCounter();
            counter(); // 1
            counter(); // 2
        """)

        assertEquals(
            mutableListOf<Any?>("1.0", "2.0"),
            printedMessages,
        )
    }

    @Test fun testClock() {
        var printedMessage: String = ""
        fun fakePrint(message: Any?): Unit {
            if (!(message is String)) {
                throw Exception("Expected printed time to be string")
            }
            printedMessage = message
        }

        Interpreter(::fakePrint).run("""
            print clock();
        """)

        val printedTime = printedMessage.toDouble()
        val timeDiff = kotlin.math.abs(printedTime - (System.currentTimeMillis() / 1000.0))
        // Assert difference in times less than 10 seconds
        assertTrue(timeDiff < 10.0)
    }
}
