package org.netmelody.jarnia.maven;

import org.netmelody.jarnia.HttpGetter;
import org.netmelody.jarnia.github.GithubFetcher.FileRef;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public final class MavenFetcher {

    private final HttpGetter getter = new HttpGetter();
    private final JsonParser jsonParser = new JsonParser();
    
    public String fetchLatestVersionOf(String jarName) {
        final String result = getter.get("http://search.maven.org/solrsearch/select?q=name%3A%22" +
                                         jarName +
                                         "%22%20AND%20type%3A1&rows=100000&core=filelisting&wt=json");
        
        JsonObject response = responseFrom(result);
        if (null == response) {
            return "";
        }
        
        
        final String path = response.get("path").getAsString();
        String s = path.substring(0, path.length() - jarName.length() - 1);
        s = s.substring(0, s.lastIndexOf("/"));
        s = s.substring(0, s.lastIndexOf("/")).replace("/", ".") + ":" + s.substring(s.lastIndexOf("/") + 1, s.length());
        
        final String artifact = getter.get("http://search.maven.org/solrsearch/select?q=id:%22" + s + "%22");
        response = responseFrom(artifact);
        
        return response.get("latestVersion").getAsString();
    }

    private JsonObject responseFrom(final String result) {
        final JsonObject response = jsonParser.parse(result).getAsJsonObject().get("response").getAsJsonObject();
        
        final int numFound = response.get("numFound").getAsInt();
        if (numFound == 0) {
            return null;
        }
        return response.get("docs").getAsJsonArray().get(0).getAsJsonObject();
    }
    
    public String findVer(FileRef ref) {
        final String jarName = ref.path.substring(ref.path.lastIndexOf("/") + 1);
        return fetchLatestVersionOf(jarName);
    }
}