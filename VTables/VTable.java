package VTables;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import SymbolTables.ClassSymbolTable;
import SymbolTables.GlobalSymbolTable;
import SymbolTables.MethodSymbolTable;

public class VTable {
    GlobalSymbolTable symbolTable;
 
    public VTable(GlobalSymbolTable globalSymbolTable){
        this.symbolTable = globalSymbolTable;
    }

    public String definition() throws Exception{
        String buffer = "";
        Set<String> classes = this.symbolTable.classesSymbolTable.keySet();
        for (String className : classes){
            if (className.equals(this.symbolTable.mainClass)){
                buffer += "@." + className + "_vtable = global [0 x i8*] []\n";
                continue;
            }
            ClassSymbolTable classSymbolTable = this.symbolTable.getClassSymbolTable(className);
            Set<String> methods = classSymbolTable.getMethodsTable().keySet();
            buffer += "@." + className + "_vtable = global [" + methods.size() + " x i8*] [";
            int methodIndex = 0;
            for (String methodName : methods){
                MethodSymbolTable methodSymbolTable = classSymbolTable.getMethodSymbolTable(methodName);
                String returnType = methodSymbolTable.getReturnType();
                buffer += "i8* bitcast (" + JavaToLLVM(returnType) + " (i8*";
                
                Map<String,String> argumentSymbolTable = methodSymbolTable.getArgumentSymbolTable();
                Set<String> arguments = argumentSymbolTable.keySet();
                for (String argument : arguments){
                    buffer += "," + JavaToLLVM(argumentSymbolTable.get(argument));
                }
                buffer += ")* @" + className + "." + methodName + " to i8*)";
                if (++methodIndex < methods.size()) buffer += ", ";
            }
            buffer += "]\n";
        }
        buffer += "\n";
        return buffer;
    }
    
    public String JavaToLLVM(String type){
        switch(type){
            case "boolean": return "i1";
            case "int": return "i32";
            case "boolean[]": return "i1*";
            case "int[]": return "i32*";
            default: return "i8*";
        }
    }
}
