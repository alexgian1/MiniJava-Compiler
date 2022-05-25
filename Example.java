class Example {
    public static void main(String[] args) {
    }
}

class A {
    int i;
    A a;

    public int foo(int a, int b, int c) { return a+b; }
    public int bar(){ return 1; }
}

class B{
    int i;

    public int foo(int i, int j, int a) { return i+j; }
    public int foobar(boolean k){ return 1; }
}