/**
 * Or Expression class for SHC language.
 *
 * @author Alec Kingsley
 */
public final class OrExpression extends Parsable {
	/** left of expression */
	private OrExpression left = null;
  /** right of expression */
  private AndExpression right;
  
	/**
	 * Constructor for the {@code OrExpression} class.
	 *
	 * @param left - the left of the expression
	 * @param right - the right of the expression
	 * @param lineIdx - index of the line {@code Parsable} starts at
	 * @param charIdx - char index in the line {@code Parsable} starts at
	 */
	public OrExpression(OrExpression left, AndExpression right, int lineIdx, int charIdx) {
		super(lineIdx, charIdx);
		this.left = left;
		this.right = right;
	}

	/**
	 * Constructor for the {@code OrExpression} class.
	 *
	 * @param value - the value of the expression (no operator)
	 * @param lineIdx - index of the line {@code Parsable} starts at
	 * @param charIdx - char index in the line {@code Parsable} starts at
	 */
	public OrExpression(AndExpression value, int lineIdx, int charIdx) {
		super(lineIdx, charIdx);
		this.right = value;
	}

	/** getter method for {@code left} */
	public OrExpression getLeft() {
		if (left == null) {
			throw new RuntimeException("left of expression not defined");
		}
		return left;
	}

	/**
	 * Return true iff {@code this} has a left side.
	 */
	public boolean hasLeft() {
		return left != null;
	}

	/** getter method for {@code right} */
  public AndExpression getRight() {
  	return right;
  }

	@Override
	public String toString() {
		if (hasLeft()) {
			return left.toString() + " || " + right.toString();
		} else {
			return right.toString();
		}
	}
}
