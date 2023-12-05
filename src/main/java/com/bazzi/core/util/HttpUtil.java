package com.bazzi.core.util;

import com.bazzi.core.ex.BusinessException;
import com.bazzi.core.generic.Result;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.*;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * HTTP请求，支持GET、POST，以及把请求参数放在requestBody里
 */
@Slf4j
public final class HttpUtil {
    private static final int GET = 0;
    private static final int POST = 1;
    private static final int PUT = 2;
    private static final int DELETE = 3;

    private static final RequestConfig DEFAULT_REQUEST_CONFIG = RequestConfig.custom()
            .setConnectTimeout(15000)//连接超时时间，毫秒
            .setConnectionRequestTimeout(5000)//从connect Manager(连接池)获取Connection 超时时间，毫秒
            .setSocketTimeout(60000)//请求获取数据的超时时间(即响应时间)，毫秒
            .build();

    /**
     * 发送GET请求
     *
     * @param url 请求地址
     * @return 请求结果
     */
    public static Result<String> sendGet(String url) {
        return baseSend(url, null, GET);
    }

    /**
     * 会把普通Bean对象转换成Map<String,String>，再拼接到url上，再发送GET请求
     *
     * @param url   请求地址
     * @param param 普通Bean对象
     * @return 请求结果
     */
    public static <T> Result<String> sendGet(String url, T param) {
        return baseSend(url, param, GET);
    }

    /**
     * 会把普通Bean对象转换成Map<String,String>，再拼接到url上，设置header相关信息，再发送GET请求
     *
     * @param url    请求地址
     * @param param  普通Bean对象
     * @param header 请求头参数
     * @return 请求结果
     */
    public static <T> Result<String> sendGet(String url, T param, T header) {
        return baseSend(url, param, header, GET);
    }

    /**
     * 参数为普通Bean对象，发送POST请求
     *
     * @param url   请求地址
     * @param param 普通Bean对象
     * @return 请求结果
     */
    public static <T> Result<String> sendPost(String url, T param) {
        return baseSend(url, param, POST);
    }

    /**
     * 参数为普通Bean对象，设置header参数，再发送POST请求
     *
     * @param url    请求地址
     * @param param  普通Bean对象
     * @param header 请求头参数
     * @return 请求结果
     */
    public static <T> Result<String> sendPost(String url, T param, T header) {
        return baseSend(url, param, header, POST);
    }

    /**
     * 参数为普通Bean对象，发送PUT请求
     *
     * @param url   请求地址
     * @param param 普通Bean对象
     * @return 请求结果
     */
    public static <T> Result<String> sendPut(String url, T param) {
        return baseSend(url, param, PUT);
    }

    /**
     * 参数为普通Bean对象，设置header参数，再发送PUT请求
     *
     * @param url    请求地址
     * @param param  普通Bean对象
     * @param header 请求头参数
     * @return 请求结果
     */
    public static <T> Result<String> sendPut(String url, T param, T header) {
        return baseSend(url, param, header, PUT);
    }

    /**
     * 以delete方式发送请求，url中占位符，会从T中获取属性对应值来替换
     *
     * @param url   请求地址
     * @param param 请求参数
     * @param <T>   泛型类型
     * @return 请求结果
     */
    public static <T> Result<String> sendDelete(String url, T param) {
        return baseSend(url, param, DELETE);
    }

    /**
     * 以delete方式发送请求，url中占位符，会从T中获取属性对应值来替换，设置header参数
     *
     * @param url    请求地址
     * @param param  请求参数
     * @param header 请求头信息
     * @param <T>    泛型类型
     * @return 请求结果
     */
    public static <T> Result<String> sendDelete(String url, T param, T header) {
        return baseSend(url, param, header, DELETE);
    }

