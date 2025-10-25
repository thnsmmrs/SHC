/**
 * Factor class for SHC language.
 *
 * @author Alec Kingsley
 */
public sealed interface Factor permits Factor.Var, Factor.Const, Factor.Str, Factor.Parentheses, Factor.Call {
	final class Var extends Parsable implements Factor {
		/** variable */
		private Variable variable;
		/** number of references away from value */
		private int nReferences;

		/**
		 * Constructor for a variable factor.
		 *
		 * @param variable - the variable
		 * @param lineIdx - index of the line {@code Parsable} starts at
		 * @param charIdx - char index in the line {@code Parsable} starts at
		 */
		public Var(Variable variable, int nReferences, int lineIdx, int charIdx) {
			super(lineIdx, charIdx);
			this.variable = variable;
			this.nReferences = nReferences;
		}

		@Override
		public String toString() {
			String name = "";
			for (int i = 0; i < nReferences; i++) {
				name += "^";
			}
			name += variable.toString();
			return name;
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
			String value = fun.getName() + "(";
			for (Expression argument : arguments) {
				value += argument.toString();
			}
			value += ")";
			return value;
		}
	}
}
