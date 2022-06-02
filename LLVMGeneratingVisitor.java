import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import SymbolTables.ClassSymbolTable;
import SymbolTables.GlobalSymbolTable;
import SymbolTables.MethodSymbolTable;
import VTables.VTable;
import syntaxtree.*;
import syntaxtree.NodeListOptional;
import syntaxtree.Node;
import visitor.*;

public class LLVMGeneratingVisitor extends GJDepthFirst<String, Void>{
    GlobalSymbolTable symbolTable;
    ClassSymbolTable curClassSymbolTable;
    MethodSymbolTable curMethodSymbolTable;
    Map<String, String> methodCallerType;
    VTable vtable;
    PrintWriter writer;

    int registerCount = 0;
    int ifCount = 0;
    int oobCount = 0;
    int loopCount = 0;
    int arrCount = 0;
    int andCount = 0;

    boolean isInMethod, keepJavaType, checkForFields, returnArguments = false;
    
    LLVMGeneratingVisitor(GlobalSymbolTable globalSymbolTable, PrintWriter llvmWriter) throws Exception{
        this.symbolTable = globalSymbolTable;
        this.writer = llvmWriter;
        this.vtable = new VTable(this.symbolTable);
        this.methodCallerType = new LinkedHashMap<String, String>();

        this.defineVTable();
        this.defineHelperMethods();
    }

    void emit(String data) { this.writer.print(data); }
    String newTemp() { return "%_" + registerCount++; }
    String newIfLabel() { return "if" + ifCount++; }
    String newOobLabel() { return "oob" + oobCount++; }
    String newLoopLabel() { return "loop" + loopCount++; }
    String newArrAllocLabel() { return "arr_alloc" + arrCount++; }
    String newAndLabel() { return "andclause" + andCount++; }
    void resetRegisters() { this.registerCount = 0; }

    String getExprType(String expr) {
        if (expr.split(" ").length == 3)
            return expr.split(" ")[1];
        else
            return expr.split(" ")[0];
    }
    
    String getExprValue(String expr) { 
        if (expr.split(" ").length == 3)
            return expr.split(" ")[2]; 
        else
            return expr.split(" ")[1]; 
    }

    String JavaToLLVM(String type){
        if (type.startsWith("i32")
            | type.startsWith("i8")
            | type.startsWith("i1"))
            return type;
        switch(type){
            case "boolean": return "i1";
            case "int": return "i32";
            case "boolean[]": return "i32*";
            case "int[]": return "i32*";
            default: return "i8*";
        }
    }

    void defineVTable() throws Exception{
        this.writer.println(this.vtable.definition());
    }

    void defineHelperMethods(){
        String helperMethods = "declare i8* @calloc(i32, i32)\n"
                            + "declare i32 @printf(i8*, ...)\n"
                            + "declare void @exit(i32)\n\n"

                            + "@_cint = constant [4 x i8] c\"%d\\0a\\00\"\n"
                            + "@_cOOB = constant [15 x i8] c\"Out of bounds\\0a\\00\"\n"
                            + "define void @print_int(i32 %i) {\n"
                            + "     %_str = bitcast [4 x i8]* @_cint to i8*\n"
                            + "     call i32 (i8*, ...) @printf(i8* %_str, i32 %i)\n"
                            + "     ret void\n"
                            + "}\n\n"

                            + "define void @throw_oob() {\n"
                            + "     %_str = bitcast [15 x i8]* @_cOOB to i8*\n"
                            + "     call i32 (i8*, ...) @printf(i8* %_str)\n"
                            + "     call void @exit(i32 1)\n"
                            + "     ret void\n"
                            + "}\n";

        this.writer.println(helperMethods);
    }

