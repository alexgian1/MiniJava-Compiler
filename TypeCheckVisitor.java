import syntaxtree.*;
import visitor.*;

public class TypeCheckVisitor extends GJDepthFirst<String, Void>{
    GlobalSymbolTable symbolTable;

    TypeCheckVisitor(GlobalSymbolTable globalSymbolTable){
        this.symbolTable = globalSymbolTable;
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
