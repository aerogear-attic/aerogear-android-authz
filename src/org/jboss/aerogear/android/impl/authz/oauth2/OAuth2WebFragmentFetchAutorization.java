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
package org.jboss.aerogear.android.impl.authz.oauth2;

import android.app.Activity;
import android.net.Uri;
import android.util.Pair;
import com.google.common.base.Charsets;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import org.jboss.aerogear.android.Callback;
import org.jboss.aerogear.android.impl.authz.AuthzConfig;
import org.jboss.aerogear.android.impl.authz.OAuth2AuthorizationException;
import static org.jboss.aerogear.android.impl.util.UrlUtils.appendToBaseURL;

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

    public void performAuthorization(AuthzConfig config, final Callback<String> callback) {

        try {
            doAuthorization(config, callback);
        } catch (UnsupportedEncodingException ex) {
            callback.onFailure(ex);
        } catch (MalformedURLException ex) {
            callback.onFailure(ex);
        }

    }

    private String formatScopes(ArrayList<String> scopes) throws UnsupportedEncodingException {

        StringBuilder scopeValue = new StringBuilder();
        String append = "";
        for (String scope : scopes) {
            scopeValue.append(append);
            scopeValue.append(URLEncoder.encode(scope, Charsets.UTF_8.name()));
            append = "+";
        }

        return scopeValue.toString();
    }

    private void doAuthorization(AuthzConfig config, final Callback<String> callback) throws UnsupportedEncodingException, MalformedURLException {

        URL baseURL = config.getBaseURL();
        URL authzEndpoint = appendToBaseURL(baseURL, config.getAuthzEndpoint());
        Uri redirectURL = Uri.parse(config.getRedirectURL());
        ArrayList<String> scopes = new ArrayList<String>(config.getScopes());
        String clientId = config.getClientId();

        String query = "?scope=%s&redirect_uri=%s&client_id=%s&state=%s&response_type=code";
        query = String.format(query, formatScopes(scopes),
                URLEncoder.encode(redirectURL.toString(), Charsets.UTF_8.name()),
                clientId, state);

        if (config.getAdditionalAuthorizationParams() != null
                && config.getAdditionalAuthorizationParams().size() > 0) {
            for (Pair<String, String> param : config.getAdditionalAuthorizationParams()) {
                query += String.format("&%s=%s", URLEncoder.encode(param.first, Charsets.UTF_8.name()), URLEncoder.encode(param.second, Charsets.UTF_8.name()));
            }
        }

        URL authURL = new URL(authzEndpoint.toString() + query);

        final OAuthWebViewDialog dialog = OAuthWebViewDialog.newInstance(authURL, redirectURL);
        dialog.setReceiver(new OAuthWebViewDialog.OAuthReceiver() {
            @Override
            public void receiveOAuthCode(String code) {
                dialog.dismiss();
                callback.onSuccess(code);
            }

            @Override
            public void receiveOAuthError(final String error) {
                dialog.dismiss();
                callback.onFailure(new OAuth2AuthorizationException(error));
            }
        });

        dialog.setStyle(android.R.style.Theme_Light_NoTitleBar, 0);
        dialog.show(activity.getFragmentManager(), "TAG");
    }

}
