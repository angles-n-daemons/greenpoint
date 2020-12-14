package greenpoint.scanner

val KEYWORDS = mutableMapOf<String, TokenType>(
    "and" to TokenType.AND,
    "class" to TokenType.CLASS,
    "else" to TokenType.ELSE,
    "false" to TokenType.FALSE,
    "for" to TokenType.FOR,
    "fun" to TokenType.FUN,
    "if" to TokenType.IF,
    "nil" to TokenType.NIL,
    "or" to TokenType.OR,
    "print" to TokenType.PRINT,
    "return" to TokenType.RETURN,
    "super" to TokenType.SUPER,
    "this" to TokenType.THIS,
    "true" to TokenType.TRUE,
    "var" to TokenType.VAR,
    "while" to TokenType.WHILE,
)

class ScannerError(
    val line: Int,
    val whereAt: String,
    override val message: String,
): Exception(message) {
    override fun toString(): String {
        return "[$line] | $whereAt: $message"
    }
}

class Scanner(val source: String) {
    val tokens = mutableListOf<Token>()
    var start = 0
    var current = 0
    var line = 1

    fun scanTokens(): List<Token> {
        while (!isAtEnd()) {
            val token = scanTokenMaybe()
            if (token != null) {
                tokens.add(token)
            }
        }
        return tokens
    }

    fun scanTokenMaybe(): Token? {
        var token: Token? = null
        val c = advance()
        when(c) {
            // Single character tokens
            '(' -> token = newToken(TokenType.LEFT_PAREN)
            ')' -> token = newToken(TokenType.RIGHT_PAREN)
            '{' -> token = newToken(TokenType.LEFT_BRACE)
            '}' -> token = newToken(TokenType.RIGHT_BRACE)
            ',' -> token = newToken(TokenType.COMMA)
            '.' -> token = newToken(TokenType.DOT)
            '+' -> token = newToken(TokenType.PLUS)
            '-' -> token = newToken(TokenType.MINUS)
            ';' -> token = newToken(TokenType.SEMICOLON)
            '*' -> token = newToken(TokenType.STAR)
            // single/double character tokens
            '!' -> {
                token = newToken(if (match('=')) TokenType.BANG_EQUAL else TokenType.BANG)
            }
            '=' -> {
                token = newToken(if (match('=')) TokenType.EQUAL_EQUAL else TokenType.EQUAL)
            }
            '>' -> {
                token = newToken(if (match('=')) TokenType.GREATER_EQUAL else TokenType.GREATER)
            }
            '<' -> {
                token = newToken(if (match('=')) TokenType.LESS_EQUAL else TokenType.LESS)
            }
            // edge case for slash, consume comment if double slash
            '/' -> token = scanSlash()
            // increment line counter for newlines
            '\n' -> line++
            // do nothing for whitespace
            ' ', '\t', '\r' -> {}
            // scan string literal if open quote
            '"' -> token = scanString()
            // scan numeric value if digit seen
            in CharRange('0', '9') -> token = scanNumber()
            // scan identifier if alpha character seen
            in CharRange('a', 'z'), in CharRange('A', 'Z') -> token = scanIdentifier()
            // Unexpected character, bail
            else -> throw(ScannerError(line, "", "Unexpected character $c"))
        }
        start = current
        return token
    }

    fun newToken(type: TokenType): Token {
        return Token(type, currentTokenStr(), null, line)
    }

    fun advance(): Char {
        current++
        return source.get(current-1)
    }

    fun match(c: Char): Boolean {
        if (isAtEnd()) return false
        if (source.get(current) != c) return false

        current++
        return true
    }

    fun peek(): Char {
        if (isAtEnd()) return '\u0000'
        return source.get(current)
    }

    fun peekNext(): Char {
        if (current + 1 >= source.length) return '\u0000'
        return source.get(current + 1)
    }

    fun scanSlash(): Token? {
        val lineBeginning = line
        if (match('/')) {
            // a comment goes until the end of the line or file
            while (peek() != '\n' && !isAtEnd()) {
                advance()
            }
        } else if (match('*')) {
            while (!(peek() == '*' && peekNext() == '/') && !isAtEnd()) {
                if (peek() == '\n') {
                    line++
                }
                advance()
            }

            if (isAtEnd()) {
                throw(ScannerError(line, "", "Unterminated multiline comment $lineBeginning"))
            }

            advance()
            advance()
        } else {
            return newToken(TokenType.SLASH)
        }
        return null
    }

    fun scanString(): Token {
        val lineBeginning = line
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n') line++
            advance()
        }
        
        if (isAtEnd()) {
            throw(ScannerError(line, "", "Unterminated string starting line $lineBeginning"))
        }

        // pass closing "
        advance()
        return Token(
            TokenType.STRING,
            currentTokenStr(),
            source.substring(start + 1, current - 1),
            line,
        )
    }

    fun scanNumber(): Token {
        while(isDigit(peek())) advance()

        // pull out decimal point
        if(peek() == '.' && isDigit(peekNext())) {
            // pass the .
            advance()

            while(isDigit(peek())) advance()
        }

        val numberStr = currentTokenStr()
        return Token(
            TokenType.NUMBER,
            numberStr,
            numberStr.toDouble(),
            line,
        )


    }

    fun scanIdentifier(): Token {
        while (isAlphaNumeric(peek())) advance()

        val text = currentTokenStr()
        var type = KEYWORDS.get(text)
        if (type == null) {
            type = TokenType.IDENTIFIER
        }
        return newToken(type)
    }

    fun isAlphaNumeric(c: Char): Boolean {
        return isAlpha(c) || isDigit(c)
    }

    fun isAlpha(c: Char): Boolean {
        return c in 'a'..'z' || c in 'A'..'Z' || c == '_'
    }

    fun currentTokenStr(): String {
        return source.substring(start, current)
    }

    fun isDigit(c: Char): Boolean {
        return c >= '0' && c <= '9'
    }

    fun isAtEnd(): Boolean {
        return current >= source.length
    }
}
