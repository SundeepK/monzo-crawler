package com.monzo.crawler;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.URL;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;

import static java.util.concurrent.Executors.newFixedThreadPool;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class WebPageCrawlerTest {

    @Mock
    DefaultHttpClient defaultHttpClient;

    @Mock
    HttpResponse<String> httpResponse;

    private static final String MULTIPLE_LINKS = "<head><body>" +
            "<a class=\"c-header__link\" href=\"/about\">About</a>" +
            "<a class=\"c-header__link\" href=\"/contact\">Contact</a>" +
            "<a class=\"c-header__link\" href=\"/blog\">Blog</a>" +
            "</body>" +
            "</head>";

    private static final String BAD_DOC = "<head><body>" +
            ">";

    private static final String DUPLICATE_LINKS = "<head><body>" +
            "<a class=\"c-header__link\" href=\"/about\">About</a>" +
            "<a class=\"c-header__link\" href=\"/contact\">Contact</a>" +
            "<a class=\"c-header__link\" href=\"/about\">About</a>" +
            "</body>" +
            "</head>";

    private static final String NON_MONZO_LINKS = "<head><body>" +
            "<a class=\"c-header__link\" href=\"/about\">About</a>" +
            "<a class=\"c-header__link\" href=\"https://play.google.com/store/apps/\">Google play</a>" +
            "</body>" +
            "</head>";

    private static final String NON_HREF_LINKS = "<head><body>" +
            "<img src=\"/static/images/some_test_img.png\" alt=\"\">" +
            "<a class=\"c-header__link\" href=\"#/about\">About</a>" +
            "<a href=\"mailto:someone@yoursite.com\">Email Us</a>  \n" +
            "</body>" +
            "</head>";


    private URL monzoCom;
    private URL monzoAbout;
    private URL monzoContact;
    private URL monzoBlog;
    private ExecutorService executor;
    private WebPageCrawler underTest;

    @BeforeEach
    void setUp() throws Exception {
        underTest = new WebPageCrawler(defaultHttpClient, "monzo.com");
        monzoCom = new URL("https://monzo.com");
        monzoAbout = new URL("https://monzo.com/about");
        monzoContact = new URL("https://monzo.com/contact");
        monzoBlog = new URL("https://monzo.com/blog");
        executor = newFixedThreadPool(10);
    }

    @AfterEach
    void cleanUp(){
        executor.shutdown();
    }

    @Test
    public void testItCrawlsWebPage() throws Exception {
        // given
        when(defaultHttpClient.fetchWebPage(monzoCom.toString()))
                .thenReturn(Optional.of(MULTIPLE_LINKS));

        // when
        ExecutorService executor = newFixedThreadPool(10);
        var urls = underTest.crawlWebPage(monzoCom, (url) ->{}, executor).get();

        // then
        assertEquals(List.of(monzoAbout, monzoContact, monzoBlog), urls);
    }

    @Test
    public void testItIgnoresBadDoc() throws Exception {
        // given
        when(defaultHttpClient.fetchWebPage(monzoCom.toString()))
                .thenReturn(Optional.of(BAD_DOC));

        // when
        ExecutorService executor = newFixedThreadPool(10);
        var urls = underTest.crawlWebPage(monzoCom, (url) ->{}, executor).get();

        // then
        assertEquals(List.of(), urls);
    }

    @Test
    public void testItCrawlsWebPageButIgnoresDuplicates() throws Exception {
        // given
        when(defaultHttpClient.fetchWebPage(monzoCom.toString()))
                .thenReturn(Optional.of(DUPLICATE_LINKS));

        // when
        ExecutorService executor = newFixedThreadPool(10);
        var urls = underTest.crawlWebPage(monzoCom, (url) ->{}, executor).get();

        // then
        assertEquals(List.of(monzoAbout, monzoContact), urls);
    }

    @Test
    public void testItCrawlsWebPageButIgnoresNonMonzoPages() throws Exception {
        // given
        when(defaultHttpClient.fetchWebPage(monzoCom.toString()))
                .thenReturn(Optional.of(NON_MONZO_LINKS));

        // when
        ExecutorService executor = newFixedThreadPool(10);
        var urls = underTest.crawlWebPage(monzoCom, (url) ->{}, executor).get();

        // then
        assertEquals(List.of(monzoAbout), urls);
    }

    @Test
    public void testItIgnoresNonCrawableLinks() throws Exception {
        // given
        when(defaultHttpClient.fetchWebPage(monzoCom.toString()))
                .thenReturn(Optional.of(NON_HREF_LINKS));

        // when
        ExecutorService executor = newFixedThreadPool(10);
        var urls = underTest.crawlWebPage(monzoCom, (url) ->{}, executor).get();

        // then
        assertEquals(List.of(), urls);
    }

}
