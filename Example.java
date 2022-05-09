class Example {
    public static void main(String[] args) {
    }
}

class A {
    int i;
    A b;
    boolean[] x;

    public int foo(int i, int j, boolean[] a){
        x = new boolean[i];
        b = new A();
        j = A.foo(i,i,x);
        x[j+1] = ((2+3) < 4) && ((3 * 4) < (2 -2));
        while (a[1]){
            if (this.test(a[0])){
                x[j] = a[i];
            }
            else{}
        }
        x = a;
        return i; 
    }
    public int bar(){ return 1; }
    public boolean test(boolean x) { return x && x; }
}

class B extends A {
    int i;
    boolean[] b;

    public int foo(int i, int j, boolean[] a) { return i+j; }
    public int foobar(boolean k){ return 1; }
}


class C extends B {
    int i;
    A objectA;
    boolean boolB;

    public int foo(int i, int j, boolean[] a) {
        objectA = new A();
        boolB = objectA.test(boolB);
        return i; 
    }
    public int foobar(boolean k){ return 1; }
}