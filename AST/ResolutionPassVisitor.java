package cool.AST;

import cool.structures.*;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * In aceasta clasa implementez cea de-a treia trecere prin arbore.
 */
public class ResolutionPassVisitor implements ASTVisitor<TypeSymbol> {

    /**
     * In aceasta metoda determin parintele comun pentru o lista de tipuri
     * data ca si parametru. Daca se intampla sa existe mai multi parinti
     * comuni, il iau pe primul care este diferit de Object.
     * @param types reprezinta lista de tipuri
     * @return intoarce parintele comun
     */
    public TypeSymbol getCommonParent(ArrayList<TypeSymbol> types) {
        int i, index = 0;
        TypeSymbol parentSymbol = null;
        ArrayList<ArrayList<TypeSymbol>> hierarchies = new ArrayList<>(new ArrayList<>());

        for (i = 0; i < types.size(); i = i + 1) {
            if (types.get(i) != null) {
                types.get(i).createHierarchy();
                hierarchies.add(types.get(i).getHierarchy());
            }
        }

        if (hierarchies.size() > 0) {
            ArrayList<TypeSymbol> results = new ArrayList<>(hierarchies.get(0));

            for (i = 0; i < types.size(); i = i + 1) {
                if (types.get(i) != null) {
                    results.retainAll(hierarchies.get(index));
                    index = index + 1;
                }
            }

            if (results.size() > 0) {
                if (results.size() > 1) {
                    results.remove(TypeSymbol.OBJECT);
                }

                parentSymbol = results.get(0);
            }
        }

        return parentSymbol;
    }

    /**
     * In aceasta functie implementez a treia trecere pentru un id.
     * Verific id-ul in map-ul aferent denumirilor de functii, in map-ul
     * aferent denumirilor de simboluri si in map-ul aferent denumirilor
     * de parametrii, pentru scope-ul curent, ca sa acopar toate cazurile.
     * @param id reprezinta ID-ul pentru care se face trecerea.
     * @return intoarce tipul variabilei id
     */
    @Override
    public TypeSymbol visit(Id id) {
        TypeSymbol typeSymbol = null;

        if (!id.token.getText().equals("self")) {

            if (id.getScope() != null) {

                Symbol idSymbol = id.getScope().lookupSymbol(id.token.getText());

                if (idSymbol != null) {
                    Symbol new_idSymbol = id.getScope().lookup_parameter(id.token.getText());

                    if (new_idSymbol != null) {
                        id.setIdSymbol((IdSymbol) new_idSymbol);
                        typeSymbol = ((IdSymbol) new_idSymbol).getTypeSymbol();
                    } else {
                        id.setIdSymbol((IdSymbol) idSymbol);
                        typeSymbol = ((IdSymbol) idSymbol).getTypeSymbol();
                    }
                } else {
                    idSymbol = id.getScope().lookup_parameter(id.token.getText());

                    if (idSymbol == null) {
                        if (id.getScope().getParent() != null) {
                            idSymbol = id.getScope().getParent().lookupFunction(id.token.getText());

                            if (idSymbol == null) {
                                SymbolTable.error(id.context, id.token,
                                        "Undefined identifier " + id.token.getText());
                                id.setIdSymbol(null);
                            } else {
                                typeSymbol = ((FunctionSymbol) idSymbol).getTypeSymbol();
                            }
                        } else {
                            SymbolTable.error(id.context, id.token,
                                    "Undefined identifier " + id.token.getText());
                            id.setIdSymbol(null);
                        }

                    } else {
                        typeSymbol = ((IdSymbol)idSymbol).getTypeSymbol();
                    }
                }
            }
        }

        return typeSymbol;
    }

    /**
     * Implementez aici cea de-a treia trecere pentru o variabila de tip Int.
     * @param intt reprezinta variabila
     * @return intoarce tipul variabilei
     */
    @Override
    public TypeSymbol visit(Int intt) {
        return TypeSymbol.INT;
    }

    /**
     * Implementez aici cea de-a treia trecere pentru o variabila de tip Bool.
     * @param bool reprezinta variabila data ca si argument functiei
     * @return intoarce tipul variabilei
     */
    @Override
    public TypeSymbol visit(Bool bool) {
        return TypeSymbol.BOOL;
    }

