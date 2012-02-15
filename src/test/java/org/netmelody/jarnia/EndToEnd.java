package org.netmelody.jarnia;

import org.junit.Test;
import org.netmelody.jarnia.github.GithubFetcher;
import org.netmelody.jarnia.github.GithubFetcher.FileRef;
import org.netmelody.jarnia.maven.MavenFetcher;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

public final class EndToEnd {

    private final GithubFetcher github = new GithubFetcher();
    private final MavenFetcher maven = new MavenFetcher();
    
    @Test public void
    go() {
        final String sha = github.fetchLatestShaFor("netmelody", "ci-eye", "master");
        final Iterable<FileRef> fileRefs = github.fetchFilesFor("netmelody", "ci-eye", sha);
        
        final Iterable<FileRef> jars = Iterables.filter(fileRefs, new Predicate<FileRef>() {
            @Override public boolean apply(FileRef input) {
                return input.path.toLowerCase().endsWith(".jar");
            }
        });

        for (FileRef jar : jars) {
            System.out.println(jar + " : " + maven.findVer(jar));
        }
    }
    
}
