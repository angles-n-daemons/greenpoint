package greenpoint

import greenpoint.scanner.Scanner

class Interpreter {
	fun run(source: String): Any {
        val scanner = Scanner(source)
        val tokens = scanner.scanTokens()
        
        for (token in tokens) {
            println(token)
        }
        return source
	}
}
