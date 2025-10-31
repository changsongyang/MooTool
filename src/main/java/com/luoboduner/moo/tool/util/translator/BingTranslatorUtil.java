package com.luoboduner.moo.tool.util.translator;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.net.ssl.SSLHandshakeException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Bing翻译工具类 (使用Bing翻译API，在中国大陆可访问)
 */
@Slf4j
public class BingTranslatorUtil implements Translator {

    /**
     * Bing Translator API endpoint identifier
     * This value is used by the Bing Translator public API
     * It may need to be updated if Bing changes their API
     */
    private static final String BING_TRANSLATOR_IID = "translator.5028.1";

    /**
     * @param word
     * @param sourceLanguage 源语言 默认auto 英文为 en
     * @param targetLanguage 目标语言 默认zh-CN
     * @return
     */
    public String translate(String word, String sourceLanguage, String targetLanguage) {
        try {
            if (StringUtils.isEmpty(word)) {
                return "";
            }
            
            if (StringUtils.isEmpty(sourceLanguage)) {
                sourceLanguage = "auto-detect";
            }
            if (StringUtils.isEmpty(targetLanguage)) {
                targetLanguage = "zh-Hans";
            }
            
            // Convert language codes to Bing format
            sourceLanguage = convertToBingLanguageCode(sourceLanguage);
            targetLanguage = convertToBingLanguageCode(targetLanguage);
            
            /**
             * Build Bing Translator API URL
             * IG parameter: Uses current timestamp, may need adjustment based on actual API requirements
             * IID parameter: Fixed identifier for translator endpoint
             * Note: This uses Bing's public translator endpoint which may change over time
             */
            String url = "https://www.bing.com/ttranslatev3?isVertical=1" +
                    "&IG=" + System.currentTimeMillis() +
                    "&IID=" + BING_TRANSLATOR_IID;

            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            con.setRequestMethod("POST");
            con.setConnectTimeout(10000);
            con.setReadTimeout(10000);
            con.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
            con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            con.setRequestProperty("Referer", "https://www.bing.com/translator");
            con.setDoOutput(true);

            String postData = "fromLang=" + sourceLanguage +
                    "&to=" + targetLanguage +
                    "&text=" + URLEncoder.encode(word, StandardCharsets.UTF_8);

            // Write request body with proper resource management
            try (java.io.OutputStream os = con.getOutputStream()) {
                os.write(postData.getBytes(StandardCharsets.UTF_8));
            }

            // Read response with proper resource management
            StringBuilder response = new StringBuilder();
            try (BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8))) {
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
            }
            
            return parseResult(response.toString());
        } catch (SSLHandshakeException e) {
            log.error("SSLHandshakeException", e);
            return "访问Bing翻译接口网络异常：" + e.getMessage();
        } catch (SocketTimeoutException e) {
            log.error("SocketTimeoutException", e);
            return "访问Bing翻译接口超时：" + e.getMessage();
        } catch (Exception e) {
            log.error("访问Bing翻译异常", e);
            return "访问Bing翻译接口异常：" + e.getMessage();
        }
    }

    private String convertToBingLanguageCode(String code) {
        if (StringUtils.isEmpty(code) || "auto".equals(code)) {
            return "auto-detect";
        }
        // Convert common codes to Bing format
        switch (code) {
            case "zh-CN":
                return "zh-Hans";
            case "cht":
                return "zh-Hant";
            case "en":
                return "en";
            case "jp":
                return "ja";
            case "kor":
                return "ko";
            case "fra":
                return "fr";
            case "spa":
                return "es";
            case "th":
                return "th";
            case "ara":
                return "ar";
            case "ru":
                return "ru";
            case "pt":
                return "pt";
            case "de":
                return "de";
            case "it":
                return "it";
            case "el":
                return "el";
            case "nl":
                return "nl";
            case "pl":
                return "pl";
            case "cs":
                return "cs";
            case "swe":
                return "sv";
            case "hu":
                return "hu";
            case "vie":
                return "vi";
            default:
                return code;
        }
    }

    private String parseResult(String inputJson) {
        try {
            if (StringUtils.isEmpty(inputJson)) {
                return "翻译返回结果为空";
            }
            
            JSONArray jsonArray = new JSONArray(inputJson);
            if (jsonArray.size() > 0) {
                JSONObject firstItem = jsonArray.getJSONObject(0);
                if (firstItem.containsKey("translations")) {
                    JSONArray translations = firstItem.getJSONArray("translations");
                    if (translations.size() > 0) {
                        JSONObject translation = translations.getJSONObject(0);
                        String result = translation.getStr("text");
                        if (!StringUtils.isEmpty(result)) {
                            return result;
                        }
                    }
                }
            }
            return "解析翻译结果失败，返回格式不符合预期";
        } catch (Exception e) {
            log.error("解析翻译结果异常", e);
            return "解析翻译结果异常：" + e.getMessage();
        }
    }
}
