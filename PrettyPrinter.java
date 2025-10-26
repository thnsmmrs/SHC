/**
 * Pretty Printer class for SHC language.
 * Converts AST back to formatted source code using K&R style.
 *
 * @author SWY
 */
public class PrettyPrinter {
	/** Output builder */
	private StringBuilder output;

	/** Current indentation level */
	private int indentLevel;

	/** Number of spaces per indent level */
	private static final int INDENT_SIZE = 4;

	/**
	 * Constructor for the {@code PrettyPrinter} class.
	 * Initializes the output builder and sets indentation level to zero.
	 */
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
	 * Print an if statement with optional else block.
	 *
	 * @param ifStmt - the if statement to print
	 */
	private void printIfStatement(Statement.If ifStmt) {
		indent();
		append("if (");
		if (ifStmt.cond() != null) {
			printFactor(ifStmt.cond());
		} else {
			append("<condition>");
		}
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
	 * Print a loop statement (while loop).
	 *
	 * @param loopStmt - the loop statement to print
	 */
	private void printLoopStatement(Statement.Loop loopStmt) {
		indent();
		append("while (");
		if (loopStmt.cond() != null) {
			printFactor(loopStmt.cond());
		} else {
			append("<condition>");
		}
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
	 *
	 * @param declStmt - the declaration statement to print
	 */
	private void printDeclStatement(Statement.Decl declStmt) {
		indent();
		printVariable(declStmt.variable());
		newline();
	}

	/**
	 * Print a function call statement.
	 *
	 * @param callStmt - the function call statement to print
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
				printExpression(args[i]);
			}
		}

		append(");");
		newline();
	}

	/**
	 * Print a jump statement (return, break, continue).
	 *
	 * @param jumpStmt - the jump statement to print
	 */
	private void printJumpStatement(Statement.Jump jumpStmt) {
		indent();

		switch (jumpStmt.type()) {
			case RETURN:
				append("return;");
				break;
			case BREAK:
				append("break;");
				break;
			case CONTINUE:
				append("continue;");
				break;
			default:
				append("<unknown jump>;");
				break;
		}

		newline();
	}

	/**
	 * Print an expression (top-level expression containing assignments).
	 *
	 * @param expression - the expression to print
	 */
	private void printExpression(Expression expression) {
		if (expression == null) {
			append("<null-expr>");
			return;
		}

		Assignment[] assignments = expression.getExpressions();
		if (assignments != null && assignments.length > 0) {
			for (int i = 0; i < assignments.length; i++) {
				if (i > 0) {
					append(", ");
				}
				printAssignment(assignments[i]);
			}
		}
	}

	/**
	 * Print an assignment expression.
	 *
	 * @param assignment - the assignment to print
	 */
	private void printAssignment(Assignment assignment) {
		if (assignment.hasAssignee()) {
			append(assignment.getAssignee().getName());
			append(" = ");
		}
		printOrExpression(assignment.getValue());
	}

	/**
	 * Print an OR expression (logical ||).
	 *
	 * @param expr - the OR expression to print
	 */
	private void printOrExpression(OrExpression expr) {
		if (expr.hasLeft()) {
			printOrExpression(expr.getLeft());
			append(" || ");
		}
		printAndExpression(expr.getRight());
	}

	/**
	 * Print an AND expression (logical &&).
	 *
	 * @param expr - the AND expression to print
	 */
	private void printAndExpression(AndExpression expr) {
		if (expr.hasLeft()) {
			printAndExpression(expr.getLeft());
			append(" && ");
		}
		printEqualityExpression(expr.getRight());
	}

	/**
	 * Print an equality expression (== or !=).
	 *
	 * @param expr - the equality expression to print
	 */
	private void printEqualityExpression(EqualityExpression expr) {
		if (expr.hasLeft()) {
			printEqualityExpression(expr.getLeft());
			String op = (expr.getOperator() == SHC.EQUAL) ? " == " : " != ";
			append(op);
		}
		printRelationalExpression(expr.getRight());
	}

	/**
	 * Print a relational expression with operators <, >, <=, or >=.
	 *
	 * @param expr - the relational expression to print
	 */
	private void printRelationalExpression(RelationalExpression expr) {
		if (expr.hasLeft()) {
			printRelationalExpression(expr.getLeft());
			String op = switch (expr.getOperator()) {
				case LESS -> " < ";
				case GREATER -> " > ";
				case LEQ -> " <= ";
				case GEQ -> " >= ";
				default -> " ? ";
			};
			append(op);
		}
		printAdditiveExpression(expr.getRight());
	}

	/**
	 * Print an additive expression with operators + or -.
	 *
	 * @param expr - the additive expression to print
	 */
	private void printAdditiveExpression(AdditiveExpression expr) {
		if (expr.hasLeft()) {
			printAdditiveExpression(expr.getLeft());
			String op = (expr.getOperator() == SHC.ADD) ? " + " : " - ";
			append(op);
		}
		printMultiplicativeExpression(expr.getRight());
	}

	/**
	 * Print a multiplicative expression with operators *, /, or %.
	 *
	 * @param expr - the multiplicative expression to print
	 */
	private void printMultiplicativeExpression(MultiplicativeExpression expr) {
		if (expr.hasLeft()) {
			printMultiplicativeExpression(expr.getLeft());
			String op = switch (expr.getOperator()) {
				case MULTIPLY -> " * ";
				case DIVIDE -> " / ";
				case MOD -> " % ";
				default -> " ? ";
			};
			append(op);
		}
		printUnaryExpression(expr.getRight());
	}

	/**
	 * Print a unary expression with operators !, -, &, or ^.
	 *
	 * @param expr - the unary expression to print
	 */
	private void printUnaryExpression(UnaryExpression expr) {
		if (expr.hasOperator()) {
			String op = switch (expr.getOperator()) {
				case NOT -> "!";
				case SUBTRACT -> "-";
				case AMP -> "&";
				case MULTIPLY -> "^";
				default -> "?";
			};
			append(op);
			printUnaryExpression(expr.getUnaryExpression());
		} else {
			printFactor(expr.getFactor());
		}
	}

	/**
	 * Print a factor (variable, constant, string, parentheses, or function call).
	 *
	 * @param factor - the factor to print
	 */
	private void printFactor(Factor factor) {
		if (factor instanceof Factor.Var var) {
			// Print dereference operators
			for (int i = 0; i < var.toString().indexOf(var.toString().replaceAll("\\^", "")); i++) {
				append("^");
			}
			append(var.toString().replaceAll("\\^", ""));
		} else if (factor instanceof Factor.Const const_) {
			append(String.valueOf(const_.constant()));
		} else if (factor instanceof Factor.Str str) {
			append("\"");
			append(str.string());
			append("\"");
		} else if (factor instanceof Factor.Parentheses paren) {
			append("(");
			printExpression(paren.expression());
			append(")");
		} else if (factor instanceof Factor.Call call) {
			append(call.fun().getName());
			append("(");
			Expression[] args = call.arguments();
			if (args != null && args.length > 0) {
				for (int i = 0; i < args.length; i++) {
					if (i > 0) {
						append(", ");
					}
					printExpression(args[i]);
				}
			}
			append(")");
		}
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

	/** Append a string to the output */
	private void append(String str) {
		output.append(str);
	}

	/** Add indentation at the current indentation level */
	private void indent() {
		for (int i = 0; i < indentLevel * INDENT_SIZE; i++) {
			output.append(' ');
		}
	}

	/** Add a newline character to the output */
	private void newline() {
		output.append('\n');
	}

	/** Increase the indentation level by one */
	private void increaseIndent() {
		indentLevel++;
	}

	/** Decrease the indentation level by one */
	private void decreaseIndent() {
		if (indentLevel > 0) {
			indentLevel--;
		}
	}

	public static void main(String[] args) {
		// Test 1: Basic structure with null conditions
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

		System.out.println("START TEST 1 (basic structure) ======================== START");
		System.out.println(output);
		System.out.println("END TEST 1 ======================== END\n");

		// Test 2: With actual expression factors
		Variable n = new Variable("n", SHC.INT, 0, 1, 1);
		Variable x = new Variable("x", SHC.INT, 0, 2, 1);

		// Create a simple factor: the constant 0
		Factor.Const zero = new Factor.Const(0, 3, 5);
		Factor.Const one = new Factor.Const(1, 4, 5);
		Factor.Const five = new Factor.Const(5, 5, 5);

		// Create a variable factor
		Factor.Var nVar = new Factor.Var(n, 0, 3, 3);
		Factor.Var xVar = new Factor.Var(x, 0, 4, 3);

		Statement[] body2 = new Statement[] {
			new Statement.Loop(
				nVar,  // while (n)
				new Statement[] {
					new Statement.If(
						zero,  // if (0)
						new Statement[] {
							new Statement.Jump(SHC.BREAK, 6, 13)
						},
						null,
						5, 9
					)
				},
				3, 5
			),
			new Statement.Jump(SHC.RETURN, 10, 5)
		};

		Variable[] params2 = new Variable[] {
			new Variable("n", SHC.INT, 0, 1, 5)
		};

		Variable[] localVars2 = new Variable[] {
			new Variable("x", SHC.INT, 0, 2, 5)
		};

		Function testFunc2 = new Function(
			SHC.VOID,
			"test",
			params2,
			localVars2,
			body2,
			1, 1
		);

		String output2 = printer.prettyPrint(testFunc2);

		System.out.println("START TEST 2 (with expressions) ======================== START");
		System.out.println(output2);
		System.out.println("END TEST 2 ======================== END\n");

		// Test 3: Comprehensive test with complex expressions
		testComprehensiveExpressions(printer);
	}

	/**
	 * Comprehensive test covering all expression types and operators.
	 */
	private static void testComprehensiveExpressions(PrettyPrinter printer) {
		System.out.println("START TEST 3 (comprehensive expressions) ======================== START");

		// Variables
		Variable a = new Variable("a", SHC.INT, 0, 1, 1);
		Variable b = new Variable("b", SHC.INT, 0, 1, 1);
		Variable c = new Variable("c", SHC.INT, 0, 1, 1);
		Variable x = new Variable("x", SHC.INT, 0, 1, 1);
		Variable y = new Variable("y", SHC.INT, 0, 1, 1);
		Variable ptr = new Variable("ptr", SHC.INT, 1, 1, 1);  // pointer
		Variable result = new Variable("result", SHC.INT, 0, 1, 1);

		// Helper function for building complex expressions
		Function helperFunc = new Function(SHC.INT, "helper", new Variable[]{}, new Variable[]{}, new Statement[]{}, 1, 1);

		// Test arithmetic expressions: (a + b) * c - 5
		Factor.Var aVar = new Factor.Var(a, 0, 1, 1);
		Factor.Var bVar = new Factor.Var(b, 0, 1, 1);
		Factor.Var cVar = new Factor.Var(c, 0, 1, 1);
		Factor.Const five = new Factor.Const(5, 1, 1);
		Factor.Const ten = new Factor.Const(10, 1, 1);
		Factor.Const zero = new Factor.Const(0, 1, 1);

		// Build: a + b
		UnaryExpression aUnary = new UnaryExpression(aVar, 1, 1);
		MultiplicativeExpression aMult = new MultiplicativeExpression(aUnary, 1, 1);
		AdditiveExpression aAdd = new AdditiveExpression(aMult, 1, 1);

		UnaryExpression bUnary = new UnaryExpression(bVar, 1, 1);
		MultiplicativeExpression bMult = new MultiplicativeExpression(bUnary, 1, 1);

		AdditiveExpression aPlusB = new AdditiveExpression(aAdd, SHC.ADD, bMult, 1, 1);

		// Build: (a + b) * c
		Factor.Parentheses aPlusBParen = new Factor.Parentheses(
			new Expression(
				new Assignment[]{new Assignment(new OrExpression(new AndExpression(new EqualityExpression(new RelationalExpression(aPlusB, 1, 1), 1, 1), 1, 1), 1, 1), 1, 1)},
				1, 1
			),
			1, 1
		);
		UnaryExpression parenUnary = new UnaryExpression(aPlusBParen, 1, 1);

		UnaryExpression cUnary = new UnaryExpression(cVar, 1, 1);

		MultiplicativeExpression aPlusBTimesC = new MultiplicativeExpression(
			new MultiplicativeExpression(parenUnary, 1, 1),
			SHC.MULTIPLY,
			cUnary,
			1, 1
		);

		// Build: (a + b) * c - 5
		UnaryExpression fiveUnary = new UnaryExpression(five, 1, 1);
		MultiplicativeExpression fiveMult = new MultiplicativeExpression(fiveUnary, 1, 1);

		AdditiveExpression complexArithmetic = new AdditiveExpression(
			new AdditiveExpression(aPlusBTimesC, 1, 1),
			SHC.SUBTRACT,
			fiveMult,
			1, 1
		);

		// Test relational: x < 10
		Factor.Var xVar = new Factor.Var(x, 0, 1, 1);
		UnaryExpression xUnary = new UnaryExpression(xVar, 1, 1);
		MultiplicativeExpression xMult = new MultiplicativeExpression(xUnary, 1, 1);
		AdditiveExpression xAdditive = new AdditiveExpression(xMult, 1, 1);

		UnaryExpression tenUnary = new UnaryExpression(ten, 1, 1);
		MultiplicativeExpression tenMult = new MultiplicativeExpression(tenUnary, 1, 1);
		AdditiveExpression tenAdditive = new AdditiveExpression(tenMult, 1, 1);

		RelationalExpression xLessThanTen = new RelationalExpression(
			new RelationalExpression(xAdditive, 1, 1),
			SHC.LESS,
			tenAdditive,
			1, 1
		);

		// Test equality: y == 0
		Factor.Var yVar = new Factor.Var(y, 0, 1, 1);
		UnaryExpression yUnary = new UnaryExpression(yVar, 1, 1);
		MultiplicativeExpression yMult = new MultiplicativeExpression(yUnary, 1, 1);
		AdditiveExpression yAdditive = new AdditiveExpression(yMult, 1, 1);
		RelationalExpression yRelational = new RelationalExpression(yAdditive, 1, 1);

		UnaryExpression zeroUnary = new UnaryExpression(zero, 1, 1);
		MultiplicativeExpression zeroMult = new MultiplicativeExpression(zeroUnary, 1, 1);
		AdditiveExpression zeroAdditive = new AdditiveExpression(zeroMult, 1, 1);
		RelationalExpression zeroRelational = new RelationalExpression(zeroAdditive, 1, 1);

		EqualityExpression yEqualsZero = new EqualityExpression(
			new EqualityExpression(yRelational, 1, 1),
			SHC.EQUAL,
			zeroRelational,
			1, 1
		);

		// Test logical AND: x < 10 && y == 0
		AndExpression logicalAnd = new AndExpression(
			new AndExpression(new EqualityExpression(xLessThanTen, 1, 1), 1, 1),
			yEqualsZero,
			1, 1
		);

		// Test unary operators: !result
		Factor.Var resultVar = new Factor.Var(result, 0, 1, 1);
		UnaryExpression notResult = new UnaryExpression(SHC.NOT, new UnaryExpression(resultVar, 1, 1), 1, 1);

		// Test pointer dereference: ^ptr
		Factor.Var ptrVar = new Factor.Var(ptr, 0, 1, 1);
		UnaryExpression derefPtr = new UnaryExpression(SHC.MULTIPLY, new UnaryExpression(ptrVar, 1, 1), 1, 1);

		// Test address-of: &a
		Factor.Var aVar2 = new Factor.Var(a, 0, 1, 1);
		UnaryExpression addrOfA = new UnaryExpression(SHC.AMP, new UnaryExpression(aVar2, 1, 1), 1, 1);

		// Test string literal
		Factor.Str helloStr = new Factor.Str("Hello, World!", 1, 1);

		// Test function call in expression: helper(a, b)
		Expression[] callArgs = new Expression[]{
			new Expression(new Assignment[]{new Assignment(new OrExpression(new AndExpression(new EqualityExpression(new RelationalExpression(new AdditiveExpression(new MultiplicativeExpression(new UnaryExpression(aVar, 1, 1), 1, 1), 1, 1), 1, 1), 1, 1), 1, 1), 1, 1), 1, 1)}, 1, 1),
			new Expression(new Assignment[]{new Assignment(new OrExpression(new AndExpression(new EqualityExpression(new RelationalExpression(new AdditiveExpression(new MultiplicativeExpression(new UnaryExpression(bVar, 1, 1), 1, 1), 1, 1), 1, 1), 1, 1), 1, 1), 1, 1), 1, 1)}, 1, 1)
		};
		Factor.Call helperCall = new Factor.Call(helperFunc, callArgs, 1, 1);

		// Create statements to test everything
		Statement[] comprehensiveBody = new Statement[]{
			// if (x < 10 && y == 0)
			new Statement.If(
				new Factor.Parentheses(
					new Expression(
						new Assignment[]{new Assignment(new OrExpression(logicalAnd, 1, 1), 1, 1)},
						1, 1
					),
					1, 1
				),
				new Statement[]{
					// result = (a + b) * c - 5;
					new Statement.Decl(
						new Variable("temp", SHC.INT, 0, 1, 1),
						1, 1
					)
				},
				null,
				1, 1
			),
			// while ((a + b) * c - 5 > 0)
			new Statement.Loop(
				new Factor.Parentheses(
					new Expression(
						new Assignment[]{new Assignment(
							new OrExpression(
								new AndExpression(
									new EqualityExpression(
										new RelationalExpression(
											new RelationalExpression(complexArithmetic, 1, 1),
											SHC.GREATER,
											new AdditiveExpression(new MultiplicativeExpression(new UnaryExpression(zero, 1, 1), 1, 1), 1, 1),
											1, 1
										),
										1, 1
									),
									1, 1
								),
								1, 1
							),
							1, 1
						)},
						1, 1
					),
					1, 1
				),
				new Statement[]{
					new Statement.Jump(SHC.BREAK, 1, 1)
				},
				1, 1
			),
			// Function call statement: helper(a, b);
			new Statement.Call(
				helperFunc,
				callArgs,
				1, 1
			),
			new Statement.Jump(SHC.RETURN, 1, 1)
		};

		Function comprehensiveFunc = new Function(
			SHC.INT,
			"comprehensive",
			new Variable[]{a, b, c, x, y, ptr},
			new Variable[]{result},
			comprehensiveBody,
			1, 1
		);

		String output = printer.prettyPrint(comprehensiveFunc);
		System.out.println(output);
		System.out.println("END TEST 3 ======================== END\n");

		// Test 4: Unary operators showcase
		System.out.println("START TEST 4 (unary operators) ======================== START");

		// Wrap unary expressions in parentheses factors to use as conditions
		Factor.Parentheses notResultFactor = new Factor.Parentheses(
			new Expression(new Assignment[]{new Assignment(new OrExpression(new AndExpression(new EqualityExpression(new RelationalExpression(new AdditiveExpression(new MultiplicativeExpression(notResult, 1, 1), 1, 1), 1, 1), 1, 1), 1, 1), 1, 1), 1, 1)}, 1, 1),
			1, 1
		);

		Factor.Parentheses derefPtrFactor = new Factor.Parentheses(
			new Expression(new Assignment[]{new Assignment(new OrExpression(new AndExpression(new EqualityExpression(new RelationalExpression(new AdditiveExpression(new MultiplicativeExpression(derefPtr, 1, 1), 1, 1), 1, 1), 1, 1), 1, 1), 1, 1), 1, 1)}, 1, 1),
			1, 1
		);

		Factor.Parentheses addrOfAFactor = new Factor.Parentheses(
			new Expression(new Assignment[]{new Assignment(new OrExpression(new AndExpression(new EqualityExpression(new RelationalExpression(new AdditiveExpression(new MultiplicativeExpression(addrOfA, 1, 1), 1, 1), 1, 1), 1, 1), 1, 1), 1, 1), 1, 1)}, 1, 1),
			1, 1
		);

		Statement[] unaryBody = new Statement[]{
			new Statement.If(notResultFactor, new Statement[]{new Statement.Jump(SHC.RETURN, 1, 1)}, null, 1, 1),
			new Statement.If(derefPtrFactor, new Statement[]{new Statement.Jump(SHC.CONTINUE, 1, 1)}, null, 1, 1),
			new Statement.If(addrOfAFactor, new Statement[]{new Statement.Jump(SHC.BREAK, 1, 1)}, null, 1, 1),
			new Statement.If(helloStr, new Statement[]{new Statement.Jump(SHC.RETURN, 1, 1)}, null, 1, 1)
		};

		Function unaryFunc = new Function(
			SHC.VOID,
			"testUnary",
			new Variable[]{a, ptr},
			new Variable[]{result},
			unaryBody,
			1, 1
		);

		String unaryOutput = printer.prettyPrint(unaryFunc);
		System.out.println(unaryOutput);
		System.out.println("END TEST 4 ======================== END");
	}
}
