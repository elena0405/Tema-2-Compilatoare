package cool.AST;

import cool.structures.*;

/**
 * In aceasta clasa implementez prima trecere prin arbore,
 * in care rezolv problemele de definire de variabile sau de functii,
 * in principiu.
 */
public class DefinitionPassVisitor implements ASTVisitor<TypeSymbol> {
    // Aici retin scope-ul curent.
    private Scope currentScope = null;

    // Aici retin tipul curent.
    private TypeSymbol currentType = null;

    /**
     * Aici implementez prima trecere pentru o variabila de tip Id.
     * @param id reprezinta variabila
     * @return intoarce null
     */
    @Override
    public TypeSymbol visit(Id id) {
        id.setScope(currentScope);

        return null;
    }

    @Override
    public TypeSymbol visit(Int intt) {
        return null;
    }

    @Override
    public TypeSymbol visit(Bool bool) {
        return null;
    }

    /**
     * Aici implementez prima trecere pentru constructia if, in care
     * dau accept pe componentele acesteia.
     * @param anIf reprezinta variabila aferenta constructiei if
     * @return intoarce null
     */
    @Override
    public TypeSymbol visit(If anIf) {
        anIf.setScope(currentScope);
        anIf.cond.accept(this);
        anIf.thenBranch.accept(this);
        anIf.elseBranch.accept(this);

        return null;
    }

    /**
     * Aici implementez prima trecere pentru program.
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
     * Implementez aici prima trecere pentru o clasa.
     * @param aClass reprezinta clasa
     * @return intoarce tipul setat clasei
     */
    @Override
    public TypeSymbol visit(Class aClass) {
        int i;
        TypeSymbol type;

        // Verific daca o clasa are un nume nepermis.
        if (aClass.class_name.getText().equals("Int") ||
            aClass.class_name.getText().equals("String") ||
            aClass.class_name.getText().equals("Bool") ||
            aClass.class_name.getText().equals("Object")) {
            SymbolTable.error(aClass.context, aClass.class_name,
                    "Class " + aClass.class_name.getText() + " is redefined");
            type = null;
        } else if (aClass.class_name.getText().equals("SELF_TYPE")) {
            // Verific daca numele clasei este SELF_TYPE.
            SymbolTable.error(aClass.context, aClass.class_name, "Class has illegal name SELF_TYPE");
            type = null;
        } else {
            // Creez un nou TypeSymbol.
            if (aClass.parent_class_name != null) {
                type = new TypeSymbol(aClass.class_name.getText(),
                        aClass.parent_class_name.getText());
            } else {
                type = new TypeSymbol(aClass.class_name.getText(), "Object");
                type.setParent(TypeSymbol.OBJECT);
            }

            // Incerc sa il adaug in tabela de simboluri.
            if (!SymbolTable.globals.add(type)) {
                // Daca exista deja acel simbol definit, atunci intorc un mesaj
                // de eroare.
                SymbolTable.error(aClass.context, aClass.class_name, "Class "
                                    + aClass.class_name.getText() + " is redefined");
                type = null;
            } else if (aClass.parent_class_name != null && (
                    aClass.parent_class_name.getText().equals("Int") ||
                            aClass.parent_class_name.getText().equals("Bool") ||
                            aClass.parent_class_name.getText().equals("String") ||
                            aClass.parent_class_name.getText().equals("SELF_TYPE"))) {
                // Verific daca clasa curenta are un nume de parinte nepermis.
                SymbolTable.error(aClass.context, aClass.parent_class_name, "Class "
                        + aClass.class_name.getText() + " has illegal parent "
                        + aClass.parent_class_name.getText());
                type = null;
            } else {
                // Daca nu apar probleme pe parcurs, setez tipul clasei curente.
                aClass.setType(type);
            }
        }

        if (type != null) {
            currentType = type;

            currentScope = new DefaultScope(currentScope);
            type.setScope(currentScope);

            ((DefaultScope)currentScope).setScope_name(aClass.class_name.getText());

            for (i = 0; i < aClass.features.size(); i = i + 1) {
                aClass.features.get(i).accept(this);
            }

            currentScope = currentScope.getParent();
        }

        // Intorc tipul setat.
        return type;
    }

