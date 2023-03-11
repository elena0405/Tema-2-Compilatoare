package cool.structures;

import org.antlr.v4.runtime.Token;

// Aici retin simbolul aferent unei functii.
public class FunctionSymbol extends Symbol{
    // Aici retin tipul clasei din care face parte functia.
    TypeSymbol type;

    // Aici retin scope-ul functiei.
    Scope scope;

    // Aici retin token-ul aferent tipului functiei.
    Token returnedType;

    // Aici retin tipul functiei.
    TypeSymbol typeSymbol;

    public TypeSymbol getTypeSymbol() {
        return typeSymbol;
    }

    public void setTypeSymbol(TypeSymbol typeSymbol) {
        this.typeSymbol = typeSymbol;
    }

    public Scope getScope() {
        return scope;
    }

    public void setScope(Scope scope) {
        this.scope = scope;
    }

    public Token getReturnedType() {
        return returnedType;
    }

    public void setReturnedType(Token returnedType) {
        this.returnedType = returnedType;
    }

    public FunctionSymbol(String name) {
        super(name);
    }

    public TypeSymbol getType() {
        return type;
    }

    public void setType(TypeSymbol type) {
        this.type = type;
    }
}
