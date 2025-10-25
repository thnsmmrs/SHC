enum SHC {
    // Keywords
    FUN,        // "fun"
    IF,         // "if"
    ELSE,       // "else"
    WHILE,      // "while"
    RETURN,     // "return"
    BREAK,      // "break"
    CONTINUE,   // "continue"
    VOID,       // "void"
    INT,        // "int"
    CHAR,       // "char"

    // Logical operators
    AND,        // "&&"
    OR,         // "||"
    NOT,        // "!"

    // Arithmetic operators
    ADD,        // "+"
    SUBTRACT,   // "-"
    MULTIPLY,   // "*"  (also unary deref)
    DIVIDE,     // "/"
    MOD,        // "%"

    // Assignment and comparisons
    ASSIGN,     // "="
    EQUAL,      // "=="
    NEQ,        // "!="
    LESS,       // "<"
    GREATER,    // ">"
    LEQ,        // "<="
    GEQ,        // ">="

    // Unary-only operator (address-of)
    AMP,        // "&"   (unary address-of only; NOT bitwise and)

    // Punctuation
    COLON,      // ":"
    SEMICOLON,  // ";"
    COMMA,      // ","
    LPAREN,     // "("
    RPAREN,     // ")"
    LSQUARE,    // "["
    RSQUARE,    // "]"
    LCURL,      // "{"
    RCURL,      // "}"

    // Identifiers and literals
    ID,
    STRING_LITERAL,
    INT_LITERAL,
    CHAR_LITERAL,
    BOOL_LITERAL, // produced from "true"/"false"
	TRUE,
	FALSE,

    // Control tokens
    EOS,        // end of source
    ERROR
}
