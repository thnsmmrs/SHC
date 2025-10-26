/**
 * Pretty Printer for SHC language.
 * Formats AST back to source using K&R-ish braces.
 * Param/decl syntax:  name : ^*type
 */
public class PrettyPrinter {
    /** Output builder */
    private StringBuilder output;

    /** Current indentation level */
    private int indentLevel;

    /** Number of spaces per indent level */
    private static final int INDENT_SIZE = 4;

    public PrettyPrinter() {
        this.output = new StringBuilder();
        this.indentLevel = 0;
    }

    /** Entry point: pretty print a function. */
    public String prettyPrint(Function function) {
        output = new StringBuilder();
        indentLevel = 0;
        printFunction(function);
        return output.toString();
    }

    /** Print a function definition. */
    private void printFunction(Function function) {
        append("fun ");
        append(function.getName());
        append("(");

        // params as: name : ^*type
        Variable[] params = function.getArguments();
        if (params != null && params.length > 0) {
            for (int i = 0; i < params.length; i++) {
                if (i > 0) append(", ");
                append(params[i].getName());
                append(" : ");
                append(hats(params[i].getNReferences()));
                printType(function.getArguments()[i].getType());
            }
        }

        append(") : ");
        // include pointer depth on function return type
        append(hats(function.getNReturnReferences()));
        printType(function.getReturnType());
        append(" {");
        newline();

        increaseIndent();

        // locals as lines: name : ^*type;
        Variable[] localVars = function.getLocalVariables();
        if (localVars != null && localVars.length > 0) {
            for (Variable var : localVars) {
                indent();
                printVariable(var);
                newline();
            }
            if (function.getBody() != null && function.getBody().length > 0) newline();
        }

        Statement[] body = function.getBody();
        if (body != null) {
            for (Statement stmt : body) printStatement(stmt);
        }

        decreaseIndent();

        append("}");
        newline();
    }

    /** Print a variable declaration line: name : ^*type; */
    private void printVariable(Variable variable) {
        append(variable.getName());
        append(" : ");
        append(hats(variable.getNReferences()));
        printType(variable.getType());
        append(";");
    }

    /** Print a statement. */
    private void printStatement(Statement statement) {
        if (statement instanceof Statement.If) {
            printIfStatement((Statement.If) statement);
        } else if (statement instanceof Statement.Loop) {
            printLoopStatement((Statement.Loop) statement);
        } else if (statement instanceof Statement.Decl) {
            printDeclStatement((Statement.Decl) statement);
        } else if (statement instanceof Statement.Call) {
            printCallStatement((Statement.Call) statement);
        } else if (statement instanceof Statement.Assign a) {
            indent();
            Assignment asg = a.assignment();
            Factor.Var lhs = asg.getAssignee();
            append(hats(lhs.getNReferences()));
            append(lhs.getVariable().getName());
            append(" = ");
            append(printOr(asg.getValue()));
            append(";");
            newline();
        } else if (statement instanceof Statement.Jump) {
            printJumpStatement((Statement.Jump) statement);
        } else {
            indent();
            append("<unknown stmt>");
            newline();
        }
    }

    /** Print an if statement. */
    private void printIfStatement(Statement.If ifStmt) {
        indent();
        append("if (");
        append(ifStmt.cond() != null ? printFactor(ifStmt.cond()) : "<condition>");
        append(") {");
        newline();

        increaseIndent();
        if (ifStmt.body() != null) {
            for (Statement stmt : ifStmt.body()) printStatement(stmt);
        }
        decreaseIndent();

        indent();
        append("}");
        if (ifStmt.otherBody() != null && ifStmt.otherBody().length > 0) {
            append(" else {");
            newline();
            increaseIndent();
            for (Statement stmt : ifStmt.otherBody()) printStatement(stmt);
            decreaseIndent();
            indent();
            append("}");
        }
        newline();
    }

    /** Print a loop statement. */
    private void printLoopStatement(Statement.Loop loopStmt) {
        indent();
        append("while (");
        append(loopStmt.cond() != null ? printFactor(loopStmt.cond()) : "<condition>");
        append(") {");
        newline();

        increaseIndent();
        if (loopStmt.body() != null) {
            for (Statement stmt : loopStmt.body()) printStatement(stmt);
        }
        decreaseIndent();

        indent();
        append("}");
        newline();
    }

    /** Print a declaration statement. */
    private void printDeclStatement(Statement.Decl declStmt) {
        indent();
        printVariable(declStmt.variable());
        newline();
    }

    /** Print a function call statement. */
    private void printCallStatement(Statement.Call callStmt) {
        indent();
        append(callStmt.function().getName());
        append("(");
        Expression[] args = callStmt.arguments();
        if (args != null && args.length > 0) {
            for (int i = 0; i < args.length; i++) {
                if (i > 0) append(", ");
                append(printExpr(args[i]));
            }
        }
        append(");");
        newline();
    }

