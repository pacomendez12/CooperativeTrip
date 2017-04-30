package mx.iteso.pam2017.a705164.cooperativetrip;

import com.loopj.android.http.*;
/**
 * Created by paco on 29/04/2017.
 */

public class RestClient {
    private static final String BASE_URL = "http://192.168.15.210/";
    //private static final String BASE_URL = "https://api.twitter.com/1.1/";

    private static AsyncHttpClient client = new AsyncHttpClient();

    public static void get(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.addHeader("Authorization", AuthenticationManager.getKey());
        client.get(getAbsoluteUrl(url), params, responseHandler);
    }

    public static void post(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.post(getAbsoluteUrl(url), params, responseHandler);
    }

    private static String getAbsoluteUrl(String relativeUrl) {
        return BASE_URL + relativeUrl;
    }
}