import java.util.Map;
import java.util.LinkedHashMap;

public class GlobalSymbolTable {
    Map<String, ClassSymbolTable> classesSymbolTable;

    GlobalSymbolTable(){
        this.classesSymbolTable = new LinkedHashMap<String, ClassSymbolTable>();
    }

    public void addClass(String className) throws ParseException{
        if (this.classesSymbolTable.containsKey(className))
            throw new ParseException("Redefinition of class '" + className + "'");
        ClassSymbolTable st = new ClassSymbolTable(className);
        this.classesSymbolTable.put(className, st);
    }

    public void addClass(String className, String parentName) throws ParseException{
        if (this.classesSymbolTable.containsKey(className))
            throw new ParseException("Redefinition of class '" + className + "'");
        ClassSymbolTable st = new ClassSymbolTable(className, parentName);
        this.classesSymbolTable.put(className, st);
    }

    public ClassSymbolTable getClassSymbolTable(String className) throws ParseException{
        if (!this.classesSymbolTable.containsKey(className))
            throw new ParseException("Unknown symbol '" + className + "'");
        return classesSymbolTable.get(className);
    }

    public boolean hasClass(String className){
        return this.classesSymbolTable.containsKey(className);
    }
}