    /** Print a jump statement (return, break, continue). */
    private void printJumpStatement(Statement.Jump jumpStmt) {
        indent();
        switch (jumpStmt.type()) {
            case RETURN:
                if (jumpStmt.hasValue()) {
                    append("return ");
                    append(printExpr(jumpStmt.value()));
                    append(";");
                } else {
                    append("return;");
                }
                break;
            case BREAK:
                append("break;");
                break;
            case CONTINUE:
                append("continue;");
                break;
            default:
                append("<unknown jump>;");
                break;
        }
        newline();
    }

    /** Print a base type token. */
    private void printType(SHC type) {
        switch (type) {
            case INT:  append("int");  break;
            case CHAR: append("char"); break;
            case VOID: append("void"); break;
            default:   append(type.toString().toLowerCase());
        }
    }

    // ===================== Expression printers =====================
    private String printExpr(Expression e) {
        AdditiveExpression a = e.getExpressions()[0];
        return printAdd(a);
    }

    private String printOr(OrExpression o) {
        if (!o.hasLeft()) return printAnd(o.getRight());
        return "(" + printOr(o.getLeft()) + " || " + printAnd(o.getRight()) + ")";
    }

    private String printAnd(AndExpression a) {
        if (!a.hasLeft()) return printEq(a.getRight());
        return "(" + printAnd(a.getLeft()) + " && " + printEq(a.getRight()) + ")";
    }

    private String printEq(EqualityExpression e) {
        if (!e.hasLeft()) return printRel(e.getRight());
        String op = switch (e.getOperator()) {
            case EQUAL -> "==";
            case NEQ   -> "!=";
            default    -> e.getOperator().toString();
        };
        return "(" + printEq(e.getLeft()) + " " + op + " " + printRel(e.getRight()) + ")";
    }

    private String printRel(RelationalExpression r) {
        if (!r.hasLeft()) return printAdd(r.getRight());
        String op = switch (r.getOperator()) {
            case LESS    -> "<";
            case LEQ     -> "<=";
            case GREATER -> ">";
            case GEQ     -> ">=";
            default      -> r.getOperator().toString();
        };
        return "(" + printRel(r.getLeft()) + " " + op + " " + printAdd(r.getRight()) + ")";
    }

    private String printAdd(AdditiveExpression a) {
        if (!a.hasLeft()) return printMul(a.getRight());
        String op = switch (a.getOperator()) {
            case ADD      -> "+";
            case SUBTRACT -> "-";
            default       -> a.getOperator().toString();
        };
        return "(" + printAdd(a.getLeft()) + " " + op + " " + printMul(a.getRight()) + ")";
    }

    private String printMul(MultiplicativeExpression m) {
        if (!m.hasLeft()) return printUnary(m.getRight());
        String op = switch (m.getOperator()) {
            case MULTIPLY -> "*";
            case DIVIDE   -> "/";
            case MOD      -> "%";
            default       -> m.getOperator().toString();
        };
        return "(" + printMul(m.getLeft()) + " " + op + " " + printUnary(m.getRight()) + ")";
    }

    private String printUnary(UnaryExpression u) {
        if (u.hasOperator()) {
            String op = switch (u.getOperator()) {
                case ADD      -> "+";
                case SUBTRACT -> "-";
                case NOT      -> "!";
                case CARET    -> "^";
                case MULTIPLY -> "*";
                default       -> u.getOperator().toString();
            };
            return "(" + op + printUnary(u.getUnaryExpression()) + ")";
        }
        return printFactor(u.getFactor());
    }

    private String printFactor(Factor f) {
        if (f instanceof Factor.Const c) return Integer.toString(c.constant());
        if (f instanceof Factor.Str s)   return "\"" + s.string() + "\"";
        if (f instanceof Factor.Parentheses p) return "(" + printExpr(p.expression()) + ")";
        if (f instanceof Factor.Var v)   return hats(v.getNReferences()) + v.getVariable().getName();
        if (f instanceof Factor.Call c) {
            StringBuilder sb = new StringBuilder();
            sb.append(c.fun().getName()).append("(");
            for (int i = 0; i < c.arguments().length; i++) {
                if (i > 0) sb.append(", ");
                sb.append(printExpr(c.arguments()[i]));
            }
            sb.append(")");
            return sb.toString();
        }
        return "<factor?>";
    }

    // ===================== tiny utils =====================

    private static String hats(int n) { return "^".repeat(Math.max(0, n)); }

    private void append(String str) { output.append(str); }

    private void indent() { for (int i = 0; i < indentLevel * INDENT_SIZE; i++) output.append(' '); }

    private void newline() { output.append('\n'); }

    private void increaseIndent() { indentLevel++; }

    private void decreaseIndent() { if (indentLevel > 0) indentLevel--; }
}