package com.basic;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * @Author: wenhongliang
 */
public class WebCrawler {
    public static void main(String[] args) {
        crawler("http://www.cs.armstrong.edu");
    }

    public static void crawler(String startingURL) {
        ArrayList<String> pendingURLs = new ArrayList<>();
        ArrayList<String> traversedURLs = new ArrayList<>();
        pendingURLs.add(startingURL);
        while (!pendingURLs.isEmpty() && traversedURLs.size() <= 100) {
            String urlStr = pendingURLs.remove(0);
            if (!traversedURLs.contains(urlStr)) {
                traversedURLs.add(urlStr);
                System.out.println("Crawl! " + urlStr);
                for (String s : getSubURLs(urlStr)) {
                    if (!traversedURLs.contains(s)) {
                        pendingURLs.add(s);
                    }
                }
            }
        }
    }

    public static ArrayList<String> getSubURLs(String urlStr) {
        ArrayList<String> list = new ArrayList<>();
        try {
            URL url = new URL(urlStr);
            Scanner input = new Scanner(url.openStream());
            int cur = 0;
            while (input.hasNext()) {
                String line = input.nextLine();
                cur = line.indexOf("http:", cur);
                while (cur > 0) {
                    int endIdx = line.indexOf("\"", cur);
                    if (endIdx > 0) {
                        list.add(line.substring(cur, endIdx));
                        cur = line.indexOf("http:", endIdx);
                    } else {
                        cur = -1;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }
}
