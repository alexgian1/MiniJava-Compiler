package SymbolTables;
import java.util.Map;
import java.util.LinkedHashMap;

public class GlobalSymbolTable {
    Map<String, ClassSymbolTable> classesSymbolTable;

    public GlobalSymbolTable(){
        this.classesSymbolTable = new LinkedHashMap<String, ClassSymbolTable>();
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
}
