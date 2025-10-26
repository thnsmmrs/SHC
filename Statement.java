/**
 * Statement class for SHC language.
 *
 * @author Alec Kingsley
 */
public sealed interface Statement
    permits Statement.If, Statement.Loop, Statement.Decl, Statement.Call, Statement.Jump, Statement.Assign {

  /**
   * If statement.
   *
   * @param cond      - condition for the if statement
   * @param body      - main body for if statement
   * @param otherBody - body for else block of if statement
   * @param lineIdx   - line index of first token
   * @param charIdx   - character index of first token
   */
  public record If(Factor cond, Statement[] body, Statement[] otherBody, int lineIdx, int charIdx)
      implements Statement {
  }

  /**
   * Loop statement.
   *
   * @param cond    - loop condition
   * @param body    - body of loop
   * @param lineIdx - line index of first token
   * @param charIdx - character index of first token
   */
  public record Loop(Factor cond, Statement[] body, int lineIdx, int charIdx)
      implements Statement {
  }

  /**
   * Declaration statement.
   *
   * @param variable - variable to declare
   * @param lineIdx  - line index of first token
   * @param charIdx  - character index of first token
   */
  public record Decl(Variable variable, int lineIdx, int charIdx)
      implements Statement {
  }

  /**
   * Function call.
   *
   * @param function  - function called
   * @param arguments - arguments to the function
   * @param lineIdx   - line index of first token
   * @param charIdx   - character index of first token
   */
  public record Call(Function function, Expression[] arguments, int lineIdx, int charIdx)
      implements Statement {
  }

  /**
   * Jump statement.
   * For {@code RETURN}, {@code value} may be non-null (i.e.,
   * {@code return <expr>;})
   * or null (i.e., {@code return;}).
   * For {@code BREAK}/{@code CONTINUE}, {@code value} must be null.
   *
   * @param type    - one of CONTINUE, BREAK, RETURN
   * @param value   - expression for return, or null
   * @param lineIdx - line index of first token
   * @param charIdx - character index of first token
   */
  public record Jump(SHC type, Expression value, int lineIdx, int charIdx)
      implements Statement {

    // Convenience ctor for old call sites (break/continue/return; with no value)
    public Jump(SHC type, int lineIdx, int charIdx) {
      this(type, null, lineIdx, charIdx);
    }

    // Add this method:
    public boolean hasValue() {
      return value != null;
    }
  }

  /**
   * Assignment statement wrapper.
   *
   * @param assignment - the assignment node
   * @param lineIdx    - line index of first token
   * @param charIdx    - character index of first token
   */
  public record Assign(Assignment assignment, int lineIdx, int charIdx)
      implements Statement {
  }
}