    /**
     * Aici este functia in care implementez cea de-a treia trecere
     * pentru constructia if.
     * @param anIf reprezinta variabila aferenta constructiiei if
     * @return intoarce parintele comun celor doua ramuri
     */
    @Override
    public TypeSymbol visit(If anIf) {
        ArrayList<TypeSymbol> types = new ArrayList<>();
        TypeSymbol condType = anIf.cond.accept(this);
        TypeSymbol parent;

        if (condType != null && condType != TypeSymbol.BOOL) {
            SymbolTable.error(anIf.context, anIf.cond.token,
                    "If condition has type " + condType.getName() + " instead of Bool");
        }

        TypeSymbol theBranchType = anIf.thenBranch.accept(this);
        TypeSymbol elseBranchType = anIf.elseBranch.accept(this);

        if (theBranchType != null) {
            types.add(theBranchType);
        }

        if (elseBranchType != null) {
            types.add(elseBranchType);
        }

        parent = getCommonParent(types);

        return parent;
    }

    /**
     * Aici implementez programul cu care se incepe analiza
     * codului.
     * @param program reprezinta variabila aferenta programului
     * @return intoarce null
     */
    @Override
    public TypeSymbol visit(Program program) {
        int i;

        for (i = 0; i < program.classes.size(); i = i + 1) {
            program.classes.get(i).accept(this);
        }

        return null;
    }

    /**
     * In aceasta metoda implementez cea de-a treia trecere pentru o clasa,
     * vizitand liniile din continutul clasei.
     * @param aClass reprezinta variabila aferenta clasei
     * @return intoarce tipul clasei
     */
    @Override
    public TypeSymbol visit(Class aClass) {
        int i;

        for (i = 0; i < aClass.features.size(); i = i + 1) {
            aClass.features.get(i).accept(this);
        }

        return aClass.getType();
    }

    /**
     * In aceasta metoda implementez una din cele doua feature-uri
     * care alcatuiesc corpul unei clase, anume declararea de variabile,
     * precum si initializarea acesteia, daca exista.
     * @param varDecl reprezinta variabila aferenta constructiei de
     *                declarare (si de initializare)
     * @return intoarce tipul variabilei declarate (si initializare)
     */
    @Override
    public TypeSymbol visit(VarDecl varDecl) {
        TypeSymbol typeSymbol = null;
        IdSymbol idSymbol = varDecl.getSymbol();

        if (idSymbol != null) {
            if (varDecl.value != null) {
                TypeSymbol valueType = varDecl.value.accept(this);

                if (valueType != null && !idSymbol.getTypeSymbol().getName().equals(valueType.getName())) {
                    if (valueType.getParentSymbol() == null) {
                        SymbolTable.error(varDecl.context, varDecl.value.token,
                                "Type " + valueType.getName()
                                        + " of initialization expression of attribute "
                                        + varDecl.var_name.getText()
                                        + " is incompatible with declared type "
                                        + idSymbol.getTypeSymbol().getName());
                        varDecl.setSymbol(null);
                    } else if (!valueType.getParentSymbol().getName().equals(idSymbol.getTypeSymbol().getName())) {
                        SymbolTable.error(varDecl.context, varDecl.value.token,
                                "Type " + valueType.getName()
                                        + " of initialization expression of attribute "
                                        + varDecl.var_name.getText()
                                        + " is incompatible with declared type "
                                        + idSymbol.getTypeSymbol().getName());
                        varDecl.setSymbol(null);
                    }
                }
            }

            typeSymbol = idSymbol.getTypeSymbol();
        }

        return typeSymbol;
    }

