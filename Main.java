import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public final class Main {
  private static String sep(String title) {
    return "\n===== " + title + " ".repeat(Math.max(0, 60 - title.length())) + "=====\n";
  }

  public static void main(String[] args) {
    if (args.length == 0) {
      System.err.println("Usage: java Main [--roundtrip] <file1> [file2 ...]");
      System.exit(1);
    }

    boolean roundtrip = false;
    ArrayList<String> files = new ArrayList<>();
    for (String a : args) {
      if ("--roundtrip".equals(a))
        roundtrip = true;
      else
        files.add(a);
    }
    if (files.isEmpty()) {
      System.err.println("No input files.");
      System.exit(1);
    }

    for (String filename : files) {
      System.out.println(sep("PARSING " + filename));

      // Parse
      Parser parser = new Parser(filename);
      ArrayList<Function> funs = parser.parseProgram();

      // Pretty print each function
      PrettyPrinter pp = new PrettyPrinter();
      System.out.println(sep("PRETTY PRINT"));
      for (Function f : funs) {
        String pretty = pp.prettyPrint(f);
        System.out.print(pretty);
        if (!pretty.endsWith("\n"))
          System.out.println();
      }

      // AST print (structural view)
      System.out.println(sep("AST PRINT"));
      System.out.print(ASTPrinter.printFunctions(funs));

      if (roundtrip) {
        System.out.println(sep("ROUNDTRIP: PRETTY -> FILE -> PARSE -> AST"));

        // 1) Concatenate pretty output of all functions
        StringBuilder all = new StringBuilder();
        for (Function f : funs) {
          all.append(pp.prettyPrint(f));
          if (all.length() == 0 || all.charAt(all.length() - 1) != '\n')
            all.append('\n');
        }

        // 2) Write to a temp file
        File tmp = null;
        try {
          tmp = File.createTempFile("shc_roundtrip_", ".shc");
          try (FileWriter fw = new FileWriter(tmp)) {
            fw.write(all.toString());
          }
          // 3) Reparse from pretty output
          Parser p2 = new Parser(tmp.getAbsolutePath());
          ArrayList<Function> funs2 = p2.parseProgram();

          // 4) Print AST again to compare
          System.out.print(ASTPrinter.printFunctions(funs2));
        } catch (IOException ioe) {
          System.err.println("Roundtrip failed: " + ioe.getMessage());
        } finally {
          if (tmp != null)
            tmp.delete();
        }
      }

      System.out.println(sep("COMPILING..."));

      System.out.println("Output file: " + filename + ".c");

      Compiler.compile(funs, filename + ".c", new Reporter(filename));

      System.out.println(sep("DONE " + filename));
    }
  }
}
