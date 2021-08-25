package learn.stream;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * @Author: wenhongliang
 */
public class TestFuture {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        runAsync();
        supplyAsync();
        then();
    }

    public static void runAsync() throws ExecutionException, InterruptedException {
        // 不带返回值
        CompletableFuture future = CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(1000L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("run");
        }).whenComplete((t, u) -> {
            System.out.println(t + ":" + u);
        });
        System.out.println("run future:" + future.get());

    }

    public static void supplyAsync() throws ExecutionException, InterruptedException {
        // 带返回值
        CompletableFuture future = CompletableFuture.supplyAsync(() -> {
            System.out.println("supply");
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return "123";
        }).whenComplete((t, u) -> {
            System.out.println(t + ":" + u + "," + Thread.currentThread());
        });
        System.out.println("supply future:" + future.get());
    }

    public static void then() throws ExecutionException, InterruptedException {
        // 不带返回值
        CompletableFuture future = CompletableFuture.supplyAsync(() -> {
            System.out.println("then");
            return "supply";
        }).thenCompose(s -> CompletableFuture.supplyAsync(() -> {
            System.out.println(s + " run then compose");
            return 123;
        }));
        System.out.println("then future:" + future.get());
    }
}