    String varToReg(String variable, String type){
        String reg = variable;
        if(!reg.matches("\\d+") && !reg.startsWith("%_")){
            reg = newTemp();
            emit("\t" + reg + " = load " + type + ", " + type + "* " + variable + "\n");
            this.methodCallerType.put(reg, type);
        }
        return reg;
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
    n.f1.accept(this, argu);
    n.f2.accept(this, argu);
    n.f3.accept(this, argu);
    n.f4.accept(this, argu);
    n.f5.accept(this, argu);
    n.f6.accept(this, argu);
    n.f7.accept(this, argu);
    emit("\ndefine i32 @main() {\n");
    n.f8.accept(this, argu);
    n.f9.accept(this, argu);
    n.f10.accept(this, argu);
    n.f11.accept(this, argu);
    n.f12.accept(this, argu);
    n.f13.accept(this, argu);
    this.isInMethod = true;
    n.f14.accept(this, argu);
    n.f15.accept(this, argu);
    this.isInMethod = false;
    emit("\n\tret i32 0\n");
    n.f16.accept(this, argu);
    emit("}\n\n");
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
        n.f2.accept(this, argu);
        this.curClassSymbolTable = symbolTable.getClassSymbolTable(className);
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
        n.f3.accept(this, argu);
        n.f4.accept(this, argu);
        this.curClassSymbolTable = symbolTable.getClassSymbolTable(className);
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
        this.resetRegisters();
        n.f0.accept(this, argu);
        String returnType = JavaToLLVM(n.f1.accept(this, argu));
        String methodName = n.f2.accept(this, argu);
        this.curMethodSymbolTable = curClassSymbolTable.getMethodSymbolTable(methodName);
        n.f3.accept(this, argu);
        emit("define " + returnType + " @" + curClassSymbolTable.getClassName() + "." + methodName + "(i8* %this");
        n.f4.accept(this, argu);
        emit(") {\n");

        //Store arguments in temp variables
        Map<String,String> argumentSymbolTable = curMethodSymbolTable.getArgumentSymbolTable();
        Set<String> arguments = argumentSymbolTable.keySet();
        for (String argument : arguments){
            String type = JavaToLLVM(argumentSymbolTable.get(argument));
            emit("\t%" + argument + " = alloca " + type + "\n");
            emit("\tstore " + type + " %." + argument + ", " + type + "* %" + argument + "\n");
        }

        n.f5.accept(this, argu);
        n.f6.accept(this, argu);
        this.isInMethod = true;
        this.returnArguments = true;
        n.f7.accept(this, argu);
        n.f8.accept(this, argu);
        n.f9.accept(this, argu);
        this.checkForFields = true;
        String expr = n.f10.accept(this, argu);
        this.checkForFields = false;
        emit("\n\tret " + getExprType(expr) + " " + getExprValue(expr) + " ");
        n.f11.accept(this, argu);
        n.f12.accept(this, argu);
        this.isInMethod = false;
        this.returnArguments = false;
        emit("\n}\n\n");
        return null;
     }

        /**
        * f0 -> Type()
        * f1 -> Identifier()
        */
    public String visit(FormalParameter n, Void argu) throws Exception {
        String type = n.f0.accept(this, argu);
        String llvmType = JavaToLLVM(type);
        String identifier = n.f1.accept(this, argu);
        emit(", " + llvmType + " %." + identifier);
        this.methodCallerType.put("%" + identifier, type);
        return null;
    }

