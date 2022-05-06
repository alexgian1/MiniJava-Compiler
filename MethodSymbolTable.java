import java.util.Map;
import java.util.LinkedHashMap;

public class MethodSymbolTable {
    String methodName;
    String returnType;
    Map<String, String> argumentsTable;
    Map<String, String> localVariablesTable;

    public MethodSymbolTable(String methodName, String returnType){
        this.methodName = methodName;
        this.returnType = returnType;
        this.argumentsTable = new LinkedHashMap<String, String>();   //varName -> varType
        this.localVariablesTable = new LinkedHashMap<String, String>();  //varName -> varType
    }

    String getMethodName() { return this.methodName; }

    String getReturnType() { return this.returnType; }

    Map<String, String> getArgumentSymbolTable() { return this.argumentsTable; }

    public void addLocalVariable(String name, String type) throws ParseException{
        if (this.localVariablesTable.containsKey(name))
            throw new ParseException("Redefinition of variable '" + name + "'");
        
        this.localVariablesTable.put(name,type);
    }

    public void addArgument(String name, String type) throws ParseException{
        if (this.argumentsTable.containsKey(name))
            throw new ParseException("Duplicate argument '" + name + "'");
        
        this.argumentsTable.put(name,type);
    }
}
