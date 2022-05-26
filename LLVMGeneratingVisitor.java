import java.io.FileNotFoundException;
import java.io.PrintWriter;

import javax.print.DocFlavor.STRING;

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
    VTable vtable;
    PrintWriter writer;

    int registerCount = 1;
    int ifCount = 1;
    int oobCount = 1;
    int loopCount = 1;
    
    LLVMGeneratingVisitor(GlobalSymbolTable globalSymbolTable, PrintWriter llvmWriter) throws Exception{
        this.symbolTable = globalSymbolTable;
        this.writer = llvmWriter;
        this.vtable = new VTable(this.symbolTable);

        this.defineVTable();
        this.defineHelperMethods();
    }

    void emit(String data) { this.writer.print(data); }
    String newTemp() { return "%_" + registerCount++; }
    String newIfLabel() { return "if" + ifCount++; }
    String newOobLabel() { return "oob" + oobCount++; }
    String newLoopLabel() { return "loop" + loopCount++; }

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
    emit("    ret i32 0\n");
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
        n.f0.accept(this, argu);
        String returnType = JavaToLLVM(n.f1.accept(this, argu));
        String methodName = n.f2.accept(this, argu);
        n.f3.accept(this, argu);
        emit("define " + returnType + " @" + curClassSymbolTable.getClassName() + "." + methodName + "(i8* %this");
        n.f4.accept(this, argu);
        emit(") {\n");
        n.f5.accept(this, argu);
        n.f6.accept(this, argu);
        this.curMethodSymbolTable = curClassSymbolTable.getMethodSymbolTable(methodName);
        n.f7.accept(this, argu);
        n.f8.accept(this, argu);
        n.f9.accept(this, argu);
        n.f10.accept(this, argu);
        n.f11.accept(this, argu);
        n.f12.accept(this, argu);
        emit("}\n\n");
        return null;
     }

        /**
        * f0 -> Type()
        * f1 -> Identifier()
        */
    public String visit(FormalParameter n, Void argu) throws Exception {
        String type = JavaToLLVM(n.f0.accept(this, argu));
        String identifier = n.f1.accept(this, argu);
        emit(", " + type + " %." + identifier);
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

    String JavaToLLVM(String type){
        switch(type){
            case "boolean": return "i1";
            case "int": return "i32";
            default: return "i8*";
        }
    }
}
