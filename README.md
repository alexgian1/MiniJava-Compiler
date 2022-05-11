# MiniJava-Compiler
A compiler for MiniJava, a subset of Java. MiniJava is designed so that its programs can be compiled by a full Java compiler like javac.


## Usage

- `make`: Compiles all files for the MiniJava compiler
- `make test`: Runs the MiniJava compiler in every test file and creates the file `test_results.txt` with the results in the root directory
- `make clean`: Deletes all files created by `make`

To use the MiniJava compiler manually:
```
make
java Main <inputFile1> <inputFile2> ...
```

If the MiniJava compiler type checking is successful, it will print the the field and method offsets for each class in the Standard Output.
