import java.io.IOException;

import java.io.FileOutputStream;
import java.util.ArrayList;

public class Compiler {
  private static final String RED = "\u001B[31m";
  private static final String RESET = "\u001B[0m";

  /** output stream for this */
  private static FileOutputStream outputFileOutputStream;

  /** reporter */
  private static Reporter reporter;

  /**
   * Safely output text.
   *
   * @param text  - text to output
   * @param nTabs - number of tabs to print
   */
  private static void output(String str, int nTabs) {
    try {
      for (int i = 0; i < nTabs; i++) {
        outputFileOutputStream.write("    ".getBytes());
        System.out.print("    ");
      }
      outputFileOutputStream.write(str.getBytes());
      System.out.print(str);
    } catch (IOException e) {
      reporter.printError("failed to write to output file");
    }
  }

  /**
   * Safely output text.
   *
   * @param text - text to output
   */
  private static void output(String str) {
    output(str, 0);
  }

  /**
   * Compile a list of functions.
   *
   * @param program  - the program to compile
   * @param globals  - global variables
   * @param filename - the filename to output to
   */
  public static void compile(ArrayList<Function> program, ArrayList<Variable> globals, String filename, Reporter reporter) {
    try {
      outputFileOutputStream = new FileOutputStream(filename);
    } catch (IOException e) {
      System.out.println(RED + "ERROR: " + RESET + "Failed to open `" + filename + "`");
      System.exit(1);
    }

    output("#include <stdio.h>\n");
    output("#include <stdlib.h>\n");
    output("#include <stdint.h>\n\n");

    // Output global variables
    for (Variable var : globals) {
      switch (var.getType()) {
        case INT:
          output("uint64_t");
          break;
        case CHAR:
          output("uint8_t");
          break;
        case VOID:
          output("void");
          break;
      }
      for (int i = 0; i < var.getNReferences(); i++) {
        output(" *");
      }
      output(" " + var.getName() + ";\n");
    }
    if (globals.size() > 0) {
      output("\n");
    }

    // Output forward declarations for all functions
    for (Function function : program) {
      compileFunctionDeclaration(function);
    }
    output("\n");

    // Output function definitions
    for (Function function : program) {
      compileFunction(function);
    }
  }

  /**
   * Compile a function declaration (forward declaration).
   *
   * @param function - function to declare
   */
  public static void compileFunctionDeclaration(Function function) {
    // Special case: main() should return int for C standards compliance
    if (function.getName().equals("main") && function.getReturnType() == SHC.INT) {
      output("int ");
    } else {
      switch (function.getReturnType()) {
        case VOID:
          output("void ");
          break;
        case CHAR:
          output("uint8_t ");
          break;
        case INT:
          output("uint64_t ");
          break;
        default:
          reporter.printError("bad function return type");
          System.exit(1);
      }
    }

    for (int i = 0; i < function.getNReturnReferences(); i++) {
      output("*");
    }
    output(function.getName());
    output("(");
    Variable[] arguments = function.getArguments();

    // Special case: main(argc, argv) should use standard C types
    boolean isMain = function.getName().equals("main");
    if (isMain && arguments.length >= 1) {
      // First parameter (argc) should be int
      output("int " + arguments[0].getName());
      if (arguments.length >= 2) {
        // Second parameter (argv) should be char**
        output(", char **" + arguments[1].getName());
      }
      // Any additional parameters use normal types
      for (int i = 2; i < arguments.length; i++) {
        output(", ");
        compileVariable(arguments[i]);
      }
    } else {
      // Normal function parameters
      for (int i = 0; i < arguments.length; i++) {
        compileVariable(arguments[i]);
        if (i < arguments.length - 1) {
          output(", ");
        }
      }
    }
    output(");");
    output("\n");
  }

