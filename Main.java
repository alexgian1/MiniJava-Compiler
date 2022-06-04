import syntaxtree.*;
import visitor.*;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;

import SymbolTables.GlobalSymbolTable;

public class Main {
    public static void main(String[] args) throws Exception {
        if(args.length < 1){
            System.err.println("Usage: java Main <inputFile1> <inputFile2> ...");
            System.exit(1);
        }

        FileInputStream fis = null;
        PrintWriter writer = new PrintWriter("test_results.txt");
        for(int i = 0; i < args.length; i++){
            try{
                    fis = new FileInputStream(args[i]);
                    MiniJavaParser parser = new MiniJavaParser(fis);
                    Goal root = parser.Goal();
                    System.err.println("Program parsed successfully.");

                    GlobalSymbolTable globalSymbolTable = new GlobalSymbolTable();
                    SymbolTableVisitor eval1 = new SymbolTableVisitor(globalSymbolTable);
                    root.accept(eval1, null);

                    TypeCheckVisitor eval2 = new TypeCheckVisitor(globalSymbolTable);
                    root.accept(eval2, null);
                    System.out.println("Type check success.");

                    globalSymbolTable.calculateOffsets();
                    PrintWriter llvmWriter = new PrintWriter(args[i].substring(0, args[i].length()-5) + ".ll");
                    LLVMGeneratingVisitor codeGen = new LLVMGeneratingVisitor(globalSymbolTable, llvmWriter);
                    root.accept(codeGen, null);
                    llvmWriter.close();
                    System.out.println("LLVM IR generation success.");
                    writer.println("Success: " + args[i]);
            }
            catch(FileNotFoundException ex){
                System.err.println(ex.getMessage());
            }
            catch(Exception ex){
                writer.println("Failed: " + args[i]);
                System.out.println(ex.getMessage());
            }
            finally{
                try{
                    if(fis != null) fis.close();
                }
                catch(IOException ex){
                    System.err.println(ex.getMessage());
                }
            }
        }
        writer.close();
    }
}


class MyVisitor extends GJDepthFirst<String, Void>{
    
}
