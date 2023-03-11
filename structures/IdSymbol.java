package cool.structures;

import org.antlr.v4.runtime.Token;

// Aici retin simbolul pentru in id.
public class IdSymbol extends Symbol{
    // Aici retin tipul parinte al id-ului.
    TypeSymbol type;

    // Aici retin tipul simbolului.
    TypeSymbol typeSymbol;

    public TypeSymbol getTypeSymbol() {
        return typeSymbol;
    }

    public void setTypeSymbol(TypeSymbol typeSymbol) {
        this.typeSymbol = typeSymbol;
    }

    // Aici retin token-ul aferent tipului simbolului.
    Token returnedType;

    public Token getReturnedType() {
        return returnedType;
    }

    public void setReturnedType(Token returnedType) {
        this.returnedType = returnedType;
    }

    public IdSymbol(String name) {
        super(name);
    }

    public TypeSymbol getType() {
        return type;
    }

    public void setType(TypeSymbol type) {
        this.type = type;
    }
}
