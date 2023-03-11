package cool.AST;

import cool.structures.*;

/**
 * Aceasta clasa constituie cea de-a doua trecere prin arbore, in care
 * rezolv problema ierarhiei de clase si cateva alte probleme de tipare.
 */
public class HierarchyPassVisitor implements ASTVisitor<Void>{
    @Override
    public Void visit(Id id) {
        return null;
    }

    @Override
    public Void visit(Int intt) {
        return null;
    }

    @Override
    public Void visit(Bool bool) {
        return null;
    }

    @Override
    public Void visit(If anIf) {
        return null;
    }

    /**
     * Iplementez aici cea de-a doua trecere pentru programul in sine.
     * @param program reprezinta variabila aferenta programului
     * @return intoarce null
     */
    @Override
    public Void visit(Program program) {
        int i;

        for (i = 0; i < program.classes.size(); i = i + 1) {
            visit(program.classes.get(i));
        }

        return null;
    }

    /**
     * Implementez aici a doua trecere pentru o clasa.
     * @param aClass reprezinta clasa pentru care se face trecerea
     * @return intoarce null
     */
    @Override
    public Void visit(Class aClass) {
        int i;
        Symbol parent;
        TypeSymbol type = aClass.getType();

        while (true) {
            if (type != null && type.getParentName() != null) {
                if (type.getParentName().equals("Object")) {
                    type.setParent(TypeSymbol.OBJECT);
                    break;
                }

                parent = SymbolTable.globals.lookupSymbol(type.getParentName());
                if (parent == null) {
                    SymbolTable.error(aClass.context, aClass.parent_class_name,
                            "Class " + aClass.class_name.getText()
                                         + " has undefined parent "
                                         + aClass.parent_class_name.getText());
                    break;
                } else if (parent.getName().equals(aClass.type.getName())) {
                    type.setParent((TypeSymbol) parent);
                    SymbolTable.error(aClass.context, aClass.class_name,
                            "Inheritance cycle for class " + aClass.class_name.getText());
                    break;
                } else {
                    type.setParent((TypeSymbol) parent);
                    type = type.getParentSymbol();
                }
            } else {
                break;
            }
        }

        for (i = 0; i < aClass.features.size(); i = i + 1){
            aClass.features.get(i).accept(this);
        }

        return null;
    }

    /**
     * Implementez aici a doua trecere pentru declararea (si intializarea)
     * unei variabile, din cadrul corpului unei clase.
     * @param varDecl reprezinta variabila aferenta constructiei
     *                de declarare
     * @return intoarce null
     */
    @Override
    public Void visit(VarDecl varDecl) {
        IdSymbol symbol = varDecl.getSymbol();

        if (symbol != null) {
            TypeSymbol typeSymbol = symbol.getType();

            while (true) {
                if (typeSymbol == null || typeSymbol.getParentName() == null) {
                    break;
                } else {
                    if (typeSymbol.getParentSymbol() != null &&
                            typeSymbol.getParentSymbol().getScope() != null &&
                            typeSymbol.getParentSymbol().getScope().lookupSymbol(varDecl.var_name.getText()) != null) {
                            SymbolTable.error(varDecl.context, varDecl.var_name,
                                "Class "
                                        + ((DefaultScope)varDecl.getScope()).getScope_name()
                                        + " redefines inherited attribute "
                                        + varDecl.var_name.getText());
                        varDecl.setSymbol(null);
                        break;
                    }

                    typeSymbol = typeSymbol.getParentSymbol();
                }
            }

            if (varDecl.getScope() != null &&
                    SymbolTable.globals.lookupSymbol(varDecl.type.getText()) == null) {
                    SymbolTable.error(varDecl.context, varDecl.type, "Class "
                        + ((DefaultScope)varDecl.getScope()).getScope_name()
                        + " has attribute " + varDecl.var_name.getText()
                        + " with undefined type "
                        + varDecl.type.getText());
                varDecl.setSymbol(null);
            } else {
                symbol.setTypeSymbol((TypeSymbol) SymbolTable.globals.lookupSymbol(varDecl.type.getText()));

                if (varDecl.value != null) {
                    varDecl.value.accept(this);
                }
            }
        }

        return null;
    }

    /**
     * Implementez aici cea de-a doua trecere pentru definitia unei functii.
     * @param funcDecl reprezinta variabila aferenta definitiei functiei
     * @return intoarce null
     */
    @Override
    public Void visit(FuncDecl funcDecl) {
        int i;
        FunctionSymbol functionSymbol = funcDecl.getSymbol();

        if (functionSymbol != null) {
            functionSymbol.setTypeSymbol((TypeSymbol) SymbolTable.globals.lookupSymbol(funcDecl.type.getText()));
            if (funcDecl.getScope() != null &&
                    SymbolTable.globals.lookupSymbol(functionSymbol.getType().getName()) == null) {
                    SymbolTable.error(funcDecl.context, funcDecl.type, "Class "
                        + ((DefaultScope)funcDecl.getScope()).getScope_name()
                        + " has method " + funcDecl.func_name.getText()
                        + " with undefined return type "
                        + funcDecl.type.getText());
                funcDecl.setSymbol(null);
            }

            for (i = 0; i < funcDecl.declarations.size(); i++) {
                funcDecl.declarations.get(i).accept(this);
            }

            for (i = 0; i < funcDecl.lines.size(); i = i + 1) {
                funcDecl.lines.get(i).accept(this);
            }
        }

        return null;
    }

