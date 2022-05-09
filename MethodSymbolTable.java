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

    public String getIdentifierType(String identifier, ClassSymbolTable curClassSymbolTable, GlobalSymbolTable globalSymbolTable) throws ParseException{
        //Search method local variables -> method arguments -> class fields -> parent class fields
        System.out.println("Searching for identifier: " + identifier);

        if (this.localVariablesTable.containsKey(identifier))
            return this.localVariablesTable.get(identifier);
        
        else if (this.argumentsTable.containsKey(identifier))
            return this.argumentsTable.get(identifier);
        
        else if (curClassSymbolTable.getFieldsTable().containsKey(identifier))
            return curClassSymbolTable.getFieldsTable().get(identifier);
        
        else{
            String curParentName = curClassSymbolTable.getParentName();
            while (curParentName != null){
                ClassSymbolTable parentClassSymbolTable = globalSymbolTable.getClassSymbolTable(curParentName);
                if (parentClassSymbolTable.getFieldsTable().containsKey(identifier)){
                    return parentClassSymbolTable.getFieldsTable().get(identifier);
                }
                curParentName = parentClassSymbolTable.getParentName();
            }
        }
        
        //Identifier not found
        return null;
    }
}
