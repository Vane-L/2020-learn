package learn.csv;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.poi.util.IOUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author: wenhongliang
 */
public class EmailCsvCreate {
    public static void main(String[] args) {
        String[] addHeadArr = new String[]{"UID", "Value1"};
        //数据
        List<List<String>> dataList = new ArrayList<>();
        for (int i = 0; i < 20000; i++) {
            List<String> tmp = new ArrayList<>();
            tmp.add(RandomStringUtils.randomAlphanumeric(6) + "@123.com");
            tmp.add("v1-" + RandomStringUtils.randomAlphanumeric(3));
            dataList.add(tmp);
        }

        String addFileName = "email20k.csv";//文件名称
        String filePath = "/Users/wenhongliang/Desktop/csv/"; //文件路径

        File addFile;
        BufferedWriter addCsvWriter = null;
        try {
            addFile = new File(filePath + addFileName);
            File parent = addFile.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }
            addFile.createNewFile();

            // GB2312使正确读取分隔符","
            addCsvWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(addFile), "GB2312"), 1024);

            // 写入文件头部
            addCsvWriter.write(String.join(",", addHeadArr));
            addCsvWriter.newLine();

            // 写入文件内容
            for (List<String> tmp : dataList) {
                addCsvWriter.write(String.join(",", tmp.toArray(new String[tmp.size()])));
                addCsvWriter.newLine();
            }
            addCsvWriter.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            IOUtils.closeQuietly(addCsvWriter);
        }

    }
}
