package SymbolTables;
import java.util.Map;
import java.util.Set;
import java.util.Iterator;
import java.util.LinkedHashMap;

public class ClassSymbolTable {
    String className;
    String parentName;
    Map<String, String>  fieldsTable;
    Map<String, MethodSymbolTable>  methodsTable;

    public int fieldsOffset;
    public int methodsOffset;

    public ClassSymbolTable(String className){
        this.className = className;
        this.parentName = null;
        this.fieldsTable = new LinkedHashMap<String, String>();  //varName -> varType
        this.methodsTable = new LinkedHashMap<String, MethodSymbolTable>(); //methodName -> methodSymbolTable
        
        this.fieldsOffset = 0;
        this.methodsOffset = 0;
    }

    public ClassSymbolTable(String className, String parentName) throws Exception{
        this.className = className;
        this.parentName = parentName;
        this.fieldsTable = new LinkedHashMap<String, String>();  //varName -> varType
        this.methodsTable = new LinkedHashMap<String, MethodSymbolTable>(); //methodName -> methodSymbolTable
    
        this.fieldsOffset = 0;
        this.methodsOffset = 0;
    }

    public String getClassName(){ return this.className; }

    public String getParentName(){ return this.parentName; }

    public int getClassSize(){ return fieldsOffset + 8; }

    public int getNumMethods(GlobalSymbolTable globalSymbolTable) throws Exception { 
        return this.getAllMethodsTable(globalSymbolTable).size(); 
    }

    public Map<String, String>  getFieldsTable() { return this.fieldsTable; }
    public Map<String, MethodSymbolTable>  getMethodsTable() { return this.methodsTable; }

    public Map<String, MethodSymbolTable>  getAllMethodsTable(GlobalSymbolTable globalSymbolTable) throws Exception {
        Map<String, MethodSymbolTable> allMethodsTable = new LinkedHashMap<String, MethodSymbolTable>();
        String curClassName = this.getClassName();
        while (curClassName != null){
            if (curClassName == globalSymbolTable.mainClass) break;
            ClassSymbolTable curClassSymbolTable = globalSymbolTable.getClassSymbolTable(curClassName);
            Map<String, MethodSymbolTable> curClassMethods = curClassSymbolTable.getMethodsTable();
            Set<String> methodNames = curClassMethods.keySet();
            for(String methodName : methodNames){
                allMethodsTable.putIfAbsent(methodName, curClassMethods.get(methodName));
            }
            curClassName = curClassSymbolTable.getParentName();
        }
        return allMethodsTable; 
    }

    public void addField(String fieldName, String type) throws Exception {
        if (this.fieldsTable.containsKey(fieldName))
            throw new Exception("Redefinition of field '" + fieldName + "'");

        fieldsTable.put(fieldName, type);
    }

    public void addMethod(String methodName, String returnType) throws Exception{
        if (this.methodsTable.containsKey(methodName)){
            throw new Exception("Duplicate method '" + methodName + "' in class '" + className + "'");
        }

        MethodSymbolTable st = new MethodSymbolTable(this.className, methodName, returnType);
        methodsTable.put(methodName, st);
    }

    public MethodSymbolTable checkMethodOverwrite(MethodSymbolTable methodSymbolTable, GlobalSymbolTable globalSymbolTable) throws Exception{
        //Check if method overwrites one from a parent class
        String methodName = methodSymbolTable.getMethodName();
        String returnType = methodSymbolTable.getReturnType();
        String curParentName = this.parentName;
        MethodSymbolTable overwrittenMethod = null;
        while(curParentName != null){
            ClassSymbolTable parentClassSymbolTable = globalSymbolTable.getClassSymbolTable(curParentName);
            if (parentClassSymbolTable.hasMethod(methodName)){
                MethodSymbolTable parentMethodSymbolTable = parentClassSymbolTable.getMethodSymbolTable(methodName);
                overwrittenMethod = parentMethodSymbolTable;
                
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
        return overwrittenMethod;
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

    public void calculateParentOffsets(GlobalSymbolTable globalSymbolTable) throws Exception{
        if (this.parentName != null){
            ClassSymbolTable parentSymbolTable = globalSymbolTable.getClassSymbolTable(this.parentName);
            this.fieldsOffset = parentSymbolTable.fieldsOffset;
            this.methodsOffset = parentSymbolTable.methodsOffset;
        }
    }

    public boolean hasField(String identifier){
        return this.fieldsTable.containsKey(identifier);
    }

    public int getFieldOffset(String identifier){
        int offset = 0;
        Set<String> fields = this.fieldsTable.keySet();
        for(String field : fields){
            if (field.equals(identifier)) return offset;
            String type = this.fieldsTable.get(field);
            switch(type){
                case "int" : 
                    offset += 4;
                    break;
                case "boolean" : 
                    offset += 1;
                    break;
                default : offset += 8;
            }
        }
        return -1;
    }

    public String getFieldType(String identifier){
        return this.fieldsTable.get(identifier);
    }
}
