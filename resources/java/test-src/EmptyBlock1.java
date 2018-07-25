public class EmptyBlock1 {
    public static int f(int a, int b) {
        if (a * b > a + b) {
        } else {
            return a - b;
        }
        
        return a + b;
    }
}