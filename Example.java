class Example {
    public static void main(String[] args) {
        System.out.println(new A().foo());
    }
}

class A {
    int[] i;

    public int foo() {
        i = new int[4];
        return i.length; 
    }
}

