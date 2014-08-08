package at.ecrit.github.core;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class GitHubConnector {
	private List<String> cookies;
	private HttpsURLConnection conn;
	private CookieManager cookieManager;
	
	private final String URL = "https://github.com/session";
	private final String USER_AGENT = "Mozilla/5.0";
	private boolean loggedIn = false;
	
	public GitHubConnector(String username, String password){
		try {
			// make sure cookies is turn on
			cookieManager = new CookieManager();
			cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
			CookieHandler.setDefault(cookieManager);
			
			// 1. Send a "GET" request, so that you can extract the form's data.
			String page = getGETPageContent(URL);
			String postParams = getFormParams(page, username, password);
			
			// 2. Construct above post's content and then send a POST request for
			// authentication
			sendPost(URL, postParams);
		} catch (IOException | URISyntaxException e) {
			e.printStackTrace();
		}
	}
	
	private void sendPost(String url, String postParams) throws IOException{
		URL obj = new URL(url);
		conn = (HttpsURLConnection) obj.openConnection();
		// Acts like a browser
		conn.setUseCaches(false);
		conn.setRequestMethod("POST");
		conn.setRequestProperty("Host", "github.com");
		conn.setRequestProperty("User-Agent", USER_AGENT);
		conn.setRequestProperty("Accept",
			"text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
		conn.setRequestProperty("Accept-Language", "de,en-US;q=0.7,en;q=0.3");
		conn.setRequestProperty("Accept-Encoding", "gzip, deflate");
		conn.setRequestProperty("Referer", "https://github.com/login");
		for (String cookie : this.cookies) {
			// conn.addRequestProperty("Cookie", cookie.split(";", 1)[0]);
			conn.setRequestProperty("Cookie", cookieManager.getCookieStore().getCookies()
				.toString());
		}
		conn.setRequestProperty("Connection", "keep-alive");
		
		conn.setDoOutput(true);
		conn.setDoInput(true);
		
		// Send post request
		DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
		wr.writeBytes(postParams);
		wr.flush();
		wr.close();
		
		int responseCode = conn.getResponseCode();
		System.out.println("\nSending 'POST' request to URL : " + url);
		System.out.println("Post parameters : " + postParams);
		System.out.println("Response Code : " + responseCode);
		
		BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();
		
		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		
		System.out.println(response.toString());
		setCookies(conn.getHeaderFields().get("Set-Cookie"));
		
		// get cookies from underlying CookieStore
		CookieStore cookieJar = cookieManager.getCookieStore();
		List<HttpCookie> cookies = cookieJar.getCookies();
		for (HttpCookie cookie : cookies) {
			System.out.println("CookieHandler retrieved cookie: " + cookie);
		}
		loggedIn = true;
		in.close();
	}
	
	private String getFormParams(String html, String username, String password)
		throws UnsupportedEncodingException{
		System.out.println("Extracting form's data...");
		
		Document doc = Jsoup.parse(html);
		
		// GitHub login form in <div id="login".../>
		Element loginDiv = doc.getElementById("login");
		Elements inputElements = loginDiv.getElementsByTag("input");
		List<String> paramList = new ArrayList<String>();
		for (Element inputElement : inputElements) {
			String key = inputElement.attr("name");
			String value = inputElement.attr("value");
			
			if (key.equals("login"))
				value = username;
			else if (key.equals("password"))
				value = password;
			paramList.add(key + "=" + URLEncoder.encode(value, "UTF-8"));
		}
		
		// build parameters list
		StringBuilder result = new StringBuilder();
		for (String param : paramList) {
			if (result.length() == 0) {
				result.append(param);
			} else {
				result.append("&" + param);
			}
		}
		return result.toString();
	}
	
	public String getGETPageContent(String url) throws IOException, URISyntaxException{
		URL obj = new URL(url);
		conn = (HttpsURLConnection) obj.openConnection();
		
		// default is GET
		conn.setRequestMethod("GET");
		conn.setUseCaches(false);
		
		// simulate browser
		conn.setRequestProperty("User-Agent", USER_AGENT);
		conn.setRequestProperty("Accept",
			"text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
		conn.setRequestProperty("Accept-Language", "de,en-US;q=0.7,en;q=0.3");
		conn.setRequestProperty("Cookie", cookieManager.getCookieStore().getCookies().toString());
		if (cookies != null) {
			for (String cookie : this.cookies) {
				conn.addRequestProperty("Cookie", cookie.split(";", 1)[0]);
			}
		}
		int responseCode = conn.getResponseCode();
		System.out.println("\nSending 'GET' request to URL : " + url);
		System.out.println("Response Code : " + responseCode);
		
		BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();
		
		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();
		
		// Get the response cookies
		if (!loggedIn) {
			setCookies(conn.getHeaderFields().get("Set-Cookie"));
		}
		
		return response.toString();
	}
	
	public List<String> getCookies(){
		return cookies;
	}
	
	public void setCookies(List<String> cookies){
		this.cookies = cookies;
	}
}
