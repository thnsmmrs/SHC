/**
 * Statement class for SHC language.
 *
 * @author Alec Kingsley
 */
public sealed interface Statement permits Statement.If, Statement.Loop, Statement.Decl, Statement.Call, Statement.Jump {
	/**
	 * If statement.
	 *
	 * @param cond - condition for the if statement
	 * @param body - main body for if statement
	 * @param otherbody - body for else block of if statement
	 * @param lineIdx - line index of first token
	 * @param charIdx - character index of first token
	 */
	public record If(Factor cond, Statement[] body, Statement[] otherBody, int lineIdx, int charIdx) implements Statement {}

	/**
	 * Loop statement.
	 * Note that this only defines for loops.
	 *
	 * @param cond - condition for ending the loop
	 * @param body - body of loop
	 * @param lineIdx - line index of first token
	 * @param charIdx - character index of first token
	 */
	public record Loop(Factor cond, Statement[] body, int lineIdx, int charIdx) implements Statement {}

	/**
	 * Decl statement.
	 *
	 * @param variable - variable to declare
	 * @param lineIdx - line index of first token
	 * @param charIdx - character index of first token
	 */
	public record Decl(Variable variable, int lineIdx, int charIdx) implements Statement {}

	/**
	 * Function Call.
	 *
	 * @param call - function call
	 * @param lineIdx - line index of first token
	 * @param charIdx - character index of first token
	 */
	public record Call(Function function, Expression[] arguments, int lineIdx, int charIdx) implements Statement {}

	/**
	 * Jump statement.
	 *
	 * @param type - CONTINUE, BREAK, or RETURN.
	 * @param lineIdx - line index of first token
	 * @param charIdx - character index of first token
	 */
	public record Jump(SHC type, int lineIdx, int charIdx) implements Statement {}
}
