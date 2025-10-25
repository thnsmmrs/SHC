/**
 * Variable class for SHC language.
 *
 * @author Alec Kingsley
 */
public final class Variable extends Parsable {
  /** the name of the variable */
  private String name;
  /** type of the variable */
	private SHC type;

	/**
	 * Constructor for the {@code Variable} class.
	 *
	 * @param name - the name of the variable
	 * @param type - the type of the variable
	 * @param lineIdx - index of the line {@code Parsable} starts at
	 * @param charIdx - char index in the line {@code Parsable} starts at
	 */
	public Variable(String name, SHC type, int lineIdx, int charIdx) {
		super(lineIdx, charIdx);
		this.name = name;
		this.type = type;
	}

	/** getter method for {@code name} */
	public String getName() {
		return name;
	}

	/**
	 * Getter method for {@code type}
	 */
	public SHC getType() {
		return type;
	}

	/**
	 * toString override.
	 */
	@Override
	public String toString() {
		return name;
	}
}
