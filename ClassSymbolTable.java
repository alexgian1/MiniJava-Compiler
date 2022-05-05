import java.util.Map;
import java.util.LinkedHashMap;

public class ClassSymbolTable {
    String className;
    Map membersTable;
    Map methodsTable;

    public ClassSymbolTable(String className){
        this.className = className;
        this.membersTable = new LinkedHashMap<String, String>();  //varName -> varType
        this.methodsTable = new LinkedHashMap<String, MethodSymbolTable>(); //methodName
    }
}
