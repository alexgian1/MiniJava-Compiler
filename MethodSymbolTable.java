import java.util.Map;
import java.util.LinkedHashMap;

public class MethodSymbolTable {
    String methodName;
    String returnType;
    Map argumentsTable;
    Map localVariablesTable;

    public MethodSymbolTable(String methodName, String returnType){
        this.methodName = methodName;
        this.returnType = returnType;
        this.argumentsTable = new LinkedHashMap<String, String>();   //varName -> varType
        this.localVariablesTable = new LinkedHashMap<String, String>();  //varName -> varType
    }
}