    /**
    * f0 -> Type()
    * f1 -> Identifier()
    * f2 -> ";"
    */
    public String visit(VarDeclaration n, Void argu) throws Exception {
        String type = n.f0.accept(this, argu);
        String identifier = n.f1.accept(this, argu);
        n.f2.accept(this, argu);
        if (isInMethod){
            String llvmType = JavaToLLVM(type);
            emit("\t%" + identifier + " = alloca " + llvmType + "\n\n");
        }
        this.methodCallerType.put("%" + identifier, type);
        return null;
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
        if (curClassSymbolTable != null) this.checkForFields = true;
        String expr = n.f0.accept(this, argu);
        this.checkForFields = false;

        String objectReg = getExprValue(expr);
        String objectType = this.methodCallerType.get(objectReg);
        if (objectType == null){
            objectType = getExprType(expr).replace("%", "");
        }
        if (expr.equals("i8* %this")){
            objectType = curClassSymbolTable.getClassName();
        }

        if (this.curClassSymbolTable == null){  //If in main class
            objectReg = varToReg(objectReg, JavaToLLVM(objectType));
        }

        n.f1.accept(this, argu);
        String methodName = n.f2.accept(this, argu);

        //Search parent classes in case method does not exist
        ClassSymbolTable tempClassSymbolTable = symbolTable.getClassSymbolTable(objectType);
        while (!tempClassSymbolTable.hasMethod(methodName)){
            String tempParentClassName = tempClassSymbolTable.getParentName();
            tempClassSymbolTable = symbolTable.getClassSymbolTable(tempParentClassName);
        }

        MethodSymbolTable methodSymbolTable = tempClassSymbolTable.getMethodSymbolTable(methodName);
        int methodOffset = methodSymbolTable.getOffset()/8;
        String methodReturnType = methodSymbolTable.getReturnType();
        String llvmReturnType = JavaToLLVM(methodReturnType);
        System.out.println(";" + objectType + "." + methodName + ": " + methodOffset  + "\n");
        emit("\n\t; " + objectType + "." + methodName + ": " + methodOffset  + "\n");
        String reg2 = newTemp();
        String reg3 = newTemp();
        String reg4 = newTemp();
        String reg5 = newTemp();
        emit("\t" + reg2 + " = bitcast i8* " + objectReg + " to i8***\n");
        emit("\t" + reg3 + " = load i8**, i8*** " + reg2 + "\n");
        emit("\t" + reg4 + " = getelementptr i8*, i8** " + reg3 + ", i32 " + methodOffset + "\n");
        emit("\t" + reg5 + " = load i8*, i8** " + reg4 + "\n");

        
        String reg6 = newTemp();
        emit("\t" + reg6 + " = bitcast i8* " + reg5 + " to " + llvmReturnType + " (i8*"); //Continue with argument types
        Map<String,String> argumentSymbolTable = methodSymbolTable.getArgumentSymbolTable();
        Set<String> arguments = argumentSymbolTable.keySet();
        for (String argument : arguments){
            String type = JavaToLLVM(argumentSymbolTable.get(argument));
            emit(", " + type);
        }
        emit(")*\n");
        
        String reg7 = newTemp();
        
        
        n.f3.accept(this, argu);
        this.checkForFields = true;
        String argList = n.f4.accept(this, argu);
        this.checkForFields = false;
        n.f5.accept(this, argu);
        emit("\t" + reg7 + " = call " + llvmReturnType + " " + reg6 + "(i8* " + objectReg); //Continue with call parameters
        if (argList != null)
            emit (", " + argList);
        emit(")\n");
        System.out.println("Called " + methodName + " with arguments: " + argList);        
        System.out.println("Returning: " + methodReturnType + " " + reg7);
        this.methodCallerType.put(reg7, methodReturnType);
        return methodReturnType + " " + reg7;
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
        if (this.curClassSymbolTable != null) this.checkForFields = true; //TODO: check if this is a problem
        String expr = n.f0.accept(this, argu);
        this.checkForFields = false;
        if (expr.equals("%this"))
            return "i8* %this";
        if (expr.equals("true"))
            return "i1 1";
        if (expr.equals("false"))
            return "i1 0";
        if (expr.matches("-?\\d+"))
            return "i32 " + expr;
        if (expr.startsWith("i32"))
            return expr;
        if (expr.startsWith("i1"))
            return expr;
        if (expr.startsWith("i8*"))
            return expr;

        return "i8* %" + expr;
    }

    /**
    * f0 -> AndExpression()
    *       | CompareExpression()
    *       | PlusExpression()
    *       | MinusExpression()
    *       | TimesExpression()
    *       | ArrayLookup()
    *       | ArrayLength()
    *       | MessageSend()
    *       | Clause()
    */
    public String visit(Expression n, Void argu) throws Exception {
        String expr = n.f0.accept(this, argu);
        return expr;
    }

    /**
    * f0 -> ","
    * f1 -> Expression()
    */
    public String visit(ExpressionTerm n, Void argu) throws Exception {
        n.f0.accept(this, argu);
        String expr = n.f1.accept(this, argu);
        return ", " + JavaToLLVM(getExprType(expr)) + " " + getExprValue(expr);
    }

