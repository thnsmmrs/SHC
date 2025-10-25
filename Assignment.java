/**
 * Assignment class for SHC language.
 *
 * @author Alec Kingsley
 */
public final class Assignment extends Parsable {
	/** thing to assign to */
  private Variable assignee = null;
  /** value to assign with */
  private OrExpression value;
  
	/**
	 * Constructor for the {@code Assignment} class.
	 *
	 * @param assignee - the thing whose value is being changed
	 * @param value - the value of the expression
	 * @param lineIdx - index of the line {@code Parsable} starts at
	 * @param charIdx - char index in the line {@code Parsable} starts at
	 */
	public Assignment(Variable assignee, OrExpression value, int lineIdx, int charIdx) {
		super(lineIdx, charIdx);
		this.assignee = assignee;
		this.value = value;
	}

	/**
	 * Constructor for the {@code Assignment} class.
	 *
	 * @param value - the value of the expression
	 * @param lineIdx - index of the line {@code Parsable} starts at
	 * @param charIdx - char index in the line {@code Parsable} starts at
	 */
	public Assignment(OrExpression value, int lineIdx, int charIdx) {
		super(lineIdx, charIdx);
		this.value = value;
	}

	/** getter method for {@code assignee} */
	public Variable getAssignee() {
		if (assignee == null) {
			throw new RuntimeException("assignment assignee not defined");
		}
		return assignee;
	}

	/**
	 * Return true iff {@code this} has assignee.
	 */
	public boolean hasAssignee() {
		return assignee != null;
	}

	/** getter method for {@code value} */
  public OrExpression getValue() {
  	return value;
  }
}
