class Example {
    public static void main(String[] args) {
    }
}

class A {
    int i;
    A b;

    public int foo(int i, int j, int a) { 
        return i+j; 
    }
    public int bar(){ return 1; }
}

class B extends A {
    int i;

    public int foo(int i, int j, int a) { return i+j; }
    public int foobar(boolean k){ return 1; }
}


class C extends B {
    int i;

    public int foo(int i, int j, int a) { return i+j; }
    public int foobar(boolean k){ return 1; }
}