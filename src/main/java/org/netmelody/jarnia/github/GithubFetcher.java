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

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import static com.google.common.collect.Iterables.find;

public final class GithubFetcher {

    private final JsonParser jsonParser = new JsonParser();
    
    public String fetchLatestShaFor() {
        return fetchLatestShaFor("netmelody", "ci-eye", "master");
    }
    
    public String fetchLatestShaFor(String owner, String repo, String branch) {
        final String url = String.format("https://api.github.com/repos/%s/%s/branches", owner, repo);
        final JsonArray branches = jsonParser.parse(fetch(url)).getAsJsonArray();
        return find(branches, isBranchNamed(branch)).getAsJsonObject().get("commit").getAsJsonObject().get("sha").getAsString();
    }
    
    public Iterable<FileRef> fetchFilesFor(String owner, String repo, String sha) {
        final String url = String.format("https://api.github.com/repos/%s/%s/git/trees/%s?recursive=1", owner, repo, sha);
        final JsonArray nodes = jsonParser.parse(fetch(url)).getAsJsonObject().get("tree").getAsJsonArray();
        return Iterables.transform(Iterables.filter(nodes, blobs()), toFileRefs());
    }
    
    private String fetch(final String url) {
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
            final HttpGet httpget = new HttpGet(url);
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

    private Predicate<JsonElement> isBranchNamed(final String branchName) {
        return new Predicate<JsonElement>() {
            @Override public boolean apply(JsonElement input) {
                return branchName.equals(input.getAsJsonObject().get("name").getAsString());
            }
        };
    }
    
    private Predicate<JsonElement> blobs() {
        return new Predicate<JsonElement>() {
            @Override public boolean apply(JsonElement input) {
                return "blob".equals(input.getAsJsonObject().get("type").getAsString());
            }
        };
    }
    
    private Function<JsonElement, FileRef> toFileRefs() {
        return new Function<JsonElement, FileRef>() {
            @Override public FileRef apply(JsonElement input) {
                final JsonObject node = input.getAsJsonObject();
                return new FileRef(node.get("path").getAsString(), node.get("size").getAsInt());
            }
        };
    }

    public static final class FileRef {
        public final String path;
        public final int size;

        public FileRef(String path, int size) {
            this.path = path;
            this.size = size;
        }
        
        @Override
        public String toString() {
            return path + " <" + size + ">";
        }
    }
}