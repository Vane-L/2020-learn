package learn.excel;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author: wenhongliang
 */
public class ReadExcelSG {
    public static void main(String[] args) {
        List<MessageTemplate> res = readExcel("/Users/wenhongliang/Documents/work/template/SG0.5.xlsx");
        res.forEach(data -> System.out.println(data.getTplId() + "\n" + JSONObject.toJSON(data)));
    }

    private static final String XLS = "xls";
    private static final String XLSX = "xlsx";

    /**
     * 根据文件后缀名类型获取对应的工作簿对象
     *
     * @param inputStream 读取文件的输入流
     * @param fileType    文件后缀名类型（xls或xlsx）
     * @return 包含文件数据的工作簿对象
     * @throws IOException
     */
    public static Workbook getWorkbook(InputStream inputStream, String fileType) throws IOException {
        Workbook workbook = null;
        if (fileType.equalsIgnoreCase(XLS)) {
            workbook = new HSSFWorkbook(inputStream);
        } else if (fileType.equalsIgnoreCase(XLSX)) {
            workbook = new XSSFWorkbook(inputStream);
        }
        return workbook;
    }

    /**
     * 读取Excel文件内容
     *
     * @param fileName 要读取的Excel文件所在路径
     * @return 读取结果列表，读取失败时返回null
     */
    public static List<MessageTemplate> readExcel(String fileName) {
        Workbook workbook = null;
        FileInputStream inputStream = null;
        try {
            // 获取Excel后缀名
            String fileType = fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length());
            // 获取Excel文件
            File excelFile = new File(fileName);
            if (!excelFile.exists()) {
                return null;
            }

            // 获取Excel工作簿
            inputStream = new FileInputStream(excelFile);
            workbook = getWorkbook(inputStream, fileType);

            // 读取excel中的数据
            List<MessageTemplate> resultDataList = parseExcel(workbook);
            return resultDataList;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    /**
     * 解析Excel数据
     *
     * @param workbook Excel工作簿对象
     * @return 解析结果
     */
    private static List<MessageTemplate> parseExcel(Workbook workbook) {
        List<MessageTemplate> resultDataList = new ArrayList<>();
        // 解析sheet
        Sheet sheet = workbook.getSheetAt(0);

        // 校验sheet是否合法
        if (sheet == null) {
            System.out.println("null sheet");
        }

        // 获取第一行数据
        int firstRowNum = sheet.getFirstRowNum();
        Row firstRow = sheet.getRow(firstRowNum);
        if (null == firstRow) {
            System.out.println("解析Excel失败，在第一行没有读取到任何数据！");
        }

        // 解析每一行的数据，构造数据对象
        for (int rowNum = 1; rowNum < 26; rowNum++) {
            Row row = sheet.getRow(rowNum);
            MessageTemplate resultData = convertRowToData2(row);
            resultDataList.add(resultData);
        }
        return resultDataList;
    }

    /**
     * 将单元格内容转换为字符串
     *
     * @param cell
     * @return
     */
    private static String convertCellValueToString(Cell cell) {
        if (cell == null) {
            return null;
        }
        String returnValue = null;
        switch (cell.getCellType()) {
            case NUMERIC:   //数字
                Double doubleValue = cell.getNumericCellValue();
                // 格式化科学计数法，取一位整数
                DecimalFormat df = new DecimalFormat("0");
                returnValue = df.format(doubleValue);
                break;
            case STRING:    //字符串
                returnValue = cell.getStringCellValue();
                break;
            case BOOLEAN:   //布尔
                Boolean booleanValue = cell.getBooleanCellValue();
                returnValue = booleanValue.toString();
                break;
            case BLANK:     // 空值
                break;
            case FORMULA:   // 公式
                returnValue = cell.getCellFormula();
                break;
            case ERROR:     // 故障
                break;
            default:
                break;
        }
        return returnValue;
    }

    private static MessageTemplate convertRowToData2(Row row) {
        MessageTemplate messageTemplate = new MessageTemplate();
        messageTemplate.setRecipientType(1);
        messageTemplate.setTplType(1);
        messageTemplate.setTplName(convertCellValueToString(row.getCell(3)));
        messageTemplate.setTplId(Integer.parseInt(convertCellValueToString(row.getCell(5))));
        messageTemplate.setTplTrigger(convertCellValueToString(row.getCell(8)));
        if (StringUtils.equalsIgnoreCase(convertCellValueToString(row.getCell(13)), "Y")) { // sms
            messageTemplate.setTplChannel("1");
            messageTemplate.setSmsContent(convertCellValueToString(row.getCell(18)));
        }
        if (StringUtils.equalsIgnoreCase(convertCellValueToString(row.getCell(14)), "Y")) { // email
            messageTemplate.setTplChannel(messageTemplate.getTplChannel() + ",2");
            messageTemplate.setEmailSubject(convertCellValueToString(row.getCell(21)));
            messageTemplate.setEmailContent(convertCellValueToString(row.getCell(24)).replaceAll("\n\n", "<br>"));
        }
        if (StringUtils.equalsIgnoreCase(convertCellValueToString(row.getCell(15)), "Y")) { // pn
            messageTemplate.setTplChannel(messageTemplate.getTplChannel() + ",3");
            messageTemplate.setPnTitle(convertCellValueToString(row.getCell(29)));
            messageTemplate.setPnContent(convertCellValueToString(row.getCell(32)));
            /*messageTemplate.setPnTitle(convertCellValueToString(row.getCell(28)));
            messageTemplate.setPnContent(convertCellValueToString(row.getCell(31)));*/
        }
        if (StringUtils.equalsIgnoreCase(convertCellValueToString(row.getCell(16)), "Y")) { // ar
            messageTemplate.setTplChannel(messageTemplate.getTplChannel() + ",4");
            messageTemplate.setArTitle(convertCellValueToString(row.getCell(36)));
            messageTemplate.setArContent(convertCellValueToString(row.getCell(38)));
            /*messageTemplate.setArTitle(convertCellValueToString(row.getCell(35)));
            messageTemplate.setArContent(convertCellValueToString(row.getCell(37)));*/
            String arType = StringUtils.trimToEmpty(convertCellValueToString(row.getCell(39)));
            if (arType.equals("Profile & Security")) {
                messageTemplate.setArFolder("2");
            } else if (arType.equals("Deposit")) {
                messageTemplate.setArFolder("3");
            } else if (arType.equals("Transfer")) {
                messageTemplate.setArFolder("4");
            }
        }
        return messageTemplate;
    }

}
