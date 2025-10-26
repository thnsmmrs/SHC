import java.util.ArrayList;

public final class Parser {
    private final SHCScanner sc;
    private final Reporter rep;

    public Parser(String filename) {
        this.sc = new SHCScanner(filename);
        if (sc.currentToken() == SHC.ERROR) System.exit(0);
        this.rep = new Reporter(filename);
    }

    // ===================== translation-unit =====================

    public ArrayList<Function> parseProgram() {
        ArrayList<Function> funs = new ArrayList<>();
        while (sc.currentToken() != SHC.EOS) {
            if (sc.currentToken() == SHC.FUN) {
                funs.add(parseFunction());
            } else if (sc.currentToken() == SHC.ID) {
                // top-level declaration line: name : ^*type [= expr] ';' (globals discarded)
                parseTopLevelDeclLine();
            } else {
                error("Expected 'fun' or a declaration, found " + sc.currentTokenString(),
                        sc.getLineIdx(), sc.getCharIdx());
            }
        }
        return funs;
    }

    // ===================== external-declaration pieces =====================

    // fun name '(' param-list-opt ')' ':' return-type compound-statement
    private Function parseFunction() {
        int line = sc.getLineIdx(), col = sc.getCharIdx();
        expect(SHC.FUN, "fun");
        String name = expectId("function name");
        expect(SHC.LPAREN, "(");
        Variable[] params = parseParamList(); // name : type
        expect(SHC.RPAREN, ")");
        expect(SHC.COLON, ":");

        // return-type := type-spec | void
        SHC retBase;
        int retStars = 0;
        if (sc.currentToken() == SHC.VOID) {
            retBase = SHC.VOID;
            sc.nextToken();
        } else {
            TypeSpec ts = parseTypeSpec(); // '^'* (int|char|void)
            retBase = ts.base;
            retStars = ts.hats;            // store pointer depth for return
        }

        expect(SHC.LCURL, "{");
        // statements handle decl lines; we don’t pre-scan locals anymore
        Statement[] body = parseStatementSeq();
        expect(SHC.RCURL, "}");

        // include pointer depth on return type
        return new Function(retBase, retStars, name, params, new Variable[0], body, line, col);
    }

    // ===================== types (name : ^*base) =====================

    private static final class TypeSpec {
        final SHC base; final int hats;
        TypeSpec(SHC b, int h) { base = b; hats = h; }
    }

    // type-spec := '^'* base-type
    private TypeSpec parseTypeSpec() {
        int hats = 0;
        while (sc.currentToken() == SHC.CARET) { hats++; sc.nextToken(); }
        SHC base = parseBaseTypeToken();
        return new TypeSpec(base, hats);
    }

    private SHC parseBaseTypeToken() {
        SHC t = sc.currentToken();
        if (t == SHC.INT || t == SHC.CHAR || t == SHC.VOID) { sc.nextToken(); return t; }
        error("Expected base type (int|char|void)", sc.getLineIdx(), sc.getCharIdx());
        return SHC.VOID;
    }

    // ===================== parameters =====================

    // parameter-list-opt := ε | parameter (',' parameter)*
    // parameter := ID ':' type-spec
    private Variable[] parseParamList() {
        ArrayList<Variable> ps = new ArrayList<>();
        if (sc.currentToken() == SHC.RPAREN) return ps.toArray(Variable[]::new);
        do {
            int line = sc.getLineIdx(), col = sc.getCharIdx();
            String name = expectId("parameter name");
            expect(SHC.COLON, ":");
            TypeSpec ts = parseTypeSpec();
            ps.add(new Variable(name, ts.base, ts.hats, line, col));
        } while (tryEat(SHC.COMMA));
        return ps.toArray(Variable[]::new);
    }

    // ===================== top-level decls (discarded) =====================

    // decl-line := name ':' type [= expr] ';'   (no multi-decls allowed)
    private void parseTopLevelDeclLine() {
        ArrayList<Variable> sink = new ArrayList<>();
        parseDeclLineStartingWith(expectId("identifier"), sink);
        // parsed globals discarded
    }

    // shared for block-level decl lines too
    private void parseDeclLineStartingWith(String firstName, ArrayList<Variable> out) {
        int line0 = sc.getLineIdx(), col0 = sc.getCharIdx();
        expect(SHC.COLON, ":");
        TypeSpec ts = parseTypeSpec();
        if (tryEat(SHC.ASSIGN)) parseExpression(); // ignore initializer
        out.add(new Variable(firstName, ts.base, ts.hats, line0, col0));

        // Disallow commas for “no multiple declarations at once”
        if (sc.currentToken() == SHC.COMMA) {
            error("Multiple declarations on one line are not allowed. Use separate lines.", sc.getLineIdx(), sc.getCharIdx());
        }
        expect(SHC.SEMICOLON, ";");
    }

