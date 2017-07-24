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
import android.content.IntentFilter;
import android.util.Log;
import java.net.HttpURLConnection;

import java.net.URI;

import org.jboss.aerogear.android.core.Callback;
import org.jboss.aerogear.android.pipe.module.AuthorizationFields;
import org.jboss.aerogear.android.authorization.AuthzModule;
import org.jboss.aerogear.android.pipe.module.ModuleFields;
import org.jboss.aerogear.android.pipe.http.HttpException;

/**
 * 
 * An Authorization module which works with the OAuth2 protocol.
 * 
 * Authorization is performed in a WebView and returned to the calling activity.
 * 
 */
public abstract class OAuth2AuthzModule implements AuthzModule {

    private static final IntentFilter AUTHZ_FILTER;

    protected final String accountId;
    protected final String clientId;
    protected final OAuth2Properties config;
    protected OAuth2AuthzSession account;
    protected OAuth2AuthzService service;

    static {
        AUTHZ_FILTER = new IntentFilter();
        AUTHZ_FILTER.addAction("org.jboss.aerogear.android.authz.RECEIVE_AUTHZ");
    }
    public final String TAG = OAuth2AuthzModule.class.getSimpleName();

    public OAuth2AuthzModule(OAuth2Properties config) {
        this.clientId = config.getClientId();
        this.accountId = config.getAccountId();
        this.config = config;
    }

    @Override
    public final boolean isAuthorized() {

        if (account == null) {
            return false;
        }

        return account.tokenIsNotExpired() && !isNullOrEmpty(account.getAccessToken());
    }

    @Override
    public final boolean hasCredentials() {

        if (account == null) {
            return false;
        }

        return !isNullOrEmpty(account.getAccessToken());
    }

    @Override
    public abstract void requestAccess(final Activity activity, final Callback<String> callback);

    @Override
    public final AuthorizationFields getAuthorizationFields(URI requestUri, String method, byte[] requestBody) {
        AuthorizationFields fields = new AuthorizationFields();

        fields.addHeader("Authorization", "Bearer " + account.getAccessToken());

        return fields;
    }


    @Override
    public final boolean refreshAccess() {

        if (!hasAccount()) {
            return false;
        } else {

            if (isAuthorized()) {
                return true;
            }

            try {
                service.fetchAccessToken(accountId, config);
                account = service.getAccount(accountId);
                Log.d(TAG, "Access token refresh complete!");
                return true;
            } catch (OAuth2AuthorizationException ex) {
                Log.e(TAG, ex.getMessage(), ex);
                return false;
            }
        }

    }

    /**
     * 
     * @return true if accountId has a value AND that value is stored in the
     *         OAuth2AuthzService
     */
    protected boolean hasAccount() {
        return (!isNullOrEmpty(accountId) && service.hasAccount(accountId));
    }

    @Override
    public final ModuleFields loadModule(URI relativeURI, String httpMethod, byte[] requestBody) {
        AuthorizationFields authzFields = getAuthorizationFields(relativeURI, httpMethod, requestBody);
        ModuleFields moduleFields = new ModuleFields();
        moduleFields.setHeaders(authzFields.getHeaders());
        moduleFields.setQueryParameters(authzFields.getQueryParameters());
        return moduleFields;
    }

    @Override
    /**
     * Will refresh the access token if the exception status is UNAUTHORIZED or
     * FORBIDDED.
     *
     * @return true if the token was refreshed. False if the token could not be
     * refreshed or if the status wasn't of UNAUTHORIZED or FORBIDDEN.
     */
    public final boolean handleError(HttpException exception) {
        int statusCode = exception.getStatusCode();

        if (statusCode == HttpURLConnection.HTTP_UNAUTHORIZED
                || statusCode == HttpURLConnection.HTTP_FORBIDDEN) {
            return refreshAccess() && isAuthorized();
        } else {
            return false;
        }
    }

    @Override
    public final void deleteAccount() {
        service.removeAccount(accountId);
        removeAccount();
    }

    protected boolean isNullOrEmpty(String testString) {
        return testString == null || testString.isEmpty();
    }
    
    /**
     * Sets the account used in the module.
     * 
     * @param account a new account to use
     */
    protected void setAccount(OAuth2AuthzSession account) {
        this.account = account;
    }

    /**
     * Removes the account used in the module.
     *
     */
    protected void removeAccount() {
        this.account = null;
    }

}
