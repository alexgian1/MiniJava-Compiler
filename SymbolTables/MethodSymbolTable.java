package SymbolTables;
import java.util.Map;
import java.util.LinkedHashMap;

public class MethodSymbolTable {
    String methodName;
    String returnType;
    Map<String, String> argumentsTable;
    Map<String, String> localVariablesTable;
    int offset;

    public MethodSymbolTable(String methodName, String returnType){
        this.methodName = methodName;
        this.returnType = returnType;
        this.argumentsTable = new LinkedHashMap<String, String>();   //varName -> varType
        this.localVariablesTable = new LinkedHashMap<String, String>();  //varName -> varType
    }

    public void setOffset(int offset) { this.offset = offset; }
    public int getOffset() { return this.offset; }

    public String getMethodName() { return this.methodName; }

    public String getReturnType() { return this.returnType; }

    public Map<String, String> getArgumentSymbolTable() { return this.argumentsTable; }

    public void addLocalVariable(String name, String type) throws Exception{
        if (this.localVariablesTable.containsKey(name))
            throw new Exception("Redefinition of variable '" + name + "'");
        
        this.localVariablesTable.put(name,type);
    }

    public void addArgument(String name, String type) throws Exception{
        if (this.argumentsTable.containsKey(name))
            throw new Exception("Duplicate argument '" + name + "'");
        
        this.argumentsTable.put(name,type);
    }

    public String getIdentifierType(String identifier, ClassSymbolTable curClassSymbolTable, GlobalSymbolTable globalSymbolTable) throws Exception{
        //Search method local variables -> method arguments -> class fields -> parent class fields

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

    public boolean hasLocalVariable(String identifier){
        if (this.localVariablesTable.containsKey(identifier))
            return true;
        
        else if (this.argumentsTable.containsKey(identifier))
            return true;

        return false;
    }

    
}