    // ===================== statements =====================

    private Statement[] parseStatementSeq() {
        ArrayList<Statement> list = new ArrayList<>();
        while (isStmtStart(sc.currentToken())) list.add(parseStatement());
        return list.toArray(Statement[]::new);
    }

    private boolean isStmtStart(SHC t) {
        return t == SHC.IF || t == SHC.WHILE || t == SHC.BREAK || t == SHC.CONTINUE || t == SHC.RETURN
                || t == SHC.ID || t == SHC.CARET || t == SHC.LCURL || t == SHC.SEMICOLON || t == SHC.LPAREN
                || t == SHC.INT_LITERAL || t == SHC.CHAR_LITERAL || t == SHC.STRING_LITERAL || t == SHC.TRUE || t == SHC.FALSE;
    }

    private Statement parseStatement() {
        int line = sc.getLineIdx(), col = sc.getCharIdx();
        switch (sc.currentToken()) {
            case LCURL -> { return parseCompoundAsStmt(); }
            case IF -> {
                sc.nextToken();
                expect(SHC.LPAREN, "(");
                Expression ce = parseExpression();
                Factor cond = new Factor.Parentheses(ce, line, col);
                expect(SHC.RPAREN, ")");
                Statement[] thenBody = parseStmtOrBlock();
                Statement[] elseBody = new Statement[0];
                if (tryEat(SHC.ELSE)) elseBody = parseStmtOrBlock();
                return new Statement.If(cond, thenBody, elseBody, line, col);
            }
            case WHILE -> {
                sc.nextToken();
                expect(SHC.LPAREN, "(");
                Expression ce = parseExpression();
                Factor cond = new Factor.Parentheses(ce, line, col);
                expect(SHC.RPAREN, ")");
                Statement[] body = parseStmtOrBlock();
                return new Statement.Loop(cond, body, line, col);
            }
            case BREAK -> { sc.nextToken(); expect(SHC.SEMICOLON, ";"); return new Statement.Jump(SHC.BREAK, line, col); }
            case CONTINUE -> { sc.nextToken(); expect(SHC.SEMICOLON, ";"); return new Statement.Jump(SHC.CONTINUE, line, col); }
            case RETURN -> {
                sc.nextToken();
                Expression value = null;
                if (sc.currentToken() != SHC.SEMICOLON) {
                    value = parseExpression();
                }
                expect(SHC.SEMICOLON, ";");
                return new Statement.Jump(SHC.RETURN, value, line, col);
            }
            default -> {
                // decl line in block: ID ':' ...
                if (sc.currentToken() == SHC.ID) {
                    String firstName = sc.getId();
                    sc.nextToken();
                    if (sc.currentToken() == SHC.COLON) {
                        ArrayList<Variable> vars = new ArrayList<>();
                        parseDeclLineStartingWith(firstName, vars);
                        return new Statement.Decl(vars.get(0), line, col); // single decl enforced
                    }
                    // not a decl; could be call or assignment starting with ID
                    if (sc.currentToken() == SHC.LPAREN) {
                        // call: ID '(' args ')' ';'
                        sc.nextToken();
                        ArrayList<Expression> args = new ArrayList<>();
                        if (sc.currentToken() != SHC.RPAREN) {
                            do { args.add(parseExpression()); } while (tryEat(SHC.COMMA));
                        }
                        expect(SHC.RPAREN, ")");
                        expect(SHC.SEMICOLON, ";");
                        Function callee = new Function(SHC.VOID, firstName, new Variable[0], new Variable[0], new Statement[0], line, col);
                        return new Statement.Call(callee, args.toArray(Expression[]::new), line, col);
                    } else if (sc.currentToken() == SHC.ASSIGN) {
                        // ID '=' expr ';'
                        sc.nextToken();
                        OrExpression rhs = parseOrBase();
                        expect(SHC.SEMICOLON, ";");
                        Variable v = new Variable(firstName, SHC.VOID, 0, line, col);
                        Factor.Var lhs = new Factor.Var(v, 0, line, col);
                        Assignment asg = new Assignment(lhs, rhs, line, col);
                        return new Statement.Assign(asg, line, col);
                    } else {
                        error("Expected ':', '(' or '=' after identifier", sc.getLineIdx(), sc.getCharIdx());
                    }
                }

                // hats-leading assignment: '^'^ ID '=' expr ';'
                if (sc.currentToken() == SHC.CARET) {
                    int hats = 0;
                    while (sc.currentToken() == SHC.CARET) { hats++; sc.nextToken(); }
                    if (sc.currentToken() != SHC.ID) error("Expected identifier after '^' in assignment LHS", sc.getLineIdx(), sc.getCharIdx());
                    String name = sc.getId(); sc.nextToken();
                    expect(SHC.ASSIGN, "=");
                    OrExpression rhs = parseOrBase();
                    expect(SHC.SEMICOLON, ";");
                    Variable v = new Variable(name, SHC.VOID, hats, line, col);
                    Factor.Var lhs = new Factor.Var(v, hats, line, col);
                    Assignment asg = new Assignment(lhs, rhs, line, col);
                    return new Statement.Assign(asg, line, col);
                }

                // empty statement
                if (tryEat(SHC.SEMICOLON)) {
                    return new Statement.Jump(SHC.BREAK, line, col);
                }

                error("Expected declaration, assignment, call, control stmt, or block.", line, col);
                return new Statement.Jump(SHC.BREAK, line, col); // unreachable
            }
        }
    }

