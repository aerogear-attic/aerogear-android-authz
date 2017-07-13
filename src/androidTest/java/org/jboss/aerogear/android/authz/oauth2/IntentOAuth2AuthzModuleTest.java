/*
 * Copyright 2015 JBoss by Red Hat.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.aerogear.android.authz.oauth2;

import android.app.Activity;
import android.os.Looper;
import android.support.test.runner.AndroidJUnit4;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;

import org.jboss.aerogear.android.core.Callback;
import org.jboss.aerogear.android.authorization.AuthorizationManager;
import org.jboss.aerogear.android.authorization.AuthzModule;
import org.jboss.aerogear.android.authorization.oauth2.OAuth2AuthorizationConfiguration;
import org.jboss.aerogear.android.authorization.oauth2.OAuth2AuthzService;
import org.jboss.aerogear.android.authorization.oauth2.OAuth2AuthzSession;
import org.jboss.aerogear.android.authorization.oauth2.OAuth2Properties;
import org.jboss.aerogear.android.authorization.oauth2.intent.OAuth2IntentAuthzModule;
import org.jboss.aerogear.android.authorization.test.MainActivity;
import org.jboss.aerogear.android.authorization.test.util.PatchedActivityInstrumentationTestCase;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.mockito.Matchers.any;
import org.mockito.Mockito;
import static org.mockito.Mockito.mock;


/**
 * Tests the {@link OAuth2IntentAuthzModule} class.
 */
@RunWith(AndroidJUnit4.class)
public class IntentOAuth2AuthzModuleTest extends PatchedActivityInstrumentationTestCase {

    private static final URL BASE_URL;

    static {
        try {
            BASE_URL = new URL("https://example.com");
        } catch (MalformedURLException ex) {
            throw new RuntimeException(ex);
        }
    }
    public IntentOAuth2AuthzModuleTest() {
        super(MainActivity.class);
    }
    
    /**
     * If the OAuth2 Config Object has withIntent set then asModule should return
     * a OAuth2IntentAuthzModule instance.
     */
    @Test
    public void testWithIntentCreatesIntentModule() {
        OAuth2AuthorizationConfiguration config = AuthorizationManager.config("name", OAuth2AuthorizationConfiguration.class);
        config.setBaseURL(BASE_URL);
        config.setWithIntent(true);

        AuthzModule module = config.asModule();

        Assert.assertTrue(module instanceof OAuth2IntentAuthzModule);
        
    }
    
    /**
     * If the OAuth2 Config Object has withIntent set then asModule should return
     * a OAuth2IntentAuthzModule instance.
     */
    @Test
    public void testIntentPutsAccountOnModule() throws Exception {
        OAuth2AuthorizationConfiguration config = AuthorizationManager.config("name", OAuth2AuthorizationConfiguration.class);
        config.setBaseURL(BASE_URL);
        config.setAccountId("ignore");
        config.setWithIntent(true);
        AuthzModule module = config.asModule();
        Assert.assertNull(MainActivity.UnitTestUtils.getSuperPrivateField(module, "account"));
//        /String state, Activity activity, Callback<String> callback, OAuth2AuthzService.AGAuthzServiceConnection instance
        Method doRequestAccessMethod = OAuth2IntentAuthzModule.class.getDeclaredMethod("doRequestAccess", String.class, Activity.class, Callback.class, OAuth2AuthzService.AGAuthzServiceConnection.class);
        OAuth2AuthzService.AGAuthzServiceConnection mockConnection = mock(OAuth2AuthzService.AGAuthzServiceConnection.class);
        OAuth2AuthzService mockService = mock(OAuth2AuthzService.class);
        
        Mockito.doReturn(mock(OAuth2AuthzSession.class)).when(mockService).getAccount(any(String.class));
        Mockito.doReturn(true).when(mockService).hasAccount(any(String.class));
        Mockito.doReturn("testToken").when(mockService).fetchAccessToken(any(String.class), any(OAuth2Properties.class));
        
        Mockito.doReturn(mockService).when(mockConnection).getService();
        Looper.prepare();
        
        doRequestAccessMethod.setAccessible(true);
        doRequestAccessMethod.invoke(module, "ignore", getActivity(), mock(Callback.class), mockConnection);
        Assert.assertNotNull(MainActivity.UnitTestUtils.getSuperPrivateField(module, "account"));
        
    }
    
}
