package org.netmelody.jarnia.github;

import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.HttpParams;

public final class GithubFetcher {

    public String fetch() {
        final SchemeRegistry schemeRegistry = new SchemeRegistry();
        schemeRegistry.register(new Scheme("http", 80, PlainSocketFactory.getSocketFactory()));
        schemeRegistry.register(new Scheme("https", 443, SSLSocketFactory.getSocketFactory()));

        final ThreadSafeClientConnManager connectionManager = new ThreadSafeClientConnManager(schemeRegistry);
        connectionManager.setMaxTotal(200);
        connectionManager.setDefaultMaxPerRoute(20);
         
        final HttpParams params = new BasicHttpParams();
        params.setIntParameter(CoreConnectionPNames.SO_TIMEOUT, 30000);
        
        DefaultHttpClient client = new DefaultHttpClient(connectionManager, params);

        try {
            final HttpGet httpget = new HttpGet("https://api.github.com/repos/netmelody/ci-eye/branches");
            httpget.setHeader("Accept", "application/json");

            final ResponseHandler<String> responseHandler = new BasicResponseHandler();
            final String result = client.execute(httpget, responseHandler);
            client.getConnectionManager().shutdown();
            return result;
        }
        catch (HttpResponseException e) {
            if (e.getStatusCode() == 404) {
                return "";
            }
            throw new IllegalStateException(e);
        }
        catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}