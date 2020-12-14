package greenpoint.scanner

enum class TokenType {
    // Single character tokens
    LEFT_PAREN, RIGHT_PAREN, LEFT_BRACE, RIGHT_BRACE,
    COMMA, DOT, MINUS, PLUS, SEMICOLON, SLASH, STAR,

    // One-two character tokens
    BANG, BANG_EQUAL,
    EQUAL, EQUAL_EQUAL,
    GREATER, GREATER_EQUAL,
    LESS, LESS_EQUAL,

    // Literals
    IDENTIFIER, STRING, NUMBER,

    // Keywords
    AND, OR,
    TRUE, FALSE,
    IF, ELSE,
    VAR, FUN, RETURN,
    CLASS, THIS, SUPER,
    WHILE, FOR,
    PRINT, EOF, NIL
}

data class Token(
    val type: TokenType,
    val lexeme: String,
    val literal: Any?,
    val line: Int,
)
