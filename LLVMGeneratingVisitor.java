import SymbolTables.GlobalSymbolTable;

import syntaxtree.*;
import visitor.*;

public class LLVMGeneratingVisitor extends GJDepthFirst<String, Void>{
    GlobalSymbolTable symbolTable;
    
    LLVMGeneratingVisitor(GlobalSymbolTable globalSymbolTable){
        this.symbolTable = globalSymbolTable;
    }
}
