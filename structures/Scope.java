package cool.structures;

import org.antlr.runtime.Token;

public interface Scope {
    boolean add(Symbol sym);

    boolean add_function(Symbol symbol);

    boolean add_parameter(Symbol symbol);

    Symbol lookupSymbol(String str);

    Symbol lookup_parameter(String str);

    Symbol lookupFunction(String str);
    
    Scope getParent();

    void add_variable_and_type(IdSymbol variable);
}