    /**
     * Implementez aici definirea parametrilor unei functii.
     * @param varDecl reprezinta variabila aferenta constructiei de definire
     * @return intoarce null
     */
    @Override
    public TypeSymbol visit(VarDecl varDecl) {
        IdSymbol symbol;

        if (varDecl.var_name.getText().equals("self")) {
            SymbolTable.error(varDecl.context, varDecl.var_name,
                    "Class "
                    + ((DefaultScope)currentScope).getScope_name()
                    + " has attribute with illegal name self");
        } else {
            symbol = new IdSymbol(varDecl.var_name.getText());
            if (!currentScope.add(symbol)) {
                SymbolTable.error(varDecl.context, varDecl.var_name,
                        "Class "
                        + ((DefaultScope)currentScope).getScope_name()
                        + " redefines attribute "
                        + varDecl.var_name.getText());
            } else {

                if (varDecl.var_name instanceof Id) {
                    visit((Id) varDecl.var_name);
                }

                varDecl.setScope(currentScope);
                varDecl.setSymbol(symbol);
                symbol.setType(currentType);
                symbol.setTypeSymbol((TypeSymbol) SymbolTable.globals.lookupSymbol(varDecl.type.getText()));

                if (varDecl.value != null) {
                    varDecl.value.accept(this);
                }
            }
        }

        return null;
    }

    /**
     * Implementez aici definirea unei functii din cadrul corpului unei clase.
     * @param funcDecl reprezinta variabila aferenta constructiei functiei
     * @return intoarce null
     */
    @Override
    public TypeSymbol visit(FuncDecl funcDecl) {
        int i;
        FunctionSymbol symbol = new FunctionSymbol(funcDecl.func_name.getText());
        symbol.setType(currentType);

        if (!currentScope.add_function(symbol)) {
            SymbolTable.error(funcDecl.context, funcDecl.func_name,
                    "Class "
                    + ((DefaultScope)currentScope).getScope_name()
                    + " redefines method "
                    + funcDecl.func_name.getText());
        } else {
            funcDecl.setSymbol(symbol);

            currentScope = new DefaultScope(currentScope);
            funcDecl.setScope(currentScope);
            symbol.setScope(currentScope);
            symbol.setReturnedType(funcDecl.type);

            ((DefaultScope)currentScope).setScope_name(funcDecl.func_name.getText());

            for (i = 0; i < funcDecl.declarations.size(); i = i + 1) {
                funcDecl.declarations.get(i).accept(this);
            }

            for (i = 0; i < funcDecl.lines.size(); i = i + 1) {
                funcDecl.lines.get(i).accept(this);
            }

            currentScope = currentScope.getParent();
        }

        return null;
    }

    /**
     * Implementez aici prima trecere aferenta parametrilor unei functii.
     * @param declaration reprezinta variabila aferenta parametrului respectiv
     * @return intoarce null
     */
    @Override
    public TypeSymbol visit(Declaration declaration) {
        IdSymbol idSymbol;

        if (declaration.id.getText().equals("self")) {
            SymbolTable.error(declaration.context, declaration.id,
                    "Method "
                        + ((DefaultScope) currentScope).getScope_name()
                        + " of class "
                        + ((DefaultScope)(currentScope.getParent())).getScope_name()
                        + " has formal parameter with illegal name self");
        } else if (declaration.type.getText().equals("SELF_TYPE")) {
            SymbolTable.error(declaration.context, declaration.type,
                        "Method "
                            + ((DefaultScope) currentScope).getScope_name()
                            + " of class "
                            + ((DefaultScope)(currentScope.getParent())).getScope_name()
                            + " has formal parameter "
                            + declaration.id.getText()
                            + " with illegal type SELF_TYPE");
        } else {
            idSymbol = new IdSymbol(declaration.id.getText());
            idSymbol.setReturnedType(declaration.type);

            if (!currentScope.add_parameter(idSymbol)) {
                SymbolTable.error(declaration.context, declaration.id,
                        "Method "
                            + ((DefaultScope) currentScope).getScope_name()
                            + " of class "
                            + ((DefaultScope) (currentScope.getParent())).getScope_name()
                            + " redefines formal parameter "
                            + declaration.id.getText());
            }

            declaration.setScope(currentScope);
            declaration.setSymbol(idSymbol);
        }

        return null;
    }

