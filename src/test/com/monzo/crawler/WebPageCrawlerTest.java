package com.monzo.crawler;

import io.reactivex.Observable;
import io.reactivex.observers.TestObserver;
import okhttp3.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.util.function.Consumer;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class WebPageCrawlerTest {

    private static final String MULTIPLE_LINKS = "<head><body>" +
            "<a class=\"c-header__link\" href=\"/about\">About</a></body>" +
            "<a class=\"c-header__link\" href=\"/contact\">Contact</a></body>" +
            "<a class=\"c-header__link\" href=\"/blog\">Blog</a></body>" +
            "</head>";

    private static final String DUPLICATE_LINKS = "<head><body>" +
            "<a class=\"c-header__link\" href=\"/about\">About</a></body>" +
            "<a class=\"c-header__link\" href=\"/contact\">Contact</a></body>" +
            "<a class=\"c-header__link\" href=\"/about\">About</a></body>" +
            "</head>";

    private static final String NON_MONZO_LINKS = "<head><body>" +
            "<a class=\"c-header__link\" href=\"/about\">About</a></body>" +
            "<a class=\"c-header__link\" href=\"https://play.google.com/store/apps/\">Google play</a></body>" +
            "</head>";

    private static final String NON_HREF_LINKS = "<head><body>" +
            "<img src=\"/static/images/some_test_img.png\" alt=\"\">" +
            "</head>";
    private static final String MONZO_COM = "https://monzo.com";
    private static final String MONZO_ABOUT = "https://monzo.com/about";
    private static final String MONZO_CONTACT = "https://monzo.com/contact";
    private static final String MONZO_BLOG = "https://monzo.com/blog";

    @Mock
    private Call call;

    @Mock
    private OkHttpClient okHttpClient;

    @Mock
    private Consumer<HttpUrl> httpUrlConsumer;

    private WebPageCrawler underTest;

    @Before
    public void setUp() throws Exception {
        when(okHttpClient.newCall(any(Request.class))).thenReturn(call);
        underTest = new WebPageCrawler(okHttpClient, "monzo.com");
    }

    @Test
    public void testItCrawlsWebPage() throws Exception {
        // given
        mockResponse(MULTIPLE_LINKS);
        TestObserver<HttpUrl> testSubscriber = new TestObserver<>();

        // when
        Observable<HttpUrl> httpUrlObservable = underTest.crawlWebPage(HttpUrl.parse(MONZO_COM), httpUrlConsumer);
        httpUrlObservable.subscribe(testSubscriber);

        // then
        testSubscriber.assertComplete();

        verify(httpUrlConsumer).accept(HttpUrl.parse(MONZO_ABOUT));
        verify(httpUrlConsumer).accept(HttpUrl.parse(MONZO_CONTACT));
        verify(httpUrlConsumer).accept(HttpUrl.parse(MONZO_BLOG));
    }

    @Test
    public void testItCrawlsWebPageButIgnoresDuplicates() throws Exception {
        // given
        mockResponse(DUPLICATE_LINKS);
        TestObserver<HttpUrl> testSubscriber = new TestObserver<>();

        // when
        Observable<HttpUrl> httpUrlObservable = underTest.crawlWebPage(HttpUrl.parse(MONZO_COM), httpUrlConsumer);
        httpUrlObservable.subscribe(testSubscriber);

        // then
        testSubscriber.assertComplete();

        verify(httpUrlConsumer).accept(HttpUrl.parse(MONZO_ABOUT));
        verify(httpUrlConsumer).accept(HttpUrl.parse(MONZO_CONTACT));
        verifyNoMoreInteractions(httpUrlConsumer);
    }

    @Test
    public void testItCrawlsWebPageButIgnoresNonMonzoPages() throws Exception {
        // given
        mockResponse(NON_MONZO_LINKS);
        TestObserver<HttpUrl> testSubscriber = new TestObserver<>();

        // when
        Observable<HttpUrl> httpUrlObservable = underTest.crawlWebPage(HttpUrl.parse(MONZO_COM), httpUrlConsumer);
        httpUrlObservable.subscribe(testSubscriber);

        // then
        testSubscriber.assertComplete();

        verify(httpUrlConsumer).accept(HttpUrl.parse(MONZO_ABOUT));
        verifyNoMoreInteractions(httpUrlConsumer);
    }

    @Test
    public void testItDoesNotProcessNonHrefElements() throws Exception {
        // given
        mockResponse(NON_HREF_LINKS);
        TestObserver<HttpUrl> testSubscriber = new TestObserver<>();

        // when
        Observable<HttpUrl> httpUrlObservable = underTest.crawlWebPage(HttpUrl.parse(MONZO_COM), httpUrlConsumer);
        httpUrlObservable.subscribe(testSubscriber);

        // then
        testSubscriber.assertComplete();

        verifyNoMoreInteractions(httpUrlConsumer);
    }

    @Test
    public void testItCallsOnError() throws Exception {
        // given
        final IOException error = new IOException("Some error");
        when(call.execute()).thenThrow(error);
        TestObserver<HttpUrl> testSubscriber = new TestObserver<>();

        // when
        Observable<HttpUrl> httpUrlObservable = underTest.crawlWebPage(HttpUrl.parse(MONZO_COM), httpUrlConsumer);
        httpUrlObservable.subscribe(testSubscriber);

        // then
        testSubscriber.assertError(error);

        verifyNoMoreInteractions(httpUrlConsumer);
    }

    private void mockResponse(String html) throws IOException {
        ResponseBody body = ResponseBody.create(MediaType.parse("text/html"), html);
        Request build = new Request.Builder().url(MONZO_COM).build();
        Response response = new Response.Builder()
                .code(200)
                .message("")
                .protocol(Protocol.HTTP_2)
                .request(build)
                .body(body)
                .build();

        when(call.execute()).thenReturn(response);
    }


}