    /**
     * 将请求参数放在RequestBody中，发送POST请求
     *
     * @param url   请求地址
     * @param param 请求参数，会用Jackson转成JSON字符串
     * @return 请求结果
     */
    public static <T> Result<String> sendJsonInBody(String url, T param) {
        Map<String, String> headerMap = new HashMap<>();
        headerMap.put(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString());
        return sendPostInBody(url, JsonUtil.toJsonString(param), headerMap);
    }

    /**
     * 将请求参数放在RequestBody中，设置header信息，发送POST请求
     *
     * @param url    请求地址
     * @param param  请求参数，会用Jackson转成JSON字符串
     * @param header 请求头信息
     * @return 请求结果
     */
    public static <T> Result<String> sendJsonInBody(String url, T param, T header) {
        String context = JsonUtil.toJsonString(header);
        Map<String, String> headerMap = JsonUtil.parseMap(context, String.class, String.class);
        headerMap.put(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString());
        return sendPostInBody(url, JsonUtil.toJsonString(param), headerMap);
    }

    /**
     * 发送POST请求，并且请求参数放在请求body里
     * <ul>
     * 注：需要根据param类型设置对应的header <br>
     * <li>如果是json，设置Content-Type: application/json;charset=UTF-8</li>
     * <li>如果是xml， 设置Content-Type: application/xml;charset=UTF-8 </li>
     * </ul>
     *
     * @param url       请求地址
     * @param param     请求参数，支持JSON或者XML格式的字符串
     * @param headerMap 请求头参数
     * @return 请求结果
     */
    public static Result<String> sendPostInBody(String url, String param, Map<String, String> headerMap) {
        if (url == null || url.isEmpty())
            throw new IllegalArgumentException("Property 'url' is required");
        if (param == null || param.isEmpty())
            throw new IllegalArgumentException("Property 'param' is required");
        CloseableHttpClient httpClient = HttpClients.createDefault();
        CloseableHttpResponse response = null;
        try {
            HttpPost postReq = new HttpPost(url);
            postReq.setConfig(DEFAULT_REQUEST_CONFIG);
            headerMap = headerMap == null ? new HashMap<>() : headerMap;
            for (Map.Entry<String, String> entry : headerMap.entrySet()) {
                postReq.addHeader(entry.getKey(), entry.getValue());
            }
            postReq.setEntity(new ByteArrayEntity(param.getBytes(StandardCharsets.UTF_8)));
            response = httpClient.execute(postReq);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != HttpStatus.SC_OK) {
                log.info("StatusCode:{},ReasonPhrase:{}", statusCode, response.getStatusLine().getReasonPhrase());
                return Result.failure("-1", String.format("异常的响应状态: %s", statusCode));
            }
            HttpEntity httpEntity = response.getEntity();
            String data = httpEntity == null ? null : EntityUtils.toString(httpEntity, StandardCharsets.UTF_8);
            return Result.success(data);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new BusinessException("-1", e.getMessage());
        } finally {
            if (response != null)
                try {
                    response.close();
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            try {
                httpClient.close();
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    /**
     * 根据type来发送相应的请求，支持GET、POST、PUT、DELETE方式
     *
     * @param url   请求地址
     * @param param 请求参数
     * @param type  请求类型，0:GET,1:POST,2:PUT,3:DELETE
     * @return 请求结果
     */
    private static <T> Result<String> baseSend(String url, T param, int type) {
        return baseSend(url, convertToMap(param), null, type);
    }

    /**
     * @param url    请求地址
     * @param param  请求参数
     * @param header 请求头参数
     * @param type   请求类型，0:GET,1:POST,2:PUT,3:DELETE
     * @return 请求结果
     */
    private static <T> Result<String> baseSend(String url, T param, T header, int type) {
        String context = JsonUtil.toJsonString(header);
        Map<String, String> headerMap = JsonUtil.parseMap(context, String.class, String.class);
        return baseSend(url, convertToMap(param), headerMap, type);
    }

    /**
     * 根据type来发送相应的请求，支持GET、POST、PUT、DELETE方式
     *
     * @param url       请求地址
     * @param paramMap  请求参数
     * @param headerMap 请求头参数
     * @param type      请求类型，0:GET,1:POST,2:PUT,3:DELETE
     * @return 请求结果
     */
    private static Result<String> baseSend(String url, Map<String, String> paramMap, Map<String, String> headerMap, int type) {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        CloseableHttpResponse response = null;
        try {
            HttpRequestBase requestBase = buildHttpRequestBase(url, paramMap, type);
            requestBase.setConfig(DEFAULT_REQUEST_CONFIG);

            addHeaderForRequest(requestBase, headerMap);

            response = httpClient.execute(requestBase);

            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != HttpStatus.SC_OK) {
                log.info("StatusCode:{},ReasonPhrase:{}", statusCode, response.getStatusLine().getReasonPhrase());
                return Result.failure("-1", String.format("异常的响应状态: %s", statusCode));
            }
            HttpEntity httpEntity = response.getEntity();
            String data = httpEntity == null ? null : EntityUtils.toString(httpEntity, StandardCharsets.UTF_8);
            return Result.success(data);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new BusinessException("-1", e.getMessage());
        } finally {
            if (response != null)
                try {
                    response.close();
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            if (httpClient != null)
                try {
                    httpClient.close();
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
        }
    }

    /**
     * 根据类型获取请求
     *
     * @param url      请求地址
     * @param paramMap 请求参数
     * @param type     请求类型，0:GET,1:POST,2:PUT,3:DELETE
     * @return 请求
     * @throws URISyntaxException 异常
     */
    private static HttpRequestBase buildHttpRequestBase(String url,
                                                        Map<String, String> paramMap,
                                                        int type) throws URISyntaxException {
        HttpRequestBase requestBase;
        switch (type) {
            case GET:
                URIBuilder builder = new URIBuilder(url);
                if (paramMap != null) {
                    for (Map.Entry<String, String> entry : paramMap.entrySet()) {
                        builder.addParameter(entry.getKey(), entry.getValue());
                    }
                }
                requestBase = new HttpGet(builder.build());
                break;
            case POST:
                HttpPost postReq = new HttpPost(url);
                List<NameValuePair> paramForPost = new ArrayList<>();
                for (Map.Entry<String, String> entry : paramMap.entrySet()) {
                    paramForPost.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
                }
                postReq.setEntity(new UrlEncodedFormEntity(paramForPost, StandardCharsets.UTF_8));
                requestBase = postReq;
                break;
            case PUT:
                HttpPut putReq = new HttpPut(url);
                List<NameValuePair> paramForPut = new ArrayList<>();
                for (Map.Entry<String, String> entry : paramMap.entrySet()) {
                    paramForPut.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
                }
                putReq.setEntity(new UrlEncodedFormEntity(paramForPut, StandardCharsets.UTF_8));
                requestBase = putReq;
                break;
            case DELETE:
                for (Map.Entry<String, String> entry : paramMap.entrySet()) {
                    url = url.replace("{" + entry.getKey() + "}", entry.getValue());
                }
                requestBase = new HttpDelete(url);
                break;
            default:
                throw new BusinessException("-1", "不支持的请求方式！");
        }
        return requestBase;
    }

    /**
     * 给请求设置头信息
     *
     * @param requestBase 请求
     * @param headerMap   头信息
     */
    private static void addHeaderForRequest(HttpRequestBase requestBase, Map<String, String> headerMap) {
        headerMap = headerMap == null ? new HashMap<>() : headerMap;
        for (Map.Entry<String, String> entry : headerMap.entrySet()) {
            requestBase.addHeader(entry.getKey(), entry.getValue());
        }
    }

    /**
     * 把普通Bean对象转换成Map<String, String>
     *
     * @param t 普通对象
     * @return map结果
     */
    private static <T> Map<String, String> convertToMap(T t) {
        Map<String, String> map = new HashMap<>();
        if (t == null)
            return map;
        return JsonUtil.parseMap(JsonUtil.toJsonString(t), String.class, String.class);
    }

}
