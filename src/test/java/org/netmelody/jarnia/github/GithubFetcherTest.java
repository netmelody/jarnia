package org.netmelody.jarnia.github;

import org.junit.Test;
import org.netmelody.jarnia.github.GithubFetcher.FileRef;

import com.google.common.collect.Iterables;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;


public final class GithubFetcherTest {
    
    private final GithubFetcher fetcher = new GithubFetcher();

    @Test public void
    canFetchLatestSha() {
        final String sha = fetcher.fetchLatestShaFor("netmelody", "ci-eye", "master");
        assertThat(sha.length(), is(40));
    }
    
    @Test public void
    canFetchFileRefs() {
        final Iterable<FileRef> fileRefs = fetcher.fetchFilesFor("netmelody", "ci-eye", "af8ae09d84624c4fa69c7d405cf254748b8ef152");
        assertThat(Iterables.size(fileRefs), is(228));
    }
}
