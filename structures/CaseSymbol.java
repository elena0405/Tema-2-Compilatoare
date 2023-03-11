package cool.structures;

// Aici retin simbolul pentru constructia case.
public class CaseSymbol extends Symbol {
    Scope scope;

    public CaseSymbol(String name) {
        super(name);
    }

    public Scope getScope() {
        return scope;
    }

    public void setScope(Scope scope) {
        this.scope = scope;
    }
}
