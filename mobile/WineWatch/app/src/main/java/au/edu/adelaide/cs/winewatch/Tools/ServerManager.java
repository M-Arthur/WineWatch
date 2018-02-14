package au.edu.adelaide.cs.winewatch.Tools;

import android.text.TextUtils;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import au.edu.adelaide.cs.winewatch.JsonObject.Ferment;
import au.edu.adelaide.cs.winewatch.JsonObject.Temperature;
import au.edu.adelaide.cs.winewatch.JsonObject.User;
import au.edu.adelaide.cs.winewatch.JsonObject.Winery;

/**
 * Created by Arthur on 24/04/15.
 */
public class ServerManager {

	private final static String SERVER_ADDRESS = "https://128.199.204.199/";
	private static final String LOGIN_URL = "users/login";
	private static final String FETCH_FERMENTS_URL = "ferments/pull";
	private static final String FETCH_TEMPERATURES_URL = "temp/pull";
	private static final String ADD_FERMENT_URL = "ferments/add_ferment";

	private static final String LOGIN_SUCCESS = "success";
	private static final String RESULT = "status";
	private static final String USERNAME = "mail";
	private static final String PASSWORD = "passwd";
	private static final String USER_ID = "uid";
	private static final String WINERIES = "wineries";
	private static final String WINERY_ID = "wid";
	private static final String WINERY_NAME = "wname";
	private static final String TOKEN = "token";
	private static final String TANKS = "tanks";
	private static final String FERMENTS = "ferments";
	private static final String FERMENT_ID = "fid";
	private static final String TANK_NUMBER = "tank_num";
	private static final String MOTE_LOCAL_ID = "mid";
	private static final String START_TIME = "t_s";
	private static final String READING = "reading";
	private static final String TEMPERATURES = "temps";
	private static final String UPDATE_TIME = "update_time";
	private static final String BASEDATION_ID = "bid";

	public final static HostnameVerifier DO_NOT_VERIFY = new HostnameVerifier() {
		@Override
		public boolean verify(String hostname, SSLSession session) {
			return true;
		}
	};

