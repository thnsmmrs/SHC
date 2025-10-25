/**
 * Expression class for SHC language.
 *
 * @author Alec Kingsley
 */
public final class Expression extends Parsable {
	/** list of expressions */
	private Assignment[] expressions;
  
	/**
	 * Constructor for the {@code Expression} class.
	 *
	 * @param expressions - the list of expressions
	 * @param lineIdx - index of the line {@code Parsable} starts at
	 * @param charIdx - char index in the line {@code Parsable} starts at
	 */
	public Expression(Assignment[] expressions, int lineIdx, int charIdx) {
		super(lineIdx, charIdx);
		this.expressions = expressions;
	}

	/** getter method for {@code expressions} */
  public Assignment[] getExpressions() {
  	return expressions;
  }
}
