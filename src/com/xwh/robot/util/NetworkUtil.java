package com.xwh.robot.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.params.ConnRouteParams;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;
import android.util.Log;

public class NetworkUtil {

	private static final String TAG = "NetworkUtil";

	protected static String convertStreamToString(InputStream is) {

		if (is == null) {
			return null;
		}

		BufferedReader reader = new BufferedReader(new InputStreamReader(is), 8000);
		StringBuilder sb = new StringBuilder();

		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				sb.append(line).append('\n');
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return sb.toString();
	}

	public static boolean dataConnected(Context context) {
		Log.i(TAG, "check dataConnected");
		if (context != null) {
			ConnectivityManager mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
			if (mNetworkInfo != null) {
				return mNetworkInfo.isAvailable();
			}
		}
		return false; 
	}

	/**
	 * 判断网络类型
	 */
	public static boolean is2G(Context mContext) {
		//Log.i(TAG, "is2G");
		ConnectivityManager connectivityManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = connectivityManager.getActiveNetworkInfo();
		if (info != null && info.getState() == NetworkInfo.State.CONNECTED && info.getType() == ConnectivityManager.TYPE_MOBILE) {
			String extraInfo = info.getExtraInfo().toUpperCase();
			String subTypeName = info.getSubtypeName().toUpperCase();
			Log.i(TAG, "getExtraInfo:" + info.getExtraInfo());
			Log.i(TAG, "getSubtypeName:" + info.getSubtypeName());
			Log.i(TAG, "getTypeName:" + info.getTypeName());
			// uninet,cmnet
			if ((subTypeName.indexOf("GPRS") > -1 || subTypeName.indexOf("EDGE") > -1) && extraInfo.indexOf("NET") > -1 && extraInfo.indexOf("3GNET") == -1) {
				return true;
			} else {
				return false;
			}
		}
		return false;
	}

	public static boolean is3G(Context mContext) {
		//Log.i(TAG, "is3G");
		ConnectivityManager connectivityManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = connectivityManager.getActiveNetworkInfo();
		if (info != null && info.getState() == NetworkInfo.State.CONNECTED && info.getType() == ConnectivityManager.TYPE_MOBILE) {
			// 接入点设置
			String extraInfo = info.getExtraInfo().toUpperCase();
			String subTypeName = info.getSubtypeName().toUpperCase();
			Log.i(TAG, "getExtraInfo:" + info.getExtraInfo());
			Log.i(TAG, "getSubtypeName:" + info.getSubtypeName());
			Log.i(TAG, "getTypeName:" + info.getTypeName());
			// 联通3G，电信3G
			if (extraInfo.indexOf("3GNET") > -1 || extraInfo.indexOf("CTNET") > -1) {
				return true;
			}
		}
		return false;
	}

	public static boolean isWap(Context mContext) {
		//Log.i(TAG, "isWap");
		ConnectivityManager connectivityManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = connectivityManager.getActiveNetworkInfo();
		if (info != null && info.getState() == NetworkInfo.State.CONNECTED && info.getType() == ConnectivityManager.TYPE_MOBILE) {
			String extraInfo = info.getExtraInfo().toUpperCase();
			String subTypeName = info.getSubtypeName().toUpperCase();
			Log.i(TAG, "getExtraInfo:" + info.getExtraInfo());
			Log.i(TAG, "getSubtypeName:" + info.getSubtypeName());
			Log.i(TAG, "getTypeName:" + info.getTypeName());
			// uniwap,cmwap,3gwap,ctwap
			if (extraInfo.indexOf("WAP") > -1) {
				return true;
			} else {
				return false;
			}
		}
		return false;
	}

	public static boolean isWifi(Context mContext) {
		//Log.i(TAG, "isWifi");
		ConnectivityManager connectivityManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
		if (activeNetInfo != null && activeNetInfo.getState() == NetworkInfo.State.CONNECTED && activeNetInfo.getType() == ConnectivityManager.TYPE_WIFI) {
			return true;
		}
		return false;
	}