	private byte[] readBytes(InputStream inputStream) {

		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

		try {
			byte[] data = new byte[1024];
			int len = 0;
			while ((len = inputStream.read(data)) != -1) {
				byteArrayOutputStream.write(data, 0, len);
			}
			byteArrayOutputStream.close();
			return byteArrayOutputStream.toByteArray();

		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	private String readString(InputStream inputStream) {
		return new String(readBytes(inputStream));
	}

//	private static SSLSocketFactory certificateManager() throws Exception {
//		// Load CAs from an InputStream
//		// (Could be from a resource or ByteArrayInputStream or ...)
//		CertificateFactory cf = null;
//		InputStream caInput = null;
//
//		cf = CertificateFactory.getInstance("X.509");
//
//		// From my own certificate
//		caInput = new BufferedInputStream(new FileInputStream("winewatchsslcert.crt"));
//
//		Certificate ca;
//		try {
//			ca = cf.generateCertificate(caInput);
//			System.out.println("ca=" + ((X509Certificate) ca).getSubjectDN());
//		} finally {
//			caInput.close();
//		}
//
//		// Create a Keystore containing our trusted CAs
//		String keyStoreType = KeyStore.getDefaultType();
//		KeyStore keyStore = KeyStore.getInstance(keyStoreType);
//		keyStore.load(null, null);
//		keyStore.setCertificateEntry("ca", ca);
//
//		// Create a TrustManager that trust the CAs in our KeyStore
//		String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
//		TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
//		tmf.init(keyStore);
//
//		// Create an SSLContext that uses our TrustManger
//		SSLContext context = SSLContext.getInstance("TLS");
//		context.init(null, tmf.getTrustManagers(), null);
//		return context.getSocketFactory();
//	}

	private SSLSocketFactory passAllCertificate() {
		// Create a trust manager that does not validate certificate chains
		// Android use X509 cert
		TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {

			@Override
			public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {

			}

			@Override
			public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {

			}

			@Override
			public X509Certificate[] getAcceptedIssuers() {
				return new X509Certificate[]{};
			}
		}};

		// Install the all-trusting trust manager
		try {
			SSLContext sc = SSLContext.getInstance("TLS");
			sc.init(null, trustAllCerts, null);
			return sc.getSocketFactory();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public HttpClient getNewHttpClient() {
		try {
			KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
			trustStore.load(null, null);

			MySSLSocketFactory sf = new MySSLSocketFactory(trustStore);
			sf.setHostnameVerifier(org.apache.http.conn.ssl.SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

			HttpParams params = new BasicHttpParams();
			HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);

			SchemeRegistry registry = new SchemeRegistry();
			registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
			registry.register(new Scheme("https", sf, 443));

			ClientConnectionManager ccm = new ThreadSafeClientConnManager(params, registry);

			return new DefaultHttpClient(ccm, params);
		} catch (Exception e) {
			return new DefaultHttpClient();
		}
	}

	public Map<String, Object> login(User user) {
		String urlPath = SERVER_ADDRESS + LOGIN_URL;
		Map<String, Object> result = new HashMap<String, Object>();
		try {
			HttpClient httpClient = getNewHttpClient();
			HttpPost httpPost = new HttpPost(urlPath);
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair(USERNAME, user.getUsername()));
			params.add(new BasicNameValuePair(PASSWORD, user.getPassword()));
			httpPost.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
			HttpResponse response = httpClient.execute(httpPost);
			if (response.getStatusLine().getStatusCode() == 200) {
				String jsonObject = EntityUtils.toString(response.getEntity());

				Log.e("user", jsonObject); // TODO: Delete this sentence

				JSONObject userInfo = new JSONObject(jsonObject);
				if (userInfo.getString(RESULT).equals(LOGIN_SUCCESS)) {
					result.put(Constants.LOGIN_RESULT, true);
					User wineMaker = user;
					wineMaker.setUid(Integer.parseInt(userInfo.getString(USER_ID)));
					wineMaker.setToken(userInfo.getString(TOKEN));
					result.put(Constants.LOGIN_USER, wineMaker);
				} else {
					result.put(Constants.LOGIN_RESULT, false);
					result.put(Constants.LOGIN_INFO, userInfo.getString(RESULT));
					result.put(Constants.LOGIN_ERROR_TYPE, Constants.LOGIN_PASSWORD_ERROR);
				}
			} else {
				result.put(Constants.LOGIN_RESULT, false);
				result.put(Constants.LOGIN_INFO, "Cannot connect to server, please check the Internet connection.");
				result.put(Constants.LOGIN_ERROR_TYPE, Constants.LOGIN_INTERNET_ERROR);
			}
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			result.put(Constants.LOGIN_RESULT, false);
			result.put(Constants.LOGIN_ERROR_TYPE, Constants.LOGIN_SYSTEM_ERROR);
			result.put(Constants.LOGIN_INFO, "System is broken down.");
			return result;
		}
	}

	public String loginWithHttpConnections(String username, String password) {
		String urlPath = SERVER_ADDRESS + "users/login";
		URL url;
		String result = "";
		try {
			url = new URL(urlPath);

			JSONObject ClientKey = new JSONObject();
			ClientKey.put("mail", username);
			ClientKey.put("passwd", password);

			String content = String.valueOf(ClientKey);

			HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();

			// Tell the URLConnection to use all pass
			conn.setSSLSocketFactory(passAllCertificate());
			conn.setHostnameVerifier(ServerManager.DO_NOT_VERIFY);

			conn.setConnectTimeout(5000);
			conn.setDoOutput(true);
			conn.setDoInput(true);
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type", "application/json");

//			StringBuilder buf = new StringBuilder();
//			buf.append(URLEncoder.encode("mail","UTF-8")+"="+ URLEncoder.encode(username,"UTF-8")+"&");
//			buf.append(URLEncoder.encode("passwd","UTF-8")+"="+URLEncoder.encode(password,"UTF-8"));
//			String content = buf.toString();


			OutputStream os = conn.getOutputStream();
			os.write(content.getBytes());
			os.flush();
			os.close();

			int code = conn.getResponseCode();
			Log.e("code", code + "");
			if (code == 200) {

				InputStream is = conn.getInputStream();

				String json = readString(is);
				Log.e("text", json);
				JSONObject jsonObject = new JSONObject(json);

				Log.e("status", jsonObject.getString("status"));
				Log.e("uid", jsonObject.getString("uid"));
				Log.e("token", jsonObject.getString("token"));
				JSONArray wineries = jsonObject.getJSONArray("wineries");
				for (int i = 0; i < wineries.length(); i++) {
					Log.e("winery name", wineries.getJSONObject(i).getString("wname"));
				}
			} else {
				result = "Cannot connect to server!";
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	public Map<String, Object> getWineryList(User user) {
		String urlPath = SERVER_ADDRESS + LOGIN_URL;
		Map<String, Object> result = new HashMap<String, Object>();
		try {
			HttpClient httpClient = getNewHttpClient();
			HttpPost httpPost = new HttpPost(urlPath);
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair(USERNAME, user.getUsername()));
			params.add(new BasicNameValuePair(PASSWORD, user.getPassword()));
			httpPost.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
			HttpResponse response = httpClient.execute(httpPost);
			if (response.getStatusLine().getStatusCode() == 200) {
				String jsonObject = EntityUtils.toString(response.getEntity());

				Log.e("winery", jsonObject);
				JSONObject userInfo = new JSONObject(jsonObject);
				if (userInfo.getString(RESULT).equals(LOGIN_SUCCESS)) {
					result.put(Constants.GET_WINERY_LIST_RESULT, true);

					List<Winery> wineriesL = new ArrayList<Winery>();
					Winery temp;
					JSONArray wineries = userInfo.getJSONArray(WINERIES);
					for (int i = 0; i < wineries.length(); i++) {
						temp = new Winery();
						temp.setWid(Integer.parseInt(wineries.getJSONObject(i).getString(WINERY_ID)));
						temp.setName(wineries.getJSONObject(i).getString(WINERY_NAME));
						wineriesL.add(temp);
					}

					result.put(Constants.WINERY_LIST, wineriesL);
				} else {
					result.put(Constants.GET_WINERY_LIST_RESULT, false);
					result.put(Constants.LOGIN_INFO, userInfo.getString(RESULT));
					result.put(Constants.GET_WINERY_LIST_ERROR_TYPE, Constants.LOGIN_PASSWORD_ERROR);
				}
			} else {
				result.put(Constants.GET_WINERY_LIST_RESULT, false);
				result.put(Constants.LOGIN_INFO, "Cannot connect to server, please check the Internet connection.");
				result.put(Constants.GET_WINERY_LIST_ERROR_TYPE, Constants.LOGIN_INTERNET_ERROR);
			}
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			result.put(Constants.GET_WINERY_LIST_RESULT, false);
			result.put(Constants.GET_WINERY_LIST_ERROR_TYPE, Constants.LOGIN_SYSTEM_ERROR);
			result.put(Constants.LOGIN_INFO, "System is broken down.");
			return result;
		}
	}

	public Map<String, Object> getFermentList(int uid, String token, int wid) {
		String urlPath = SERVER_ADDRESS + FETCH_FERMENTS_URL;
		Map<String, Object> result = new HashMap<String, Object>();
		try {
			HttpClient httpClient = getNewHttpClient();
			HttpPost httpPost = new HttpPost(urlPath);
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair(USER_ID, uid + ""));
			params.add(new BasicNameValuePair(WINERY_ID, wid + ""));
			params.add(new BasicNameValuePair(TOKEN, token));
			Log.e("Ferment_uid", uid + "");
			Log.e("Ferment_wid", wid + "");
			Log.e("Ferment_token", token);
			httpPost.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
			HttpResponse response = httpClient.execute(httpPost);
			if (response.getStatusLine().getStatusCode() == 200) {
				String entity = EntityUtils.toString(response.getEntity());

				Log.e("Ferment", entity);

				JSONObject jsonObject = new JSONObject(entity);
				if (jsonObject.getString(RESULT).equals(LOGIN_SUCCESS)) {
					result.put(Constants.RESULT, true);

					List<Ferment> fermentList = new ArrayList<>();
					Ferment temp;
					JSONArray ferments = jsonObject.getJSONArray(FERMENTS);

					for (int i = 0; i < ferments.length(); i++) {
						temp = new Ferment();
						temp.setFermentId(Integer.parseInt(ferments.getJSONObject(i).getString(FERMENT_ID)));
						temp.setTankNumber(ferments.getJSONObject(i).getString(TANK_NUMBER));
						temp.setMoteId(Integer.parseInt(ferments.getJSONObject(i).getString(MOTE_LOCAL_ID)));
						temp.setStartTime(ferments.getJSONObject(i).getString(START_TIME));
						fermentList.add(temp);
					}

					// TODO: verify is there are data

					result.put(Constants.RESULT_OBJECT, fermentList);
				} else {
					result.put(Constants.RESULT, false);
					result.put(Constants.LOGIN_INFO, jsonObject.getString(RESULT));
					result.put(Constants.ERROR_TYPE, Constants.VERIFICATION_ERROR);
				}
			} else {
				result.put(Constants.RESULT, false);
				result.put(Constants.LOGIN_INFO, "Cannot connect to server, please check the Internet connection.");
				result.put(Constants.ERROR_TYPE, Constants.INTERNET_ERROR);
			}
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			result.put(Constants.RESULT, false);
			result.put(Constants.ERROR_TYPE, Constants.SYSTEM_ERROR);
			result.put(Constants.INFO, "System is broken down.");
			return result;
		}
	}

	public Map<String, Object> getTemperatures(int userId, String token, int wineryId, int fermentId) {
		String urlPath = SERVER_ADDRESS + FETCH_TEMPERATURES_URL;
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair(USER_ID, userId + ""));
		params.add(new BasicNameValuePair(WINERY_ID, wineryId + ""));
		params.add(new BasicNameValuePair(TOKEN, token));
		params.add(new BasicNameValuePair(FERMENT_ID, fermentId + ""));
		Map<String, Object> result = connect(urlPath, params);
		if ((boolean) result.get(Constants.RESULT)) {
			JSONObject jsonObject = (JSONObject) result.get(Constants.RESULT_JSON_OBJECT);
			try {
				JSONArray reading = jsonObject.getJSONArray(READING);
				if (TextUtils.isEmpty(reading.toString())) {
					result.put(Constants.RESULT, false);
					result.put(Constants.INFO, "It is an empty tank.");
				} else {
					ArrayList<Temperature> temps = new ArrayList<>();
					Temperature tempT;
					JSONObject tempR;
					for (int i = 0; i < reading.length(); i++) {
						tempR = reading.getJSONObject(i);
						tempT = new Temperature();
						tempT.setUpdatingTime(tempR.getString(UPDATE_TIME));

						String data = tempR.getString("data");
						JSONObject dataJO = new JSONObject(data);
						String tempStr = dataJO.getString(TEMPERATURES);
						tempStr = tempStr.substring(1, tempStr.length() - 1);
						String battery = dataJO.getString("battery");
						String sampleInterval = dataJO.getString("sample_interval");
						String[] tempStrArr = tempStr.split(",");
						double[] tempArray = new double[tempStrArr.length];
						try {
							for (int j = 0; j < tempArray.length; j++) {
								tempArray[j] = Double.parseDouble(tempStrArr[j]);
							}
							tempT.setBattery(Double.parseDouble(battery));
							tempT.setInterval(Double.parseDouble(sampleInterval));
						} catch (NumberFormatException nfe) {
							nfe.printStackTrace();
						}
						tempT.setTemperatures(tempArray);
						temps.add(tempT);
					}
					result.put(Constants.RESULT_OBJECT, temps.get(0));
					result.put(Constants.RESULT_JSON_OBJECT_HISTORY, temps);
				}
			} catch (JSONException e) {
				e.printStackTrace();
				result.put(Constants.RESULT, false);
				result.put(Constants.INFO, "It is an empty tank");
				return result;
			}
		}
		return result;
	}

	public Map<String, Object> connect(String urlPath, List<NameValuePair> params) {
		Map<String, Object> result = new HashMap<String, Object>();
		try {
			HttpClient httpClient = getNewHttpClient();
			HttpPost httpPost = new HttpPost(urlPath);
			httpPost.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
			HttpResponse response = httpClient.execute(httpPost);
			if (response.getStatusLine().getStatusCode() == 200) {
				String entity = EntityUtils.toString(response.getEntity());
				Log.e("Connect_Entity", entity);
				JSONObject jsonObject = new JSONObject(entity);
				if (jsonObject.getString(RESULT).toLowerCase().equals(LOGIN_SUCCESS)) {
					result.put(Constants.RESULT, true);
					result.put(Constants.RESULT_JSON_OBJECT, jsonObject);
				} else {
					result.put(Constants.RESULT, false);
					result.put(Constants.LOGIN_INFO, jsonObject.getString(RESULT));
				}
			} else {
				Log.e("Error", "LostConnect");
				result.put(Constants.RESULT, false);
				result.put(Constants.LOGIN_INFO, "Cannot connect to server, please check the Internet connection.");
			}
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			result.put(Constants.RESULT, false);
			result.put(Constants.INFO, "System is broken down.");
			return result;
		}
	}

	public Map<String, Object> addFermentation(User user, Ferment ferment, int wineryId, int bid) {
		String urlPath = SERVER_ADDRESS + ADD_FERMENT_URL;
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair(USER_ID, user.getUid() + ""));
		params.add(new BasicNameValuePair(WINERY_ID, wineryId + ""));
		params.add(new BasicNameValuePair(TOKEN, user.getToken()));
		params.add(new BasicNameValuePair(MOTE_LOCAL_ID, ferment.getMoteId() + ""));
		params.add(new BasicNameValuePair(BASEDATION_ID, bid + ""));
		params.add(new BasicNameValuePair(TANK_NUMBER, ferment.getTankNumber() + ""));
		Map<String, Object> result = connect(urlPath, params);
//		if((boolean)result.get(Constants.RESULT)){
//			JSONObject jsonObject = (JSONObject) result.get(Constants.RESULT_JSON_OBJECT);
//			try {
//				String status = jsonObject.getString("status");
//				Log.e("test result",status);
//			}
//			catch (Exception e){
//				e.printStackTrace();
//				result.put(Constants.RESULT, false);
//				result.put(Constants.INFO, "error transformation.");
//				return result;
//			}
//		}
		return result;
	}
}
