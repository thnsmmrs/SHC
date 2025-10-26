public sealed interface Factor permits Factor.Var, Factor.Const, Factor.Str, Factor.Parentheses, Factor.Call {
  final class Var extends Parsable implements Factor {
    /** variable */
    private Variable variable;
    /** number of references away from value (leading '^') */
    private int nReferences;

    public Var(Variable variable, int nReferences, int lineIdx, int charIdx) {
      super(lineIdx, charIdx);
      this.variable = variable;
      this.nReferences = nReferences;
    }

    /** Getter method for {@code variable} */
    public Variable getVariable() {
      return variable;
    }

    /** Getter method for {@code nReferences} */
    public int getNReferences() {
      return nReferences;
    }

    @Override
    public String toString() {
      StringBuilder name = new StringBuilder();
      for (int i = 0; i < nReferences; i++)
        name.append("^");
      name.append(variable.toString());
      return name.toString();
    }
  }

  public record Const(int constant, int lineIdx, int charIdx) implements Factor {
    @Override
    public String toString() {
      return "" + constant;
    }
  }

  public record Str(String string, int lineIdx, int charIdx) implements Factor {
    @Override
    public String toString() {
      return "\"" + string + "\"";
    }
  }

  public record Parentheses(Expression expression, int lineIdx, int charIdx) implements Factor {
    @Override
    public String toString() {
      return "(" + expression.toString() + ")";
    }
  }

  public record Call(Function fun, Expression[] arguments, int lineIdx, int charIdx) implements Factor {
    @Override
    public String toString() {
      StringBuilder value = new StringBuilder(fun.getName()).append("(");
      for (int i = 0; i < arguments.length; i++) {
        if (i > 0)
          value.append(", ");
        value.append(arguments[i].toString());
      }
      value.append(")");
      return value.toString();
    }
  }
}
