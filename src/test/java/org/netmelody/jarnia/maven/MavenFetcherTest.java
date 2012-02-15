package org.netmelody.jarnia.maven;

import org.junit.Test;

public final class MavenFetcherTest {

    private final MavenFetcher fetcher = new MavenFetcher();

    @Test public void
    canFetchLatestSha() {
        final String ver = fetcher.fetchLatestVersionOf("guava-11.0.1.jar");
        
//        assertThat(sha.length(), is(40));
    }
}
