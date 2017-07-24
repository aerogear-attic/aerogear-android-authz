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
package org.jboss.aerogear.android.authorization.oauth2;

import android.app.Activity;
import android.net.Uri;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import org.jboss.aerogear.android.core.Callback;

/**
 * This class displays a WebView Dialog Fragment to facilitates exchanging
 * credentials for authz tokens.
 * 
 * @author summers
 */
public class OAuth2WebFragmentFetchAutorization {

    private final Activity activity;
    private final String state;

    public OAuth2WebFragmentFetchAutorization(Activity activity, String state) {
        this.activity = activity;
        this.state = state;
    }

    public void performAuthorization(OAuth2Properties config, final Callback<String> callback) {

        try {
            doAuthorization(config, callback);
        } catch (UnsupportedEncodingException ex) {
            callback.onFailure(ex);
        } catch (MalformedURLException ex) {
            callback.onFailure(ex);
        }

    }

     private void doAuthorization(OAuth2Properties config, final Callback<String> callback) throws UnsupportedEncodingException, MalformedURLException {

        URL baseURL = config.getBaseURL();
        Uri redirectURL = Uri.parse(config.getRedirectURL());
        
        URL authzURL = OAuth2Utils.buildAuthzURL(config, state);

        final OAuthWebViewDialog dialog = OAuthWebViewDialog.newInstance(authzURL, redirectURL);
        dialog.setReceiver(new OAuthWebViewDialog.OAuthReceiver() {
            @Override
            public void receiveOAuthCode(String code) {
                dialog.removeReceive();
                dialog.dismiss();
                callback.onSuccess(code);
            }

            @Override
            public void receiveOAuthError(final String error) {
                dialog.removeReceive();
                dialog.dismiss();
                callback.onFailure(new OAuth2AuthorizationException(error));
            }
        });

        dialog.setStyle(android.R.style.Theme_Light_NoTitleBar, 0);
        dialog.show(activity.getFragmentManager(), "TAG");
    }


}
