package cool.structures;

import org.antlr.runtime.Token;

import java.util.*;

public class DefaultScope implements Scope {

    // Aici retin simboluri ale scope-ului, care nu sunt functii sau
    // parametrii.
    private final Map<String, Symbol> symbols = new LinkedHashMap<>();

    // Aici retin simboluri aferente functiilor din scope.
    private final Map<String, Symbol> functions = new LinkedHashMap<>();

    // Aici retin parametrii aferenti functiilor si/sau constructiei let.
    private final Map<String, Symbol> parameters = new LinkedHashMap<>();

    public ArrayList<IdSymbol> getVariables() {
        return variables;
    }

    // Aici retin variabilele aferente constructiei case.
    private final ArrayList<IdSymbol> variables = new ArrayList<>();

    // Aici retin scope-ul parinte.
    private final Scope parent;

    // Aici retin numele scope-ului curent.
    private String scope_name;
    
    public DefaultScope(Scope parent) {
        this.parent = parent;
    }

    public Map<String, Symbol> getFunctions() {
        return this.functions;
    }

    public Map<String, Symbol> getParameters() {
        return this.parameters;
    }

    public String getScope_name() {
        return scope_name;
    }

    public void setScope_name(String scope_name) {
        this.scope_name = scope_name;
    }


    @Override
    public boolean add(Symbol sym) {
        // Reject duplicates in the same scope.
        if (symbols.containsKey(sym.getName()))
            return false;
        
        symbols.put(sym.getName(), sym);
        
        return true;
    }

    @Override
    public boolean add_function(Symbol sym) {
        // Reject duplicates in the same scope.
        if (functions.containsKey(sym.getName()))
            return false;

        functions.put(sym.getName(), sym);

        return true;
    }

    public boolean add_parameter(Symbol symbol) {
        // Reject duplicates in the same scope.
        if (parameters.containsKey(symbol.getName()))
            return false;

        parameters.put(symbol.getName(), symbol);

        return true;
    }

    @Override
    public Symbol lookupSymbol(String name) {
        var sym = symbols.get(name);
        
        if (sym != null)
            return sym;
        
        if (parent != null)
            return parent.lookupSymbol(name);
        
        return null;
    }

    public Symbol lookup_parameter(String name) {
        var sym = parameters.get(name);

        if (sym != null)
            return sym;

        if (parent != null)
            return parent.lookup_parameter(name);

        return null;
    }

    @Override
    public Symbol lookupFunction(String str) {
        var sym = functions.get(str);

        if (sym != null)
            return sym;

        if (parent != null)
            return parent.lookupSymbol(str);

        return null;
    }

    @Override
    public void add_variable_and_type(IdSymbol variable) {
        variables.add(variable);
    }

    @Override
    public Scope getParent() {
        return parent;
    }
    
    @Override
    public String toString() {
        return symbols.values().toString();
    }

}
