/**
 * Equality Expression class for SHC language.
 *
 * @author Alec Kingsley
 */
public final class EqualityExpression extends Parsable {
	/** left of expression */
	private EqualityExpression left = null;
	/** operator for expression */
	private SHC operator;
  /** right of expression */
  private RelationalExpression right;
  
	/**
	 * Constructor for the {@code EqualityExpression} class.
	 *
	 * @param left - the left of the expression
	 * @param operator - the operator of the expression
	 * @param right - the right of the expression
	 * @param lineIdx - index of the line {@code Parsable} starts at
	 * @param charIdx - char index in the line {@code Parsable} starts at
	 */
	public EqualityExpression(EqualityExpression left, SHC operator, RelationalExpression right, int lineIdx, int charIdx) {
		super(lineIdx, charIdx);
		this.left = left;
		this.operator = operator;
		this.right = right;
	}

	/**
	 * Constructor for the {@code EqualityExpression} class.
	 *
	 * @param value - the value of the expression (no operator)
	 * @param lineIdx - index of the line {@code Parsable} starts at
	 * @param charIdx - char index in the line {@code Parsable} starts at
	 */
	public EqualityExpression(RelationalExpression value, int lineIdx, int charIdx) {
		super(lineIdx, charIdx);
		this.right = value;
	}

	/** getter method for {@code left} */
	public EqualityExpression getLeft() {
		if (left == null) {
			throw new RuntimeException("left of expression not defined");
		}
		return left;
	}

	/** getter method for {@code operator} */
	public SHC getOperator() {
		if (left == null) {
			throw new RuntimeException("left of expression not defined");
		}
		return operator;
	}

	/**
	 * Return true iff {@code this} has a left side.
	 */
	public boolean hasLeft() {
		return left != null;
	}

	/** getter method for {@code right} */
  public RelationalExpression getRight() {
  	return right;
  }
}
