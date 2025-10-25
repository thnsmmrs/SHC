/**
 * Function class for SHC language.
 *
 * @author Alec Kingsley
 */
public final class Function extends Parsable {
	/** return type of method */
	private SHC returnType;

	/** name of the function */
	private String name;

	/** arguments */
	private Variable[] arguments;

	/** local variables */
	private Variable[] localVariables;

	/** ordered statements in method */
	private Statement[] body;

	/**
	 * Constructor for a local method call.
	 *
	 * @param returnType - return type of the function
	 * @param name - name of the method
	 * @param arguments - the arguments of the method
	 * @param localVariables - the local variables defined in the section above the body
	 * @param body - the body of the method
	 * @param lineIdx - index of the line {@code Parsable} starts at
	 * @param charIdx - char index in the line {@code Parsable} starts at
	 */
	public Function(SHC returnType, String name, Variable[] arguments, Variable[] localVariables, Statement[] body, int lineIdx, int charIdx) {
		super(lineIdx, charIdx);
		this.returnTypeName = returnTypeName;
		this.name = name;
		this.arguments = arguments;
		this.localVariables = localVariables;
		this.body = body;
	}

	/** getter method for {@code returnType} */
	public SHC getReturnType() {
		return returnType;
	}

	/** getter method for {@code name} */
	public String getName() {
		return name;
	}

	/** getter method for {@code arguments} */
	public Variable[] getArguments() {
		return arguments;
	}

	/** getter method for {@code localVariables} */
	public Variable[] getLocalVariables() {
		return localVariables;
	}

	/** getter method for {@code body} */
	public Statement[] getBody() {
		return body;
	}

	/** getter method for {@code returnVariable} */
	public Variable getReturnVariable() {
		return returnVariable;
	}
}