    /**
    * f0 -> Expression()
    * f1 -> ExpressionTail()
    */
    public String visit(ExpressionList n, Void argu) throws Exception {
        System.out.println("---READING EXPRESSION LIST");
        String expr1 = n.f0.accept(this, argu);
        System.out.println(expr1);
        String expr2 = n.f1.accept(this, argu);
        System.out.println(expr2);
        expr1 = JavaToLLVM(getExprType(expr1)) + " " + getExprValue(expr1);
        String ret = expr1 + expr2;
        ret = ret.replace(", null", "");
        ret = ret.replace("null", "");
        System.out.println("------------------------------");
        return ret;
    }

    /**
    * f0 -> ( ExpressionTerm() )*
    */
    public String visit(ExpressionTail n, Void argu) throws Exception {
        NodeListOptional nodeList = n.f0;
        String ret = "";
        for (int i=0; i<nodeList.size(); i++)
            ret += nodeList.elementAt(i).accept(this, argu); 
        
        return ret;
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
        checkForFields = true;
        String expr = n.f3.accept(this, argu);
        checkForFields = false;
        n.f4.accept(this, argu);

        String reg1 = newTemp();
        String labelError = newArrAllocLabel();
        String labelSuccess = newArrAllocLabel();
        emit("\t" + reg1 + " = icmp slt i32 " + getExprValue(expr) + ", 0\n");
        emit("\tbr i1 " + reg1 + ", label %" + labelError + ", label %" + labelSuccess + "\n\n");

        emit(labelError + ":\n");
        emit("\tcall void @throw_oob()\n");
        emit("\tbr label %" + labelSuccess + "\n\n");

        emit(labelSuccess + ":\n");
        String reg2 = newTemp();
        emit("\t" + reg2 + " = add i32 " + getExprValue(expr) + ", 1\n");
        String reg3 = newTemp();
        emit("\t" + reg3 + " = call i8* @calloc(i32 4, i32 " + reg2 + ")\n");
        String reg4 = newTemp();
        emit("\t" + reg4 + " = bitcast i8* " + reg3 + " to i32*\n");
        emit("\tstore i32 " + getExprValue(expr) + ", i32* " + reg4 + "\n");

        return "i32* " + reg4;
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
        checkForFields = true;
        String expr = n.f3.accept(this, argu);
        checkForFields = false;
        n.f4.accept(this, argu);

        String reg1 = newTemp();
        String labelError = newArrAllocLabel();
        String labelSuccess = newArrAllocLabel();
        emit("\t" + reg1 + " = icmp slt i32 " + getExprValue(expr) + ", 0\n");
        emit("\tbr i1 " + reg1 + ", label %" + labelError + ", label %" + labelSuccess + "\n\n");

        emit(labelError + ":\n");
        emit("\tcall void @throw_oob()\n");
        emit("\tbr label %" + labelSuccess + "\n\n");

        emit(labelSuccess + ":\n");
        String reg2 = newTemp();
        emit("\t" + reg2 + " = add i32 " + getExprValue(expr) + ", 1\n");
        String reg3 = newTemp();
        emit("\t" + reg3 + " = call i8* @calloc(i32 4, i32 " + reg2 + ")\n");
        String reg4 = newTemp();
        emit("\t" + reg4 + " = bitcast i8* " + reg3 + " to i32*\n");
        emit("\tstore i32 " + getExprValue(expr) + ", i32* " + reg4 + "\n");

        return "i32* " + reg4;
    }

    /**
    * f0 -> "new"
    * f1 -> Identifier()
    * f2 -> "("
    * f3 -> ")"
    */
    public String visit(AllocationExpression n, Void argu) throws Exception {
        n.f0.accept(this, argu);
        String className = n.f1.accept(this, argu);
        n.f2.accept(this, argu);
        n.f3.accept(this, argu);

        String reg1 = newTemp();
        String reg2 = newTemp();
        String reg3 = newTemp();
        
        int classSize = symbolTable.getClassSymbolTable(className).getClassSize();
        int numMethods = symbolTable.getClassSymbolTable(className).getNumMethods();
        emit("\t" + reg1 + " = call i8* @calloc(i32 1, i32 " + classSize + ")\n");
        emit("\t" + reg2 + " = bitcast i8* " + reg1 + " to i8***\n");
        emit("\t" + reg3 + " = getelementptr [" + numMethods + " x i8*], [" + numMethods + " x i8*]* @." + className + "_vtable, i32 0, i32 0\n");
        emit("\t" + "store i8** " + reg3 + ", i8*** " + reg2 + "\n");
        this.methodCallerType.put(reg3, className);
        return "i8* %" + className + " " + reg1;
    }

