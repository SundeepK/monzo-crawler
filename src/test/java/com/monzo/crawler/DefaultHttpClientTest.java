package com.monzo.crawler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DefaultHttpClientTest {

    @Mock
    HttpClient client;

    @Mock
    HttpResponse<Object> httpResponse;

    @Mock
    HttpResponse<Object> httpResponse2;

    @Mock
    HttpHeaders httpHeaders;

    DefaultHttpClient underTest;

    @BeforeEach
    void setUp(){
        underTest = new DefaultHttpClient(client, 1);
    }

    @Test
    public void testItFetchesUrl() throws Exception {
        when(httpResponse.statusCode()).thenReturn(200);
        when(httpResponse.body()).thenReturn("hello world");
        when(client.send(any(), any())).thenReturn(httpResponse);

        var actualResponse = underTest.fetchWebPage("https://monzo.com").get();

        assertEquals("hello world", actualResponse);
    }

    @Test
    public void testItEncodesUrl() throws Exception {
        var req = HttpRequest.newBuilder(URI.create("https://monzo.com/some%20url%20that%20needs%20encoding"))
                .timeout(Duration.ofMinutes(2))
                .build();
        when(httpResponse.statusCode()).thenReturn(200);
        when(httpResponse.body()).thenReturn("hello world");
        when(client.send(any(), any())).thenReturn(httpResponse);

        var actualResponse = underTest.fetchWebPage("https://monzo.com/some url that needs encoding").get();

        verify(client, times(1)).send(eq(req), any());
        assertEquals("hello world", actualResponse);
    }

    @Test
    public void testItFailOnBadURL() {
        assertThrows(MalformedURLException.class, () -> underTest.fetchWebPage("https:monzo"), "MalformedURLException not thrown");
    }

    @Test
    public void testItRetriesOn500s() throws Exception {
        when(httpResponse.statusCode()).thenReturn(500);
        when(client.send(any(), any())).thenReturn(httpResponse);

        underTest.fetchWebPage("https://monzo.com");

        verify(client, times(5)).send(any(), any());
    }

    @Test
    public void testItRetriesAndSucceeds() throws Exception {
        when(httpResponse.statusCode()).thenReturn(500);
        when(httpResponse2.statusCode()).thenReturn(200);
        when(httpResponse2.body()).thenReturn("hello world");
        when(client.send(any(), any())).thenReturn(httpResponse, httpResponse, httpResponse, httpResponse, httpResponse2);

        var actualResponse = underTest.fetchWebPage("https://monzo.com").get();

        verify(client, times(5)).send(any(), any());
        assertEquals("hello world", actualResponse);
    }

    @Test
    public void testItFollowsRedirects() throws Exception {
        when(httpResponse.headers()).thenReturn(httpHeaders);
        when(httpHeaders.firstValue(anyString())).thenReturn(Optional.of("https://monzo.com/redirect"));
        when(httpResponse.statusCode()).thenReturn(301);
        when(httpResponse2.statusCode()).thenReturn(200);
        when(httpResponse2.body()).thenReturn("hello world");
        when(client.send(any(), any())).thenReturn(httpResponse, httpResponse, httpResponse, httpResponse, httpResponse2);

        var actualResponse = underTest.fetchWebPage("https://monzo.com").get();

        verify(client, times(5)).send(any(), any());
        assertEquals("hello world", actualResponse);
    }


}