    private Statement parseCompoundAsStmt() {
        int line = sc.getLineIdx(), col = sc.getCharIdx();
        expect(SHC.LCURL, "{");
        Statement[] body  = parseStatementSeq();
        expect(SHC.RCURL, "}");
        return body.length > 0 ? body[0] : new Statement.Jump(SHC.BREAK, line, col);
    }

    private Statement[] parseStmtOrBlock() {
        if (sc.currentToken() == SHC.LCURL) {
            expect(SHC.LCURL, "{");
            Statement[] body  = parseStatementSeq();
            expect(SHC.RCURL, "}");
            return body;
        } else {
            return new Statement[]{ parseStatement() };
        }
    }

    // ===================== expressions (by precedence) =====================

    private Expression parseExpression() {
        AdditiveExpression add = parseAddFrom(parseRelFrom(parseEqFrom(parseAndFrom(parseOrBase()))));
        return new Expression(new AdditiveExpression[]{ add }, add.getLineIdx(), add.getCharIdx());
    }

    private OrExpression parseOrBase() {
        AndExpression right = parseAndBase();
        int line = sc.getLineIdx(), col = sc.getCharIdx();
        OrExpression acc = new OrExpression(right, line, col);
        while (sc.currentToken() == SHC.OR) {
            sc.nextToken();
            AndExpression r = parseAndBase();
            acc = new OrExpression(acc, r, line, col);
        }
        return acc;
    }

    private AndExpression parseAndBase() {
        EqualityExpression right = parseEqualityBase();
        int line = sc.getLineIdx(), col = sc.getCharIdx();
        AndExpression acc = new AndExpression(right, line, col);
        while (sc.currentToken() == SHC.AND) {
            sc.nextToken();
            EqualityExpression r = parseEqualityBase();
            acc = new AndExpression(acc, r, line, col);
        }
        return acc;
    }

    private EqualityExpression parseEqualityBase() {
        RelationalExpression right = parseRelationalBase();
        int line = sc.getLineIdx(), col = sc.getCharIdx();
        EqualityExpression acc = new EqualityExpression(right, line, col);
        while (sc.currentToken() == SHC.EQUAL || sc.currentToken() == SHC.NEQ) {
            SHC op = sc.currentToken(); sc.nextToken();
            RelationalExpression r = parseRelationalBase();
            acc = new EqualityExpression(acc, op, r, line, col);
        }
        return acc;
    }

    private RelationalExpression parseRelationalBase() {
        AdditiveExpression right = parseAdditiveBase();
        int line = sc.getLineIdx(), col = sc.getCharIdx();
        RelationalExpression acc = new RelationalExpression(right, line, col);
        while (isRelOp(sc.currentToken())) {
            SHC op = sc.currentToken(); sc.nextToken();
            AdditiveExpression r = parseAdditiveBase();
            acc = new RelationalExpression(acc, op, r, line, col);
        }
        return acc;
    }

    private boolean isRelOp(SHC t) {
        return t == SHC.LESS || t == SHC.LEQ || t == SHC.GREATER || t == SHC.GEQ;
    }

    private AdditiveExpression parseAdditiveBase() {
        MultiplicativeExpression right = parseMultiplicativeBase();
        int line = sc.getLineIdx(), col = sc.getCharIdx();
        AdditiveExpression acc = new AdditiveExpression(right, line, col);
        while (sc.currentToken() == SHC.ADD || sc.currentToken() == SHC.SUBTRACT) {
            SHC op = sc.currentToken(); sc.nextToken();
            MultiplicativeExpression r = parseMultiplicativeBase();
            acc = new AdditiveExpression(acc, op, r, line, col);
        }
        return acc;
    }