    /**
     * Implementez aici a doua trecere pentru declararea de
     * parametrii in cadrul definirii unei functii.
     * @param declaration reprezinta variabila pentru care se face
     *                    analiza celei de-a doua treceri
     * @return intoarce null
     */
    @Override
    public Void visit(Declaration declaration) {
        IdSymbol idSymbol = declaration.getSymbol();

        if (idSymbol != null) {
            if (declaration.getScope() != null &&
                    SymbolTable.globals.lookupSymbol(declaration.type.getText()) == null) {
                    SymbolTable.error(declaration.context, declaration.type,
                        "Method "
                            + ((DefaultScope) declaration.scope).getScope_name()
                            + " of class "
                            + ((DefaultScope) declaration.scope.getParent()).getScope_name()
                            + " has formal parameter " + idSymbol.getName()
                            + " with undefined type " + declaration.type.getText());
                    declaration.setSymbol(null);
            }
        }

        return null;
    }

    @Override
    public Void visit(Neg neg) {
        return null;
    }

    @Override
    public Void visit(Paren paren) {
        return null;
    }

    @Override
    public Void visit(RelOp relOp) {
        return null;
    }

    @Override
    public Void visit(Not not) {
        return null;
    }

    @Override
    public Void visit(Assign assign) {
        return null;
    }

    @Override
    public Void visit(While aWhile) {
        return null;
    }

    /**
     * Implementez a doua trecere pentru constructia let.
     * @param let reprezinta variabila aferenta constructiei let.
     * @return intoarce null
     */
    @Override
    public Void visit(Let let) {
        int i;

        for (i = 0; i < let.declarations.size(); i = i + 1) {
            let.declarations.get(i).accept(this);
        }

        let.line.accept(this);

        return null;
    }

    @Override
    public Void visit(IsVoid isVoid) {
        return null;
    }

    @Override
    public Void visit(Instantiation instantiation) {
        return null;
    }

    @Override
    public Void visit(StringType stringType) {
        return null;
    }

    /**
     * Implementez a doua trecere pentru constructia case.
     * @param aCase reprezinta variabila aferenta constructiei case
     * @return intoarce null
     */
    @Override
    public Void visit(Case aCase) {
        int i;
        IdSymbol idSymbol;
        CaseSymbol caseSymbol = aCase.getCaseSymbol();

        if (caseSymbol != null) {
            for (i = 0; i < ((DefaultScope)caseSymbol.getScope()).getVariables().size(); i = i + 1) {
                idSymbol = ((DefaultScope)caseSymbol.getScope()).getVariables().get(i);

                if (SymbolTable.globals.lookupSymbol(idSymbol.getReturnedType().getText()) == null) {
                    SymbolTable.error(aCase.context, idSymbol.getReturnedType(),
                            "Case variable "
                                    + idSymbol.getName()
                                    + " has undefined type "
                                    + idSymbol.getReturnedType().getText());
                    aCase.setCaseSymbol(null);
                }
            }
        }

        return null;
    }

    @Override
    public Void visit(Block block) {
        return null;
    }

    /**
     * Implementez a doua trecere pentru declararea si
     * initializarea unei variabile, in cadrul constructiei let.
     * @param varDeclAndInit reprezinta variabila aferenta constructiei let
     * @return intoarce null
     */
    @Override
    public Void visit(VarDeclAndInit varDeclAndInit) {
        IdSymbol idSymbol = varDeclAndInit.getIdSymbol();

        if (idSymbol != null) {
            if (SymbolTable.globals.lookupSymbol(idSymbol.getReturnedType().getText()) == null) {
                SymbolTable.error(varDeclAndInit.context, varDeclAndInit.type,
                        "Let variable "
                        + idSymbol.getName()
                        + " has undefined type "
                        + idSymbol.getReturnedType().getText());
                varDeclAndInit.setIdSymbol(null);
            }

            if (varDeclAndInit.value instanceof Id) {
                if (SymbolTable.globals.lookupSymbol(varDeclAndInit.value.name) == null) {
                    SymbolTable.error(varDeclAndInit.context, varDeclAndInit.value.token,
                            "Undefined identifier " + varDeclAndInit.value.token.getText());
                    varDeclAndInit.setIdSymbol(null);
                }
            }
        }

        return null;
    }

    @Override
    public Void visit(Method method) {

        return null;
    }

    @Override
    public Void visit(FuncCall funcCall) {
        return null;
    }

    @Override
    public Void visit(Plus plus) {
        return null;
    }

    @Override
    public Void visit(Minus minus) {
        return null;
    }

    @Override
    public Void visit(Div div) {
        return null;
    }

    @Override
    public Void visit(Mul mul) {
        return null;
    }
}
