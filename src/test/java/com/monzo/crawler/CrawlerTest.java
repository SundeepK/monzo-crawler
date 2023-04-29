package com.monzo.crawler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.URL;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CrawlerTest {

    @Mock
    ExecutorService service;

    @Mock
    WebPageCrawler webPageCrawler;

    @Mock
    ExecutorFactory executorFactory;

    Crawler underTest;

    @BeforeEach
    void setUp() {
        underTest = new Crawler(webPageCrawler, executorFactory);
    }

    @Test
    public void testItCrawls() throws Exception {
        // given
        var urls = List.of(new URL("https://monzo.com/aboutus"), new URL("https://monzo.com/other"));
        var urls2 = List.of(new URL("https://monzo.com/contact"));
        when(webPageCrawler
                .crawlWebPage(any(), any(), any()))
                .thenReturn(CompletableFuture.completedFuture(urls),
                        CompletableFuture.completedFuture(urls2),
                        CompletableFuture.completedFuture(List.of()));
        when(executorFactory.getExecutor()).thenReturn(service);
        // when
        var actualUrls = underTest.crawl(new URL("https://monzo.com"));

        // then
        var expectedUrls = Stream.of(List.of(new URL("https://monzo.com")), urls, urls2)
                .flatMap(List::stream)
                .collect(Collectors.toList());
        assertEquals(expectedUrls, actualUrls);
        verify(executorFactory, times(1)).getExecutor();
        verify(service, times(1)).shutdown();
    }

}