    // >>> missing earlier — now implemented
    private MultiplicativeExpression parseMultiplicativeBase() {
        UnaryExpression right = parseUnary();
        int line = sc.getLineIdx(), col = sc.getCharIdx();
        MultiplicativeExpression acc = new MultiplicativeExpression(right, line, col);
        while (sc.currentToken() == SHC.MULTIPLY || sc.currentToken() == SHC.DIVIDE || sc.currentToken() == SHC.MOD) {
            SHC op = sc.currentToken(); sc.nextToken();
            UnaryExpression r = parseUnary();
            acc = new MultiplicativeExpression(acc, op, r, line, col);
        }
        return acc;
    }

    // unary := ('+'|'-'|'^') unary | factor
    private UnaryExpression parseUnary() {
        int line = sc.getLineIdx(), col = sc.getCharIdx();
        if (sc.currentToken() == SHC.ADD || sc.currentToken() == SHC.SUBTRACT || sc.currentToken() == SHC.CARET) {
            SHC op = sc.currentToken(); sc.nextToken();
            UnaryExpression e = parseUnary();
            return new UnaryExpression(op, e, line, col);
        }
        Factor f = parseFactor();
        return new UnaryExpression(f, line, col);
    }

    private Factor parseFactor() {
        int line = sc.getLineIdx(), col = sc.getCharIdx();
        switch (sc.currentToken()) {
            case INT_LITERAL -> { int v = sc.getInt(); sc.nextToken(); return new Factor.Const(v, line, col); }
            case CHAR_LITERAL -> { int v = sc.getChar(); sc.nextToken(); return new Factor.Const(v, line, col); }
            case STRING_LITERAL -> { String s = sc.getString(); sc.nextToken(); return new Factor.Str(s, line, col); }
            case TRUE -> { sc.nextToken(); return new Factor.Const(1, line, col); }
            case FALSE -> { sc.nextToken(); return new Factor.Const(0, line, col); }
            case LPAREN -> {
                sc.nextToken();
                Expression e = parseExpression();
                expect(SHC.RPAREN, ")");
                return new Factor.Parentheses(e, line, col);
            }
            case ID -> {
                String id = sc.getId(); sc.nextToken();
                if (sc.currentToken() == SHC.LPAREN) {
                    sc.nextToken();
                    ArrayList<Expression> args = new ArrayList<>();
                    if (sc.currentToken() != SHC.RPAREN) {
                        do { args.add(parseExpression()); } while (tryEat(SHC.COMMA));
                    }
                    expect(SHC.RPAREN, ")");
                    Function callee = new Function(SHC.VOID, id, new Variable[0], new Variable[0], new Statement[0], line, col);
                    return new Factor.Call(callee, args.toArray(Expression[]::new), line, col);
                } else {
                    Variable v = new Variable(id, SHC.VOID, 0, line, col);
                    return new Factor.Var(v, 0, line, col);
                }
            }
            default -> {
                error("Expected expression", line, col);
                return new Factor.Const(0, line, col); // unreachable
            }
        }
    }

    // Expression wrapper helpers
    private AdditiveExpression parseAddFrom(RelationalExpression re) { return re.getRight(); }
    private AdditiveExpression parseAddFrom(AndExpression x)      { return parseAddFrom(parseRelFrom(parseEqFrom(x))); }
    private AdditiveExpression parseAddFrom(EqualityExpression x) { return parseAddFrom(parseRelFrom(x)); }
    private RelationalExpression parseRelFrom(EqualityExpression ee) { return ee.getRight(); }
    private EqualityExpression parseEqFrom(AndExpression ae) { return ae.getRight(); }
    private AndExpression       parseAndFrom(OrExpression oe){ return oe.getRight(); }

    // ===================== tiny utils =====================

    private boolean tryEat(SHC wanted) { if (sc.currentToken() == wanted) { sc.nextToken(); return true; } return false; }

    private void expect(SHC wanted, String human) {
        if (sc.currentToken() != wanted) error("Expected " + human + " but found " + sc.currentTokenString(), sc.getLineIdx(), sc.getCharIdx());
        sc.nextToken();
    }

    private String expectId(String what) {
        if (sc.currentToken() != SHC.ID) error("Expected " + what, sc.getLineIdx(), sc.getCharIdx());
        String s = sc.getId(); sc.nextToken(); return s;
    }

    private void error(String msg, int line, int col) {
        rep.tokenError(msg, line, col);
        System.exit(0);
    }
}