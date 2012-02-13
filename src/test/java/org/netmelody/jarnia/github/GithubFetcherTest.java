package org.netmelody.jarnia.github;

import org.junit.Test;

import com.google.common.collect.Iterables;


public final class GithubFetcherTest {

    @Test public void
    canFetch() {
        GithubFetcher fetcher = new GithubFetcher();
        String sha = fetcher.fetchLatestShaFor("netmelody", "ci-eye", "master");
        System.out.println(Iterables.toString(fetcher.fetchFilesFor("netmelody", "ci-eye", sha)));
    }
}