	/**
	 * http发送Post请求
	 */
	public static String post(String url, Map<String, String> map_params) {
		HttpClient httpClient = new DefaultHttpClient();
		String response = null;
		try {
			HttpPost httpRequest = new HttpPost(url);

			List<NameValuePair> params = new ArrayList<NameValuePair>();

			Iterator<String> it_keys = map_params.keySet().iterator();
			while (it_keys.hasNext()) {
				String key = it_keys.next();
				String value = map_params.get(key);
				params.add(new BasicNameValuePair(key, value));
			}

			/* 添加请求参数到请求对象 */
			httpRequest.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
			/* 发送请求并等待响应 */
			ResponseHandler<String> responseHandler = new BasicResponseHandler();
			response = httpClient.execute(httpRequest, responseHandler);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			httpClient.getConnectionManager().shutdown();
		}

		return response;

	}

	public static HttpResponse getHttpResponse(String url, HttpHost proxy, int timeout) throws ClientProtocolException, IOException {
		Log.i(TAG, "Get HTTP response >>> " + url + ", proxy: " + proxy);

		if (TextUtils.isEmpty(url)) {
			return null;
		}

		HttpParams params = new BasicHttpParams();
		if(timeout>0){
			HttpConnectionParams.setConnectionTimeout(params, timeout);
			HttpConnectionParams.setSoTimeout(params, timeout);
		}
		
		if (proxy != null) {
			ConnRouteParams.setDefaultProxy(params, proxy);
		}
		HttpClient httpClient = new DefaultHttpClient(params);
		HttpGet httpGet = new HttpGet(url);
		httpGet.addHeader("User-Agent", "android");
		HttpResponse response = null;

		// 失败重试
		for (int i = 0; i < 2; i++) {
			Log.i(TAG, "getHttpResponse times:" + i);
			try {
				response = httpClient.execute(httpGet);
			} catch (Exception es) {
				es.printStackTrace();
				Log.i(TAG, "getHttpResponse Exception");
			}
			if (response != null) {
				break;
			}
		}
		Log.i(TAG, "response===========  " + response);
		return response;
	}

/*	public static JSONObject getJsonObject(String url, HttpHost proxy, int timeout) throws IOException, JSONException {
		HttpResponse response = getHttpResponse(url, proxy, timeout);
		if (response == null) {
			return null;
		}

		return getJsonObject(response, false);
	}*/


	/**
	 * 请求网络，获取http文本
	 * @throws IOException 
	 * @throws ClientProtocolException 
	 */
	public static String getHttpString(String url, HttpHost proxy, int timeout) throws ClientProtocolException, IOException {
		HttpResponse response = getHttpResponse(url, proxy, timeout);
		if (response == null) {
			return null;
		}
		HttpEntity entity = response.getEntity();
		String result = null;
		if (entity != null) {
			result = EntityUtils.toString(entity);
		}
		
		return result;

	}
	
	/**
	 * 请求网络，获取json对象
	 */
	public static JSONObject getJsonObject(String url, HttpHost proxy, int timeout) throws IOException, JSONException {
		
		String http_result = getHttpString(url, proxy, timeout);

		JSONObject json = null;
		if (http_result != null && !"".equals(http_result)) {
			json = new JSONObject(http_result);
		}
		return json;
	}


	/**
	 * 请求网络，获取json数组
	 */
	public static JSONArray getJsonArray(String url, HttpHost proxy, int timeout) throws IllegalStateException, IOException, JSONException {
		
		String http_result = getHttpString(url, proxy, timeout);

		JSONArray json = null;
		if (http_result != null && !"".equals(http_result)) {
			json = new JSONArray(http_result);
		}
		return json;
	}

	
	public static JSONObject tojson(InputStream is) {
		String res = convertStreamToString(is);
		JSONObject json = null;
		try {
			json = new JSONObject(res);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return json;
	}

}