    /**
     * Implementez aici prima trecere aferenta constructiei de negare
     * @param neg reprezinta variabila aferenta constructiei de negare
     * @return intoarce null
     */
    @Override
    public TypeSymbol visit(Neg neg) {
        neg.unary_op.accept(this);
        return null;
    }

    /**
     * Implementez aici prima trecere aferenta unei expresii parantezate.
     * @param paren reprezinta variabila aferenta expresiei de paramtezare
     * @return intoarce null
     */
    @Override
    public TypeSymbol visit(Paren paren) {
        paren.expression.accept(this);
        return null;
    }

    /**
     * Implementez aici constructia aferenta unei relatii operationale.
     * @param relOp reprezinta variabila aferenta expresiei relationale
     * @return intoarce null
     */
    @Override
    public TypeSymbol visit(RelOp relOp) {
        relOp.left_op.accept(this);
        relOp.right_op.accept(this);
        return null;
    }

    /**
     * Implementez aici constructia aferenta unei constructii not.
     * @param not reprezinta variabila aferenta constructiei not
     * @return intoarce null
     */
    @Override
    public TypeSymbol visit(Not not) {
        not.not_op.accept(this);
        return null;
    }

    /**
     * Implementez aici constructia de atribuire.
     * @param assign reprezinta variabila aferenta expresiei.
     * @return intoarce null
     */
    @Override
    public TypeSymbol visit(Assign assign) {
        if (assign.var_name.token.getText().equals("self")) {
            SymbolTable.error(assign.context, assign.var_name.token,
                    "Cannot assign to self");
        } else {
            assign.setScope(currentScope);
            assign.var_name.accept(this);
            assign.value.accept(this);
        }

        return null;
    }

    /**
     * Implementez aici constructia aferenta expresiei while.
     * @param aWhile reprezinta variabila aferenta expresiei while
     * @return intoarce null
     */
    @Override
    public TypeSymbol visit(While aWhile) {
        aWhile.setScope(currentScope);
        aWhile.cond.accept(this);
        aWhile.line.accept(this);

        return null;
    }

    /**
     * Implementez aici constructia aferenta expresiei let.
     * @param let reprezinta variabila aferenta constructiei let
     * @return intoarce null
     */
    @Override
    public TypeSymbol visit(Let let) {
        int i;
        LetSymbol letSymbol;

        currentScope = new DefaultScope(currentScope);

        letSymbol = new LetSymbol("Let");
        letSymbol.setScope(currentScope);
        let.setLetSymbol(letSymbol);
        for (i = 0; i < let.declarations.size(); i = i + 1) {
            let.declarations.get(i).accept(this);
        }

        let.line.accept(this);

        currentScope = currentScope.getParent();

        return null;
    }

    @Override
    public TypeSymbol visit(IsVoid isVoid) {
        return null;
    }

    @Override
    public TypeSymbol visit(Instantiation instantiation) {
        return null;
    }

    @Override
    public TypeSymbol visit(StringType stringType) {
        return null;
    }

    /**
     * Implementez aici constructia aferenta expresiei case.
     * @param aCase reprezinta variabila aferenta constructiei case
     * @return intoarce null
     */
    @Override
    public TypeSymbol visit(Case aCase) {
        int i;
        IdSymbol symbol;
        CaseSymbol caseSymbol;

        currentScope = new DefaultScope(currentScope);

        caseSymbol = new CaseSymbol("Case");
        caseSymbol.setScope(currentScope);

        for (i = 0; i < aCase.vars.size(); i = i + 1) {
            if (aCase.vars.get(i).getText().equals("self")) {
                SymbolTable.error(aCase.context, aCase.vars.get(i),
                        "Case variable has illegal name self");
            } else if (aCase.types.get(i).getText().equals("SELF_TYPE")) {
                SymbolTable.error(aCase.context, aCase.types.get(i),
                        "Case variable "
                        + aCase.vars.get(i).getText()
                        + " has illegal type SELF_TYPE");
            } else {
                symbol = new IdSymbol(aCase.vars.get(i).getText());
                symbol.setReturnedType(aCase.types.get(i));
                caseSymbol.getScope().add_variable_and_type(symbol);
            }
        }

        for (i = 0; i < aCase.branches.size(); i = i + 1) {
            aCase.branches.get(i).accept(this);
        }

        aCase.setCaseSymbol(caseSymbol);

        currentScope = currentScope.getParent();

        return null;
    }

