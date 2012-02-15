package org.netmelody.jarnia.github;

import org.netmelody.jarnia.HttpGetter;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import static com.google.common.collect.Iterables.find;

public final class GithubFetcher {

    private final HttpGetter getter = new HttpGetter();
    private final JsonParser jsonParser = new JsonParser();
    
    public String fetchLatestShaFor(String owner, String repo, String branch) {
        final String url = String.format("https://api.github.com/repos/%s/%s/branches", owner, repo);
        final JsonArray branches = jsonParser.parse(getter.get(url)).getAsJsonArray();
        return find(branches, isBranchNamed(branch)).getAsJsonObject().get("commit").getAsJsonObject().get("sha").getAsString();
    }
    
    public Iterable<FileRef> fetchFilesFor(String owner, String repo, String sha) {
        final String url = String.format("https://api.github.com/repos/%s/%s/git/trees/%s?recursive=1", owner, repo, sha);
        final JsonArray nodes = jsonParser.parse(getter.get(url)).getAsJsonObject().get("tree").getAsJsonArray();
        return Iterables.transform(Iterables.filter(nodes, blobs()), toFileRefs());
    }
    
    public String findVer(Iterable<FileRef> jars) {
        FileRef ref = Iterables.find(jars, Predicates.alwaysTrue());
        
        final String jarName = ref.path.substring(ref.path.lastIndexOf("/") + 1);
        System.out.println(jarName);
        final String url = "http://search.maven.org/solrsearch/select?q=name%3A%22" + jarName + "%22%20AND%20type%3A1&rows=100000&core=filelisting&wt=json";
        System.out.println(getter.get(url));
        return "";
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