    /**
     * In aceasta metoda implementez cel de-al doilea feature ce alcatuieste
     * corpul unei clase, anume declararea/definirea unei functii.
     * @param funcDecl reprezinta variabila aferenta definirii functiei
     *                 in cadrul clasei
     * @return intoarce tipul functiei
     */
    @Override
    public TypeSymbol visit(FuncDecl funcDecl) {
        int i;
        TypeSymbol returnedType = null;
        IdSymbol firstParameter, secondParameter;
        FunctionSymbol originalFunction;
        FunctionSymbol functionSymbol = funcDecl.getSymbol();

        if (functionSymbol != null) {
            if (funcDecl.getScope() != null &&
                functionSymbol.getType().getParentSymbol() != null &&
                functionSymbol.getType().getParentSymbol().getScope() != null) {
                originalFunction = (FunctionSymbol)
                        functionSymbol.getType().getParentSymbol().getScope().
                                            lookupFunction(funcDecl.func_name.getText());
                if (originalFunction != null) {
                    if (((DefaultScope)originalFunction.getScope()).getParameters().size()
                            != ((DefaultScope) functionSymbol.getScope()).getParameters().size()) {
                        SymbolTable.error(funcDecl.context, funcDecl.func_name,
                                    "Class " + ((DefaultScope) funcDecl.getScope().getParent()).getScope_name()
                                        + " overrides method "
                                        + ((DefaultScope) funcDecl.getScope()).getScope_name()
                                        + " with different number of formal parameters");
                        funcDecl.setSymbol(null);
                    } else if (!originalFunction.getReturnedType().getText().
                            equals(functionSymbol.getReturnedType().getText())) {
                        SymbolTable.error(funcDecl.context, funcDecl.type,
                                "Class " + ((DefaultScope) funcDecl.getScope().getParent()).getScope_name()
                                        + " overrides method "
                                        + ((DefaultScope) funcDecl.getScope()).getScope_name()
                                        + " but changes return type from "
                                        + originalFunction.getReturnedType().getText()
                                        + " to "
                                        + functionSymbol.getReturnedType().getText());

                        funcDecl.setSymbol(null);
                    } else {
                        Iterator<Symbol> iterator1
                                = ((DefaultScope) originalFunction.getScope()).getParameters().values().iterator();
                        Iterator<Symbol> iterator2 =
                                ((DefaultScope) functionSymbol.getScope()).getParameters().values().iterator();

                        while (iterator1.hasNext()) {
                            firstParameter = (IdSymbol)iterator1.next();
                            secondParameter = (IdSymbol) iterator2.next();

                            if (!firstParameter.getReturnedType().getText()
                                    .equals(secondParameter.getReturnedType().getText())) {
                                SymbolTable.error(funcDecl.context, secondParameter.getReturnedType(),
                                        "Class " + ((DefaultScope) funcDecl.getScope().getParent()).getScope_name()
                                                + " overrides method "
                                                + ((DefaultScope) funcDecl.getScope()).getScope_name()
                                                + " but changes type of formal parameter "
                                                + secondParameter.getName()
                                                + " from "
                                                + firstParameter.getReturnedType().getText()
                                                + " to "
                                                + secondParameter.getReturnedType().getText());

                                funcDecl.setSymbol(null);
                                break;
                            }
                        }
                    }
                }
            }

            for (i = 0; i < funcDecl.declarations.size(); i = i + 1) {
                funcDecl.declarations.get(i).accept(this);
            }

            for (i = 0; i < funcDecl.lines.size() - 1; i = i + 1) {
                funcDecl.lines.get(i).accept(this);
            }

            TypeSymbol typeSymbol = funcDecl.lines.get(i).accept(this);

            if (typeSymbol != null &&
                    functionSymbol.getTypeSymbol() != null &&
                    !typeSymbol.getName().equals(functionSymbol.getTypeSymbol().getName())) {

                if (typeSymbol.getHierarchy().size() == 0) {
                    typeSymbol.createHierarchy();
                }

                if (!typeSymbol.getHierarchy().contains(functionSymbol.getTypeSymbol())) {
                    SymbolTable.error(funcDecl.context, funcDecl.lines.get(i).token,
                            "Type " + typeSymbol.getName()
                                    + " of the body of method " + functionSymbol.getName()
                                    + " is incompatible with declared return type "
                                    + functionSymbol.getTypeSymbol().getName());
                }

                returnedType = typeSymbol;
            }
        }

        return returnedType;
    }

    /**
     * In aceasta metoda implementez cea de-a treia trecere pentru
     * definirea parametrilor unei functii, in momentul declararii acesteia.
     * @param declaration reprezinta variabila prin care avem acces la
     *                    definirea unui parametru
     * @return intoarce tipul parametrului declarat
     */
    @Override
    public TypeSymbol visit(Declaration declaration) {
        TypeSymbol typeSymbol = null;
        IdSymbol idSymbol = declaration.getSymbol();

        if (idSymbol != null) {
            typeSymbol = idSymbol.getTypeSymbol();
            idSymbol.setTypeSymbol((TypeSymbol) SymbolTable.globals.lookupSymbol(declaration.type.getText()));
        }

        return typeSymbol;
    }

