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
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @Author: wenhongliang
 */
public class GenEnglishTemplate {
    public static void main(String[] args) {
        List<MessageTemplate> res = readExcel("/Users/wenhongliang/Downloads/bke/BKE9.xlsx");
        res.forEach(data -> System.out.println(JSONObject.toJSONString(data)));
    }

    public static void execShell(String shell) {
        try {
            Process process = Runtime.getRuntime().exec(new String[]{"/bin/sh", "-c", shell}, null, null);
            InputStreamReader ir = new InputStreamReader(process.getInputStream());
            LineNumberReader input = new LineNumberReader(ir);
            String line;
            process.waitFor();
            while ((line = input.readLine()) != null) {
                System.out.println(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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
        } catch (Exception e) {
            return null;
        } finally {
            try {
                if (null != workbook) {
                    workbook.close();
                }
                if (null != inputStream) {
                    inputStream.close();
                }
            } catch (Exception e) {
                return null;
            }
        }
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
        for (int rowNum = 1; rowNum < 61; rowNum++) {
            if (rowNum == 5 || rowNum == 7 || rowNum == 27 || rowNum == 28 || rowNum == 35 || rowNum == 36) {
                continue;
            }
            Row row = sheet.getRow(rowNum);
            if (null == row) {
                System.out.println("row is null at " + rowNum);
                break;
            }
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
        MessageTemplate tplReqDTO = new MessageTemplate();
        tplReqDTO.setTplName(convertCellValueToString(row.getCell(1)));
        tplReqDTO.setTplId(Integer.parseInt(convertCellValueToString(row.getCell(2))));
        tplReqDTO.setTplTrigger(convertCellValueToString(row.getCell(6)));
        tplReqDTO.setTplType(1);
        List<String> channel = new ArrayList<>();
        if (StringUtils.isNotEmpty(convertCellValueToString(row.getCell(7)))) { // pn
            channel.add("3");
            tplReqDTO.setPnTitle(convertCellValueToString(row.getCell(22)));
            tplReqDTO.setPnContent(convertCellValueToString(row.getCell(24)).replace("\n", " "));
            if (StringUtils.isNotEmpty(convertCellValueToString(row.getCell(27))) && !convertCellValueToString(row.getCell(27)).equals("No redirection")) {
                tplReqDTO.setPnRedirect(convertCellValueToString(row.getCell(27)));
            }
        }
        if (StringUtils.isNotEmpty(convertCellValueToString(row.getCell(8)))) { // ar
            channel.add("4");
            tplReqDTO.setArTitle(convertCellValueToString(row.getCell(16)));
            tplReqDTO.setArContent(convertCellValueToString(row.getCell(18)).replace("\n", " "));
            String arType = convertCellValueToString(row.getCell(15));
            if (arType.equals("Profile & Security")) {
                tplReqDTO.setArFolder("2");
            } else if (arType.equals("Deposit")) {
                tplReqDTO.setArFolder("3");
            } else if (arType.equals("Transfer")) {
                tplReqDTO.setArFolder("4");
            }
            if (StringUtils.isNotEmpty(convertCellValueToString(row.getCell(21))) && !convertCellValueToString(row.getCell(21)).equals("N.A") && !convertCellValueToString(row.getCell(21)).equals("No redirection")) {
                tplReqDTO.setArRedirect(convertCellValueToString(row.getCell(21)));
            }
        }
        if (StringUtils.isNotEmpty(convertCellValueToString(row.getCell(9)))) { // email
            channel.add("2");
            String emailStr = convertCellValueToString(row.getCell(11));
            if (emailStr.contains("Subject:")) {
                tplReqDTO.setEmailSubject(emailStr.substring(emailStr.indexOf("Subject:") + 8, emailStr.indexOf("Dear")).replace("\n", " ").trim());
            }
            /*StringBuilder sb = new StringBuilder("<p style=\"color: #ee4d2d; font-size: 23px;\">");
            emailStr.replaceAll("\n\n", "<br>");
            int idx = emailStr.indexOf("Dear ${Customer Name},");
            sb.append(emailStr.substring(idx, idx + "Dear ${Customer Name},".length()));
            sb.append("</p><p style=\"font-size: 16px;\">");
            sb.append(emailStr.substring(emailStr.indexOf("Name},") + "Name},".length(), emailStr.length()).replaceAll("\n\n", "<br>"));
            tplReqDTO.setEmailContent(sb.append("</p>").toString());*/
            tplReqDTO.setEmailContent(emailStr.substring(emailStr.indexOf("Name},") + "Name},".length()));
        }
        if (StringUtils.isNotEmpty(convertCellValueToString(row.getCell(10)))) { // sms
            channel.add("1");
            tplReqDTO.setSmsContent(convertCellValueToString(row.getCell(13)).replace("\n", " "));
        }
        Collections.sort(channel);
        tplReqDTO.setTplChannel(String.join(",", channel));
        return tplReqDTO;
    }

}
