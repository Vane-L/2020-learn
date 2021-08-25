package com.elastic.demo;

import com.google.gson.Gson;
import org.apache.http.HttpHost;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author: wenhongliang
 */
public class Demo {
    public final static String HOST = "localhost";
    // elastic search默认tcp端口9300，http端口9200
    public final static int PORT = 9200;

    public static void main(String[] args) throws IOException {
        RestHighLevelClient client = new RestHighLevelClient(RestClient.builder(new HttpHost(HOST, PORT, "http")));

        User user = new User(1, "abc", "1233324");

        String jsonString = new Gson().toJson(user);
        System.out.println("--->>> user: " + jsonString);

        List<User> list = new ArrayList<>();
        list.add(new User(1, "root", "123455"));
        list.add(new User(2, "admin", "123456"));
        list.add(new User(3, "test-3", "123455"));
        list.add(new User(4, "test-4", "abd"));
        list.add(new User(5, "test-5", "123455"));

        // single
        IndexRequest indexRequest = new IndexRequest("posts").id("1").source(jsonString, XContentType.JSON);
        // bulk
        BulkRequest bulkRequest = new BulkRequest();
        for (User u : list) {
            jsonString = new Gson().toJson(u);
            bulkRequest.add(new IndexRequest("posts").id(String.valueOf(u.getId())).source(jsonString, XContentType.JSON));
        }

        // execute single
        IndexResponse indexResponse = client.index(indexRequest, RequestOptions.DEFAULT);
        System.out.println("--->>> indexResponse: " + indexResponse.getIndex() + ":" + indexResponse.getId());
        GetRequest getRequest = new GetRequest("posts", "1");
        GetResponse getResponse = client.get(getRequest, RequestOptions.DEFAULT);
        if (getResponse.isExists()) {
            System.out.println("--->>> getResponse: " + getResponse.getIndex() + ":" + getResponse.getId());
        }
        // execute bulk
        DocWriteResponse.Result result = indexResponse.getResult();
        System.out.println("--->>> result: " + result);
        BulkResponse bulkResponse = client.bulk(bulkRequest, RequestOptions.DEFAULT);
        for (BulkItemResponse bulkItemResponse : bulkResponse) {
            DocWriteResponse writeResponse = bulkItemResponse.getResponse();
            System.out.println("--->>> writeResponse: " + writeResponse);
        }

        client.close();
    }
}
