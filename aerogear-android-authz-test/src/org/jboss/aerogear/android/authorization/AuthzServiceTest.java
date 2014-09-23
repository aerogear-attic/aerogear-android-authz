/**
 * JBoss, Home of Professional Open Source
 * Copyright Red Hat, Inc., and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.aerogear.android.authorization;

import com.google.gson.JsonObject;
import org.jboss.aerogear.android.datamanager.Store;
import org.jboss.aerogear.android.http.HeaderAndBody;
import org.jboss.aerogear.android.http.HttpProvider;
import org.jboss.aerogear.android.impl.authz.oauth2.OAuth2Properties;
import org.jboss.aerogear.android.impl.authz.AuthzService;
import org.jboss.aerogear.android.impl.authz.oauth2.OAuth2AuthorizationException;
import org.jboss.aerogear.android.impl.authz.oauth2.OAuth2AuthzSession;
import org.jboss.aerogear.android.impl.datamanager.MemoryStore;
import org.jboss.aerogear.android.impl.helper.UnitTestUtils;
import org.jboss.aerogear.android.impl.util.PatchedActivityInstrumentationTestCase;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.net.URL;
import java.util.Calendar;
import java.util.HashMap;

import static java.util.Calendar.HOUR;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AuthzServiceTest extends PatchedActivityInstrumentationTestCase<MainActivity> {

    private AuthzService service;
    private Store mockStore;
    private OAuth2AuthzSession account;
    private URL baseUrl;
    private HttpProvider mockProvider;

    public AuthzServiceTest() {
        super(MainActivity.class);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        mockStore = mock(MemoryStore.class);
        mockProvider = mock(HttpProvider.class);
        service = new AuthzService() {

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

    public void testFetchTokenReturnsNullForNoAccount() throws OAuth2AuthorizationException {
        assertEquals(null, service.fetchAccessToken("testAccount", new OAuth2Properties(null, null)));
    }

    public void testFetchTokenForFreshAccount() throws OAuth2AuthorizationException {
        account.setExpires_on(hourFromNow());
        when(mockStore.read(eq("testAccountId"))).thenReturn(account);

        assertEquals("testToken", service.fetchAccessToken("testAccountId", new OAuth2Properties(null, null)));
    }

    public void testExchangeToken() {

    }

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

        assertEquals("testRefreshedAccessToken", service.fetchAccessToken("testAccountId", new OAuth2Properties(baseUrl, null)));
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
