import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.NoSuchElementException;

class SHCScanner {
  private final String RED = "\u001B[31m";
  private final String RESET = "\u001B[0m";
  // Error message color formatting

  private java.util.Scanner input;
  // Main.java scanner

  private String currentLine = "";
  private String currentTokenLine = "";
  // Current vars

  private int lineIdx = 0;
  private int charIdx = 0;
  private int linesScanned = 0;
  private int charsScanned = 0;
  // index positions

  private SHC currentToken;
  // current token

  private String latestIdentifier;
  private int latestInt;
  private int latestChar;
  private String latestString;

  // Keywords as defined in SHC
  private static final String[] KEYWORDS = {
      "fun", "if", "else", "while", "return", "break",
      "continue", "void", "int", "char", "true", "false"
  };

  // Keyword Tokens
  private static final SHC[] KEYWORD_TOKENS = {
      SHC.FUN, SHC.IF, SHC.ELSE, SHC.WHILE, SHC.RETURN, SHC.BREAK,
      SHC.CONTINUE, SHC.VOID, SHC.INT, SHC.CHAR, SHC.TRUE, SHC.FALSE
  };

  // two-char first, always
  private static final String[] SYMBOLS = {
      "==", "!=", "<=", ">=", "&&", "||", // 0..5
      "+", "-", "*", "/", "%", // 6..10
      "=", "<", ">", "!", "&", "^", // 11..16
      "(", ")", "[", "]", "{", "}", // 17..22
      ",", ";", ":" // 23..25
  };

  private static final SHC[] SYMBOL_TOKENS = {
      SHC.EQUAL, SHC.NEQ, SHC.LEQ, SHC.GEQ, SHC.AND, SHC.OR, // 0..5
      SHC.ADD, SHC.SUBTRACT, SHC.MULTIPLY, SHC.DIVIDE, SHC.MOD, // 6..10
      SHC.ASSIGN, SHC.LESS, SHC.GREATER, SHC.NOT, SHC.CARET, SHC.CARET, // 11..16
      SHC.LPAREN, SHC.RPAREN, SHC.LSQUARE, SHC.RSQUARE, SHC.LCURL, SHC.RCURL, // 17..22
      SHC.COMMA, SHC.SEMICOLON, SHC.COLON // 23..25
  };

  // Constructor for scanner class
  SHCScanner(String filename) {
    try {
      this.input = new java.util.Scanner(new FileReader(filename));
      nextLine();
      nextToken();
    } catch (FileNotFoundException e) {
      System.out.println(RED + "ERROR: " + RESET + "Could not read `" + filename + "`");
      this.currentToken = SHC.ERROR;
    }
  }

  private void nextLine() {
    try {
      do {
        this.currentLine = this.input.nextLine(); // may throw NoSuchElementException
        linesScanned++;
        charsScanned = 0;
      } while (this.currentLine.isEmpty());
    } catch (NoSuchElementException e) {
      try {
        this.input.close();
      } catch (IllegalStateException ignored) {
      }
      this.input = null;
      this.currentLine = "";
      this.charsScanned = 0;
    } catch (IllegalStateException e) {
      // scanner already closed
    }
  }

  private boolean atEOL() {
    return charsScanned >= currentLine.length();
  }

  private char cur() {
    return currentLine.charAt(charsScanned);
  }

  private char peek(int ahead) {
    int i = charsScanned + ahead;
    return (i < currentLine.length()) ? currentLine.charAt(i) : '\0';
  }

  private boolean startsWithAt(String s) {
    return currentLine.startsWith(s, charsScanned);
  }

  private void advance(int n) {
    charsScanned += n;
  }

  private void skipWhitespaceAndComments() {
    boolean progressed;
    do {
      progressed = false;
      while (!atEOL() && Character.isWhitespace(cur())) {
        advance(1);
        progressed = true;
      }
      if (!atEOL() && cur() == '/' && peek(1) == '/') {
        charsScanned = currentLine.length();
        progressed = true;
      }
      if (!atEOL() && cur() == '/' && peek(1) == '*') {
        advance(2);
        boolean closed = false;
        while (true) {
          while (!atEOL()) {
            if (cur() == '*' && peek(1) == '/') {
              advance(2);
              closed = true;
              break;
            }
            advance(1);
          }
          if (closed)
            break;
          if (currentLine.isEmpty() && atEOL()) {
            System.out.println(RED + "ERROR: " + RESET + "Unclosed block comment at line " + lineIdx);
            this.currentToken = SHC.ERROR;
            return;
          }
          nextLine();
        }
        progressed = true;
      }
      if (atEOL() && this.input != null) {
        nextLine();
        progressed = true;
      }
    } while (progressed && this.input != null);
  }

