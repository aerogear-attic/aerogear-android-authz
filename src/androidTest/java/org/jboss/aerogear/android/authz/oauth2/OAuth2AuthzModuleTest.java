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
package org.jboss.aerogear.android.authz.oauth2;

import android.app.Activity;
import android.content.Intent;
import android.content.ServiceConnection;
import android.support.test.annotation.UiThreadTest;
import android.support.test.runner.AndroidJUnit4;

import org.jboss.aerogear.android.authorization.AuthorizationManager;
import org.jboss.aerogear.android.authorization.oauth2.OAuth2AuthorizationConfiguration;
import org.jboss.aerogear.android.authorization.oauth2.OAuth2AuthorizationException;
import org.jboss.aerogear.android.authorization.oauth2.OAuth2AuthzModule;
import org.jboss.aerogear.android.authorization.oauth2.OAuth2AuthzService;
import org.jboss.aerogear.android.authorization.oauth2.OAuth2AuthzSession;
import org.jboss.aerogear.android.authorization.oauth2.webview.OAuth2WebViewAuthzModule;
import org.jboss.aerogear.android.authorization.test.MainActivity;
import org.jboss.aerogear.android.authorization.test.util.VoidCallback;
import org.jboss.aerogear.android.authz.util.PatchedActivityInstrumentationTestCase;
import org.jboss.aerogear.android.core.Callback;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.matches;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

@RunWith(AndroidJUnit4.class)
public class OAuth2AuthzModuleTest extends PatchedActivityInstrumentationTestCase {

    private static final URL BASE_URL;

    static {
        try {
            BASE_URL = new URL("https://example.com");
        } catch (MalformedURLException ex) {
            throw new RuntimeException(ex);
        }
    }

    public OAuth2AuthzModuleTest() {
        super(MainActivity.class);
    }

    @Test(expected = IllegalStateException.class)
    public void testRequiresBaseURL() {

            OAuth2AuthorizationConfiguration config = AuthorizationManager.config("name", OAuth2AuthorizationConfiguration.class);
            OAuth2AuthzModule module = (OAuth2AuthzModule) config.asModule();


    }

    @Test
    public void testCreation() throws MalformedURLException {
        OAuth2AuthorizationConfiguration config = AuthorizationManager.config("name", OAuth2AuthorizationConfiguration.class);
        config.setBaseURL(BASE_URL);

        OAuth2AuthzModule module = (OAuth2AuthzModule) config.asModule();

        Assert.assertFalse(module.isAuthorized());
        Assert.assertEquals(module, AuthorizationManager.getModule("name"));
    }

    @Test
    public void testRequestAccess() {
        OAuth2AuthorizationConfiguration config = AuthorizationManager.config("name", OAuth2AuthorizationConfiguration.class);
        config.setBaseURL(BASE_URL);
        OAuth2AuthzModule module = (OAuth2AuthzModule) config.asModule();

        Activity mockActivity = mock(Activity.class);
        Callback mockCallback = mock(Callback.class);
        when(mockActivity.bindService(any(Intent.class), any(ServiceConnection.class), any(Integer.class))).thenReturn(Boolean.TRUE);
        when(mockActivity.getApplicationContext()).thenReturn(super.getActivity());
        module.requestAccess(mockActivity, mockCallback);

        Mockito.verify(mockActivity, times(1)).bindService((Intent) any(), (ServiceConnection) any(), any(Integer.class));

    }

    @Test
    public void testGetAccessTokens() throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {

        OAuth2AuthorizationConfiguration config = AuthorizationManager.config("name", OAuth2AuthorizationConfiguration.class);
        config.setBaseURL(BASE_URL);
        OAuth2AuthzModule module = (OAuth2AuthzModule) config.asModule();

        OAuth2AuthzSession account = new OAuth2AuthzSession();
        account.setAccessToken("testToken");

        MainActivity.UnitTestUtils.setPrivateField(module, "account", account);

        Assert.assertEquals("Bearer testToken", module.getAuthorizationFields(null, null, null).getHeaders().get(0).second);
        Assert.assertEquals("Authorization", module.getAuthorizationFields(null, null, null).getHeaders().get(0).first);
    }

    @UiThreadTest
    public void testOAuth2AuthorizationCallback() throws ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException, NoSuchFieldException, OAuth2AuthorizationException, InterruptedException {

        OAuth2AuthzService mockService = mock(OAuth2AuthzService.class);
        Activity mockActivity = mock(Activity.class);
        ServiceConnection mockServiceConnection = mock(ServiceConnection.class);

        ArgumentCaptor<OAuth2AuthzSession> sessionCaptor = ArgumentCaptor.forClass(OAuth2AuthzSession.class);

        OAuth2AuthorizationConfiguration config = AuthorizationManager.config("name", OAuth2AuthorizationConfiguration.class);
        config.setAccountId("testAccountId");
        config.setBaseURL(BASE_URL);

        OAuth2AuthzModule module = (OAuth2AuthzModule) config.asModule();
        Class<?> callbackClass = Class.forName("org.jboss.aerogear.android.authorization.oauth2.webview.OAuth2WebViewAuthzModule$OAuth2AuthorizationCallback");
        Constructor<?> constructor = callbackClass.getDeclaredConstructor(OAuth2WebViewAuthzModule.class, Activity.class, Callback.class, ServiceConnection.class);
        constructor.setAccessible(true);

        Callback callback = (Callback) constructor.newInstance(module, mockActivity, new VoidCallback(), mockServiceConnection);

        MainActivity.UnitTestUtils.setPrivateField(module, "service", mockService);

        callback.onSuccess("testCode");

        Mockito.verify(mockService, times(1)).addAccount(sessionCaptor.capture());
        OAuth2AuthzSession account = sessionCaptor.getValue();
        Assert.assertEquals("testCode", account.getAuthorizationCode());
    }

    @UiThreadTest
    public void testOAuth2AccessCallback() throws ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException, NoSuchFieldException {
        OAuth2AuthzService mockService = mock(OAuth2AuthzService.class);
        Activity mockActivity = mock(Activity.class);
        ServiceConnection mockServiceConnection = mock(ServiceConnection.class);
        OAuth2AuthzSession account = new OAuth2AuthzSession();
        account.setAccessToken("testToken");

        when(mockService.getAccount(matches("testAccountId"))).thenReturn(account);

        OAuth2AuthorizationConfiguration config = AuthorizationManager.config("name", OAuth2AuthorizationConfiguration.class);
        config.setAccountId("testAccountId");
        config.setBaseURL(BASE_URL);

        OAuth2AuthzModule module = (OAuth2AuthzModule) config.asModule();
        Class<?> callbackClass = Class.forName("org.jboss.aerogear.android.authorization.oauth2.webview.OAuth2WebViewAuthzModule$OAuth2AccessCallback");
        Constructor<?> constructor = callbackClass.getDeclaredConstructor(OAuth2WebViewAuthzModule.class, Activity.class, Callback.class, ServiceConnection.class);
        constructor.setAccessible(true);

        Callback callback = (Callback) constructor.newInstance(module, mockActivity, new VoidCallback(), mockServiceConnection);

        MainActivity.UnitTestUtils.setPrivateField(module, "service", mockService);

        callback.onSuccess("testToken");

        Mockito.verify(mockActivity, times(1)).unbindService(eq(mockServiceConnection));
        Assert.assertEquals("testToken", ((OAuth2AuthzSession)MainActivity.UnitTestUtils.getSuperPrivateField(module, "account")).getAccessToken());
    }
}
