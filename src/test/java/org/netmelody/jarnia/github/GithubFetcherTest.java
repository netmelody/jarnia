package org.netmelody.jarnia.github;

import org.junit.Test;


public final class GithubFetcherTest {

    @Test public void
    canFetch() {
        GithubFetcher fetcher = new GithubFetcher();
        String result = fetcher.fetchLatestShaFor();
        System.out.println(result);
    }
}