    /**
     * In aceasta functie implementez cea de-a treiia trecere pentru
     * variabila de tip NEG, ce simbolizeaza negarea unei alte variabile.
     * @param neg este variabila de negare, data ca si parametru
     * @return itoarce tipul Bool
     */
    @Override
    public TypeSymbol visit(Neg neg) {
        var unary_op = neg.unary_op.accept(this);

        if (unary_op != TypeSymbol.BOOL) {
            SymbolTable.error(neg.context, neg.unary_op.token,
                    "Operand of ~ has type " + unary_op + " instead of Int");
        }

        return TypeSymbol.BOOL;
    }

    /**
     * In aceasta metoda implementez cea de-a treia trecere pentru
     * o variabila ce constituie o expresie parantezata.
     * @param paren este variabila de interes, data ca si parametru
     * @return intoarce tipul expresiei din paranteze
     */
    @Override
    public TypeSymbol visit(Paren paren) {
        return paren.expression.accept(this);
    }

    /**
     * Aici implementez cea de-a treia trecere pentru un operator
     * relational. Generez erorile aferente, precizate in enunt.
     * @param relOp reprezinta variabila de interes
     * @return intoarce tipul Bool
     */
    @Override
    public TypeSymbol visit(RelOp relOp) {
        var left = relOp.left_op.accept(this);
        var right = relOp.right_op.accept(this);

        if (left == TypeSymbol.INT && right != TypeSymbol.INT) {
            if (relOp.token.getText().equals("=")) {
                SymbolTable.error(relOp.context, relOp.token,
                        "Cannot compare " + left + " with " + right);
            } else {
                SymbolTable.error(relOp.context, relOp.right_op.token,
                        "Operand of " + relOp.token.getText()
                                + " has type " + right + " instead of " + left);
            }
        } else if (left != TypeSymbol.INT && right == TypeSymbol.INT) {
            if (relOp.token.getText().equals("=")) {
                SymbolTable.error(relOp.context, relOp.token,
                        "Cannot compare " + left + " with " + right);
            } else {
                SymbolTable.error(relOp.context, relOp.left_op.token,
                        "Operand of " + relOp.token.getText()
                                + " has type " + left + " instead of " + right);
            }

        } else if ((left == TypeSymbol.BOOL && right == TypeSymbol.STRING) ||
                (left == TypeSymbol.STRING && right == TypeSymbol.BOOL)) {
            if (relOp.token.getText().equals("=")) {
                SymbolTable.error(relOp.context, relOp.token,
                        "Cannot compare " + left + " with " + right);
            }
        }

        return TypeSymbol.BOOL;
    }

    /**
     * Aici implementez constructia not.
     * @param not reprezinta variabila de interes
     * @return intoarce tipul Bool sau null
     */
    @Override
    public TypeSymbol visit(Not not) {
        TypeSymbol typeSymbol = null;
        var not_op = not.not_op.accept(this);

        if (not_op != null) {
            if (not_op != TypeSymbol.BOOL) {
                SymbolTable.error(not.context, not.not_op.token,
                        "Operand of not has type " + not_op + " instead of Bool");
            }

            typeSymbol = TypeSymbol.BOOL;
        }


        return typeSymbol;
    }

