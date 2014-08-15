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
package org.jboss.aerogear.android.impl.authz.oauth2;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import com.google.common.base.Strings;

import java.net.URI;
import java.util.UUID;
import org.apache.http.HttpStatus;

import org.jboss.aerogear.android.Callback;
import org.jboss.aerogear.android.authentication.AuthorizationFields;
import org.jboss.aerogear.android.impl.authz.AuthzConfig;
import org.jboss.aerogear.android.authorization.AuthzModule;
import org.jboss.aerogear.android.code.ModuleFields;
import org.jboss.aerogear.android.http.HttpException;
import org.jboss.aerogear.android.impl.authz.AuthzService;
import org.jboss.aerogear.android.impl.authz.OAuth2AuthorizationException;

/**
 *
 * An Authorization module which works with the OAuth2 protocol.
 *
 * Authorization is performed in a WebView and returned to the calling activity.
 *
 */
public class OAuth2AuthzModule implements AuthzModule {

    private static final IntentFilter AUTHZ_FILTER;

    private final String accountId;
    private final String clientId;
    private final AuthzConfig config;
    private OAuth2AuthzSession account;
    private AuthzService service;

    static {
        AUTHZ_FILTER = new IntentFilter();
        AUTHZ_FILTER.addAction("org.jboss.aerogear.android.authz.RECEIVE_AUTHZ");
    }
    private String TAG = OAuth2AuthzModule.class.getSimpleName();

    public OAuth2AuthzModule(AuthzConfig config) {
        this.clientId = config.getClientId();
        this.accountId = config.getAccountId();
        this.config = config;

    }

    @Override
    public boolean isAuthorized() {

        if (account == null) {
            return false;
        }

        return account.tokenIsNotExpired() && !Strings.isNullOrEmpty(account.getAccessToken());
    }

    public boolean hasCredentials() {

        if (account == null) {
            return false;
        }

        return !Strings.isNullOrEmpty(account.getAccessToken());
    }

    @Override
    public void requestAccess(final Activity activity, final Callback<String> callback) {

        final String state = UUID.randomUUID().toString();

        final AuthzService.AGAuthzServiceConnection connection = new AuthzService.AGAuthzServiceConnection() {

            @Override
            public void onServiceConnected(ComponentName className, IBinder iBinder) {
                super.onServiceConnected(className, iBinder);
                doRequestAccess(state, activity, callback, this);
            }

        };

        activity.bindService(
                new Intent(activity.getApplicationContext(), AuthzService.class
                ), connection, Context.BIND_AUTO_CREATE
        );

    }

    @Override
    public AuthorizationFields getAuthorizationFields(URI requestUri, String method, byte[] requestBody) {
        AuthorizationFields fields = new AuthorizationFields();

        fields.addHeader("Authorization", "Bearer " + account.getAccessToken());

        return fields;
    }

    private void doRequestAccess(final String state, final Activity activity, final Callback<String> callback, final AuthzService.AGAuthzServiceConnection instance) {

        service = instance.getService();

        if (Strings.isNullOrEmpty(accountId)) {
            throw new IllegalArgumentException("need to have accountId set");
        }

        if (!service.hasAccount(accountId)) {

            OAuth2WebFragmentFetchAutorization authzFetch = new OAuth2WebFragmentFetchAutorization(activity, state);
            authzFetch.performAuthorization(config, new OAuth2AuthorizationCallback(activity, callback, instance));

        } else {

            OAuth2FetchAccess fetcher = new OAuth2FetchAccess(service);
            fetcher.fetchAccessCode(accountId, config, new OAuth2AccessCallback(activity, callback, instance));

        }

    }

    @Override
    public boolean refreshAccess() {

        if (!hasAccount()) {
            return false;
        } else {

            if (isAuthorized()) {
                return true;
            }

            try {
                account.setAccessToken(service.fetchAccessToken(accountId, config));
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
     * AuthzService
     */
    private boolean hasAccount() {
        return (!Strings.isNullOrEmpty(accountId) && service.hasAccount(accountId));
    }

    @Override
    public ModuleFields loadModule(URI relativeURI, String httpMethod, byte[] requestBody) {
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
    public boolean handleError(HttpException exception) {
        if (exception.getStatusCode() == HttpStatus.SC_UNAUTHORIZED
                || exception.getStatusCode() == HttpStatus.SC_FORBIDDEN) {
            return refreshAccess();
        } else {
            return false;
        }
    }

    private class OAuth2AccessCallback implements Callback<String> {

        private final Activity callingActivity;
        private final Callback<String> originalCallback;
        private final ServiceConnection serviceConnection;
        private final Handler myHandler;

        public OAuth2AccessCallback(Activity callingActivity, Callback<String> originalCallback, ServiceConnection serviceConnection) {
            this.callingActivity = callingActivity;
            this.originalCallback = originalCallback;
            this.serviceConnection = serviceConnection;
            myHandler = new Handler(Looper.myLooper());
        }

        @Override
        public void onSuccess(final String accessToken) {
            account = service.getAccount(accountId);
            callingActivity.unbindService(serviceConnection);
            myHandler.post(new Runnable() {
                @Override
                public void run() {
                    originalCallback.onSuccess(accessToken);
                }
            });
        }

        @Override
        public void onFailure(final Exception e) {
            myHandler.post(new Runnable() {
                @Override
                public void run() {
                    callingActivity.unbindService(serviceConnection);
                    originalCallback.onFailure(e);
                }
            });
        }
    }

    private class OAuth2AuthorizationCallback implements Callback<String> {

        private final Activity callingActivity;
        private final Callback<String> originalCallback;
        private final ServiceConnection serviceConnection;
        private final Handler myHandler;

        public OAuth2AuthorizationCallback(Activity callingActivity, Callback<String> originalCallback, ServiceConnection serviceConnection) {
            this.callingActivity = callingActivity;
            this.originalCallback = originalCallback;
            this.serviceConnection = serviceConnection;
            myHandler = new Handler(Looper.myLooper());
        }

        @Override
        public void onSuccess(final String code) {
            OAuth2AuthzSession session = new OAuth2AuthzSession();
            session.setAuthorizationCode(code);
            session.setAccountId(accountId);
            session.setCliendId(clientId);
            service.addAccount(session);

            OAuth2FetchAccess fetcher = new OAuth2FetchAccess(service);
            fetcher.fetchAccessCode(accountId, config, new OAuth2AccessCallback(callingActivity, originalCallback, serviceConnection));
        }

        @Override
        public void onFailure(final Exception e) {
            myHandler.post(new Runnable() {
                @Override
                public void run() {
                    callingActivity.unbindService(serviceConnection);
                    originalCallback.onFailure(e);
                }
            });
        }
    }

}
