import syntaxtree.*;
import visitor.*;

public class SymbolTableVisitor extends GJDepthFirst<String, Void>{
    GlobalSymbolTable symbolTable;
    ClassSymbolTable curClassSymbolTable;
    MethodSymbolTable curMethodSymbolTable;
    String scope;

    SymbolTableVisitor(){
        symbolTable = new GlobalSymbolTable();
    }


    /**
    * f0 -> "class"
    * f1 -> Identifier()
    * f2 -> "{"
    * f3 -> "public"
    * f4 -> "static"
    * f5 -> "void"
    * f6 -> "main"
    * f7 -> "("
    * f8 -> "String"
    * f9 -> "["
    * f10 -> "]"
    * f11 -> Identifier()
    * f12 -> ")"
    * f13 -> "{"
    * f14 -> ( VarDeclaration() )*
    * f15 -> ( Statement() )*
    * f16 -> "}"
    * f17 -> "}"
    */
    public String visit(MainClass n, Void argu) throws Exception {
        n.f0.accept(this, argu);
        String className = n.f1.accept(this, argu);
        symbolTable.addClass(className);
        this.curClassSymbolTable = this.symbolTable.getClassSymbolTable(className);
        n.f2.accept(this, argu);
        n.f3.accept(this, argu);
        n.f4.accept(this, argu);
        n.f5.accept(this, argu);
        n.f6.accept(this, argu);
        n.f7.accept(this, argu);
        n.f8.accept(this, argu);
        n.f9.accept(this, argu);
        n.f10.accept(this, argu);
        n.f11.accept(this, argu);
        n.f12.accept(this, argu);
        n.f13.accept(this, argu);
        scope = "class";
        n.f14.accept(this, argu);
        n.f15.accept(this, argu);
        n.f16.accept(this, argu);
        n.f17.accept(this, argu);
        return null;
    }

    /**
    * f0 -> "class"
    * f1 -> Identifier()
    * f2 -> "{"
    * f3 -> ( VarDeclaration() )*
    * f4 -> ( MethodDeclaration() )*
    * f5 -> "}"
    */
    public String visit(ClassDeclaration n, Void argu) throws Exception {
        n.f0.accept(this, argu);
        String className = n.f1.accept(this, argu);
        symbolTable.addClass(className);
        this.curClassSymbolTable = this.symbolTable.getClassSymbolTable(className);
        n.f2.accept(this, argu);
        scope = "class";
        n.f3.accept(this, argu);
        n.f4.accept(this, argu);
        n.f5.accept(this, argu);
        return null;
    }

  /**
    * f0 -> "class"
    * f1 -> Identifier()
    * f2 -> "extends"
    * f3 -> Identifier()
    * f4 -> "{"
    * f5 -> ( VarDeclaration() )*
    * f6 -> ( MethodDeclaration() )*
    * f7 -> "}"
    */
    public String visit(ClassExtendsDeclaration n, Void argu) throws Exception {
        n.f0.accept(this, argu);
        String className = n.f1.accept(this, argu);
        n.f2.accept(this, argu);
        String parentName = n.f3.accept(this, argu);
        symbolTable.addClass(className, parentName);
        this.curClassSymbolTable = this.symbolTable.getClassSymbolTable(className);
        n.f4.accept(this, argu);
        scope = "class";
        n.f5.accept(this, argu);
        n.f6.accept(this, argu);
        n.f7.accept(this, argu);
        return null;
     }

     /**
      * f0 -> Type()
      * f1 -> Identifier()
      * f2 -> ";"
      */
    public String visit(VarDeclaration n, Void argu) throws Exception {
        String type = n.f0.accept(this, argu);
        String name = n.f1.accept(this, argu);
        //System.out.println("Got " + type + " " + name + " in scope :" + scope);
        if (this.scope == "class")
            this.curClassSymbolTable.addField(name, type);
        else if (this.scope == "method")
            this.curMethodSymbolTable.addLocalVariable(name, type);
        n.f2.accept(this, argu);
        return null;
    }

        /**
    * f0 -> "public"
    * f1 -> Type()
    * f2 -> Identifier()
    * f3 -> "("
    * f4 -> ( FormalParameterList() )?
    * f5 -> ")"
    * f6 -> "{"
    * f7 -> ( VarDeclaration() )*
    * f8 -> ( Statement() )*
    * f9 -> "return"
    * f10 -> Expression()
    * f11 -> ";"
    * f12 -> "}"
    */
    public String visit(MethodDeclaration n, Void argu) throws Exception {
        n.f0.accept(this, argu);
        String returnType = n.f1.accept(this, argu);
        String methodName = n.f2.accept(this, argu);
        this.curClassSymbolTable.addMethod(methodName, returnType);
        this.curMethodSymbolTable = this.curClassSymbolTable.getMethodSymbolTable(methodName);
        n.f3.accept(this, argu);
        n.f4.accept(this, argu);  //-->Populates the argument map of the method
        curClassSymbolTable.checkMethodOverwrite(curMethodSymbolTable, symbolTable);
        n.f5.accept(this, argu);
        n.f6.accept(this, argu);
        scope = "method";
        n.f7.accept(this, argu);
        n.f8.accept(this, argu);
        n.f9.accept(this, argu);
        n.f10.accept(this, argu);
        n.f11.accept(this, argu);
        n.f12.accept(this, argu);
        return null;
    }

    /**
    * f0 -> Type()
    * f1 -> Identifier()
    */
    public String visit(FormalParameter n, Void argu) throws Exception {
        String type = n.f0.accept(this, argu);
        String ident = n.f1.accept(this, argu);
        //System.out.println("Method '" + this.curMethodSymbolTable.methodName + "' of class '" + this.curClassSymbolTable.className + "' argument: " + type + " " + ident);
        this.curMethodSymbolTable.addArgument(ident, type);
        this.curMethodSymbolTable.addLocalVariable(ident, type);
        return null;
    }


    public String visit(IntegerArrayType n, Void argu) throws Exception {
        return "int[]";
    }

    public String visit(BooleanArrayType n, Void argu) throws Exception {
        return "boolean[]";
    }

    public String visit(BooleanType n, Void argu) {
        return "boolean";
    }

    public String visit(IntegerType n, Void argu) {
        return "int";
    }

     /**
    * f0 -> <IDENTIFIER>
    */
    public String visit(Identifier n, Void argu) throws Exception {
        return n.f0.toString();
    }
}