  private void nextString() {
    // assumes current char is '"'
    advance(1); // skip opening "
    var builder = new StringBuilder();
    boolean error = false;

    while (true) {
      if (atEOL()) {
        // newline before closing quote → error
        System.out.println(RED + "ERROR: " + RESET + "Unclosed string at line " + lineIdx);
        currentToken = SHC.ERROR;
        return;
      }
      char c = cur();
      if (c == '"') {
        advance(1); // consume closing "
        latestString = builder.toString();
        currentToken = SHC.STRING_LITERAL;
        return;
      }
      if (c == '\\') {
        if (charsScanned + 1 >= currentLine.length()) {
          System.out.println(RED + "ERROR: " + RESET + "Bad escape at EOL on line " + lineIdx);
          currentToken = SHC.ERROR;
          return;
        }
        char e = peek(1);
        switch (e) {
          case 'n':
            builder.append('\n');
            advance(2);
            break;
          case 't':
            builder.append('\t');
            advance(2);
            break;
          case 'r':
            builder.append('\r');
            advance(2);
            break;
          case '\\':
            builder.append('\\');
            advance(2);
            break;
          case '\'':
            builder.append('\'');
            advance(2);
            break;
          case '"':
            builder.append('"');
            advance(2);
            break;
          case '0':
            builder.append('\0');
            advance(2);
            break;
          default:
            System.out.println(RED + "ERROR: " + RESET + "Unknown escape \\" + e + " at line " + lineIdx);
            currentToken = SHC.ERROR;
            return;
        }
      } else {
        builder.append(c);
        advance(1);
      }
    }
  }

  // Char: 'a' or escapes; exactly one char, must close
  private void nextCharLiteral() {
    // assumes current char is '\''
    advance(1); // skip opening '
    if (atEOL()) {
      System.out.println(RED + "ERROR: " + RESET + "Empty char literal at line " + lineIdx);
      currentToken = SHC.ERROR;
      return;
    }
    int value;
    if (cur() == '\\') {
      if (charsScanned + 1 >= currentLine.length()) {
        System.out.println(RED + "ERROR: " + RESET + "Bad escape in char at line " + lineIdx);
        currentToken = SHC.ERROR;
        return;
      }
      char e = peek(1);
      switch (e) {
        case 'n':
          value = '\n';
          advance(2);
          break;
        case 't':
          value = '\t';
          advance(2);
          break;
        case 'r':
          value = '\r';
          advance(2);
          break;
        case '\\':
          value = '\\';
          advance(2);
          break;
        case '\'':
          value = '\'';
          advance(2);
          break;
        case '"':
          value = '"';
          advance(2);
          break;
        case '0':
          value = '\0';
          advance(2);
          break;
        default:
          System.out.println(RED + "ERROR: " + RESET + "Unknown char escape \\" + e + " at line " + lineIdx);
          currentToken = SHC.ERROR;
          return;
      }
    } else {
      value = cur();
      advance(1);
    }
    if (atEOL() || (cur() != '\'')) {
      System.out.println(RED + "ERROR: " + RESET + "Unclosed char literal at line " + lineIdx);
      currentToken = SHC.ERROR;
      return;
    }
    advance(1); // consume closing '
    latestChar = value;
    currentToken = SHC.CHAR_LITERAL;
  }

  // Integers: decimal | octal | hex
  private void nextIntLiteral() {
    int start = charsScanned;

    if (cur() == '0') {
      advance(1);
      if (!atEOL() && Character.isDigit(cur())) {
        String bad = currentLine.substring(start, Math.min(currentLine.length(), start + 16));
        System.out
            .println(RED + "ERROR: " + RESET + "Invalid decimal with leading zero: `" + bad + "` at line " + lineIdx);
        currentToken = SHC.ERROR;
        return;
      }
      latestInt = 0;
      currentToken = SHC.INT_LITERAL;
      return;
    }

    if (cur() < '1' || cur() > '9') {
      System.out.println(RED + "ERROR: " + RESET + "Invalid integer start: `" + cur() + "` at line " + lineIdx);
      currentToken = SHC.ERROR;
      return;
    }

    advance(1);
    while (!atEOL() && Character.isDigit(cur()))
      advance(1);

    String lex = currentLine.substring(start, charsScanned);
    try {
      long v = Long.parseLong(lex, 10);
      if (v > Integer.MAX_VALUE)
        throw new NumberFormatException();
      latestInt = (int) v;
      currentToken = SHC.INT_LITERAL;
    } catch (NumberFormatException ex) {
      System.out.println(RED + "ERROR: " + RESET + "Integer too large: `" + lex + "` at line " + lineIdx);
      currentToken = SHC.ERROR;
    }
  }

