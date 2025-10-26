/**
 * Assignment class for SHC language.
 *
 * LHS is a Factor.Var (so it can carry the caret count), RHS is an
 * OrExpression.
 */
public final class Assignment extends Parsable {
  /** thing to assign to (with its leading ^ count) */
  private Factor.Var assignee;

  /** value to assign with */
  private OrExpression value;

  /**
   * Constructor for the {@code Assignment} class.
   *
   * @param assignee - the thing whose value is being changed (Factor.Var)
   * @param value    - the value of the expression (OrExpression)
   * @param lineIdx  - index of the line {@code Parsable} starts at
   * @param charIdx  - char index in the line {@code Parsable} starts at
   */
  public Assignment(Factor.Var assignee, OrExpression value, int lineIdx, int charIdx) {
    super(lineIdx, charIdx);
    this.assignee = assignee;
    this.value = value;
  }

  /** Getter for assignee */
  public Factor.Var getAssignee() {
    if (assignee == null) {
      throw new RuntimeException("assignment assignee not defined");
    }
    return assignee;
  }

  /** Does this assignment have an LHS? */
  public boolean hasAssignee() {
    return assignee != null;
  }

  /** Getter for RHS value */
  public OrExpression getValue() {
    return value;
  }
}
