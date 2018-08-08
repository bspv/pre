package com.bazzi.core.util;

import com.bazzi.core.ex.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.lang.reflect.Field;
import java.net.URI;
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

	/**
	 * 发送GET请求
	 *
	 * @param url 请求地址
	 * @return 请求结果
	 */
	public static String sendGet(String url) {
		return sendGet(url, null, null);
	}

	/**
	 * 会把普通Bean对象转换成Map<String,String>，再拼接到url上，再发送GET请求
	 *
	 * @param url   请求地址
	 * @param param 普通Bean对象
	 * @return 请求结果
	 */
	public static <T> String sendGet(String url, T param) {
		return sendGet(url, convertToMap(param), null);
	}

	/**
	 * 会把普通Bean对象转换成Map<String,String>，再拼接到url上，设置header相关信息，再发送GET请求
	 *
	 * @param url    请求地址
	 * @param param  普通Bean对象
	 * @param header 请求头参数
	 * @return 请求结果
	 */
	public static <T> String sendGet(String url, T param, T header) {
		String context = JsonUtil.toJsonString(header);
		Map<String, String> headerMap = JsonUtil.parseMap(context, String.class, String.class);
		return sendGet(url, convertToMap(param), headerMap);
	}

	/**
	 * 会把请求参数paramMap拼接到url上，设置header相关信息，再发送GET请求
	 *
	 * @param url       请求地址
	 * @param paramMap  请求参数
	 * @param headerMap 请求头参数
	 * @return 请求结果
	 */
	public static String sendGet(String url, Map<String, String> paramMap, Map<String, String> headerMap) {
		if (url == null || "".equals(url))
			throw new IllegalArgumentException("Property 'url' is required");
		CloseableHttpClient httpClient = HttpClients.createDefault();
		CloseableHttpResponse response = null;
		try {
			URIBuilder builder = new URIBuilder(url);
			if (paramMap != null) {
				for (Map.Entry<String, String> entry : paramMap.entrySet()) {
					builder.addParameter(entry.getKey(), entry.getValue());
				}
			}
			URI uri = builder.build();
			HttpGet getReq = new HttpGet(uri);
			headerMap = headerMap == null ? new HashMap<>() : headerMap;
			for (Map.Entry<String, String> entry : headerMap.entrySet()) {
				getReq.addHeader(entry.getKey(), entry.getValue());
			}
			response = httpClient.execute(getReq);
			int statusCode = response.getStatusLine().getStatusCode();
			if (statusCode == HttpStatus.SC_OK) {
				HttpEntity httpEntity = response.getEntity();
				return httpEntity == null ? null : EntityUtils.toString(httpEntity, StandardCharsets.UTF_8);
			} else {
				log.info("通讯异常，错误码：HTTP CODE(" + statusCode + ")");
				return null;
			}
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
	 * 发送GET请求，结果为byte数组
	 *
	 * @param url 请求地址
	 * @return 请求结果
	 */
	public static byte[] sendGetByte(String url) {
		return sendGetByte(url, null, null);
	}

	/**
	 * 会把普通Bean对象转换成Map<String,String>，再拼接到url上，再发送GET请求，结果为byte数组
	 *
	 * @param url   请求地址
	 * @param param 普通Bean对象
	 * @return 请求结果
	 */
	public static <T> byte[] sendGetByte(String url, T param) {
		return sendGetByte(url, convertToMap(param), null);
	}

	/**
	 * 会把普通Bean对象转换成Map<String,String>，再拼接到url上，设置header相关信息，再发送GET请求，结果为byte数组
	 *
	 * @param url    请求地址
	 * @param param  普通Bean对象
	 * @param header 请求头参数
	 * @return 请求结果
	 */
	public static <T> byte[] sendGetByte(String url, T param, T header) {
		String context = JsonUtil.toJsonString(header);
		Map<String, String> headerMap = JsonUtil.parseMap(context, String.class, String.class);
		return sendGetByte(url, convertToMap(param), headerMap);
	}

	/**
	 * 会把请求参数paramMap拼接到url上，设置header相关信息，再发送GET请求，结果为byte数组
	 *
	 * @param url       请求地址
	 * @param paramMap  请求参数
	 * @param headerMap 请求头参数
	 * @return 请求结果
	 */
	public static byte[] sendGetByte(String url, Map<String, String> paramMap, Map<String, String> headerMap) {
		if (url == null || "".equals(url))
			throw new IllegalArgumentException("Property 'url' is required");
		CloseableHttpClient httpClient = HttpClients.createDefault();
		CloseableHttpResponse response = null;
		try {
			URIBuilder builder = new URIBuilder(url);
			if (paramMap != null) {
				for (Map.Entry<String, String> entry : paramMap.entrySet()) {
					builder.addParameter(entry.getKey(), entry.getValue());
				}
			}
			URI uri = builder.build();
			HttpGet getReq = new HttpGet(uri);
			headerMap = headerMap == null ? new HashMap<>() : headerMap;
			for (Map.Entry<String, String> entry : headerMap.entrySet()) {
				getReq.addHeader(entry.getKey(), entry.getValue());
			}
			response = httpClient.execute(getReq);
			int statusCode = response.getStatusLine().getStatusCode();
			if (statusCode == HttpStatus.SC_OK) {
				HttpEntity httpEntity = response.getEntity();
				return httpEntity == null ? null : EntityUtils.toByteArray(httpEntity);
			} else {
				log.info("通讯异常，错误码：HTTP CODE(" + statusCode + ")");
				return null;
			}
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
	 * 参数为普通Bean对象，发送POST请求
	 *
	 * @param url   请求地址
	 * @param param 普通Bean对象
	 * @return 请求结果
	 */
	public static <T> String sendPost(String url, T param) {
		return sendPost(url, convertToMap(param), null);
	}

	/**
	 * 参数为普通Bean对象，设置header参数，再发送POST请求
	 *
	 * @param url    请求地址
	 * @param param  普通Bean对象
	 * @param header 请求头参数
	 * @return 请求结果
	 */
	public static <T> String sendPost(String url, T param, T header) {
		String context = JsonUtil.toJsonString(header);
		Map<String, String> headerMap = JsonUtil.parseMap(context, String.class, String.class);
		return baseSendPost(url, convertToMap(param), headerMap);
	}

	/**
	 * 发送POST请求
	 *
	 * @param url       请求地址
	 * @param paramMap  请求参数
	 * @param headerMap 请求头参数
	 * @return 请求结果
	 */
	public static String baseSendPost(String url, Map<String, String> paramMap, Map<String, String> headerMap) {
		CloseableHttpClient httpClient = HttpClients.createDefault();
		CloseableHttpResponse response = null;
		try {
			HttpPost postReq = new HttpPost(url);
			headerMap = headerMap == null ? new HashMap<>() : headerMap;
			for (Map.Entry<String, String> entry : headerMap.entrySet()) {
				postReq.addHeader(entry.getKey(), entry.getValue());
			}

			List<NameValuePair> paramList = new ArrayList<>();
			for (Map.Entry<String, String> entry : paramMap.entrySet()) {
				paramList.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
			}
			postReq.setEntity(new UrlEncodedFormEntity(paramList, StandardCharsets.UTF_8));
			response = httpClient.execute(postReq);
			int statusCode = response.getStatusLine().getStatusCode();
			if (statusCode == HttpStatus.SC_OK) {
				HttpEntity httpEntity = response.getEntity();
				return httpEntity == null ? null : EntityUtils.toString(httpEntity, StandardCharsets.UTF_8);
			} else {
				log.info("通讯异常，错误码：HTTP CODE(" + statusCode + ")");
				return null;
			}
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
	 * 将请求参数放在RequestBody中，发送POST请求
	 *
	 * @param url   请求地址
	 * @param param 请求参数，会用Jackson转成JSON字符串
	 * @return 请求结果
	 */
	public static <T> String sendJsonInBody(String url, T param) {
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
	public static <T> String sendJsonInBody(String url, T param, T header) {
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
	public static String sendPostInBody(String url, String param, Map<String, String> headerMap) {
		if (url == null || "".equals(url))
			throw new IllegalArgumentException("Property 'url' is required");
		if (param == null || "".equals(param))
			throw new IllegalArgumentException("Property 'param' is required");
		CloseableHttpClient httpClient = HttpClients.createDefault();
		CloseableHttpResponse response = null;
		try {
			HttpPost postReq = new HttpPost(url);
			headerMap = headerMap == null ? new HashMap<>() : headerMap;
			for (Map.Entry<String, String> entry : headerMap.entrySet()) {
				postReq.addHeader(entry.getKey(), entry.getValue());
			}
			postReq.setEntity(new ByteArrayEntity(param.getBytes(StandardCharsets.UTF_8)));
			response = httpClient.execute(postReq);
			int statusCode = response.getStatusLine().getStatusCode();
			if (statusCode == HttpStatus.SC_OK) {
				HttpEntity httpEntity = response.getEntity();
				return httpEntity == null ? null : EntityUtils.toString(httpEntity, StandardCharsets.UTF_8);
			} else {
				log.info("通讯异常，错误码：HTTP CODE(" + statusCode + ")");
				return null;
			}
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
	 * 把普通Bean对象转换成Map<String, String>
	 *
	 * @param t 普通对象
	 * @return map结果
	 */
	private static <T> Map<String, String> convertToMap(T t) {
		Map<String, String> map = new HashMap<>();
		if (t == null)
			return map;
		Field[] fields = t.getClass().getDeclaredFields();
		for (Field field : fields) {
			String fieldName = field.getName();
			// 排除serialVersionUID
			if (!"serialVersionUID".equals(fieldName)) {
				Object val = BeanUtil.getValueByField(field, t);
				String value = val != null ? String.valueOf(val) : null;
				map.put(fieldName, value);
			}
		}
		return map;
	}

}
