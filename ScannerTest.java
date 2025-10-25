import java.io.*;

public class ScannerTest {
    public static void main(String[] args) {
        // Test cases as strings
        String[] tests = {
            "fun main() { return 42; }",      // keywords + int
            "if (x >= 10) x = x + 1;",        // identifiers + symbols + int
            "\"Hello\\nWorld\"",              // string literal with escape
            "'a'",                            // char literal
            "'\\n'",                          // escaped char literal
            "0123",                           // invalid int (leading zero)
            "/* unclosed comment",            // unclosed block comment
            "// single line comment",         // comment only
            "x && y || z"                     // logical operators
        };

        for (String input : tests) {
            System.out.println("\n=== Test: " + input + " ===");
            try {
                // Write input to temp file
                File temp = File.createTempFile("scan", ".txt");
                try (FileWriter fw = new FileWriter(temp)) {
                    fw.write(input);
                }

                // Create scanner instance
                SHCScanner scanner = new SHCScanner(temp.getAbsolutePath());

                // Iterate tokens until EOS
                while (scanner.currentToken() != SHC.EOS) {
                    System.out.println("Token: " + scanner.currentTokenString());
                    scanner.nextToken();
                }

                temp.delete();
            } catch (IOException e) {
                System.out.println("Error during test: " + e.getMessage());
            }
        }
    }
}