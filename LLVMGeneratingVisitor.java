import java.io.PrintWriter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;


import SymbolTables.ClassSymbolTable;
import SymbolTables.GlobalSymbolTable;
import SymbolTables.MethodSymbolTable;
import VTables.VTable;
import syntaxtree.*;
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

    boolean isInMethod, isCallerParameter, checkForFields, returnArguments = false;
    
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
    void resetRegisters() { this.registerCount = 0; }

    String getExprType(String expr) {
        System.out.println("REQUESTED TYPE OF " + expr);
        if (expr.split(" ").length == 3)
            return expr.split(" ")[1];
        else
            return expr.split(" ")[0];
    }
    
    String getExprValue(String expr) { 
        if (expr.split(" ").length == 2)
            return expr.split(" ")[1]; 
        else
            return expr.split(" ")[2]; 
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
    n.f14.accept(this, argu);
    n.f15.accept(this, argu);
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
        String expr = n.f10.accept(this, argu);
        emit("\nret " + getExprType(expr) + " " + getExprValue(expr) + " ");
        n.f11.accept(this, argu);
        n.f12.accept(this, argu);
        this.isInMethod = false;
        this.returnArguments = false;
        emit("}\n\n");
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
        String expr = n.f0.accept(this, argu);
        System.out.println("Expr = " + expr);
        String objectReg = getExprValue(expr);
        String objectType = this.methodCallerType.get(objectReg);
        if (objectType == null)
            objectType = getExprType(expr).replace("%", "");
        if (expr.equals("i8* %this"))
            objectType = curClassSymbolTable.getClassName();

        n.f1.accept(this, argu);
        String methodName = n.f2.accept(this, argu);
        MethodSymbolTable methodSymbolTable = symbolTable.getClassSymbolTable(objectType).getMethodSymbolTable(methodName);
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
            System.out.println("Method " + methodName + " has argument: " + argument);
            String type = JavaToLLVM(argumentSymbolTable.get(argument));
            emit(", " + type);
        }
        emit(")*\n");
        
        String reg7 = newTemp();
        
        
        n.f3.accept(this, argu);
        this.isCallerParameter = true;
        String argList = n.f4.accept(this, argu);
        this.isCallerParameter = false;
        n.f5.accept(this, argu);
        emit("\t" + reg7 + " = call " + llvmReturnType + " " + reg6 + "(i8* " + objectReg); //Continue with call parameters
        if (argList != null)
            emit (", " + argList);
        emit(")\n");
        System.out.println("Called " + methodName + " with arguments: " + argList);        
        System.out.println(methodReturnType + " " + reg7);
        return llvmReturnType + " " + reg7;
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
        String expr = n.f0.accept(this, argu);
        System.out.println("Read PrimaryExpression: " + expr);
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
        
        System.out.println(expr);
        //if (isCallerParameter){
        //    String type = getExprType(expr);
        //    String value = getExprValue(expr);
        //    emit(", " + type + " " + value);
        //}
        return expr;
    }

    /**
    * f0 -> ","
    * f1 -> Expression()
    */
    public String visit(ExpressionTerm n, Void argu) throws Exception {
        n.f0.accept(this, argu);
        String expr = n.f1.accept(this, argu);
        return ", " + expr;
    }

    /**
    * f0 -> Expression()
    * f1 -> ExpressionTail()
    */
    public String visit(ExpressionList n, Void argu) throws Exception {
        String expr1 = n.f0.accept(this, argu);
        String expr2 = n.f1.accept(this, argu);
        String ret = expr1 + expr2;
        ret = ret.replace(", null", "");
        ret = ret.replace("null", "");
        return ret;
    }

    /**
    * f0 -> ( ExpressionTerm() )*
    */
    public String visit(ExpressionTail n, Void argu) throws Exception {
        return ", " + n.f0.accept(this, argu);
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
        String expr = n.f3.accept(this, argu);
        n.f4.accept(this, argu);

        String reg1 = newTemp();
        String labelError = newArrAllocLabel();
        String labelSuccess = newArrAllocLabel();
        emit("\t" + reg1 + " = icmp slt i32 " + expr + ", 0\n");
        emit("\tbr i1 " + reg1 + ", label %" + labelError + ", label %" + labelSuccess + "\n\n");

        emit(labelError + ":\n");
        emit("\tcall void @throw_oob()\n");
        emit("\tbr label %" + labelSuccess + "\n\n");

        emit(labelSuccess + ":\n");
        String reg2 = newTemp();
        emit("\t" + reg2 + " = add i32 " + expr + ", 1\n");
        String reg3 = newTemp();
        emit("\t" + reg3 + " = call i8* @calloc(i32 1, i32 " + reg2 + ")\n");
        String reg4 = newTemp();
        emit("\t" + reg4 + " = bitcast i8* " + reg3 + " to i32*\n");
        emit("\tstore " + expr + ", i32* " + reg4 + "\n");

        return "i1* " + reg4;
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
        String expr = n.f3.accept(this, argu);
        n.f4.accept(this, argu);

        String reg1 = newTemp();
        String labelError = newArrAllocLabel();
        String labelSuccess = newArrAllocLabel();
        emit("\t" + reg1 + " = icmp slt i32 " + expr + ", 0\n");
        emit("\tbr i1 " + reg1 + ", label %" + labelError + ", label %" + labelSuccess + "\n\n");

        emit(labelError + ":\n");
        emit("\tcall void @throw_oob()\n");
        emit("\tbr label %" + labelSuccess + "\n\n");

        emit(labelSuccess + ":\n");
        String reg2 = newTemp();
        emit("\t" + reg2 + " = add i32 " + expr + ", 1\n");
        String reg3 = newTemp();
        emit("\t" + reg3 + " = call i8* @calloc(i32 4, i32 " + reg2 + ")\n");
        String reg4 = newTemp();
        emit("\t" + reg4 + " = bitcast i8* " + reg3 + " to i32*\n");
        emit("\tstore i32 " + expr + ", i32* " + reg4 + "\n");

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
        String expr = n.f1.accept(this, argu);
        String value = getExprValue(expr);
        String reg1 = newTemp();
        emit("\t" + reg1 + " = xor i1 1, " + value);
        return "i1 " + reg1;
    }

    /**
    * f0 -> Clause()
    * f1 -> "&&"
    * f2 -> Clause()
    */
    public String visit(AndExpression n, Void argu) throws Exception {
        n.f0.accept(this, argu);
        n.f1.accept(this, argu);
        n.f2.accept(this, argu);
        return "i1 ";
    }

    /**
     * f0 -> PrimaryExpression()
    * f1 -> "<"
    * f2 -> PrimaryExpression()
    */
    public String visit(CompareExpression n, Void argu) throws Exception {
        String expr1 = n.f0.accept(this, argu);
        String value1 = getExprValue(expr1);
        n.f1.accept(this, argu);
        String expr2 = n.f2.accept(this, argu);
        String value2 = getExprValue(expr2);
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
        System.out.println("Parsed " + value1 + " of type " + getExprType(expr1));
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
        System.out.println("reached--------------------");
        String arrayExpr = n.f0.accept(this, argu);
        String arrayReg = getExprValue(arrayExpr);
        System.out.println("arrayExpr: " + arrayReg);

        String arrayType;
        if(arrayExpr.startsWith("i")) arrayType = getExprType(arrayExpr);
        else arrayType = JavaToLLVM(methodCallerType.get(arrayReg));
        
        n.f1.accept(this, argu);
        this.checkForFields = true;
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
        if (arrayType.equals("i32*")){
            emit("\n\t" + reg1 + " = getelementptr i8, i8* %this, i32 8\n");
            emit("\t" + reg2 + " = bitcast i8* " + reg1 + " to i32**\n");
            emit("\t" + reg3 + " = load i32*, i32** " + reg2 + "\n");
            indexReg = varToReg(indexReg, "i32");
            emit("\t" + reg4 + " = load i32, i32* " + reg3 + "\n");
            emit("\t" + reg5 + " = icmp ult i32 " + indexReg + ", " + reg4 + "\n");
            emit("\tbr i1 " + reg5 + ", label %" + oob1 + ", label %" + oob2 + "\n");

            emit("\n" + oob1 + ":\n");
            emit("\t" + reg6 + " = add " + indexReg + ", 1\n");
            emit("\t" + reg7 + " = getelementptr i32, i32* " + reg3 + ", " + reg6 + "\n");
            emit("\t" + reg8 + " = load i32, i32* " + reg7 + "\n");
            emit("\tbr label %" + oob3 + "\n");

            emit("\n" + oob2 + ":\n");
            emit("\tcall void @throw_oob()\n");
            emit("\tbr label %" + oob3 + "\n");

            emit("\n" + oob3 + ":\n");
        }
        //bool array
        else{

        }
        
        return "i32* " + reg8;
    }

    /**
    * f0 -> Identifier()
    * f1 -> "="
    * f2 -> Expression()
    * f3 -> ";"
    */
    public String visit(AssignmentStatement n, Void argu) throws Exception {
        String identifier = n.f0.accept(this, argu);
        n.f1.accept(this, argu);
        checkForFields = true;
        String expr = n.f2.accept(this, argu);
        checkForFields = false;
        n.f3.accept(this, argu);
        
        
        String identifierType = JavaToLLVM(curMethodSymbolTable.getIdentifierType(identifier, curClassSymbolTable, symbolTable));

        emit("\tstore " + expr + ", " + identifierType + "* %" + identifier + "\n");
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
        System.out.println("Trying to print " + expr);
        n.f3.accept(this, argu);
        n.f4.accept(this, argu);
        emit("\n\tcall void (i32) @print_int(i32 " + getExprValue(expr) + ")\n");
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

        if (checkForFields){
            //TODO: Check for local variables/parameters
            if (curMethodSymbolTable.hasLocalVariable(identifier)){
                String varType = JavaToLLVM(curMethodSymbolTable.getIdentifierType(identifier, curClassSymbolTable, symbolTable));
                System.out.println("Found " + identifier + " with type " + varType + " in local vars");
                String reg1 = newTemp();
                emit("\t" + reg1 + " = load " + varType + ", " + varType + "* %" + identifier + "\n");
                return varType + " " + reg1;
            }
            else if (curClassSymbolTable.hasField(identifier)){
                System.out.println(identifier + " is class field");
                int fieldOffset = curClassSymbolTable.getFieldOffset(identifier) + 8;
                String fieldType = JavaToLLVM(curClassSymbolTable.getFieldType(identifier));
                System.out.println(identifier + " offset: " + fieldOffset);
                String reg1 = newTemp();
                String reg2 = newTemp();
                String reg3 = newTemp();
                emit("\t" + reg1 + " = getelementptr i8, i8* %this, i32 " + fieldOffset + "\n");
                emit("\t" + reg2 + " = bitcast i8* " + reg1 + " to " + fieldType + "*\n");
                emit("\t" + reg3 + " = load " + fieldType + ", " + fieldType + "* " + reg2 + "\n");
                return fieldType + " " + reg3;
            }
        }

        return identifier;
    }

    String JavaToLLVM(String type){
        switch(type){
            case "boolean": return "i1";
            case "int": return "i32";
            case "boolean[]": return "i1*";
            case "int[]": return "i32*";
            default: return "i8*";
        }
    }
}