    /**
     * Implementez aici constructia assign. Generez erorile aferente,
     * precizate in enunt.
     * @param assign reprezinta variabila pentru care se realizeaza analiza
     * @return intoarce tipul expresiei
     */
    @Override
    public TypeSymbol visit(Assign assign) {
        TypeSymbol typeSymbol = null;

        if (assign.getScope() != null) {
            var idSymbol = assign.getScope().lookupSymbol(assign.var_name.token.getText());
            var valueType = assign.value.accept(this);

            if (idSymbol == null) {
                idSymbol = assign.getScope().lookup_parameter(assign.var_name.token.getText());
            }

            if (idSymbol != null) {
                if (valueType != null &&
                        !((IdSymbol)idSymbol).getTypeSymbol().getName().equals(valueType.getName())) {
                    if (valueType.getParentSymbol() != null &&
                    !valueType.getParentSymbol().getName().equals(((IdSymbol)idSymbol).getTypeSymbol().getName())) {
                        SymbolTable.error(assign.context, assign.value.token,
                                "Type " + valueType.getName()
                                        + " of assigned expression is incompatible with declared type "
                                        + ((IdSymbol)idSymbol).getTypeSymbol().getName()
                                        + " of identifier " + idSymbol.getName());
                    } else if (valueType.getParentSymbol() == null) {
                        SymbolTable.error(assign.context, assign.value.token,
                                "Type " + valueType.getName()
                                        + " of assigned expression is incompatible with declared type "
                                        + ((IdSymbol)idSymbol).getTypeSymbol().getName()
                                        + " of identifier " + idSymbol.getName());
                    }
                }

                typeSymbol = valueType;

            }

            assign.setIdSymbol((IdSymbol) idSymbol);
        }

        return typeSymbol;
    }

    /**
     * Implementez aici constructia while, pentru care verific
     * conditiile din cerinta. Apelez metoda accept pe corpul
     * while-ului.
     * @param aWhile reprezinta variabila aferenta constructiei while
     * @return intoarce tipul Object
     */
    @Override
    public TypeSymbol visit(While aWhile) {
        TypeSymbol condType = aWhile.cond.accept(this);

        if (condType != null &&
                condType != TypeSymbol.BOOL) {
            SymbolTable.error(aWhile.context, aWhile.cond.token,
                    "While condition has type " + condType.getName()
                        + " instead of Bool");
        }

        aWhile.line.accept(this);

        return TypeSymbol.OBJECT;
    }

    /**
     * Implementez aici constructia let, pentru care apelez metoda accept
     * pe declaratiile acesteia, apoi pe corpul let-ului.
     * @param let reprezinta variabila aferenta constructiei let
     * @return intoarce tipul corpului let-ului
     */
    @Override
    public TypeSymbol visit(Let let) {
        int i;
        LetSymbol letSymbol = let.getLetSymbol();

        for (i = 0; i < let.declarations.size(); i = i + 1) {
            let.declarations.get(i).accept(this);
        }

        return let.line.accept(this);
    }

    /**
     * Implementez aici metoda isvoid, care intoarce intotdeauna
     * tipul Bool.
     * @param isVoid reprezinta variabila aferenta simbolului isvoid
     * @return intoarce tipul Bool
     */
    @Override
    public TypeSymbol visit(IsVoid isVoid) {
        return TypeSymbol.BOOL;
    }

    /**
     * Implementez aici constructia aferenta unei instantieri, ce contine
     * cuvantul cheie 'new'.
     * @param instantiation reprezinta variabila aferenta instantierii
     * @return intoarce tipul expresiei
     */
    @Override
    public TypeSymbol visit(Instantiation instantiation) {
        TypeSymbol typeSymbol = (TypeSymbol) SymbolTable.globals.lookupSymbol(instantiation.type.getText());

        if (typeSymbol == null) {
            SymbolTable.error(instantiation.context, instantiation.type,
                    "new is used with undefined type " + instantiation.type.getText());
        }

        return typeSymbol;
    }

    /**
     * Implementez aici cea de-a treia trecere pentru o variabila
     * de tipul String.
     * @param stringType reprezinta variabila
     * @return intoarce tipul String
     */
    @Override
    public TypeSymbol visit(StringType stringType) {
        return TypeSymbol.STRING;
    }

    /**
     * Implementez aici constructia case.
     * @param aCase reprezinta variabila aferenta constructiei case.
     * @return intoarce parintele comun pentru toate ramurile
     *          constructiei case
     */
    @Override
    public TypeSymbol visit(Case aCase) {
        int i;
        TypeSymbol typeSymbol, parent;
        ArrayList<TypeSymbol> types = new ArrayList<>();

        for (i = 0; i < aCase.branches.size(); i = i + 1) {
            typeSymbol = aCase.branches.get(i).accept(this);

            if (typeSymbol != null) {
                types.add(typeSymbol);
            }
        }

        parent = getCommonParent(types);

        return parent;
    }

