package org.netmelody.jarnia.maven;

import org.netmelody.jarnia.HttpGetter;
import org.netmelody.jarnia.github.GithubFetcher.FileRef;

public final class MavenFetcher {

    private final HttpGetter getter = new HttpGetter();
    
    public String fetchLatestVersionOf(String jarName) {
        final String result = getter.get("http://search.maven.org/solrsearch/select?q=name%3A%22" +
                                         jarName +
                                         "%22%20AND%20type%3A1&rows=100000&core=filelisting&wt=json");
        
        System.out.println(result);
        return result;
    }
    
    public String findVer(FileRef ref) {
        final String jarName = ref.path.substring(ref.path.lastIndexOf("/") + 1);
        return fetchLatestVersionOf(jarName);
    }
}