    /**
    * f0 -> "("
    * f1 -> Expression()
    * f2 -> ")"
    */
    public String visit(BracketExpression n, Void argu) throws Exception {
        n.f0.accept(this, argu);
        String expr = n.f1.accept(this, argu);
        n.f2.accept(this, argu);
        return expr;
    }

    /**
    * f0 -> "!"
    * f1 -> Clause()
    */
    public String visit(NotExpression n, Void argu) throws Exception {
        n.f0.accept(this, argu);
        checkForFields = true;
        String expr = n.f1.accept(this, argu);
        checkForFields = false;
        String value = getExprValue(expr);
        String reg1 = newTemp();
        emit("\t" + reg1 + " = xor i1 1, " + value + "\n");
        return "i1 " + reg1;
    }

    /**
    * f0 -> Clause()
    * f1 -> "&&"
    * f2 -> Clause()
    */
    public String visit(AndExpression n, Void argu) throws Exception {
        String and1 = newAndLabel();
        String and2 = newAndLabel();
        String and3 = newAndLabel();
        String and4 = newAndLabel();
        String reg1 = newTemp();

        this.checkForFields = true;
        String expr1 = n.f0.accept(this, argu);
        n.f1.accept(this, argu);
        emit("\tbr label %" + and1 + "\n");

        emit("\n" + and1 + ":\n");
        emit("\tbr i1 " + getExprValue(expr1) + ", label %" + and2 + ", label %" + and4 + "\n");

        emit("\n" + and2 + ":\n");
        String expr2 = n.f2.accept(this, argu);
        this.checkForFields = false;
        emit("\tbr label %" + and3 + "\n");

        emit("\n" + and3 + ":\n");
        emit("\tbr label %" + and4 + "\n");

        emit("\n" + and4 + ":\n");
        emit("\t" + reg1 + " = phi i1 [ 0, %" + and1 + " ], [ " + getExprValue(expr2) + ", %" + and3 + " ]\n");
        
        return "i1 " + reg1;
    }

    /**
     * f0 -> PrimaryExpression()
    * f1 -> "<"
    * f2 -> PrimaryExpression()
    */
    public String visit(CompareExpression n, Void argu) throws Exception {
        this.checkForFields = true;
        String expr1 = n.f0.accept(this, argu);
        String value1 = getExprValue(expr1);
        n.f1.accept(this, argu);
        String expr2 = n.f2.accept(this, argu);
        String value2 = getExprValue(expr2);
        this.checkForFields = false;
        String reg1 = newTemp();
        emit("\t" + reg1 + " = icmp slt i32 " + value1 + ", " + value2 + "\n");
        return "i1 " + reg1;
    }

    /**
    * f0 -> PrimaryExpression()
    * f1 -> "+"
    * f2 -> PrimaryExpression()
    */
    public String visit(PlusExpression n, Void argu) throws Exception {
        String expr1 = n.f0.accept(this, argu);
        String value1 = getExprValue(expr1);
        n.f1.accept(this, argu);
        String expr2 = n.f2.accept(this, argu);
        String value2 = getExprValue(expr2);
        String reg1 = newTemp();
        emit("\t" + reg1 + " = add i32 " + value1 + ", " + value2 + "\n");
        return "i32 " + reg1;
    }

    /**
     * f0 -> PrimaryExpression()
    * f1 -> "-"
    * f2 -> PrimaryExpression()
    */
    public String visit(MinusExpression n, Void argu) throws Exception {
        String expr1 = n.f0.accept(this, argu);
        String value1 = getExprValue(expr1);
        n.f1.accept(this, argu);
        String expr2 = n.f2.accept(this, argu);
        String value2 = getExprValue(expr2);
        String reg1 = newTemp();
        emit("\t" + reg1 + " = sub i32 " + value1 + ", " + value2 + "\n");
        return "i32 " + reg1;
    }

