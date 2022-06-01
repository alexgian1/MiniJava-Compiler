class Example {
    public static void main(String[] args) {
        System.out.println(new A().foo(15));
        System.out.println(new A().bar(3));
    }
}

class A {
    int[] i;

    public int foo(int x) {
        i = new int[x];
        return i.length; 
    }

    public int bar(int x) {
        int ret;
        i = new int[x];
        i[1] = 100;
        ret = i[1];
        return ret; 
    }
}