    /**
     * Vizitez aici constructia aferenta unui bloc de instructiuni.
     * @param block - variabila ce reprezinta blocul de instructiuni
     * @return intoarce tipul ultimei linii din corpul blocului de
     *          instructiuni
     */
    @Override
    public TypeSymbol visit(Block block) {
        int i;
        TypeSymbol typeSymbol;

        for (i = 0; i < block.lines.size() - 1; i = i + 1) {
            block.lines.get(i).accept(this);
        }

        typeSymbol = block.lines.get(i).accept(this);

        return typeSymbol;
    }

    /**
     * Implementez aici declararea si initializarea unei variabile,
     * pentru constructia let.
     * @param varDeclAndInit reprezinta variabila aferenta constructiei
     * @return intoarce tipul variabilei
     */
    @Override
    public TypeSymbol visit(VarDeclAndInit varDeclAndInit) {
        TypeSymbol valueType = null;
        var varType = varDeclAndInit.type;

        if (varDeclAndInit.value != null) {
            valueType = varDeclAndInit.value.accept(this);
        }

        if (varDeclAndInit.getIdSymbol() != null) {
            if (valueType != null && !varType.getText().equals(valueType.getName())) {
                if (valueType.getParentSymbol() == null) {
                    SymbolTable.error(varDeclAndInit.context, varDeclAndInit.value.token,
                            "Type " + valueType.getName()
                                    + " of initialization expression of identifier "
                                    + varDeclAndInit.var_name.getText()
                                    + " is incompatible with declared type "
                                    + varType.getText());
                    varDeclAndInit.setIdSymbol(null);
                } else if (!valueType.getParentSymbol().getName().equals(varType.getText())) {
                    SymbolTable.error(varDeclAndInit.context, varDeclAndInit.value.token,
                            "Type " + valueType.getName()
                                    + " of initialization expression of identifier "
                                    + varDeclAndInit.var_name.getText()
                                    + " is incompatible with declared type "
                                    + varType.getText());
                    varDeclAndInit.setIdSymbol(null);
                }
            }
        }

        return valueType;
    }

    /**
     * Implementez aici constructia aferenta unui static dispatch.
     * @param method reprezinta constructia unui static dispatch
     * @return intoarce tipul unui static dispatch
     */
    @Override
    public TypeSymbol visit(Method method) {
        TypeSymbol typeSymbol = null;
        TypeSymbol varType = method.func_name.accept(this);

        if (method.type != null) {
            if (method.type.getText().equals("SELF_TYPE")) {
                SymbolTable.error(method.context, method.type,
                        "Type of static dispatch cannot be SELF_TYPE");
                typeSymbol = TypeSymbol.OBJECT;
            } else if (SymbolTable.globals.lookupSymbol(method.type.getText()) == null) {
                SymbolTable.error(method.context, method.type,
                        "Type " + method.type.getText() + " of static dispatch is undefined");
                typeSymbol = TypeSymbol.OBJECT;
            } else {
                TypeSymbol castType = (TypeSymbol) SymbolTable.globals.lookupSymbol(method.type.getText());

                if (varType != null) {
                    if (varType.getHierarchy().size() == 0) {
                        varType.createHierarchy();
                    }

                    if (!varType.getHierarchy().contains(castType)) {
                        SymbolTable.error(method.context, method.type,
                                "Type " + castType.getName()
                                        + " of static dispatch is not a superclass of type " + varType.getName());
                        typeSymbol = TypeSymbol.OBJECT;
                    } else {
                        if (!((DefaultScope)castType.getScope()).getFunctions().containsKey(method.met_name.token.getText())) {
                            SymbolTable.error(method.context, method.method_name,
                                    "Undefined method " + method.method_name.getText()
                                        + " in class " + castType.getName());
                            typeSymbol = TypeSymbol.OBJECT;
                        } else {
                            TypeSymbol methodType = method.met_name.accept(this);

                            if (methodType != null) {
                                methodType.setScope(method.scope);

                                if (((DefaultScope)methodType.getScope()).getParameters() != null &&
                                        method.args.size() != ((DefaultScope)methodType.getScope()).getParameters().size()) {
                                    SymbolTable.error(method.context, method.method_name,
                                            "Method " + method.method_name.getText()
                                                    + " of class " + varType.getName()
                                                    + " is applied to wrong number of arguments");
                                    typeSymbol = TypeSymbol.OBJECT;
                                }
                            }
                        }
                    }
                }
            }
        }

        return typeSymbol;
    }

