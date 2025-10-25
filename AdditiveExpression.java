/**
 * Additive Expression class for SHC language.
 *
 * @author Alec Kingsley
 */
public final class AdditiveExpression extends Parsable {
	/** left of expression */
	private AdditiveExpression left = null;
	/** operator for expression */
	private SHC operator;
  /** right of expression */
  private MultiplicativeExpression right;
  
	/**
	 * Constructor for the {@code AdditiveExpression} class.
	 *
	 * @param left - the left of the expression
	 * @param operator - the operator of the expression
	 * @param right - the right of the expression
	 * @param lineIdx - index of the line {@code Parsable} starts at
	 * @param charIdx - char index in the line {@code Parsable} starts at
	 */
	public AdditiveExpression(AdditiveExpression left, SHC operator, MultiplicativeExpression right, int lineIdx, int charIdx) {
		super(lineIdx, charIdx);
		this.left = left;
		this.operator = operator;
		this.right = right;
	}

	/**
	 * Constructor for the {@code AdditiveExpression} class.
	 *
	 * @param value - the value of the expression (no operator)
	 * @param lineIdx - index of the line {@code Parsable} starts at
	 * @param charIdx - char index in the line {@code Parsable} starts at
	 */
	public AdditiveExpression(MultiplicativeExpression value, int lineIdx, int charIdx) {
		super(lineIdx, charIdx);
		this.right = value;
	}

	/** getter method for {@code left} */
	public AdditiveExpression getLeft() {
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
  public MultiplicativeExpression getRight() {
  	return right;
  }
}
