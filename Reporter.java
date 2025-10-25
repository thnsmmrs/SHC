public class Reporter {
	/** the name of the source file */
	private String filename;

	/** escape codes for some colors */
	private final String RED = "\u001B[31m";
	private final String YELLOW = "\u001B[33m";
	private final String RESET = "\u001B[0m";

	/**
	 * Constructor.
	 *
	 * @param filename - the name of the source file
	 */
	public Reporter(String filename) {
		this.filename = filename;
	}

	/**
	 * Move the {@code SHCScanner} to the token at {@code (lineIdx, charIdx)}.
	 *
	 * @param scanner - the scanner to move
	 * @param lineIdx - line of token
	 * @param charIdx - char of token
	 */
	private void moveSHCScannerToPos(SHCScanner scanner, int lineIdx, int charIdx) {
		while (scanner.getLineIdx() != lineIdx && scanner.currentToken() != SHC.EOS) {
			scanner.nextToken();
		}
		while (scanner.getCharIdx() != charIdx && scanner.currentToken() != SHC.EOS) {
			scanner.nextToken();
		}
		if (scanner.currentToken() == SHC.EOS) {
			throw new RuntimeException("failed to move scanner to position");
		}
	}

	/**
	 * Print an error to the screen.
	 *
	 * @param error - error to print
	 */

	public void printError(String error) {
		System.out.println(RED + "ERROR: " + RESET + error);
	}

	/**
	 * Print an warning to the screen.
	 *
	 * @param warning - error to print
	 */

	public void printWarning(String warning) {
		System.out.println(YELLOW + "WARNING: " + RESET + warning);
	}

	/**
	 * Report an error to the screen, regarding the token at
	 * {@code (lineIdx, charIdx)}.
	 *
	 * @param error - error to print
	 * @param lineIdx - line of the offending token
	 * @param charIdx - char of the offending token
	 */
	public void tokenError(String error, int lineIdx, int charIdx) {
		var scanner = new SHCScanner(filename);
		printError(error);
		System.out.println("At line " + lineIdx + ", character " + charIdx);
		moveSHCScannerToPos(scanner, lineIdx, charIdx);
		String line = scanner.getCurrentTokenLine();
		System.out.print(line.substring(0, charIdx));
		// highlight offending token in red
		System.out.print(RED);
		int endingLineIdx = scanner.getEndingLineIdx();
		int endingCharIdx = scanner.getEndingCharIdx();
		if (lineIdx == endingLineIdx - 1 && endingCharIdx == 0) {
			endingLineIdx--;
			endingCharIdx = line.length();
		}
		if (endingLineIdx == lineIdx) {
			// it's all on the same line
			System.out.println(line.substring(charIdx, endingCharIdx) + RESET + line.substring(endingCharIdx));
		} else {
			// it's on multiple lines
			System.out.println(line.substring(charIdx) + "\n..." + RESET);
		}
	}

	/**
	 * Report a warning to the screen, regarding the token at
	 * {@code (lineIdx, charIdx)}.
	 *
	 * @param warning - error to print
	 * @param lineIdx - line of the offending token
	 * @param charIdx - char of the offending token
	 */
	public void tokenWarning(String warning, int lineIdx, int charIdx) {
		var scanner = new SHCScanner(filename);
		printWarning(warning);
		System.out.println("At line " + lineIdx + ", character " + charIdx);
		moveSHCScannerToPos(scanner, lineIdx, charIdx);
		String line = scanner.getCurrentTokenLine();
		System.out.print(line.substring(0, charIdx));
		// highlight offending token in yellow
		System.out.print(YELLOW);
		int endingLineIdx = scanner.getEndingLineIdx();
		int endingCharIdx = scanner.getEndingCharIdx();
		if (lineIdx == endingLineIdx - 1 && endingCharIdx == 0) {
			endingLineIdx--;
			endingCharIdx = line.length();
		}
		if (endingLineIdx == lineIdx) {
			// it's all on the same line
			System.out.println(line.substring(charIdx, endingCharIdx) + RESET + line.substring(endingCharIdx));
		} else {
			// it's on multiple lines
			System.out.println(line.substring(charIdx) + "\n..." + RESET);
		}
	}


}
