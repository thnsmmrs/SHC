/**
 * Parsable class for SHC language.
 * Parent class for parsable things, which must extend this.
 *
 * This aids in reporting errors.
 * It is a sealed class to allow for pattern-matching Switch statements.
 * All of its children should be final.
 */
public sealed class Parsable permits
	Expression, Function, Variable, Statement, Factor.Var,
	Assignment, OrExpression, AndExpression, EqualityExpression,
	RelationalExpression, AdditiveExpression, MultiplicativeExpression,
	UnaryExpression {

	/** the line of the parsable thing */
	private int lineIdx;
	/** the char index within the line of the parsable thing */
	private int charIdx;

	/**
	 * Constructor.
	 *
	 * @param lineIdx - index of the line {@code Parsable} starts at
	 * @param charIdx - char index in the line {@code Parsable} starts at
	 */
	public Parsable(int lineIdx, int charIdx) {
		this.lineIdx = lineIdx;
		this.charIdx = charIdx;
	}

	/** getter method for {@code lineIdx} */
	public int getLineIdx() {
		return lineIdx;
	}

	/** getter method for {@code charIdx} */
	public int getCharIdx() {
		return charIdx;
	}
}
