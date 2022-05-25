package SymbolTables;
import java.util.Map;
import java.util.LinkedHashMap;

public class GlobalSymbolTable {
    public Map<String, ClassSymbolTable> classesSymbolTable;
    public String mainClass;

    public GlobalSymbolTable(){
        this.classesSymbolTable = new LinkedHashMap<String, ClassSymbolTable>();
    }

    public void addMainClass(String className){
        this.mainClass = className;
        ClassSymbolTable st = new ClassSymbolTable(className);
        this.classesSymbolTable.put(className, st);
    }

    public void addClass(String className) throws Exception{
        if (this.classesSymbolTable.containsKey(className))
            throw new Exception("Redefinition of class '" + className + "'");
        ClassSymbolTable st = new ClassSymbolTable(className);
        this.classesSymbolTable.put(className, st);
    }

    public void addClass(String className, String parentName) throws Exception{
        if (this.classesSymbolTable.containsKey(className))
            throw new Exception("Redefinition of class '" + className + "'");
        ClassSymbolTable st = new ClassSymbolTable(className, parentName);
        this.classesSymbolTable.put(className, st);
    }

    public ClassSymbolTable getClassSymbolTable(String className) throws Exception{
        if (!this.classesSymbolTable.containsKey(className))
            throw new Exception("Unknown symbol '" + className + "'");
        return classesSymbolTable.get(className);
    }

    public boolean hasClass(String className){
        return this.classesSymbolTable.containsKey(className);
    }

    public boolean isDerived(String derived, String base){
        ClassSymbolTable derivedClassSymbolTable = this.classesSymbolTable.get(derived);
        
        String curParent = derivedClassSymbolTable.getParentName();
        while (curParent != null){
            ClassSymbolTable parentClassSymbolTable = this.classesSymbolTable.get(curParent);
            if (curParent == base) return true;
            curParent = parentClassSymbolTable.getParentName();
        }

        return false;
    }

    public void calculateOffsets() throws Exception{
        for (String className : this.classesSymbolTable.keySet()){
            if (className == this.mainClass) continue;

            ClassSymbolTable classSymbolTable = this.classesSymbolTable.get(className);
            classSymbolTable.calculateParentOffsets(this);
            System.out.println("-----------Class " + className + "-----------");

            //Print fields
            Map<String, String> fieldsTable = classSymbolTable.getFieldsTable();
            System.out.println("--Variables---");
            for (String fieldName : fieldsTable.keySet()){
                System.out.println(className + "." + fieldName + " : " + classSymbolTable.fieldsOffset);
                String fieldType = fieldsTable.get(fieldName);
                if (fieldType == "boolean")
                    classSymbolTable.fieldsOffset += 1;
                else if (fieldType == "int")
                    classSymbolTable.fieldsOffset += 4;
                else
                    classSymbolTable.fieldsOffset += 8;     //array or method
            }

            //Print methods
            Map<String, MethodSymbolTable> methodsTable = classSymbolTable.getMethodsTable();
            System.out.println("---Methods---");
            for (String methodName : methodsTable.keySet()){
                if (!classSymbolTable.checkMethodOverwrite(methodsTable.get(methodName), this)){
                    System.out.println(className + "." + methodName + " : " + classSymbolTable.methodsOffset);
                    classSymbolTable.methodsOffset += 8;
                }
            }

            System.out.println();
        }
    }
}
