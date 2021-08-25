package learn.stream;

/**
 * @Author: wenhongliang
 */
public class TestCatch {
    public static void main(String[] args) {
        new TestCatch().run1();
    }

    public void run1() {
        try {
            int k = runInner();
            System.out.println(k);
        } catch (RuntimeException e) {
            System.out.println("RuntimeException:" + e.getMessage());
            throw e;
        }
    }

    private int runInner() {
        try {
            int c = 10 / 0;
            return c;
        } catch (Exception e) {
            System.out.println("Inner Exception:" + e.getMessage());
        }
        return 0;
    }
}
