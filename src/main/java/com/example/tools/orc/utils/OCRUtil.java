package com.example.tools.orc.utils;


import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import okhttp3.*;

import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Objects;

public class OCRUtil {
    public static final String URL_BAIDU_OAUTH_TOKEN = "https://aip.baidubce.com/oauth/2.0/token";
    public static final String API_KEY = "xxxxxxxx";
    public static final String SECRET_KEY = "xxxxxxxx";
    public static final String TEMPLATE_SIGN = "xxxxxxxx";
    public static final String CLASSIFIER_ID = "xxxxxxxx";

    static final OkHttpClient HTTP_CLIENT = new OkHttpClient().newBuilder().build();

    public static void main(String []args) throws IOException{
        String path = "/home/admin/workspace/code/src/main/resources/image/shipping_sheet.jpg";
        String imageBase64 = getFileContentAsBase64(path).replaceAll("data:image/jpeg;base64","");
        JSONObject iOCR = iOCR(imageBase64);
        System.out.println("iOCR = " + iOCR);
    }

    /**
     * 图片文字识别-iOCR
     * @param imageBase64 图片地址
     * @return 识别结果
     * @throws IOException IO异常
     */
    public static JSONObject iOCR(String imageBase64) throws IOException {
        String encodeImageBase64 =URLEncoder.encode(imageBase64, StandardCharsets.UTF_8);
        String content = "image=" + encodeImageBase64 + "&templateSign=" + TEMPLATE_SIGN;
        MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
        RequestBody body = RequestBody.Companion.create(content,mediaType);
        Request request = new Request.Builder()
                .url("https://aip.baidubce.com/rest/2.0/solution/v1/iocr/recognise?access_token=" + getAccessToken())
                .method("POST", body)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .addHeader("Accept", "application/json")
                .build();
        Response response = HTTP_CLIENT.newCall(request).execute();
        return JSON.parseObject(Objects.requireNonNull(response.body()).string());
    }

    /**
     * 获取文件base64编码
     * @param path 文件路径
     * @return base64编码信息，不带文件头
     * @throws IOException IO异常
     */
    static String getFileContentAsBase64(String path) throws IOException {
        byte[] b = Files.readAllBytes(Paths.get(path));
        return Base64.getEncoder().encodeToString(b);
    }

    /**
     * 从用户的AK，SK生成鉴权签名（Access Token）
     *
     * @return 鉴权签名（Access Token）
     * @throws IOException IO异常
     */
    static String getAccessToken() throws IOException {
        MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
        String content = "grant_type=client_credentials&client_id=" + API_KEY + "&client_secret=" + SECRET_KEY;
        RequestBody body = RequestBody.Companion.create(content,mediaType);
        Request request = new Request.Builder()
                .url(URL_BAIDU_OAUTH_TOKEN)
                .method("POST", body)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .build();
        Response response = HTTP_CLIENT.newCall(request).execute();
        JSONObject jsonObject = JSON.parseObject(Objects.requireNonNull(response.body()).string());
        return jsonObject.getString("access_token");
    }

}