  /**
   * Compile a function.
   *
   * @param function - function to compile
   */
  public static void compileFunction(Function function) {
    // Special case: main() should return int for C standards compliance
    if (function.getName().equals("main") && function.getReturnType() == SHC.INT) {
      output("int ");
    } else {
      switch (function.getReturnType()) {
        case VOID:
          output("void ");
          break;
        case CHAR:
          output("uint8_t ");
          break;
        case INT:
          output("uint64_t ");
          break;
        default:
          reporter.printError("bad function return type");
          System.exit(1);
      }
    }

    for (int i = 0; i < function.getNReturnReferences(); i++) {
      output("*");
    }
    output(function.getName());
    output("(");
    Variable[] arguments = function.getArguments();

    // Special case: main(argc, argv) should use standard C types
    boolean isMain = function.getName().equals("main");
    if (isMain && arguments.length >= 1) {
      // First parameter (argc) should be int
      output("int " + arguments[0].getName());
      if (arguments.length >= 2) {
        // Second parameter (argv) should be char**
        output(", char **" + arguments[1].getName());
      }
      // Any additional parameters use normal types
      for (int i = 2; i < arguments.length; i++) {
        output(", ");
        compileVariable(arguments[i]);
      }
    } else {
      // Normal function parameters
      for (int i = 0; i < arguments.length; i++) {
        compileVariable(arguments[i]);
        if (i < arguments.length - 1) {
          output(", ");
        }
      }
    }
    output(")");
    output(" {\n");
    for (Statement statement : function.getBody()) {
      compileStatement(statement, 1);
    }

    output("}\n");
  }

  /**
   * Compile a statement.
   *
   * @param statement - statement to compile
   * @param nTabs     - number of tabs to print
   */
  public static void compileStatement(Statement statement, int nTabs) {
    switch (statement) {
      case Statement.If ifStatement:
        compileIfStatement(ifStatement, nTabs);
        break;
      case Statement.Loop loopStatement:
        compileLoopStatement(loopStatement, nTabs);
        break;
      case Statement.Decl declStatement:
        compileDeclStatement(declStatement, nTabs);
        break;
      case Statement.Call callStatement:
        compileCallStatement(callStatement, nTabs);
        break;
      case Statement.Jump jumpStatement:
        compileJumpStatement(jumpStatement, nTabs);
        break;
      case Statement.Assign assignStatement:
        output("", nTabs);
        compileAssignment(assignStatement.assignment());
        output(";\n");
        break;
    }
  }

  /**
   * Compile an if statement.
   *
   * @param statement - statement to compile
   * @param nTabs     - number of tabs to print
   */
  public static void compileIfStatement(Statement.If statement, int nTabs) {
    output("if (", nTabs);
    compileFactor(statement.cond());
    output(")");
    output(" {\n");
    for (Statement subStatement : statement.body()) {
      compileStatement(subStatement, nTabs + 1);
    }
    // Only generate else block if it has statements
    if (statement.otherBody() == null || statement.otherBody().length == 0) {
      output("}\n", nTabs);
    } else {
      output("} else {\n", nTabs);
      for (Statement subStatement : statement.otherBody()) {
        compileStatement(subStatement, nTabs + 1);
      }
      output("}\n", nTabs);
    }
  }

  /**
   * Compile a loop statement.
   *
   * @param statement - statement to compile
   * @param nTabs     - number of tabs to print
   */
  public static void compileLoopStatement(Statement.Loop statement, int nTabs) {
    output("while (", nTabs);
    compileFactor(statement.cond());
    output(")");
    output(" {\n");
    for (Statement subStatement : statement.body()) {
      compileStatement(subStatement, nTabs + 1);
    }
    output("}\n", nTabs);
  }

  /**
   * Compile a declaration statement.
   *
   * @param statement - statement to compile
   * @param nTabs     - number of tabs to print
   */
  public static void compileDeclStatement(Statement.Decl statement, int nTabs) {
    output("", nTabs);
    compileVariable(statement.variable());
    output(";\n");
  }

