import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import SymbolTables.ClassSymbolTable;
import SymbolTables.GlobalSymbolTable;
import SymbolTables.MethodSymbolTable;
import syntaxtree.*;
import visitor.*;

public class TypeCheckVisitor extends GJDepthFirst<String, Void>{
    GlobalSymbolTable symbolTable;
    ClassSymbolTable curClassSymbolTable;
    MethodSymbolTable curMethodSymbolTable;
    Boolean identifierTypeCheck;
    Queue<String> argumentTypesToCheck;

    TypeCheckVisitor(GlobalSymbolTable globalSymbolTable){
        this.symbolTable = globalSymbolTable;
        this.identifierTypeCheck = false;
        this.argumentTypesToCheck = new LinkedList<String>();
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
        this.curClassSymbolTable = this.symbolTable.getClassSymbolTable(className);
        n.f2.accept(this, argu);
        n.f3.accept(this, argu);
        n.f4.accept(this, argu);
        n.f5.accept(this, argu);
        n.f6.accept(this, argu);
        this.curMethodSymbolTable = this.curClassSymbolTable.getMethodSymbolTable("main");
        n.f7.accept(this, argu);
        n.f8.accept(this, argu);
        n.f9.accept(this, argu);
        n.f10.accept(this, argu);
        n.f11.accept(this, argu);
        n.f12.accept(this, argu);
        n.f13.accept(this, argu);
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
        this.curClassSymbolTable = this.symbolTable.getClassSymbolTable(className);
        n.f2.accept(this, argu);
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
        this.curClassSymbolTable = this.symbolTable.getClassSymbolTable(className);
        n.f2.accept(this, argu);
        n.f3.accept(this, argu);
        n.f4.accept(this, argu);
        n.f5.accept(this, argu);
        n.f6.accept(this, argu);
        n.f7.accept(this, argu);
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
        n.f1.accept(this, argu);

        String methodName = n.f2.accept(this, argu);
        this.curMethodSymbolTable = this.curClassSymbolTable.getMethodSymbolTable(methodName, symbolTable);
        
        n.f3.accept(this, argu);
        n.f4.accept(this, argu);
        n.f5.accept(this, argu);
        n.f6.accept(this, argu);
        n.f7.accept(this, argu);
        n.f8.accept(this, argu);
        n.f9.accept(this, argu);

        String expressionType = n.f10.accept(this, argu);
        if (!expressionType.equals(this.curMethodSymbolTable.getReturnType()))
            throw new ParseException(expressionType + " does not match the return type of " + 
                                    curClassSymbolTable.getClassName() + "." + curMethodSymbolTable.getMethodName());
        
                                    n.f11.accept(this, argu);
        n.f12.accept(this, argu);
        this.curMethodSymbolTable = null;
        return null;
    }

