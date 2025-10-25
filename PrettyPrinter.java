/**
 * Pretty Printer for SHC language.
 * Converts AST back to formatted source code using K&R style.
 */
public class PrettyPrinter {
	/** Output builder */
	private StringBuilder output;

	/** Current indentation level */
	private int indentLevel;

	/** Number of spaces per indent level */
	private static final int INDENT_SIZE = 4;

	public PrettyPrinter() {
		this.output = new StringBuilder();
		this.indentLevel = 0;
	}

	/**
	 * Main entry point: pretty print a function.
	 *
	 * @param function - the function to pretty print
	 * @return formatted source code
	 */
	public String prettyPrint(Function function) {
		output = new StringBuilder();
		indentLevel = 0;
		printFunction(function);
		return output.toString();
	}

	/**
	 * Print a function definition.
	 *
	 * @param function - the function to print
	 */
	private void printFunction(Function function) {
		append("fun ");
		append(function.getName());
		append("(");

		Variable[] params = function.getArguments();
		if (params != null && params.length > 0) {
			for (int i = 0; i < params.length; i++) {
				if (i > 0) {
					append(", ");
				}
				printType(params[i].getType());
				append(" ");
				append(params[i].getName());
			}
		}

		append(") : ");
		printType(function.getReturnType());
		append(" {");
		newline();

		increaseIndent();

		Variable[] localVars = function.getLocalVariables();
		if (localVars != null && localVars.length > 0) {
			for (Variable var : localVars) {
				indent();
				printVariable(var);
				newline();
			}
			if (function.getBody() != null && function.getBody().length > 0) {
				newline();
			}
		}

		Statement[] body = function.getBody();
		if (body != null) {
			for (Statement stmt : body) {
				printStatement(stmt);
			}
		}

		decreaseIndent();

		append("}");
		newline();
	}

	/**
	 * Print a variable declaration.
	 *
	 * @param variable - the variable to print
	 */
	private void printVariable(Variable variable) {
		printType(variable.getType());
		append(" : ");
		append(variable.getName());
		append(";");
	}

	/**
	 * Print a statement.
	 *
	 * @param statement - the statement to print
	 */
	private void printStatement(Statement statement) {
		if (statement instanceof Statement.If) {
			printIfStatement((Statement.If) statement);
		} else if (statement instanceof Statement.Loop) {
			printLoopStatement((Statement.Loop) statement);
		} else if (statement instanceof Statement.Decl) {
			printDeclStatement((Statement.Decl) statement);
		} else if (statement instanceof Statement.Call) {
			printCallStatement((Statement.Call) statement);
		} else if (statement instanceof Statement.Jump) {
			printJumpStatement((Statement.Jump) statement);
		}
	}

	/**
	 * Print an if statement.
	 */
	private void printIfStatement(Statement.If ifStmt) {
		indent();
		append("if (");
		append("<condition>");
		append(") {");
		newline();

		increaseIndent();
		if (ifStmt.body() != null) {
			for (Statement stmt : ifStmt.body()) {
				printStatement(stmt);
			}
		}
		decreaseIndent();

		indent();
		append("}");

		if (ifStmt.otherBody() != null && ifStmt.otherBody().length > 0) {
			append(" else {");
			newline();

			increaseIndent();
			for (Statement stmt : ifStmt.otherBody()) {
				printStatement(stmt);
			}
			decreaseIndent();

			indent();
			append("}");
		}

		newline();
	}

	/**
	 * Print a loop statement.
	 */
	private void printLoopStatement(Statement.Loop loopStmt) {
		indent();
		append("while (");
		append("<condition>");
		append(") {");
		newline();

		increaseIndent();
		if (loopStmt.body() != null) {
			for (Statement stmt : loopStmt.body()) {
				printStatement(stmt);
			}
		}
		decreaseIndent();

		indent();
		append("}");
		newline();
	}

	/**
	 * Print a declaration statement.
	 */
	private void printDeclStatement(Statement.Decl declStmt) {
		indent();
		printVariable(declStmt.variable());
		newline();
	}

	/**
	 * Print a function call statement.
	 */
	private void printCallStatement(Statement.Call callStmt) {
		indent();
		append(callStmt.function().getName());
		append("(");

		Expression[] args = callStmt.arguments();
		if (args != null && args.length > 0) {
			for (int i = 0; i < args.length; i++) {
				if (i > 0) {
					append(", ");
				}
				append("<expr>");
			}
		}

		append(");");
		newline();
	}

	/**
	 * Print a jump statement (return, break, continue).
	 */
	private void printJumpStatement(Statement.Jump jumpStmt) {
		indent();

		switch (jumpStmt.type()) {
			case SHC.RETURN:
				append("return;");
				break;
			case SHC.BREAK:
				append("break;");
				break;
			case SHC.CONTINUE:
				append("continue;");
				break;
			default:
				append("<unknown jump>;");
				break;
		}

		newline();
	}

	/**
	 * Print a type specifier.
	 *
	 * @param type - the type token
	 */
	private void printType(SHC type) {
		switch (type) {
			case INT:
				append("int");
				break;
			case CHAR:
				append("char");
				break;
			case VOID:
				append("void");
				break;
			default:
				append(type.toString().toLowerCase());
				break;
		}
	}

	private void append(String str) {
		output.append(str);
	}

	private void indent() {
		for (int i = 0; i < indentLevel * INDENT_SIZE; i++) {
			output.append(' ');
		}
	}

	private void newline() {
		output.append('\n');
	}

	private void increaseIndent() {
		indentLevel++;
	}

	private void decreaseIndent() {
		if (indentLevel > 0) {
			indentLevel--;
		}
	}

	public static void main(String[] args) {
		Variable[] params = new Variable[] {
			new Variable("a", SHC.INT, 0, 1, 5),
			new Variable("b", SHC.INT, 0, 1, 8),
			new Variable("c", SHC.CHAR, 0, 1, 11)
		};

		Variable[] localVars = new Variable[] {
			new Variable("result", SHC.INT, 0, 2, 5)
		};

		Statement[] body = new Statement[] {
			new Statement.Loop(
				null,
				new Statement[] {
					new Statement.If(
						null,
						new Statement[] {
							new Statement.Jump(SHC.CONTINUE, 6, 13)
						},
						new Statement[] {
							new Statement.Jump(SHC.BREAK, 8, 13)
						},
						5, 9
					)
				},
				3, 5
			),
			new Statement.Jump(SHC.RETURN, 10, 5)
		};

		Function testFunc = new Function(
			SHC.INT,
			"factorial",
			params,
			localVars,
			body,
			1, 1
		);

		PrettyPrinter printer = new PrettyPrinter();
		String output = printer.prettyPrint(testFunc);

		System.out.println("START ======================== START");
		System.out.println(output);
		System.out.println("END ======================== END");
	}
}
