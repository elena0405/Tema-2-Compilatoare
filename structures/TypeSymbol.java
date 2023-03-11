package cool.structures;

import java.util.ArrayList;
import java.util.Objects;

public class TypeSymbol extends Symbol {
    private String parentName;
    private TypeSymbol parentSymbol;

    private Scope scope;

    private ArrayList<TypeSymbol> hierarchy;

    public ArrayList<TypeSymbol> getHierarchy() {
        return hierarchy;
    }

    public TypeSymbol(String name, String parent) {
        super(name);
        this.parentName = parent;
        this.hierarchy = new ArrayList<>();

        if (name.equals("Bool") || name.equals("Int") || name.equals("String") || name.equals("SELF_TYPE")) {
            this.parentSymbol = TypeSymbol.OBJECT;
        }
    }

    public static final TypeSymbol OBJECT = new TypeSymbol("Object", null);
    public static final TypeSymbol INT = new TypeSymbol("Int", "Object");
    public static final TypeSymbol BOOL = new TypeSymbol("Bool", "Object");
    public static final TypeSymbol STRING = new TypeSymbol("String", "Object");
    public static final TypeSymbol SELF_TYPE = new TypeSymbol("SELF_TYPE", "Object");

    public void setParent(TypeSymbol parent) {
        this.parentSymbol = parent;
    }

    public String getParentName() {
        return this.parentName;
    }

    public TypeSymbol getParentSymbol() {
        return this.parentSymbol;
    }

    public Scope getScope() {
        return this.scope;
    }

    public void setScope(Scope scope) {
        this.scope = scope;
    }

    public void createHierarchy() {
        TypeSymbol typeSymbol = this;

        while (true) {
            hierarchy.add(typeSymbol);

            if (typeSymbol == TypeSymbol.OBJECT) {
                break;
            }

            if (typeSymbol.getParentSymbol() != null) {
                typeSymbol = typeSymbol.getParentSymbol();
            } else {
                break;
            }
        }
    }
}