    /**
    * f0 -> IntegerLiteral()
    *       | TrueLiteral()
    *       | FalseLiteral()
    *       | Identifier()
    *       | ThisExpression()
    *       | ArrayAllocationExpression()
    *       | AllocationExpression()
    *       | BracketExpression()
    */
    public String visit(PrimaryExpression n, Void argu) throws Exception {
        this.identifierTypeCheck = true;
        String result = n.f0.accept(this, argu);
        this.identifierTypeCheck = false;

        if (result.equals("true") || result.equals("false"))
            return "boolean";
        else 
            return result;
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
    * f0 -> <INTEGER_LITERAL>
    */
    public String visit(IntegerLiteral n, Void argu) throws Exception {
        n.f0.accept(this, argu);
        return "int";
    }

    /**
     * f0 -> "true"
    */
    public String visit(TrueLiteral n, Void argu) throws Exception {
        n.f0.accept(this, argu);
        return "true";
    }

    /**
     * f0 -> "false"
    */
    public String visit(FalseLiteral n, Void argu) throws Exception {
        n.f0.accept(this, argu);
        return "false";
    }

    /**
     * f0 -> "this"
    */
    public String visit(ThisExpression n, Void argu) throws Exception {
        n.f0.accept(this, argu);
        return this.curClassSymbolTable.getClassName();
    }

     /**
    * f0 -> <IDENTIFIER>
    */
    public String visit(Identifier n, Void argu) throws Exception {
        String identifier = n.f0.toString();
        
        if (this.identifierTypeCheck == false){
            System.out.println("Type check off for " + identifier);
            return identifier;
        }
        
        System.out.println("Type check on for " + identifier);
        String type = curMethodSymbolTable.getIdentifierType(identifier, curClassSymbolTable, symbolTable);
        //If identifier is not a variable
        if (type == null){
            //Check if identifier is a class
            if (this.symbolTable.hasClass(identifier))
                type = identifier;
            else
                throw new ParseException("Unknown identifier '" + identifier + "'");
        }
        System.out.println("Found identifier '" + identifier + "' with type '" + type + "' in " + curClassSymbolTable.getClassName() + "." + curMethodSymbolTable.getMethodName());
        if (type == "String[]")
            throw new ParseException("Illegal use of " + curClassSymbolTable.getClassName() + ".main arguments");
        return type;
    }

    /**
    * f0 -> "!"
    * f1 -> Clause()
    */
    public String visit(NotExpression n, Void argu) throws Exception {
        n.f0.accept(this, argu);
        String clause = n.f1.accept(this, argu);
        if (!clause.equals("boolean"))
            throw new ParseException("Invalid use of operator '!'");
        return "boolean";
    }

    /**
    * f0 -> "new"
    * f1 -> "boolean"
    * f2 -> "["
    * f3 -> Expression()
    * f4 -> "]"
    */
    public String visit(BooleanArrayAllocationExpression n, Void argu) throws Exception {
        n.f0.accept(this, argu);
        n.f1.accept(this, argu);
        n.f2.accept(this, argu);
        //Add integer check
        String indexType = n.f3.accept(this, argu);
        if (!indexType.equals("int"))
            throw new ParseException("Array index must be an integer");
        n.f4.accept(this, argu);
        return "boolean[]";
    }

    /**
     * f0 -> "new"
    * f1 -> "int"
    * f2 -> "["
    * f3 -> Expression()
    * f4 -> "]"
    */
    public String visit(IntegerArrayAllocationExpression n, Void argu) throws Exception {
        n.f0.accept(this, argu);
        n.f1.accept(this, argu);
        n.f2.accept(this, argu);
        //Add integer check
        String indexType = n.f3.accept(this, argu);
        if (!indexType.equals("int"))
            throw new ParseException("Array index must be an integer");
        n.f4.accept(this, argu);
        return "int[]";
    }

    /**
    * f0 -> "new"
    * f1 -> Identifier()
    * f2 -> "("
    * f3 -> ")"
    */
    public String visit(AllocationExpression n, Void argu) throws Exception {
        n.f0.accept(this, argu);
        this.identifierTypeCheck = false;
        String type = n.f1.accept(this, argu);
        this.identifierTypeCheck = true;
        n.f2.accept(this, argu);
        n.f3.accept(this, argu);
        return type;
    }

    /**
    * f0 -> "("
    * f1 -> Expression()
    * f2 -> ")"
    */
    public String visit(BracketExpression n, Void argu) throws Exception {
        n.f0.accept(this, argu);
        String type = n.f1.accept(this, argu);
        n.f2.accept(this, argu);
        return type;
    }

    /**
    * f0 -> Clause()
    * f1 -> "&&"
    * f2 -> Clause()
    */
    public String visit(AndExpression n, Void argu) throws Exception {
        String t1 = n.f0.accept(this, argu);
        n.f1.accept(this, argu);
        String t2 = n.f2.accept(this, argu);
        if (!t1.equals("boolean") || !t2.equals("boolean"))
            throw new ParseException("Invalid use of operator '&&'");
        return "boolean";
    }

    /**
    * f0 -> PrimaryExpression()
    * f1 -> "<"
    * f2 -> PrimaryExpression()
    */
    public String visit(CompareExpression n, Void argu) throws Exception {
        String t1 = n.f0.accept(this, argu);
        n.f1.accept(this, argu);
        String t2 = n.f2.accept(this, argu);
        if (!t1.equals("int") || !t2.equals("int"))
            throw new ParseException("Invalid use of operator '<'");
        return "boolean";
    }

    /**
    * f0 -> PrimaryExpression()
    * f1 -> "+"
    * f2 -> PrimaryExpression()
    */
    public String visit(PlusExpression n, Void argu) throws Exception {
        String t1 = n.f0.accept(this, argu);
        n.f1.accept(this, argu);
        String t2 = n.f2.accept(this, argu);
        if (!t1.equals("int") || !t2.equals("int"))
            throw new ParseException("Invalid use of operator '+'");
        return "int";
    }

    /**
    * f0 -> PrimaryExpression()
    * f1 -> "-"
    * f2 -> PrimaryExpression()
    */
    public String visit(MinusExpression n, Void argu) throws Exception {
        String t1 = n.f0.accept(this, argu);
        n.f1.accept(this, argu);
        String t2 = n.f2.accept(this, argu);
        if (!t1.equals("int") || !t2.equals("int"))
            throw new ParseException("Invalid use of operator '-'");
        return "int";
    }

    /**
     * f0 -> PrimaryExpression()
    * f1 -> "*"
    * f2 -> PrimaryExpression()
    */
    public String visit(TimesExpression n, Void argu) throws Exception {
        String t1 = n.f0.accept(this, argu);
        n.f1.accept(this, argu);
        String t2 = n.f2.accept(this, argu);
        if (!t1.equals("int") || !t2.equals("int"))
            throw new ParseException("Invalid use of operator '*'");
        return "int";
    }

    /**
    * f0 -> PrimaryExpression()
    * f1 -> "["
    * f2 -> PrimaryExpression()
    * f3 -> "]"
    */
    public String visit(ArrayLookup n, Void argu) throws Exception {
        String arrayType = n.f0.accept(this, argu);
        n.f1.accept(this, argu);
        String indexType = n.f2.accept(this, argu);
        n.f3.accept(this, argu);
        
        if (!indexType.equals("int"))
            throw new ParseException("Invalid index type. ('int' required)");
        
        if (arrayType.equals("int[]"))
            return "int";
        else if (arrayType.equals("boolean[]"))
            return "boolean";
        else 
            throw new ParseException("Invalid use of array index");
    }

    /**
    * f0 -> PrimaryExpression()
    * f1 -> "."
    * f2 -> "length"
    */
    public String visit(ArrayLength n, Void argu) throws Exception {
        String type = n.f0.accept(this, argu);
        if (!type.equals("int[]") && !type.equals("boolean[]"))
            throw new ParseException("Invalid use of 'length' (Not an array)");
        n.f1.accept(this, argu);
        n.f2.accept(this, argu);
        return "int";
    }

    /**
    * f0 -> PrimaryExpression()
    * f1 -> "."
    * f2 -> Identifier()
    * f3 -> "("
    * f4 -> ( ExpressionList() )?
    * f5 -> ")"
    */
    public String visit(MessageSend n, Void argu) throws Exception {
        String className = n.f0.accept(this, argu);
        ClassSymbolTable classSymbolTable = this.symbolTable.getClassSymbolTable(className);
        n.f1.accept(this, argu);
        String methodName = n.f2.accept(this, argu);
        MethodSymbolTable methodSymbolTable = classSymbolTable.getMethodSymbolTable(methodName, symbolTable);
        n.f3.accept(this, argu);
        this.argumentTypesToCheck.clear();
        n.f4.accept(this, argu);
        n.f5.accept(this, argu);

        //Check if given arguments match the method template
        Map<String, String> requiredArguments = methodSymbolTable.getArgumentSymbolTable();
        if (requiredArguments.size() != argumentTypesToCheck.size())
            throw new ParseException("Invalid argument count for " + className + "." + methodName);

        Set<String> requiredArgumentNames = requiredArguments.keySet();
        for (String argument : requiredArgumentNames){
            String type1 = requiredArguments.get(argument);
            String type2 = argumentTypesToCheck.poll();
            if (!type1.equals(type2)){
                //Check if arguments are derived classes
                if (symbolTable.hasClass(type1) && symbolTable.hasClass(type2)){
                    if (symbolTable.isDerived(type2, type1)){
                        return methodSymbolTable.getReturnType();
                    }
                }

                throw new ParseException("Invalid argument types for " + className + "." + methodName);
            }
        }
        System.out.println("Method " + className + "." + methodName + " executed succesfully and returned: " + methodSymbolTable.getReturnType());
        return methodSymbolTable.getReturnType();
    }

    /**
    * f0 -> Expression()
    * f1 -> ExpressionTail()
    */
   public String visit(ExpressionList n, Void argu) throws Exception {
        String exprType = n.f0.accept(this, argu);
        this.argumentTypesToCheck.add(exprType);
        System.out.println("Adding in argumnetsToCheck: " + exprType);
        n.f1.accept(this, argu);
        return null;
    }

    /**
     * f0 -> ","
    * f1 -> Expression()
    */
    public String visit(ExpressionTerm n, Void argu) throws Exception {
        n.f0.accept(this, argu);
        String exprType = n.f1.accept(this, argu);
        this.argumentTypesToCheck.add(exprType);
        System.out.println("Adding in argumnetsToCheck: " + exprType);
        return null;
    }

    /**
    * f0 -> Identifier()
    * f1 -> "="
    * f2 -> Expression()
    * f3 -> ";"
    */
    public String visit(AssignmentStatement n, Void argu) throws Exception {
        identifierTypeCheck = true;
        String identifierType = n.f0.accept(this, argu);
        identifierTypeCheck = false;
        n.f1.accept(this, argu);
        String expressionType = n.f2.accept(this, argu);
        n.f3.accept(this, argu);

        if (!identifierType.equals(expressionType)){
            //check if they are derived and base classes
            if (symbolTable.hasClass(identifierType) && symbolTable.hasClass(expressionType)){
                if (symbolTable.isDerived(expressionType, identifierType)){
                    return null;
                }
            }
            throw new ParseException("Cannot assign '" + expressionType + "' to '" + identifierType + "'");
        }
        return null;
    }

    /**
    * f0 -> Identifier()
    * f1 -> "["
    * f2 -> Expression()
    * f3 -> "]"
    * f4 -> "="
    * f5 -> Expression()
    * f6 -> ";"
    */
    public String visit(ArrayAssignmentStatement n, Void argu) throws Exception {
        identifierTypeCheck = true;
        String identifierType = n.f0.accept(this, argu);
        identifierTypeCheck = false;
        identifierType = identifierType.replaceAll("\\[\\]", ""); //remove square brackets from identifier
        
        n.f1.accept(this, argu);
        String indexType = n.f2.accept(this, argu);
        if (!indexType.equals("int"))
            throw new ParseException("Invalid index type. ('int' required)");
        n.f3.accept(this, argu);
        n.f4.accept(this, argu);
        String expressionType = n.f5.accept(this, argu);
        n.f6.accept(this, argu);

        if (!identifierType.equals(expressionType))
            throw new ParseException("Cannot assign '" + expressionType + "' to '" + identifierType + "'");
        return null;
    }

    /**
    * f0 -> "if"
    * f1 -> "("
    * f2 -> Expression()
    * f3 -> ")"
    * f4 -> Statement()
    * f5 -> "else"
    * f6 -> Statement()
    */
    public String visit(IfStatement n, Void argu) throws Exception {
        n.f0.accept(this, argu);
        n.f1.accept(this, argu);
        String expressionType = n.f2.accept(this, argu);
        if (!expressionType.equals("boolean"))
            throw new ParseException("'if' condition must be boolean");
        n.f3.accept(this, argu);
        n.f4.accept(this, argu);
        n.f5.accept(this, argu);
        n.f6.accept(this, argu);
        return null;
    }
    
    /**
     * f0 -> "while"
    * f1 -> "("
    * f2 -> Expression()
    * f3 -> ")"
    * f4 -> Statement()
    */
    public String visit(WhileStatement n, Void argu) throws Exception {
        n.f0.accept(this, argu);
        n.f1.accept(this, argu);
        String expressionType = n.f2.accept(this, argu);
        if (!expressionType.equals("boolean"))
            throw new ParseException("'while' condition must be boolean");
        n.f3.accept(this, argu);
        n.f4.accept(this, argu);
        return null;
    }

    /**
    * f0 -> "System.out.println"
    * f1 -> "("
    * f2 -> Expression()
    * f3 -> ")"
    * f4 -> ";"
    */
    public String visit(PrintStatement n, Void argu) throws Exception {
        n.f0.accept(this, argu);
        n.f1.accept(this, argu);
        n.f2.accept(this, argu);String expressionType = n.f2.accept(this, argu);
        if (!expressionType.equals("int"))
            throw new ParseException("Print statement can only be used with type 'int'");
        n.f3.accept(this, argu);
        n.f4.accept(this, argu);
        return null;
    }

    /**
    * f0 -> ArrayType()
    *       | BooleanType()
    *       | IntegerType()
    *       | Identifier()
    */
    public String visit(Type n, Void argu) throws Exception {

        String type = n.f0.accept(this, argu);

        if (!type.equals("int") && !type.equals("int[]")
            && !type.equals("boolean") && !type.equals("boolean[]")
            && !this.symbolTable.hasClass(type))
        {
            throw new ParseException("Unknown type '" + type + "'");
        }
        return type;
    }
}