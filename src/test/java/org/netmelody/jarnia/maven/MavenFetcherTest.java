package org.netmelody.jarnia.maven;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public final class MavenFetcherTest {

    private final MavenFetcher fetcher = new MavenFetcher();

    @Test public void
    canFetchLatestVersion() {
        final String ver = fetcher.fetchLatestVersionOf("guava-10.0.1.jar");
        
        assertThat(ver, is("11.0.1"));
    }
}
