/**
 * Function class for SHC language.
 *
 * @author Alec Kingsley
 */
public final class Function extends Parsable {
    private SHC returnType;
    private int nReturnReferences; // number of leading ^ on return type
    private String name;
    private Variable[] arguments;
    private Variable[] localVariables;
    private Statement[] body;

    // New canonical ctor including return pointer depth
    public Function(SHC returnType, int nReturnReferences, String name,
                    Variable[] arguments, Variable[] localVariables, Statement[] body,
                    int lineIdx, int charIdx) {
        super(lineIdx, charIdx);
        this.returnType = returnType;
        this.nReturnReferences = Math.max(0, nReturnReferences);
        this.name = name;
        this.arguments = arguments;
        this.localVariables = localVariables;
        this.body = body;
    }

    // Back-compat ctor (no return hats) if you still call it anywhere:
    public Function(SHC returnType, String name,
                    Variable[] arguments, Variable[] localVariables, Statement[] body,
                    int lineIdx, int charIdx) {
        this(returnType, 0, name, arguments, localVariables, body, lineIdx, charIdx);
    }

    public SHC getReturnType() { return returnType; }
    public int getNReturnReferences() { return nReturnReferences; }
    public String getName() { return name; }
    public Variable[] getArguments() { return arguments; }
    public Variable[] getLocalVariables() { return localVariables; }
    public Statement[] getBody() { return body; }
}