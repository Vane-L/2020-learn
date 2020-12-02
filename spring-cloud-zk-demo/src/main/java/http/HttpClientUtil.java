package http;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;

/**
 * @Author: wenhongliang
 */
@Slf4j
@Component
public class HttpClientUtil {

    private static final CloseableHttpClient HTTP_CLIENT;

    static {
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setMaxTotal(300); // 连接池最大并发连接数
        connectionManager.setDefaultMaxPerRoute(100); // 单路由的最大并发连接数
        RequestConfig config = RequestConfig.custom()
                .setConnectTimeout(5000) //建立连接的timeout时间
                .setConnectionRequestTimeout(5000) // 从连接池中后去连接的timeout时间
                .setSocketTimeout(8000) // 数据传输处理时间
                .build();
        HTTP_CLIENT = HttpClients.custom().setDefaultRequestConfig(config).setConnectionManager(connectionManager).build();
    }

    public String doGet(String url, Map<String, String> parameters) {
        CloseableHttpResponse response = null;
        try {
            URIBuilder builder = new URIBuilder(url);
            for (Map.Entry<String, String> map : parameters.entrySet()) {
                builder.setParameter(map.getKey(), map.getValue());
            }
            HttpGet httpGet = new HttpGet(builder.build());
            response = HTTP_CLIENT.execute(httpGet);

            int statusCode = response.getStatusLine().getStatusCode();
            String result = EntityUtils.toString(response.getEntity());
            if (statusCode == HttpStatus.SC_OK) {
                return result;
            } else {
                log.error("url: {}, code:{}, result:{}", url, statusCode, result);
                return null;
            }
        } catch (URISyntaxException | IOException e) {
            log.error("error msg in HttpClientUtil: {}", e.getMessage(), e);
        } finally {
            try {
                if (null != response) {
                    response.close();
                }
            } catch (IOException e) {
                log.error("error closing response: {}", e.getMessage(), e);
            }
        }
        return null;
    }

    public static String post(String url, String params) {
        CloseableHttpResponse response = null;
        try {
            HttpPost httpPost = new HttpPost(url);
            StringEntity paramEntity = new StringEntity(params);
            httpPost.setHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString());
            httpPost.setEntity(paramEntity);
            response = HTTP_CLIENT.execute(httpPost);
            int statusCode = response.getStatusLine().getStatusCode();
            String result = EntityUtils.toString(response.getEntity());
            if (statusCode == HttpStatus.SC_OK) {
                return result;
            } else {
                log.error("url: {}, code:{}, result:{}", url, statusCode, result);
                return null;
            }
        } catch (IOException e) {
            log.error("收集服务配置http请求异常", e);
        } finally {
            try {
                if (response != null) {
                    response.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
