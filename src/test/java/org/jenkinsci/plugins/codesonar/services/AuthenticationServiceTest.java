package org.jenkinsci.plugins.codesonar.services;

import hudson.AbortException;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.net.URI;
import java.security.KeyStore;

public class AuthenticationServiceTest extends Mockito {

    @Mock
    private AuthenticationService authenticationService;

    @Mock
    private HttpService httpService;

    @Mock
    private HttpClient httpClient;

    @Mock
    private HttpGet httpGet;

    @Mock
    private HttpResponse httpResponse;

    @Mock
    private StatusLine statusLine;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testKeystoreLogin(){
        try {
            when(this.statusLine.getStatusCode()).thenReturn(HttpStatus.SC_FORBIDDEN);
            when(this.httpResponse.getStatusLine()).thenReturn(statusLine);
            this.authenticationService.authenticate(any(URI.class), any(KeyStore.class), anyString());
        } catch (Exception e) {
            Assert.assertTrue(e instanceof IOException);
            Assert.assertEquals(
                e.getMessage(),
         "[CodeSonar] failed to authenticate. %n[CodeSonar] HTTP status code: " + HttpStatus.SC_FORBIDDEN);
        }
    }

    @Test
    public void testSuccessfulAuthenticateUserPass(){
        when(this.statusLine.getStatusCode()).thenReturn(HttpStatus.SC_OK);
        when(this.httpResponse.getStatusLine()).thenReturn(statusLine);

        try {
            when(this.httpClient.execute(httpGet)).thenReturn(httpResponse);
            this.authenticationService.authenticate(anyObject(), anyString(), anyString());
            StatusLine statusLine = this.httpResponse.getStatusLine();
            Assert.assertEquals(statusLine.getStatusCode(), HttpStatus.SC_OK);
        } catch(Exception ignored){
        }
    }

    @Test
    public void testUnsuccessfulAuthenticateUserPass(){
        when(this.statusLine.getStatusCode()).thenReturn(HttpStatus.SC_FORBIDDEN);
        when(this.httpResponse.getStatusLine()).thenReturn(statusLine);

        try {
            when(this.httpClient.execute(httpGet)).thenReturn(httpResponse);
            this.authenticationService.authenticate(anyObject(), anyString(), anyString());
        } catch(Exception e){
            Assert.assertTrue(e instanceof AbortException);
            Assert.assertEquals(e.getMessage(), "[CodeSonar] failed to authenticate");
        }
    }

    @Test
    public void testBadSignout() {
        when(this.statusLine.getStatusCode()).thenReturn(HttpStatus.SC_FORBIDDEN);
        when(this.httpResponse.getStatusLine()).thenReturn(statusLine);

        try {
            when(this.httpClient.execute(httpGet)).thenReturn(httpResponse);
            this.authenticationService.signOut(anyObject());
        } catch(Exception e){
            Assert.assertTrue(e instanceof AbortException);
            Assert.assertEquals(e.getMessage(), "[CodeSonar] Failed to sign out");
        }
    }

    @Test
    public void testSuccessfulSignout() {
        when(this.statusLine.getStatusCode()).thenReturn(HttpStatus.SC_OK);
        when(this.httpResponse.getStatusLine()).thenReturn(statusLine);

        try {
            when(this.httpClient.execute(httpGet)).thenReturn(httpResponse);
            this.authenticationService.signOut(anyObject());
        } catch(Exception ignored){
        }
    }
}
