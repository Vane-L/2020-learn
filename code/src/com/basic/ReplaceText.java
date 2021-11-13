package com.basic;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Scanner;

/**
 * @Author: wenhongliang
 */
public class ReplaceText {
    public static void main(String[] args) throws FileNotFoundException {
        File src = new File("/src/file");
        if (!src.exists()) {
            System.out.println("Source file not exist");
            System.exit(2);
        }

        File dst = new File("/src/file");
        if (dst.exists()) {
            System.out.println("Target file not exist");
            System.exit(3);
        }

        try (
                Scanner input = new Scanner(src);
                PrintWriter output = new PrintWriter(dst);
        ) {
            while (input.hasNext()) {
                String s1 = input.nextLine();
                String s2 = s1.replaceAll("a", "b");
                output.write(s2);
            }
        }
    }
}
