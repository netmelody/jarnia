package org.netmelody.jarnia.maven;

import java.util.List;
import java.util.regex.Pattern;

import org.netmelody.jarnia.HttpGetter;
import org.netmelody.jarnia.github.GithubFetcher.FileRef;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import static com.google.common.base.Predicates.not;
import static com.google.common.base.Predicates.or;
import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Iterables.transform;

public final class MavenFetcher {

    private final HttpGetter getter = new HttpGetter();
    private final JsonParser jsonParser = new JsonParser();
    
    public String fetchLatestVersionOf(String jarName) {
        final String result = getter.get("http://search.maven.org/solrsearch/select?q=name%3A%22" +
                                         jarName +
                                         "%22%20AND%20type%3A1&rows=100000&core=filelisting&wt=json");
        
        JsonArray results = responseFrom(result);
        if (results.size() == 0) {
            return "<unknown>";
        }
        JsonObject response = results.get(0).getAsJsonObject();
        final String path = response.get("path").getAsString();
        String s = path.substring(0, path.length() - jarName.length() - 1);
        s = s.substring(0, s.lastIndexOf("/"));
        final String g = s.substring(0, s.lastIndexOf("/")).replace("/", ".");
        final String a = s.substring(s.lastIndexOf("/") + 1, s.length());
        
        final String artifact = getter.get("http://search.maven.org/solrsearch/select?q=g:%22" + g + "%22%20AND%20a:%22" + a + "%22&rows=100000&core=gav&wt=json");
        results = responseFrom(artifact);
        
        @SuppressWarnings("unchecked")
        final List<String> versions = Lists.newArrayList(filter(transform(results, toVersionNumbers()),
                                                                not(or(snapshots(), releaseCandidates(), betas(), alphas()))));
        
        return Iterables.find(versions, Predicates.alwaysTrue(), "<unknown>");
    }

    private Predicate<String> snapshots() {
        final Pattern snapshot = Pattern.compile("\\d\\d\\d\\d\\d\\d\\d\\d");
        return new Predicate<String>() {
            @Override public boolean apply(String ver) {
                return snapshot.matcher(ver).find();
            }
        };
    }
    
    private Predicate<String> releaseCandidates() {
        final Pattern snapshot = Pattern.compile("RC");
        return new Predicate<String>() {
            @Override public boolean apply(String ver) {
                return snapshot.matcher(ver).find();
            }
        };
    }
    
    private Predicate<String> betas() {
        final Pattern snapshot = Pattern.compile("beta");
        return new Predicate<String>() {
            @Override public boolean apply(String ver) {
                return snapshot.matcher(ver).find();
            }
        };
    }
    
    private Predicate<String> alphas() {
        final Pattern snapshot = Pattern.compile("alpha");
        return new Predicate<String>() {
            @Override public boolean apply(String ver) {
                return snapshot.matcher(ver).find();
            }
        };
    }

    private Function<JsonElement, String> toVersionNumbers() {
        return new Function<JsonElement, String>() {
            @Override public String apply(JsonElement input) {
                return input.getAsJsonObject().get("v").getAsString();
            }
        };
    }

    private JsonArray responseFrom(final String result) {
        final JsonObject response = jsonParser.parse(result).getAsJsonObject().get("response").getAsJsonObject();
        
        final int numFound = response.get("numFound").getAsInt();
        if (numFound == 0) {
            return new JsonArray();
        }
        return response.get("docs").getAsJsonArray();
    }
    
    public String findVer(FileRef ref) {
        final String jarName = ref.path.substring(ref.path.lastIndexOf("/") + 1);
        return fetchLatestVersionOf(jarName);
    }
}