    /**
     * f0 -> PrimaryExpression()
    * f1 -> "*"
    * f2 -> PrimaryExpression()
    */
    public String visit(TimesExpression n, Void argu) throws Exception {
        String expr1 = n.f0.accept(this, argu);
        String value1 = getExprValue(expr1);
        n.f1.accept(this, argu);
        String expr2 = n.f2.accept(this, argu);
        String value2 = getExprValue(expr2);
        String reg1 = newTemp();
        emit("\t" + reg1 + " = mul i32 " + value1 + ", " + value2 + "\n");
        return "i32 " + reg1;
    }

    /**
    * f0 -> PrimaryExpression()
    * f1 -> "["
    * f2 -> PrimaryExpression()
    * f3 -> "]"
    */
    public String visit(ArrayLookup n, Void argu) throws Exception {
        this.checkForFields = true;
        String arrayExpr = n.f0.accept(this, argu);
        String arrayReg = getExprValue(arrayExpr);

        String arrayType;
        if(arrayExpr.startsWith("i")) arrayType = getExprType(arrayExpr);
        else arrayType = JavaToLLVM(methodCallerType.get(arrayReg));
        
        n.f1.accept(this, argu);
        
        String indexExpr = n.f2.accept(this, argu);
        this.checkForFields = false;
        String indexReg = getExprValue(indexExpr);
        n.f3.accept(this, argu);

        String reg1 = newTemp();
        String reg2 = newTemp();
        String reg3 = newTemp();
        String reg4 = newTemp();
        String reg5 = newTemp();
        String reg6 = newTemp();
        String reg7 = newTemp();
        String reg8 = newTemp();
        String oob1 = newOobLabel();
        String oob2 = newOobLabel();
        String oob3 = newOobLabel();
        
        //int array
        System.out.println("Requested access to " + arrayType + " array: " + arrayExpr);
        
        emit("\n\t" + reg1 + " = getelementptr i8, i8* %this, i32 8\n");
        emit("\t" + reg2 + " = bitcast i8* " + reg1 + " to i32**\n");
        emit("\t" + reg3 + " = load i32*, i32** " + reg2 + "\n");
        indexReg = varToReg(indexReg, "i32");
        emit("\t" + reg4 + " = load i32, i32* " + reg3 + "\n");
        emit("\t" + reg5 + " = icmp ult i32 " + indexReg + ", " + reg4 + "\n");
        emit("\tbr i1 " + reg5 + ", label %" + oob1 + ", label %" + oob2 + "\n");

        emit("\n" + oob1 + ":\n");
        emit("\t" + reg6 + " = add i32 " + indexReg + ", 1\n");
        emit("\t" + reg7 + " = getelementptr i32, i32* " + reg3 + ", i32 " + reg6 + "\n");
        emit("\t" + reg8 + " = load i32, i32* " + reg7 + "\n");
        emit("\tbr label %" + oob3 + "\n");

        emit("\n" + oob2 + ":\n");
        emit("\tcall void @throw_oob()\n");
        emit("\tbr label %" + oob3 + "\n");

        emit("\n" + oob3 + ":\n");
        
        //TODO: return i1 in case of boolean
        return "i32* " + reg8;
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
        emit("\n");
        String reg1 = newTemp();
        String reg2 = newTemp();
        String reg3 = newTemp();
        String reg4 = newTemp();
        String oob1 = newOobLabel();
        String oob2 = newOobLabel();
        String oob3 = newOobLabel();

        this.checkForFields = true;
        String identifier = n.f0.accept(this, argu);
        this.checkForFields = false;
        n.f1.accept(this, argu);
        String indexExpr = n.f2.accept(this, argu);
        n.f3.accept(this, argu);
        n.f4.accept(this, argu);
        String expr = n.f5.accept(this, argu);
        n.f6.accept(this, argu);

        emit("\t" + reg1 + " = load i32, i32 *" + getExprValue(identifier) + "\n");
        emit("\t" + reg2 + " = icmp ult i32 " + getExprValue(indexExpr) + ", " + reg1 + "\n");
        emit("\tbr i1 " + reg2 + ", label %" + oob1 + ", label %" + oob2);

        emit("\n" + oob1 + ":\n");
        emit("\t" + reg3 + " = add i32 " + getExprValue(indexExpr) + ", 1\n");
        emit("\t" + reg4 + " = getelementptr i32, i32* " + getExprValue(identifier) + ", i32 " + reg3 + "\n");
        emit("\tstore i32 " + getExprValue(expr) + ", i32* " + reg4 + "\n");
        emit("\tbr label %" + oob3 + "\n");

        emit("\n" + oob2 + ":\n");
        emit("\tcall void @throw_oob()\n");
        emit("\tbr label %" + oob3 + "\n");

        emit("\n" + oob3 + ":\n");

        return null;
    }

