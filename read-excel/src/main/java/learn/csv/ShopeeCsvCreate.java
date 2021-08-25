package learn.csv;

import org.apache.poi.util.IOUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author: wenhongliang
 */
public class ShopeeCsvCreate {
    public static void main(String[] args) {
        String[] addHeadArr = new String[]{"Shopee UID", "Shopee Phone", "Shopee Name"};
        String[] deleteHeadArr = new String[]{"Staff ID"};
        //数据
        List<String> dataList = new ArrayList<>();
        for (int i = 0; i < 20000; i++) {
            DecimalFormat df = new DecimalFormat("0000000");
            dataList.add("91" + df.format(i) + ",81" + df.format(i) + ",a" + df.format(i));
        }

        String addFileName = "testShopee-20k.csv";//文件名称
        String deleteFileName = "deleteShopee-20k.csv";//文件名称
        String filePath = "/Users/wenhongliang/Desktop/csv/"; //文件路径

        File addFile, deleteFile;
        BufferedWriter addCsvWriter = null, deleteCsvWriter = null;
        try {
            addFile = new File(filePath + addFileName);
            deleteFile = new File(filePath + deleteFileName);
            File parent = addFile.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }
            addFile.createNewFile();
            deleteFile.createNewFile();

            // GB2312使正确读取分隔符","
            addCsvWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(addFile), "GB2312"), 1024);
            deleteCsvWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(deleteFile), "GB2312"), 1024);

            // 写入文件头部
            addCsvWriter.write(String.join(",", addHeadArr));
            addCsvWriter.newLine();

            deleteCsvWriter.write(String.join(",", deleteHeadArr));
            deleteCsvWriter.newLine();

            // 写入文件内容
            for (String strings : dataList) {
                addCsvWriter.write(strings);
                addCsvWriter.newLine();

                deleteCsvWriter.write(strings.split(",")[0]);
                deleteCsvWriter.newLine();
            }
            addCsvWriter.flush();
            deleteCsvWriter.flush();
        } catch (
                Exception e) {
            e.printStackTrace();
        } finally {
            IOUtils.closeQuietly(addCsvWriter);
            IOUtils.closeQuietly(deleteCsvWriter);
        }

    }
}
