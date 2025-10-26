/**
 * Function class for SHC language.
 *
 * @author Alec Kingsley
 */
public final class Variable extends Parsable {
	/** the name of the variable */
	private String name;
	/** type of the variable */
	private SHC type;
	/** number of references */
	private int nReferences;

	/**
	 * Constructor for the {@code Variable} class.
	 *
	 * @param name        - the name of the variable
	 * @param type        - the type of the variable
	 * @param nReferences - the number of references away from the data
	 * @param lineIdx     - index of the line {@code Parsable} starts at
	 * @param charIdx     - char index in the line {@code Parsable} starts at
	 */
	public Variable(String name, SHC type, int nReferences, int lineIdx, int charIdx) {
		super(lineIdx, charIdx);
		this.name = name;
		this.type = type;
		this.nReferences = nReferences;
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
	 * Getter method for {@code nReferences}
	 */
	public int getNReferences() {
		return nReferences;
	}

	/**
	 * toString override.
	 */
	@Override
	public String toString() {
		return name;
	}
}