  // Identifiers or keywords
  private void nextIdentifierOrKeyword() {
    int start = charsScanned;
    if (!isIdentStart(cur())) {
      System.out.println(RED + "ERROR: " + RESET + "Invalid identifier start: " + cur() + " at line " + lineIdx);
      currentToken = SHC.ERROR;
      return;
    }
    advance(1);
    while (!atEOL() && isIdentPart(cur()))
      advance(1);

    String token = currentLine.substring(start, charsScanned);

    // keyword check
    for (int i = 0; i < KEYWORDS.length; i++) {
      if (token.equals(KEYWORDS[i])) {
        currentToken = KEYWORD_TOKENS[i];
        if (currentToken == SHC.TRUE) {
          latestInt = 1;
        } // optional value
        if (currentToken == SHC.FALSE) {
          latestInt = 0;
        }
        return;
      }
    }
    // identifier
    currentToken = SHC.ID;
    latestIdentifier = token;
  }

  private boolean isIdentStart(char c) {
    return c == '_' || Character.isLetter(c);
  }

  private boolean isIdentPart(char c) {
    return c == '_' || Character.isLetterOrDigit(c);
  }

  // Try to consume a symbol at current position (maximal munch)
  private boolean tryNextSymbol() {
    int bestLen = 0;
    SHC found = null;
    for (int i = 0; i < SYMBOLS.length; i++) {
      String s = SYMBOLS[i];
      if (startsWithAt(s) && s.length() > bestLen) {
        bestLen = s.length();
        found = SYMBOL_TOKENS[i];
      }
    }
    if (bestLen != 0) {
      currentToken = found;
      // advance; if we exactly finished the line, move to next line like Mantle
      if (currentLine.length() - charsScanned == bestLen) {
        advance(bestLen);
        // do not auto nextLine() here—let skipWhitespace handle EOL consistently
      } else {
        advance(bestLen);
      }
      return true;
    }
    return false;
  }

  // ===== public driver =====

  public void nextToken() {
    skipWhitespaceAndComments();
    lineIdx = linesScanned;
    charIdx = charsScanned;
    currentTokenLine = currentLine;

    if (this.currentLine.isEmpty() && (this.input == null || (this.input != null && !this.input.hasNextLine()))) {
      this.currentToken = SHC.EOS;
      return;
    }
    if (!this.currentLine.isEmpty() && (this.currentLine.length() - charsScanned == 0)) {
      // EOL: advance line and recurse
      nextLine();
      nextToken();
      return;
    }

    if (!tryNextSymbol()) {
      if (atEOL()) {
        nextLine();
        nextToken();
        return;
      }

      char c = cur();
      if (c == '"') {
        nextString();
      } else if (c == '\'') {
        nextCharLiteral();
      } else if (Character.isDigit(c)) {
        nextIntLiteral();
      } else if (isIdentStart(c)) {
        nextIdentifierOrKeyword();
      } else {
        System.out.println(RED + "ERROR: " + RESET + "Unexpected character: `" + c + "` at line " + lineIdx);
        this.currentToken = SHC.ERROR;
      }
    }
  }

  // ===== getters (Mantle-style) =====

  public SHC currentToken() {
    return this.currentToken;
  }

  public String getId() {
    return this.latestIdentifier;
  }

  public int getInt() {
    return this.latestInt;
  }

  public int getChar() {
    return this.latestChar;
  }

  public String getString() {
    return this.latestString;
  }

  public String getCurrentTokenLine() {
    return currentTokenLine;
  }

  public int getLineIdx() {
    return lineIdx;
  }

  public int getCharIdx() {
    return charIdx;
  }

  public int getEndingLineIdx() {
    return linesScanned;
  }

  public int getEndingCharIdx() {
    return charsScanned;
  }

  public String currentTokenString() {
    String result = this.currentToken().toString();
    if (this.currentToken() == SHC.ID) {
      result += "[" + latestIdentifier + "]";
    } else if (this.currentToken() == SHC.INT_LITERAL) {
      result += "[" + latestInt + "]";
    } else if (this.currentToken() == SHC.STRING_LITERAL) {
      result += "[" + latestString + "]";
    } else if (this.currentToken() == SHC.CHAR_LITERAL) {
      result += "[" + latestChar + "]";
    } else if (this.currentToken() == SHC.TRUE) {
      result += "[true]";
    } else if (this.currentToken() == SHC.FALSE) {
      result += "[false]";
    }
    return result;
  }

}
