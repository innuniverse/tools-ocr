package com.example.tools.orc.api;


import com.alibaba.fastjson2.JSONObject;
import com.example.tools.orc.utils.OCRUtil;
import com.example.tools.orc.utils.ZipUtil;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class OcrApi {

    private static final Logger log = LoggerFactory.getLogger(OcrApi.class);

    @GetMapping("/ocr")
    public Map<String, JSONObject> ocr(@RequestParam("file")MultipartFile file) throws Exception {
        Map<String, JSONObject> result = new HashMap<>();

//        List<String> fileNames = ZipUtil.parseZipFile(file);
//        for (String fileName : fileNames) {
//            String imageBase64 = OCRUtil.getFileContentAsBase64(fileName).replaceAll("data:image/jpeg;base64","");
//            JSONObject iOCR = OCRUtil.iOCR(imageBase64);
//            result.put(fileName, iOCR);
//            System.out.println("file:"+fileName+" = " + iOCR);
//        }

        List<MultipartFile> multipartFiles = ZipUtil.unZip(file);
        for (MultipartFile multipartFile : multipartFiles) {
            String imageBase64 = OCRUtil.getFileContentAsBase64(multipartFile).replaceAll("data:image/jpeg;base64","");
            JSONObject iOCR = OCRUtil.iOCR(imageBase64);
            result.put(multipartFile.getOriginalFilename(), iOCR);
            System.out.println("file:"+multipartFile.getOriginalFilename()+" = " + iOCR);
        }

        return result;
    }


    @GetMapping(value = "/excel")
    public String readExcel() throws Exception {
        InputStream inputStream = this.getClass().getResourceAsStream("/data.xlsx");
        List<List<String>> datas = readXlsx(inputStream);
        StringBuilder sb = new StringBuilder();
        sb.append("<pre>");
        for (List<String> row : datas) {
            for (String cell : row) {
                sb.append(cell).append("\t");
            }
            sb.append("\n");
        }
        sb.append("</pre>");
        return sb.toString();
    }

    public List<List<String>> readXlsx(InputStream inputStream) throws Exception {
        List<List<String>> result = new ArrayList<List<String>>();
        XSSFWorkbook workbook = null;
        try {
//            TODO 请参照以下注释，使用代码智能补全编写代码
//            workbook = new XSSFWorkbook(inputStream);
//            XSSFSheet sheet = workbook.getSheetAt(0);
//            int rows = sheet.getLastRowNum();
//            for (int i = 0; i <= rows; i++) {
//                XSSFRow row = sheet.getRow(i);
//                // TODO 请右键点击XSSFRow，选择"查询代码示例"，根据搜索出的代码示例，遍历每行的单元格，取出字符串数据填充到result变量中
//            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

}
