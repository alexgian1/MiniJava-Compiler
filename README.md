# MiniJava-Compiler
A compiler for MiniJava, a subset of Java. MiniJava is designed so that its programs can be compiled by a full Java compiler like javac.


## Usage

- `make`: Compiles all files for the MiniJava compiler
- `make test`: Runs the MiniJava compiler in every test file and creates the file `test_results.txt` with the results in the root directory
- `make clean`: Deletes all files created by `make`

### To use the MiniJava compiler manually
```
make
java Main <inputFile1>.java <inputFile2>.java ...
```

### LLVM File Execution 
Install clang: `sudo apt update && sudo apt install clang`
```
clang -o out1 <inputFile1>.ll
./out1
```


If the MiniJava compiler type checking is successful, it will output the field and method offsets for each class in the Standard Output.
