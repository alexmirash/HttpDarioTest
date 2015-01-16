package com.example.mirash.httpdariotest;

import android.net.Uri;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.ProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultRedirectHandler;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static com.example.mirash.httpdariotest.LogUtils.log;

/**
 * @author Mirash
 */
public class HttpClientHelper {
    public static final class CREDENTIALS {
        public static final String LOGIN = "test1";
        public static final String PASSWORD = "ZaqXsw";
    }

    public static final String URL = "https://portaldoc.mac.org.il/dana-na/auth/url_65/login.cgi";

    private static final String CONTENT_TYPE_KEY = "Content-Type";
    private static final String CONTENT_TYPE_VALUE = "application/x-www-form-urlencoded";
    private static final String LOCATION_KEY = "Location";
    private static final String REALM_VALUE = "elad-test";

    public static final String POST_KEY_USERNAME = "username";
    public static final String POST_KEY_PASSWORD = "password";
    public static final String POST_KEY_REALM = "realm";
    public static final String POST_KEY_TZ_OFFSET = "tz_offset";
    //tz_offset=120&username=test1&password=ZaqXsw&realm=elad-test&btnSubmit=%D7%90%D7%A9%D7%A8

    private static DefaultHttpClient initHttpClient() throws Exception {
        DefaultHttpClient httpClient = HttpClientFactory.getDarioHttpClient();
        httpClient.setRedirectHandler(new DefaultRedirectHandler() {
            @Override
            public boolean isRedirectRequested(HttpResponse response, HttpContext context) {
                boolean result = super.isRedirectRequested(response, context);
                log("isRedirectRequested " + result);
                return result;
            }

            @Override
            public URI getLocationURI(HttpResponse response, HttpContext context) throws ProtocolException {
                URI uri = super.getLocationURI(response, context);
                log("getLocationURI " + uri);
                return uri;
            }
        });
        return httpClient;
    }

    public static void login(String login, String password) throws Exception {
        log("login <" + login + ":" + password + ">");
        DefaultHttpClient httpClient = initHttpClient();
        HttpRequestBase httpPost = createHttpPostRequest(login, password);
        HttpResponse postResponse = httpClient.execute(httpPost);
        log("post resp = " + postResponse.getStatusLine().getStatusCode() + "; " + EntityUtils.toString(postResponse.getEntity()));
        // expect a 302 response.
        if (postResponse.getStatusLine().getStatusCode() == 302) {
            String redirectURL = postResponse.getFirstHeader(LOCATION_KEY).getValue();
            HttpGet httpGet = new HttpGet(redirectURL);
            HttpResponse httpGetResponse = httpClient.execute(httpGet);
            log("get resp = " + httpGetResponse.getStatusLine().getStatusCode() + "; " + EntityUtils.toString(httpGetResponse.getEntity()));
        }
        HttpEntity entity = postResponse.getEntity();
        if (entity != null) {
            entity.consumeContent();
        }
        List<Cookie> cookies = httpClient.getCookieStore().getCookies();
        if (cookies.isEmpty()) {
            log("cookies.isEmpty()");
        } else {
            for (int i = 0; i < cookies.size(); i++) {
                log(i + ")" + cookies.get(i).toString());
            }
        }
        httpClient.getConnectionManager().shutdown();
    }

    public static String getPostRequestUriString(String login, String password) {
        Uri.Builder builder = new Uri.Builder();
        builder.scheme("https")
                .authority("portaldoc.mac.org.il")
                .appendPath("dana-na")
                .appendPath("auth")
                .appendPath("url_65")
                .appendPath("login.cgi")
                .appendQueryParameter(POST_KEY_USERNAME, Uri.encode(login))
                .appendQueryParameter(POST_KEY_PASSWORD, Uri.encode(password))
                .appendQueryParameter(POST_KEY_REALM, Uri.encode(REALM_VALUE));
        return builder.build().toString();
    }

    private static HttpRequestBase createHttpPostRequest(String login, String password) {
        HttpPost httpPost;
        //"https://portaldoc.mac.org.il/dana-na/auth/url_65/login.cgi"

/*        httpPost = new HttpPost(URL);
        List<NameValuePair> args = new ArrayList<>();
        args.add(new BasicNameValuePair(POST_KEY_USERNAME, Uri.encode(login)));
        args.add(new BasicNameValuePair(POST_KEY_PASSWORD, Uri.encode(password)));
        args.add(new BasicNameValuePair(POST_KEY_REALM, Uri.encode("elad-test")));
        try {
            httpPost.setEntity(new UrlEncodedFormEntity(args));
        } catch (UnsupportedEncodingException ignored) {
        }*/

        String uriString = getPostRequestUriString(login, password);
        log("uri=" + uriString);
        httpPost = new HttpPost(uriString);

        httpPost.setHeader(CONTENT_TYPE_KEY, CONTENT_TYPE_VALUE);
        return httpPost;
    }
}