  /**
   * Compile a call statement.
   *
   * @param statement - statement to compile
   * @param nTabs     - number of tabs to print
   */
  public static void compileCallStatement(Statement.Call statement, int nTabs) {
    output(statement.function().getName(), nTabs);
    output("(");
    Expression[] arguments = statement.arguments();
    for (int i = 0; i < arguments.length; i++) {
      compileExpression(arguments[i]);
      if (i < arguments.length - 1) {
        output(", ");
      }
    }
    output(");\n");
  }

  /**
   * Compile a jump statement.
   *
   * @param statement - statement to compile
   * @param nTabs     - number of tabs to print
   */
  public static void compileJumpStatement(Statement.Jump statement, int nTabs) {
    switch (statement.type()) {
      case RETURN:
        if (statement.hasValue()) {
          output("return ", nTabs);
          compileExpression(statement.value());
          output(";\n");
        } else {
          output("return;\n", nTabs);
        }
        break;
      case CONTINUE:
        output("continue;\n", nTabs);
        break;
      case BREAK:
        output("break;\n", nTabs);
        break;
      default:
        reporter.printError("bad jump statement type");
        System.exit(1);
    }
  }

  /**
   * Compile an expression.
   *
   * @param expression - expression to compile
   */
  public static void compileExpression(Expression expression) {
    var expressions = expression.getExpressions();
    for (int i = 0; i < expressions.length; i++) {
      compileAssignment(expressions[i]);
      if (i < expressions.length - 1) {
        output(", ");
      }
    }
  }

  /**
   * Compile an assignment.
   *
   * @param assignment - assignment to compile
   */
  public static void compileAssignment(Assignment assignment) {
    if (assignment.hasAssignee()) {
      var assignee = assignment.getAssignee();
      // Same logic as Factor.Var: number of stars = assigneeRefs
      // Examples:
      // - `temp = value` where `temp : ^char` → C: `temp = value` (0 stars)
      // - `^temp = value` where `temp : ^char` → C: `*temp = value` (1 star)
      int varRefs = assignee.getVariable().getNReferences();
      int assigneeRefs = assignee.getNReferences();

      if (varRefs == 0 && assigneeRefs == 1) {
        // Special case: taking address in assignment (very rare)
        output("&");
      } else {
        // General case: output assigneeRefs stars for dereferencing
        for (int i = 0; i < assigneeRefs; i++) {
          output("*");
        }
      }

      output(assignee.getVariable().getName());
      output(" = ");
    }
    compileOrExpression(assignment.getValue());
  }

  /**
   * Compile an or expression.
   *
   * @param expression - expression to compile
   */
  public static void compileOrExpression(OrExpression expression) {
    if (expression.hasLeft()) {
      var left = expression.getLeft();
      compileOrExpression(left);
      output(" || ");
    }
    var right = expression.getRight();
    compileAndExpression(right);
  }

  /**
   * Compile an and expression.
   *
   * @param expression - expression to compile
   */
  public static void compileAndExpression(AndExpression expression) {
    if (expression.hasLeft()) {
      var left = expression.getLeft();
      compileAndExpression(left);
      output(" && ");
    }
    var right = expression.getRight();
    compileEqualityExpression(right);
  }

  /**
   * Compile an equality expression.
   *
   * @param expression - expression to compile
   */
  public static void compileEqualityExpression(EqualityExpression expression) {
    if (expression.hasLeft()) {
      var left = expression.getLeft();
      compileEqualityExpression(left);
      output(expression.getOperator() == SHC.EQUAL ? " == " : " != ");
    }
    var right = expression.getRight();
    compileRelationalExpression(right);
  }

  /**
   * Compile a relational expression.
   *
   * @param expression - expression to compile
   */
  public static void compileRelationalExpression(RelationalExpression expression) {
    if (expression.hasLeft()) {
      var left = expression.getLeft();
      compileRelationalExpression(left);

      output(switch (expression.getOperator()) {
        case SHC.LESS -> " < ";
        case SHC.GREATER -> " > ";
        case SHC.LEQ -> " <= ";
        case SHC.GEQ -> " >= ";
        default -> {
          reporter.printError("invalid relational expression operator");
          yield " ERROR ";
        }
      });
    }
    var right = expression.getRight();
    compileAdditiveExpression(right);
  }

