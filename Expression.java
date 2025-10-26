/**
 * Expression class for SHC language.
 *
 * @author Alec Kingsley
 */
public final class Expression extends Parsable {
	/** list of expressions */
	private AdditiveExpression[] expressions;

	/**
	 * Constructor for the {@code AdditiveExpression} class.
	 *
	 * @param expressions - the list of expressions
	 * @param lineIdx - index of the line {@code Parsable} starts at
	 * @param charIdx - char index in the line {@code Parsable} starts at
	 */
	public Expression(AdditiveExpression[] expressions, int lineIdx, int charIdx) {
		super(lineIdx, charIdx);
		this.expressions = expressions;
	}

	/** getter method for {@code expressions} */
  public AdditiveExpression[] getExpressions() {
  	return expressions;
  }
}
