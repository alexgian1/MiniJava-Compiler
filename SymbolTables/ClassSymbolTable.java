package SymbolTables;
import java.util.Map;
import java.util.Iterator;
import java.util.LinkedHashMap;

public class ClassSymbolTable {
    String className;
    String parentName;
    Map<String, String>  fieldsTable;
    Map<String, MethodSymbolTable>  methodsTable;

    public ClassSymbolTable(String className){
        this.className = className;
        this.parentName = null;
        this.fieldsTable = new LinkedHashMap<String, String>();  //varName -> varType
        this.methodsTable = new LinkedHashMap<String, MethodSymbolTable>(); //methodName -> methodSymbolTable
    }

    public ClassSymbolTable(String className, String parentName){
        this.className = className;
        this.parentName = parentName;
        this.fieldsTable = new LinkedHashMap<String, String>();  //varName -> varType
        this.methodsTable = new LinkedHashMap<String, MethodSymbolTable>(); //methodName -> methodSymbolTable
    }

    public String getClassName(){ return this.className; }

    public String getParentName(){ return this.parentName; }

    public Map<String, String>  getFieldsTable() { return this.fieldsTable;}

    public void addField(String fieldName, String type) throws Exception {
        if (this.fieldsTable.containsKey(fieldName))
            throw new Exception("Redefinition of field '" + fieldName + "'");

        fieldsTable.put(fieldName, type);
    }

    public void addMethod(String methodName, String returnType) throws Exception{
        if (this.methodsTable.containsKey(methodName)){
            throw new Exception("Duplicate method '" + methodName + "' in class '" + className + "'");
        }

        MethodSymbolTable st = new MethodSymbolTable(methodName, returnType);
        methodsTable.put(methodName, st);
    }

    public void checkMethodOverwrite(MethodSymbolTable methodSymbolTable, GlobalSymbolTable globalSymbolTable) throws Exception{
        //Check if method overwrites one from a parent class
        String methodName = methodSymbolTable.getMethodName();
        String returnType = methodSymbolTable.getReturnType();
        String curParentName = this.parentName;
        while(curParentName != null){
            ClassSymbolTable parentClassSymbolTable = globalSymbolTable.getClassSymbolTable(curParentName);
            if (parentClassSymbolTable.hasMethod(methodName)){
                MethodSymbolTable parentMethodSymbolTable = parentClassSymbolTable.getMethodSymbolTable(methodName);
                
                //Check return types
                if (!parentMethodSymbolTable.getReturnType().equals(returnType))
                    throw new Exception("Return type incompatible with " + parentClassSymbolTable.getClassName()
                                            + "." + methodName);
                
                //Check argument count and types
                Map<String, String> parentMethodArguments = parentMethodSymbolTable.getArgumentSymbolTable();
                Map<String, String> curMethodArguments = methodSymbolTable.getArgumentSymbolTable();
                if (parentMethodArguments.size() != curMethodArguments.size())
                    throw new Exception("Argument count incompatible with " + parentClassSymbolTable.getClassName()
                                            + "." + methodName);

                Iterator<?> parentIterator = parentMethodArguments.entrySet().iterator();
                Iterator<?> curIterator = curMethodArguments.entrySet().iterator();
                while (curIterator.hasNext()){
                    //System.out.println(curIterator.next().toString() + " == " + parentIterator.next().toString());
                    if (!curIterator.next().toString().equals(parentIterator.next().toString()))
                        throw new Exception("Argument types incompatible with " + parentClassSymbolTable.getClassName()
                                                    + "." + methodName);
                }
            }
            curParentName = parentClassSymbolTable.getParentName();
        }
    }

    public MethodSymbolTable getMethodSymbolTable(String methodName) throws Exception{
        if (!this.methodsTable.containsKey(methodName))
            throw new Exception("Unknown symbol '" + methodName + "'");
        return methodsTable.get(methodName);
    }

    //Search parent classes too if method not found
    public MethodSymbolTable getMethodSymbolTable(String methodName, GlobalSymbolTable globalSymbolTable) throws Exception{
        if (this.methodsTable.containsKey(methodName))
            return methodsTable.get(methodName);

        String curParentName = this.parentName;
        while(curParentName != null){
            ClassSymbolTable parentClassSymbolTable = globalSymbolTable.getClassSymbolTable(curParentName);
            if (parentClassSymbolTable.hasMethod(methodName))
                return parentClassSymbolTable.getMethodSymbolTable(methodName);
            curParentName = parentClassSymbolTable.getParentName();
        }
        
        throw new Exception("Unknown symbol '" + methodName + "'");
    }

    public boolean hasMethod(String methodName){
        return this.methodsTable.containsKey(methodName);
    }

}
