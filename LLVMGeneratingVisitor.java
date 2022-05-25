import java.io.FileNotFoundException;
import java.io.PrintWriter;

import SymbolTables.GlobalSymbolTable;
import VTables.VTable;
import syntaxtree.*;
import visitor.*;

public class LLVMGeneratingVisitor extends GJDepthFirst<String, Void>{
    GlobalSymbolTable symbolTable;
    VTable vtable;
    PrintWriter writer;
    
    LLVMGeneratingVisitor(GlobalSymbolTable globalSymbolTable, PrintWriter llvmWriter) throws Exception{
        this.symbolTable = globalSymbolTable;
        this.writer = llvmWriter;
        this.vtable = new VTable(this.symbolTable);

        this.defineVTable();
        this.defineHelperMethods();
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
    writer.println("\ndefine i32 @main() {");
    n.f8.accept(this, argu);
    n.f9.accept(this, argu);
    n.f10.accept(this, argu);
    n.f11.accept(this, argu);
    n.f12.accept(this, argu);
    n.f13.accept(this, argu);
    n.f14.accept(this, argu);
    n.f15.accept(this, argu);
    writer.println("    ret i32 0");
    n.f16.accept(this, argu);
    writer.println("}");
    n.f17.accept(this, argu);
    return null;
 }
}