    @Override
    public TypeSymbol visit(FuncCall funcCall) {

        return null;
    }

    /**
     * Implementez aici operatia de adunare.
     * @param plus reprezinta variabila aferenta constructiei de adunare.
     * @return intoarce tipul adunarii
     */
    @Override
    public TypeSymbol visit(Plus plus) {
        TypeSymbol typeSymbol = null;

        var left = plus.left_op.accept(this);
        var right = plus.right_op.accept(this);

        if (left != null && right != null) {
            if (left == TypeSymbol.INT && right != TypeSymbol.INT) {
                SymbolTable.error(plus.context, plus.left_op.token,
                        "Operand of + has type " + right + " instead of " + left);
                typeSymbol = right;
            } else if (left != TypeSymbol.INT && right == TypeSymbol.INT) {
                SymbolTable.error(plus.context, plus.left_op.token,
                        "Operand of + has type " + left + " instead of " + right);
                typeSymbol = left;
            } else {
                typeSymbol = TypeSymbol.INT;
            }
        }

        return typeSymbol;
    }

    /**
     * Implementez aici operatia de scadere.
     * @param minus reprezinta variabila aferenta constructiei de scadere
     * @return intoarce tipul expresiei
     */
    @Override
    public TypeSymbol visit(Minus minus) {
        TypeSymbol typeSymbol = null;

        var right = minus.right_op.accept(this);
        var left = minus.left_op.accept(this);

        if (left != null && right != null) {
            if (right == TypeSymbol.INT && left != TypeSymbol.INT) {
                SymbolTable.error(minus.context, minus.left_op.token,
                        "Operand of - has type " + left + " instead of " + right);
                typeSymbol = left;
            } else if (left == TypeSymbol.INT && right != TypeSymbol.INT) {
                SymbolTable.error(minus.context, minus.left_op.token,
                        "Operand of - has type " + right + " instead of " + left);
                typeSymbol = right;
            } else if (left == right && left != TypeSymbol.INT) {
                SymbolTable.error(minus.context, minus.right_op.token,
                        "Operand of - has type " + right + " instead of " + TypeSymbol.INT);
                typeSymbol = right;
            } else {
                typeSymbol = TypeSymbol.INT;
            }
        }

        return typeSymbol;
    }

    /**
     * Implementez aici operatia de impartire.
     * @param div reprezinta variabila aferenta constructiei de impartire
     * @return intoarce rezultatul impartirii
     */
    @Override
    public TypeSymbol visit(Div div) {
       TypeSymbol typeSymbol = null;

        var right = div.right_op.accept(this);
        var left = div.left_op.accept(this);

        if (left != null && right != null) {
            if (right == TypeSymbol.INT && left != TypeSymbol.INT) {
                SymbolTable.error(div.context, div.left_op.token,
                        "Operand of / has type " + left + " instead of " + right);
                typeSymbol = left;
            } else if (left == TypeSymbol.INT && right != TypeSymbol.INT) {
                SymbolTable.error(div.context, div.left_op.token,
                        "Operand of / has type " + right + " instead of " + left);
                typeSymbol = right;
            } else {
                typeSymbol = TypeSymbol.INT;
            }
        }

        return typeSymbol;
    }

    /**
     * Implementez aici operatia de inmultire.
     * @param mul reprezinta variabila aferenta constructiei de impartire
     * @return intoarce rezultatul impartirii
     */
    @Override
    public TypeSymbol visit(Mul mul) {
        TypeSymbol typeSymbol = null;

        var right = mul.right_op.accept(this);
        var left = mul.left_op.accept(this);

        if (left != null && right != null) {
            if (right == TypeSymbol.INT && left != TypeSymbol.INT) {
                SymbolTable.error(mul.context, mul.left_op.token,
                        "Operand of * has type " + left + " instead of " + right);
                typeSymbol = left;
            } else if (left == TypeSymbol.INT && right != TypeSymbol.INT) {
                SymbolTable.error(mul.context, mul.left_op.token,
                        "Operand of * has type " + right + " instead of " + left);
                typeSymbol = right;
            } else {
                typeSymbol = TypeSymbol.INT;
            }
        }

        return typeSymbol;
    }
}
