package learn.csv;

import org.apache.commons.lang.RandomStringUtils;
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
public class StaffCsvCreate {
    public static void main(String[] args) {
        String[] addHeadArr = new String[]{"Staff ID", "Full Name", "Office Email Address", "Whitelist Phone Number"};
        String[] deleteHeadArr = new String[]{"Staff ID"};
        //数据
        List<Staff> dataList = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            DecimalFormat df = new DecimalFormat("00000000");
            Staff staff = new Staff();
            staff.setId("a" + df.format(i));
            staff.setFullName("b" + df.format(i));
            staff.setEmail("c" + df.format(i) + "@123.com");
            staff.setPhone("8" + df.format(i));
            dataList.add(staff);
        }

        String addFileName = "testStaff.csv";//文件名称
        String deleteFileName = "deleteStaff.csv";//文件名称
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
            for (Staff staff : dataList) {
                addCsvWriter.write(staff.toRow());
                addCsvWriter.newLine();

                deleteCsvWriter.write(staff.getId());
                deleteCsvWriter.newLine();
            }
            addCsvWriter.flush();
            deleteCsvWriter.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            IOUtils.closeQuietly(addCsvWriter);
            IOUtils.closeQuietly(deleteCsvWriter);
        }

    }
}
