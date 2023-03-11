package cool.structures;

// Aici retin simbolul aferent constructiei let.
public class LetSymbol extends Symbol {
    Scope scope;

    TypeSymbol typeSymbol;

    public LetSymbol(String name) {
        super(name);
    }

    public Scope getScope() {
        return scope;
    }

    public void setScope(Scope scope) {
        this.scope = scope;
    }
}
