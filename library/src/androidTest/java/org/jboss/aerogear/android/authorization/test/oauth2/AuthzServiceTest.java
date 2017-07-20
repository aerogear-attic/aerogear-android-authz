/**
 * JBoss, Home of Professional Open Source Copyright Red Hat, Inc., and
 * individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.jboss.aerogear.android.authorization.test.oauth2;

import android.support.test.runner.AndroidJUnit4;

import com.google.gson.JsonObject;

import org.jboss.aerogear.android.authorization.oauth2.OAuth2AuthorizationException;
import org.jboss.aerogear.android.authorization.oauth2.OAuth2AuthzService;
import org.jboss.aerogear.android.authorization.oauth2.OAuth2AuthzSession;
import org.jboss.aerogear.android.authorization.oauth2.OAuth2Properties;
import org.jboss.aerogear.android.authorization.test.util.UnitTestUtils;
import org.jboss.aerogear.android.pipe.http.HeaderAndBody;
import org.jboss.aerogear.android.pipe.http.HttpException;
import org.jboss.aerogear.android.pipe.http.HttpProvider;
import org.jboss.aerogear.android.store.Store;
import org.jboss.aerogear.android.store.sql.SQLStore;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;
import java.util.HashMap;

import static java.util.Calendar.HOUR;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(AndroidJUnit4.class)
public class AuthzServiceTest {

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    private OAuth2AuthzService service;
    private Store mockStore;
    private OAuth2AuthzSession account;
    private URL baseUrl;
    private HttpProvider mockProvider;


    @Before
    public void setUp() throws Exception {
        mockStore = mock(SQLStore.class);
        mockProvider = mock(HttpProvider.class);
        service = new OAuth2AuthzService() {

            @Override
            protected HttpProvider getHttpProvider(URL url) {
                return mockProvider;
            }

        };
        UnitTestUtils.setPrivateField(service, "sessionStore", mockStore);

        account = new OAuth2AuthzSession();
        account.setAccessToken("testToken");
        account.setAccountId("testAccountId");
        account.setAuthorizationCode(null);
        account.setClientId("testClientId");
        account.setRefreshToken("testRefreshToken");

        baseUrl = new URL("http://example.com");

    }

    @Test
    public void testFetchTokenReturnsNullForNoAccount() throws OAuth2AuthorizationException {
        Assert.assertEquals(null, service.fetchAccessToken("testAccount", new OAuth2Properties(null, null)));
    }

    @Test
    public void testFetchTokenForFreshAccount() throws OAuth2AuthorizationException {
        account.setExpires_on(hourFromNow());
        when(mockStore.read(eq("testAccountId"))).thenReturn(account);

        Assert.assertEquals("testToken", service.fetchAccessToken("testAccountId", new OAuth2Properties(null, null)));
    }

 
    @Test
    public void testErrorJsonMessage() {
        account.setExpires_on(hourAgo());
        when(mockStore.read(eq("testAccountId"))).thenReturn(account);

        when(mockProvider.post((byte[]) any())).thenThrow(new HttpException("{\"error\":{\"message\":\"this is a message\"}}".getBytes(), HttpURLConnection.HTTP_BAD_REQUEST));
        try {
            service.fetchAccessToken("testAccountId", new OAuth2Properties(baseUrl, null));
        } catch (OAuth2AuthorizationException exception) {
            //I'm not using the test annotation or an exception rule here
            // because for some reason the android libs miss one of the classes
            // Junit needs for it.  Using stock JUnit causes the 
            // tests to not run.  Will have to research later.
            Assert.assertEquals("{\"message\":\"this is a message\"}", exception.error);
            return;
        }
        Assert.fail("Exception not thrown");
        
    }

    @Test
    public void testErrorStringMessage() {
        account.setExpires_on(hourAgo());
        when(mockStore.read(eq("testAccountId"))).thenReturn(account);

        when(mockProvider.post((byte[]) any())).thenThrow(new HttpException("{\"error\":\"this is a message\"}".getBytes(), HttpURLConnection.HTTP_BAD_REQUEST));
        try {
            service.fetchAccessToken("testAccountId", new OAuth2Properties(baseUrl, null));
        } catch (OAuth2AuthorizationException exception) {
            //I'm not using the test annotation or an exception rule here
            // because for some reason the android libs miss one of the classes
            // Junit needs for it.  Using stock JUnit causes the 
            // tests to not run.  Will have to research later.
            Assert.assertEquals("this is a message", exception.error);
            return;
        }
        Assert.fail("Exception not thrown");
        
    }


    @Test
    public void testRefreshToken() throws OAuth2AuthorizationException {
        account.setExpires_on(hourAgo());
        when(mockStore.read(eq("testAccountId"))).thenReturn(account);

        when(mockProvider.post((byte[]) any())).thenAnswer(new Answer<HeaderAndBody>() {

            @Override
            public HeaderAndBody answer(InvocationOnMock invocation) throws Throwable {

                JsonObject object = new JsonObject();
                object.addProperty("access_token", "testRefreshedAccessToken");
                object.addProperty("expires_in", 3600);
                object.addProperty("refresh_token", "testRefreshToken");

                return new HeaderAndBody(object.toString().getBytes(), new HashMap<String, Object>());
            }
        });

        Assert.assertEquals("testRefreshedAccessToken", service.fetchAccessToken("testAccountId", new OAuth2Properties(baseUrl, null)));
    }

    private long hourFromNow() {
        Calendar hourFromNow = Calendar.getInstance();
        hourFromNow.set(HOUR, hourFromNow.get(HOUR) + 1);
        return hourFromNow.getTimeInMillis();
    }

    private long hourAgo() {
        Calendar hourFromNow = Calendar.getInstance();
        hourFromNow.set(HOUR, hourFromNow.get(HOUR) - 1);
        return hourFromNow.getTimeInMillis();
    }

}
