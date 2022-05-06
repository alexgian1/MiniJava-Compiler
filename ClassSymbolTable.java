import java.util.Map;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.LinkedHashMap;

public class ClassSymbolTable {
    public String className;
    Map<String, String>  membersTable;
    Map<String, MethodSymbolTable>  methodsTable;

    public ClassSymbolTable(String className){
        this.className = className;
        this.membersTable = new LinkedHashMap<String, String>();  //varName -> varType
        this.methodsTable = new LinkedHashMap<String, MethodSymbolTable>(); //methodName -> methodSymbolTable
    }

    //Arguments in the form: "type1 arg1, type2 arg2, .." or null
    void addMethod(String methodName, String returnType) throws Exception{
        MethodSymbolTable st = new MethodSymbolTable(methodName, returnType);
        methodsTable.put(methodName, st);
    }

    MethodSymbolTable getMethodSymbolTable(String methodName) throws ParseException{
        if (!this.methodsTable.containsKey(methodName))
            throw new ParseException("Unknown symbol '" + methodName + "'");
        return methodsTable.get(methodName);
    }
}
