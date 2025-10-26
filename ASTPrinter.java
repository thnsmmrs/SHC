public final class ASTPrinter {

    public static String printFunctions(java.util.List<Function> funs) {
        StringBuilder sb = new StringBuilder();
        for (Function f : funs) printFunction(f, sb);
        return sb.toString();
    }

    private static void printFunction(Function f, StringBuilder sb) {
        sb.append("fun ").append(f.getName()).append(" : ").append(f.getReturnType()).append("\n");
        sb.append("  params:\n");
        for (Variable v : f.getArguments()) sb.append("    ").append(v.getType()).append(stars(v.getNReferences())).append(" ").append(v.getName()).append("\n");
        sb.append("  locals:\n");
        for (Variable v : f.getLocalVariables()) sb.append("    ").append(v.getType()).append(stars(v.getNReferences())).append(" ").append(v.getName()).append("\n");
        sb.append("  body:\n");
        for (Statement s : f.getBody()) printStmt("    ", s, sb);
    }

    private static String stars(int n) { return "*".repeat(Math.max(0, n)); }

    private static void printStmt(String ind, Statement s, StringBuilder sb) {
        if (s instanceof Statement.If iff) {
            sb.append(ind).append("if ").append(printFactor(iff.cond())).append("\n");
            sb.append(ind).append("{\n");
            for (Statement t : iff.body()) printStmt(ind + "  ", t, sb);
            sb.append(ind).append("}\n");
            if (iff.otherBody().length > 0) {
                sb.append(ind).append("else {\n");
                for (Statement t : iff.otherBody()) printStmt(ind + "  ", t, sb);
                sb.append(ind).append("}\n");
            }
        } else if (s instanceof Statement.Loop loop) {
            sb.append(ind).append("while ").append(printFactor(loop.cond())).append("\n");
            sb.append(ind).append("{\n");
            for (Statement t : loop.body()) printStmt(ind + "  ", t, sb);
            sb.append(ind).append("}\n");
        } else if (s instanceof Statement.Decl d) {
            sb.append(ind).append("decl ").append(d.variable().getType()).append(stars(d.variable().getNReferences()))
                    .append(" ").append(d.variable().getName()).append("\n");
        } else if (s instanceof Statement.Call c) {
            sb.append(ind).append("call ").append(c.function().getName()).append("(");
            for (int i = 0; i < c.arguments().length; i++) {
                if (i > 0) sb.append(", ");
                sb.append(printExpr(c.arguments()[i]));
            }
            sb.append(")\n");
        } else if (s instanceof Statement.Jump j) {
            sb.append(ind).append(j.type().toString().toLowerCase()).append("\n");
        } else if (s instanceof Statement.Assign a) {
            var asg = a.assignment();
            sb.append(ind)
                    .append("assign ")
                    .append(asg.hasAssignee() ? asg.getAssignee().getVariable().getName() : "<lhs?>")
                    .append(" = ")
                    .append("<or-expr>") // you didn’t expose printers for OrExpression; stub is fine
                    .append("\n");
        } else {
            sb.append(ind).append("<unknown stmt>\n");
        }
    }

    private static String printExpr(Expression e) {
        AdditiveExpression a = e.getExpressions()[0];
        return printAdd(a);
    }
    private static String printAdd(AdditiveExpression a) {
        if (!a.hasLeft()) return printMul(a.getRight());
        return "(" + printAdd(a.getLeft()) + " " + a.getOperator() + " " + printMul(a.getRight()) + ")";
    }
    private static String printMul(MultiplicativeExpression m) {
        if (!m.hasLeft()) return printUnary(m.getRight());
        return "(" + printMul(m.getLeft()) + " " + m.getOperator() + " " + printUnary(m.getRight()) + ")";
    }
    private static String printUnary(UnaryExpression u) {
        if (u.hasOperator()) return "(" + u.getOperator() + printUnary(u.getUnaryExpression()) + ")";
        return printFactor(u.getFactor());
    }
    private static String printFactor(Factor f) {
        if (f instanceof Factor.Const c) return Integer.toString(c.constant());
        if (f instanceof Factor.Str s) return "\"" + s.string() + "\"";
        if (f instanceof Factor.Parentheses p) return "(" + printExpr(p.expression()) + ")";
        if (f instanceof Factor.Var v) return v.toString(); // uses Variable.toString()
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
}