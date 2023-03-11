package cool.AST;

import cool.structures.*;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import java.util.ArrayList;

// Pentru crearea nodurilor arborelui, m-am inspirat din
// laboratorul 4 de CPLang.

public abstract class ASTNode {
    // Aici retin numele nodului.
    String name;
    Token token;
    ParserRuleContext context;

    Scope scope;

    ASTNode(String name, Token token, ParserRuleContext context) {
        this.name = name;
        this.token = token;
        this.context = context;
    }

    public Scope getScope() {
        return scope;
    }

    public void setScope(Scope scope) {
        this.scope = scope;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return null;
    }
}

class Program extends ASTNode {
    ArrayList<Class> classes;

    Program(String name, Token token, ParserRuleContext context, ArrayList<Class> classes) {
        super(name, token, context);
        this.classes = classes;
    }

    public String getName() {
        return this.name;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class Class extends ASTNode {
    Token class_name;
    Token parent_class_name;
    ArrayList<Feature> features;
    TypeSymbol type;

    Class(String name, Token token, ParserRuleContext context, Token class_name, Token parent_class_name,
          ArrayList<Feature> features) {
        super(name, token, context);
        this.features = features;
        this.class_name = class_name;
        this.parent_class_name = parent_class_name;
    }

    public void setType(TypeSymbol type) {
        this.type = type;
    }

    public TypeSymbol getType() {
        return type;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

// Aici retin o clasa care generalizeaza un feature, ce
// reprezinta compozitia unei clase. Ea va fi extinsa de clasele
// VarDecl si FuncDecl.

abstract class Feature extends ASTNode {
    Feature(String name, Token token, ParserRuleContext context) {
        super(name, token, context);
    }
}

// Aici creez un nou nod pentru declararea de functie
// (aferenta regulii unei clase: o clasa este formata din
// declarari de functii si declarari de variabile).

class FuncDecl extends Feature {
    Token func_name;
    Token type;
    ArrayList<Declaration> declarations;
    ArrayList<Expression> lines;
    Scope scope;

    FunctionSymbol symbol;

    FuncDecl(String name, Token token, ParserRuleContext context, Token func_name, Token type,
             ArrayList<Declaration> declarations, ArrayList<Expression> lines) {
        super(name, token, context);
        this.type = type;
        this.func_name = func_name;
        this.lines = lines;
        this.declarations = declarations;
    }

    public Token getType() {
        return this.type;
    }

    public Scope getScope() {
        return this.scope;
    }

    public void setScope(Scope scope) {
        this.scope = scope;
    }

    public void setSymbol(FunctionSymbol symbol) {
        this.symbol = symbol;
    }

    public FunctionSymbol getSymbol() {
        return this.symbol;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

// Aici creez un nou nod pentru declararea de variabila
// (aferenta regulii unei clase: o clasa este formata din
// declarari de functii si declarari de variabile).

class VarDecl extends Feature {
    Token var_name;
    Token type;
    Expression value;

    Scope scope;

    TypeSymbol typeSymbol;
    IdSymbol symbol;

    VarDecl(String name, Token token, ParserRuleContext context, Token var_name, Token type, Expression value) {
        super(name, token, context);
        this.var_name = var_name;
        this.type = type;
        this.value = value;
    }

    public Token getType() {
        return this.type;
    }

    public TypeSymbol getTypeSymbol() {
        return this.typeSymbol;
    }

    public void setTypeSymbol(TypeSymbol typeSymbol) {
        this.typeSymbol = typeSymbol;
    }

    public Token getVar_name() {
        return this.var_name;
    }

    public Scope getScope() {
        return this.scope;
    }

    public void setScope(Scope scope) {
        this.scope = scope;
    }

    public void setSymbol(IdSymbol idSymbol) {
        this.symbol = idSymbol;
    }

    public IdSymbol getSymbol() {
        return this.symbol;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

// Aici creez un nou nod pentru declarari de variabile, aferente
// parametrilor functiilor.

class Declaration extends ASTNode {
    Token id;
    Token type;

    Scope scope;

    IdSymbol symbol;

    Declaration(String name, Token token, ParserRuleContext context, Token id, Token type) {
        super(name, token, context);
        this.id = id;
        this.type = type;
    }

    public Token getType() {
        return this.type;
    }

    public Token getId() {
        return this.id;
    }

    public Scope getScope() {
        return this.scope;
    }

    public void setScope(Scope scope) {
        this.scope = scope;
    }

    public IdSymbol getSymbol() {
        return this.symbol;
    }

    public void setSymbol(IdSymbol symbol) {
        this.symbol = symbol;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

// Aici creez un nod ce reprezinta declararea (si initializarea) unei
// variabile, pe care il voi folosi in cadrul celorlaltor noduri
// din gramatica expr.

class VarDeclAndInit extends ASTNode {
    Token var_name;
    Token type;
    Expression value;

    IdSymbol idSymbol;

    Scope scope;

    VarDeclAndInit(String name, Token token,
                   ParserRuleContext context, Token var_name,
                   Token type, Expression value) {
        super(name, token, context);
        this.type = type;
        this.var_name =var_name;
        this.value = value;
    }

    public IdSymbol getIdSymbol() {
        return this.idSymbol;
    }

    public void setIdSymbol(IdSymbol idSymbol) {
        this.idSymbol = idSymbol;
    }

    public Scope getScope() {
        return scope;
    }

    public void setScope(Scope scope) {
        this.scope = scope;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

abstract class Expression extends ASTNode {
    Expression(String name, Token token, ParserRuleContext context) {
        super(name, token, context);
    }
}

// Nume de variabila.

class Id extends Expression {
    IdSymbol idSymbol;

    Scope scope;

    Id(String name, Token token, ParserRuleContext context) {
        super(name, token, context);
    }

    public IdSymbol getIdSymbol() {
        return this.idSymbol;
    }

    public void setIdSymbol(IdSymbol idSymbol) {
        this.idSymbol = idSymbol;
    }

    public Scope getScope() {
        return this.scope;
    }

    public void setScope(Scope scope) {
        this.scope = scope;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

// Valoare de tip Int.

class Int extends Expression {
    Int(String name, Token token, ParserRuleContext context) {
        super(name, token, context);
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

// Valoare de tip Bool.

class Bool extends Expression {

    Bool(String name, Token token, ParserRuleContext context) {
        super(name, token, context);
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

// Valoare de tip String (cu prelucrarile precizate in cerinta).

class StringType extends Expression {

    StringType(String name, Token token, ParserRuleContext context) {
        super(name, token, context);
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

// Operatorul Not.

class Not extends Expression {
    Expression not_op;

    Not(String name, Token token, ParserRuleContext context, Expression not_op) {
        super(name, token, context);
        this.not_op = not_op;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

// Operatorul Neg

class Neg extends Expression {
    Expression unary_op;

    Neg(String name, Token token, ParserRuleContext context, Expression unary_op) {
        super(name, token, context);
        this.unary_op = unary_op;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

// Operatorii de inmultire/impartire.

class Mul extends Expression {
    Expression left_op;
    Expression right_op;

    Mul(String name, Token token, ParserRuleContext context, Expression left_op, Expression right_op) {
        super(name, token, context);
        this.left_op = left_op;
        this.right_op = right_op;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class Div extends Expression {
    Expression left_op;
    Expression right_op;

    Div(String name, Token token, ParserRuleContext context, Expression left_op, Expression right_op) {
        super(name, token, context);
        this.left_op = left_op;
        this.right_op = right_op;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

// Operatorii de adunare/scadere.
class Plus extends Expression {
    Expression left_op;
    Expression right_op;

    Plus(String name, Token token, ParserRuleContext context, Expression left_op, Expression right_op) {
        super(name, token, context);
        this.left_op = left_op;
        this.right_op = right_op;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class Minus extends Expression {
    Expression left_op;
    Expression right_op;

    Minus(String name, Token token, ParserRuleContext context, Expression left_op, Expression right_op) {
        super(name, token, context);
        this.left_op = left_op;
        this.right_op = right_op;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class Paren extends Expression {
    Expression expression;

    Paren(String name, Token token, ParserRuleContext context, Expression expression) {
        super(name, token, context);
        this.expression = expression;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

// Operator relational.

class RelOp extends Expression {
    Expression left_op;
    Expression right_op;

    RelOp(String name, Token token, ParserRuleContext context, Expression left_op, Expression right_op) {
        super(name, token, context);
        this.left_op = left_op;
        this.right_op = right_op;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

// Operatorul de atribuire

class Assign extends Expression {
    Id var_name;
    Expression value;
    IdSymbol idSymbol;

    Scope scope;

    Assign(String name, Token token, ParserRuleContext context, Id var_name, Expression value) {
        super(name, token, context);
        this.var_name = var_name;
        this.value = value;
    }

    public IdSymbol getIdSymbol() {
        return this.idSymbol;
    }

    public void setIdSymbol(IdSymbol idSymbol) {
        this.idSymbol = idSymbol;
    }

    public Scope getScope() {
        return this.scope;
    }

    public void setScope(Scope scope) {
        this.scope = scope;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

// Constructia If

class If extends Expression {

    Expression cond;
    Expression thenBranch;
    Expression elseBranch;

    Scope scope;

    If(Expression cond, Token token, ParserRuleContext context, Expression thenBranch, Expression elseBranch, String name) {
        super(name, token, context);
        this.cond = cond;
        this.thenBranch = thenBranch;
        this.elseBranch = elseBranch;
    }

    public Scope getScope() {
        return this.scope;
    }

    public void setScope(Scope scope) {
        this.scope = scope;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

// Constructia While

class While extends Expression {
    Expression cond;
    Expression line;

    Scope scope;

    While(String name, Token token, ParserRuleContext context, Expression cond, Expression line) {
        super(name, token, context);
        this.line = line;
        this.cond = cond;
    }

    public Scope getScope() {
        return this.scope;
    }

    public void setScope(Scope scope) {
        this.scope = scope;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

// Constructia Let

class Let extends Expression {

    Expression line;
    ArrayList<VarDeclAndInit> declarations;

    LetSymbol letSymbol;

    Let(String name, Token token, ParserRuleContext context, Expression line, ArrayList<VarDeclAndInit> declarations) {
        super(name, token, context);
        this.line = line;
        this.declarations = declarations;
    }

    public LetSymbol getLetSymbol() {
        return this.letSymbol;
    }

    public void setLetSymbol(LetSymbol letSymbol) {
        this.letSymbol = letSymbol;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

// Functia isvoid

class IsVoid extends Expression {
    Expression expr;

    IsVoid(String name, Token token, ParserRuleContext context, Expression expression) {
        super(name, token, context);
        this.expr = expression;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

// Instantierea unei clase (folosind cuvantul-cheie NEW).

class Instantiation extends Expression {
    Token type;

    Instantiation(String name, Token token, ParserRuleContext context, Token type) {
        super(name, token, context);
        this.type = type;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

// Constructia Case

class Case extends Expression {
    Expression var_name;
    ArrayList<Token> vars;
    ArrayList<Token> types;
    ArrayList<Expression> branches;

    CaseSymbol caseSymbol;

    Case(String name, Token token, ParserRuleContext context, Expression var_name, ArrayList<Token> vars,
         ArrayList<Token> types, ArrayList<Expression> expressions) {
        super(name, token, context);
        this.var_name = var_name;
        this.vars = vars;
        this.types = types;
        this.branches = expressions;
    }

    public CaseSymbol getCaseSymbol() {
        return this.caseSymbol;
    }

    public void setCaseSymbol(CaseSymbol caseSymbol) {
        this.caseSymbol = caseSymbol;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

// Bloc de instructiuni

class Block extends Expression {
    Scope scope;
    ArrayList<Expression> lines;

    Block(String name, Token token, ParserRuleContext context, ArrayList<Expression> lines) {
        super(name, token, context);
        this.lines = lines;
    }

    public Scope getScope() {
        return scope;
    }

    public void setScope(Scope scope) {
        this.scope = scope;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

// Explicit dispatch

class Method extends Expression {
    Expression func_name;

    Id met_name;
    Token type;
    Token method_name;
    ArrayList<Expression> args;

    Scope scope;

    Method(String name, Token token, ParserRuleContext context, Id funcName, Expression func_name, Token type,
           Token method_name, ArrayList<Expression> args) {
        super(name, token, context);
        this.func_name = func_name;
        this.method_name = method_name;
        this.type = type;
        this.args = args;
        this.met_name = funcName;
    }

    public void setScope(Scope scope) {
        this.scope = scope;
    }

    public Scope getScope() {
        return scope;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

// Implicit dispatch

class FuncCall extends Expression {
    Token func_name;
    ArrayList<Expression> args;

    FuncCall(String name, Token token, ParserRuleContext context, Token func_name, ArrayList<Expression> args) {
        super(name, token, context);
        this.func_name = func_name;
        this.args = args;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
