package com.liang.function;

import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

/**
 * @Author: wenhongliang
 */
public class Test {
    public static void main(String[] args) {
        System.out.println("----Function------");
        testFunction();
        System.out.println("----Consumer------");
        testConsumer();
        System.out.println("----Supplier------");
        testSupplier();
        System.out.println("----Predicate------");
        testPredicate();
        System.out.println("----BinaryOperator------");
        testBinaryOperator();
        System.out.println("----UnaryOperator------");
        testUnaryOperator();
    }


    public static void testUnaryOperator() {
        UnaryOperator<Integer> binaryOperator = (x) -> x * 10;
        System.out.println(binaryOperator.apply(3));
    }

    public static void testBinaryOperator() {
        BinaryOperator<Integer> binaryOperator = (x, y) -> x * y;
        System.out.println(binaryOperator.apply(3, 200));
    }

    public static void testPredicate() {
        Predicate<Integer> predicate = x -> x % 2 == 0;
        System.out.println(predicate.test(1));
        System.out.println(predicate.test(2));
    }

    public static void testSupplier() {
        Supplier<String> supplier = () -> "Hello World!";
        System.out.println(supplier.get());
    }

    public static void testConsumer() {
        Consumer<String> consumer = s -> System.out.println("consumer: " + s);
        Consumer<String> consumer1 = consumer.andThen(s -> System.out.println("after consumer: " + s));
        consumer1.accept("hello world");
    }

    public static void testFunction() {
        Function<Integer, Integer> function = x -> {
            int res = x + 100;
            System.out.println("function: " + res);
            return res;
        };
        Function<Integer, Integer> function1 = function.compose(y -> {
            int res = y + 200;
            System.out.println("function1: " + res);
            return res;
        });
        Function<Integer, Integer> function2 = function1.andThen(z -> {
            int res = z + 500;
            System.out.println("function2: " + res);
            return res;
        });
        System.out.println("result: " + function2.apply(10));
    }
}