  /**
   * Compile an additive expression.
   *
   * @param expression - expression to compile
   */
  public static void compileAdditiveExpression(AdditiveExpression expression) {
    if (expression.hasLeft()) {
      var left = expression.getLeft();
      compileAdditiveExpression(left);
      output(expression.getOperator() == SHC.ADD ? " + " : " - ");
    }
    var right = expression.getRight();
    compileMultiplicativeExpression(right);
  }

  /**
   * Compile a multiplicative expression.
   *
   * @param expression - expression to compile
   */
  public static void compileMultiplicativeExpression(MultiplicativeExpression expression) {
    if (expression.hasLeft()) {
      var left = expression.getLeft();
      compileMultiplicativeExpression(left);
      var op = expression.getOperator();
      output(op == SHC.MULTIPLY ? " * " : op == SHC.DIVIDE ? " / " : " % ");
    }
    var right = expression.getRight();
    compileUnaryExpression(right);
  }

  /**
   * Compile a unary expression.
   *
   * @param expression - expression to compile
   */
  public static void compileUnaryExpression(UnaryExpression expression) {
    if (expression.hasOperator()) {
      output(expression.getOperator() == SHC.ADD ? "+" : "-");
      compileUnaryExpression(expression.getUnaryExpression());
    } else {
      compileFactor(expression.getFactor());
    }
  }

  /**
   * Compile a factor.
   *
   * @param factor - factor to compile
   */
  public static void compileFactor(Factor factor) {
    switch (factor) {
      case Factor.Var varFactor:
        // In SHC: `^` is used for both address-of and dereference
        // The semantics depend on the variable's declared type:
        // - varRefs = number of `^` in variable declaration (e.g., `x : ^^int` has varRefs=2)
        // - usageRefs = number of `^` in usage (e.g., `^^x` has usageRefs=2)
        //
        // Examples:
        // - `x : int`, use `^x` → taking address → C: `&x`
        // - `ptr : ^int`, use `^ptr` → dereferencing → C: `*ptr`
        // - `ptr : ^int`, use `ptr` → plain usage → C: `ptr`
        int varRefs = varFactor.getVariable().getNReferences();
        int usageRefs = varFactor.getNReferences();

        if (varRefs == 0 && usageRefs == 1) {
          // Special case: taking address of a non-pointer variable
          output("&");
        } else {
          // General case: output usageRefs stars for dereferencing
          for (int i = 0; i < usageRefs; i++) {
            output("*");
          }
        }

        output(varFactor.getVariable().getName());
        break;
      case Factor.Const constFactor:
        output("" + constFactor.constant());
        break;
      case Factor.Str strFactor:
        // Escape special characters for C string literal
        String escaped = strFactor.string()
          .replace("\\", "\\\\")
          .replace("\n", "\\n")
          .replace("\r", "\\r")
          .replace("\t", "\\t")
          .replace("\"", "\\\"");
        output("\"" + escaped + "\"");
        break;
      case Factor.Parentheses parenthesesFactor:
        output("(");
        compileExpression(parenthesesFactor.expression());
        output(")");
        break;
      case Factor.Call callFactor:
        output(callFactor.fun().getName());
        var arguments = callFactor.arguments();
        output("(");
        for (int i = 0; i < arguments.length; i++) {
          compileExpression(arguments[i]);
          if (i < arguments.length - 1) {
            output(", ");
          }
        }
        output(")");
        break;
    }
  }

  /**
   * Compile a variable.
   *
   * @param variable - variable to compile
   */
  public static void compileVariable(Variable variable) {
    switch (variable.getType()) {
      case VOID:
        output("void ");
        break;
      case CHAR:
        output("uint8_t ");
        break;
      case INT:
        output("uint64_t ");
        break;
      default:
        reporter.printError("bad variable type");
        System.exit(1);
    }
    for (int i = 0; i < variable.getNReferences(); i++) {
      output("*");
    }
    output(variable.getName());
  }
}