    /**
    * f0 -> Identifier()
    * f1 -> "="
    * f2 -> Expression()
    * f3 -> ";"
    */
    public String visit(AssignmentStatement n, Void argu) throws Exception {
        if (this.curClassSymbolTable == null) this.checkForFields = true;
        String identifier = n.f0.accept(this, argu);
        System.out.println("Assigning to: " + identifier);
        if (this.curClassSymbolTable == null) this.checkForFields = false;

        String identifierType;
        if (this.curClassSymbolTable == null){
            System.out.println("In main class -> searching type of " + identifier);
            identifierType = JavaToLLVM(methodCallerType.get("%" + identifier));
            System.out.println("Found type: " + identifierType);
        }
        else{
            identifierType = JavaToLLVM(curMethodSymbolTable.getIdentifierType(identifier, curClassSymbolTable, symbolTable)); 
        }

        if (this.curClassSymbolTable != null){
            //Search class and parents for field
            ClassSymbolTable tempClassSymbolTable = this.curClassSymbolTable;
            while(true){
                if (tempClassSymbolTable.hasField(identifier)){
                    int fieldOffset = tempClassSymbolTable.getFieldOffset(identifier) + 8;
                    System.out.println("----------------------Found field assignment: " + identifier + " with offset " + fieldOffset);

                    String reg1 = newTemp();
                    String reg2 = newTemp();
                    emit("\n\t" + reg1 + " = getelementptr i8, i8* %this, i32 " + fieldOffset + "\n");
                    emit("\t" + reg2 + " = bitcast i8* " + reg1 + " to " + identifierType + "*\n");
                    identifier = reg2.replace("%", "");
                }
            
                String parentClassName = tempClassSymbolTable.getParentName();
                if (parentClassName == null) {
                    break;
                }
                else{
                    tempClassSymbolTable = this.symbolTable.getClassSymbolTable(parentClassName);
                }
            }
        }
        n.f1.accept(this, argu);
        checkForFields = true;
        String expr = n.f2.accept(this, argu);
        checkForFields = false;
        n.f3.accept(this, argu);
        
        emit("\tstore " + identifierType + " " + getExprValue(expr) + ", " + identifierType + "* %" + identifier + "\n");
        
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
        
        String expr = n.f2.accept(this, argu);
        String exprValue = getExprValue(expr);
        exprValue = varToReg(exprValue, "i32");
        
        n.f3.accept(this, argu);
        n.f4.accept(this, argu);
        emit("\n\tcall void (i32) @print_int(i32 " + exprValue + ")\n");
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
    * f0 -> <INTEGER_LITERAL>
    */
    public String visit(IntegerLiteral n, Void argu) throws Exception {
        return n.f0.toString();
    }

    /**
    * f0 -> "true"
    */
    public String visit(TrueLiteral n, Void argu) throws Exception {
        return "true";
    }

    /**
     * f0 -> "false"
    */
    public String visit(FalseLiteral n, Void argu) throws Exception {
        return "false";
    }

    /**
    * f0 -> "this"
    */
    public String visit(ThisExpression n, Void argu) throws Exception {
        return "%this";
    }

     /**
    * f0 -> <IDENTIFIER>
    */
    public String visit(Identifier n, Void argu) throws Exception {
        String identifier = n.f0.toString();
        System.out.println("READ: " + identifier);

        if (checkForFields && this.curClassSymbolTable != null){
            System.out.println("Checking if " + identifier + " is a field/localvar");
            if (curMethodSymbolTable.hasLocalVariable(identifier)){
                String varType = curMethodSymbolTable.getIdentifierType(identifier, curClassSymbolTable, symbolTable);
                String varTypeLLVM = JavaToLLVM(varType);
                String reg1 = newTemp();
                this.methodCallerType.put(reg1, varType);
                emit("\t" + reg1 + " = load " + varTypeLLVM + ", " + varTypeLLVM + "* %" + identifier + "\n");
                return varTypeLLVM + " " + reg1;
            }
            else{ //check class and parents for field
                ClassSymbolTable tempClassSymbolTable = this.curClassSymbolTable;
                while(true){
                    if (tempClassSymbolTable.hasField(identifier)){
                        int fieldOffset = tempClassSymbolTable.getFieldOffset(identifier) + 8;
                        String fieldType = JavaToLLVM(tempClassSymbolTable.getFieldType(identifier));
                        String reg1 = newTemp();
                        String reg2 = newTemp();
                        String reg3 = newTemp();
                        emit("\t" + reg1 + " = getelementptr i8, i8* %this, i32 " + fieldOffset + "\n");
                        emit("\t" + reg2 + " = bitcast i8* " + reg1 + " to " + fieldType + "*\n");
                        emit("\t" + reg3 + " = load " + fieldType + ", " + fieldType + "* " + reg2 + "\n");
                        this.methodCallerType.put(reg1, fieldType);
                        return fieldType + " " + reg3;
                    }
                    String parentClassName = tempClassSymbolTable.getParentName();
                    if (parentClassName == null){
                        break;
                    }
                    else{
                        tempClassSymbolTable = this.symbolTable.getClassSymbolTable(parentClassName);
                    }
                }
            }
        }

        return identifier;
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
        String if1 = newIfLabel();
        String if2 = newIfLabel();
        String if3 = newIfLabel();
        this.checkForFields = true;
        String expr = n.f2.accept(this, argu);
        this.checkForFields = false;
        n.f3.accept(this, argu);
        System.out.println("if expression with: " + expr);
        emit("\n\tbr i1 " + getExprValue(expr) + ", label %" + if1 + ", label %" + if2 + "\n");
        
        emit("\n" + if1 + ":\n");
        n.f4.accept(this, argu);
        emit("\n\tbr label %" + if3 + "\n");
        
        emit("\n" + if2 + ":\n");
        n.f5.accept(this, argu);
        n.f6.accept(this, argu);
        emit("\n\tbr label %" + if3 + "\n");

        emit("\n" + if3 + ":\n");
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
        
        String loop1 = newLoopLabel();
        String loop2 = newLoopLabel();
        String loop3 = newLoopLabel();
        emit("\n\tbr label %" + loop1 + "\n");

        emit("\n" + loop1 + ":\n");
        String expr = n.f2.accept(this, argu);
        n.f3.accept(this, argu);

        emit("\tbr i1 " + getExprValue(expr) + ", label %" + loop2 + ", label %" + loop3 + "\n");

        emit("\n" + loop2 + ":\n");
        n.f4.accept(this, argu);
        emit("\n\tbr label %" + loop1 + "\n");
        
        emit("\n" + loop3 + ":\n");
        return null;
    }

    /**
    * f0 -> PrimaryExpression()
    * f1 -> "."
    * f2 -> "length"
    */
    public String visit(ArrayLength n, Void argu) throws Exception {
        String expr = n.f0.accept(this, argu);
        String reg1 = newTemp();
        System.out.println("Requested length of array: " + expr);
        n.f1.accept(this, argu);
        n.f2.accept(this, argu);
        emit("\t" + reg1 + " = load i32, i32* " + getExprValue(expr));
        return "i32 " + reg1 ;
    }

    
    
}
