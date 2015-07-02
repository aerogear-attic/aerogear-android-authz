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
package org.jboss.aerogear.android.authorization.test.oauth2;

import android.support.test.runner.AndroidJUnit4;
import java.net.MalformedURLException;
import java.net.URL;
import org.jboss.aerogear.android.authorization.AuthorizationManager;
import org.jboss.aerogear.android.authorization.AuthzModule;
import org.jboss.aerogear.android.authorization.oauth2.OAuth2AuthorizationConfiguration;
import org.jboss.aerogear.android.authorization.oauth2.intent.OAuth2IntentAuthzModule;
import org.jboss.aerogear.android.authorization.test.MainActivity;
import org.jboss.aerogear.android.authorization.test.util.PatchedActivityInstrumentationTestCase;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

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
    
}
