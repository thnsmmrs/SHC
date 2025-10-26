/**
 * Unary Expression class for SHC language.
 *
 * @author Alec Kingsley
 */
public final class UnaryExpression extends Parsable {
	/** left of expression */
	private UnaryExpression unaryExpression = null;
	/** operator */
	private SHC operator;
	/** right of expression */
	private Factor factor = null;

	/**
	 * Constructor for the {@code UnaryExpression} class.
	 * 
	 * @param operator   - the operator used on expression
	 * @param expression - the expression
	 * @param lineIdx    - index of the line {@code Parsable} starts at
	 * @param charIdx    - char index in the line {@code Parsable} starts at
	 */
	public UnaryExpression(SHC operator, UnaryExpression expression,
			int lineIdx, int charIdx) {
		super(lineIdx, charIdx);
		this.operator = operator;
		this.unaryExpression = expression;
	}

	/**
	 * Constructor for the {@code UnaryExpression} class.
	 * 
	 * @param expression - the expression
	 * @param lineIdx    - index of the line {@code Parsable} starts at
	 * @param charIdx    - char index in the line {@code Parsable} starts at
	 */
	public UnaryExpression(Factor expression,
			int lineIdx, int charIdx) {
		super(lineIdx, charIdx);
		this.factor = expression;
	}

	/** getter method for {@code unaryExpression} */
	public UnaryExpression getUnaryExpression() {
		if (unaryExpression == null) {
			throw new RuntimeException("expression has no operator");
		}
		return unaryExpression;
	}

	/** getter method for {@code operator} */
	public SHC getOperator() {
		if (unaryExpression == null) {
			throw new RuntimeException("expression has no operator");
		}
		return operator;
	}

	/** getter method for {@code factor} */
	public Factor getFactor() {
		if (factor == null) {
			throw new RuntimeException("expression has an operator");
		}
		return factor;
	}

	/**
	 * Return true iff {@code this} has a left side.
	 */
	public boolean hasOperator() {
		return factor == null;
	}

	@Override
	public String toString() {
		if (hasOperator()) {
			String op = switch (operator) {
				case ADD -> "+";
				case SUBTRACT -> "-";
				default -> "?";
			};
			return op + unaryExpression.toString();
		} else {
			return factor.toString();
		}
	}
}