    /**
     * Implementez aici constructia aferenta expresiei block.
     * @param block reprezinta constructia aferenta expresiei
     * @return intoarce null
     */
    @Override
    public TypeSymbol visit(Block block) {
        block.setScope(currentScope);

        for (int i = 0; i < block.lines.size(); i = i + 1) {
            block.lines.get(i).accept(this);
        }

        return null;
    }

    /**
     * Implementez aici constructia aferenta definirii parametrilor
     * constructiei let
     * @param varDeclAndInit reprezinta variabila aferenta constructiei
     * @return intoarce null
     */
    @Override
    public TypeSymbol visit(VarDeclAndInit varDeclAndInit) {
        IdSymbol idSymbol = null;
        varDeclAndInit.setScope(currentScope);

        if (varDeclAndInit.var_name.getText().equals("self")) {
            SymbolTable.error(varDeclAndInit.context,
                    varDeclAndInit.var_name,
                    "Let variable has illegal name self");
        } else {
            idSymbol = new IdSymbol(varDeclAndInit.var_name.getText());
            idSymbol.setReturnedType(varDeclAndInit.type);
            idSymbol.setTypeSymbol((TypeSymbol) SymbolTable.globals.lookupSymbol(varDeclAndInit.type.getText()));

            currentScope.add_parameter(idSymbol);
        }
        
        varDeclAndInit.setIdSymbol(idSymbol);

        if (varDeclAndInit.value != null) {
            varDeclAndInit.value.accept(this);
        }

        return null;
    }

    /**
     * Implementez aici prima trecere pentru static dispatch.
     * @param method reprezinta variabila aferenta constructiei
     *               de static dispatch
     * @return intoarce null
     */
    @Override
    public TypeSymbol visit(Method method) {
        method.setScope(currentScope);

        method.met_name.setScope(currentScope);
        method.func_name.setScope(currentScope);

        for (int i = 0; i < method.args.size(); i = i + 1) {
            method.args.get(i).setScope(currentScope);
        }

        return null;
    }

    @Override
    public TypeSymbol visit(FuncCall funcCall) {
        return null;
    }

    /**
     * Implementez aici prima trecere pentru operatia de adunare.
     * @param plus reprezinta variabila aferenta adunarii
     * @return intoarce null
     */
    @Override
    public TypeSymbol visit(Plus plus) {
        plus.left_op.accept(this);
        plus.right_op.accept(this);
        return null;
    }

    /**
     * Implementez aici prima trecere pentru operatia de scadere.
     * @param minus reprezinta variabila aferenta scaderii
     * @return intoarce null
     */
    @Override
    public TypeSymbol visit(Minus minus) {
        minus.left_op.accept(this);
        minus.right_op.accept(this);
        return null;
    }

    /**
     * Implementez aici prima trecere pentru operatia de impartire.
     * @param div reprezinta variabila aferenta impartirii
     * @return intoarce null
     */
    @Override
    public TypeSymbol visit(Div div) {
        div.left_op.accept(this);
        div.right_op.accept(this);
        return null;
    }

    /**
     * Implementez aici prima trecere pentru operatia de inmultire.
     * @param mul reprezinta variabila aferenta inmultirii
     * @return intoarce null
     */
    @Override
    public TypeSymbol visit(Mul mul) {
        mul.left_op.accept(this);
        mul.right_op.accept(this);
        return null;
    }
}
