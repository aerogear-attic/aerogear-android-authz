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
package org.jboss.aerogear.android.authorization.oauth2.webview;

import org.jboss.aerogear.android.authorization.oauth2.*;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import java.util.UUID;

import org.jboss.aerogear.android.core.Callback;

/**
 * 
 * An Authorization module which works with the OAuth2 protocol.
 * 
 * Authorization is performed in a WebView and returned to the calling activity.
 * 
 */
public class OAuth2WebViewAuthzModule extends OAuth2AuthzModule {

    private static final IntentFilter AUTHZ_FILTER;

    static {
        AUTHZ_FILTER = new IntentFilter();
        AUTHZ_FILTER.addAction("org.jboss.aerogear.android.authz.RECEIVE_AUTHZ");
    }
    
    private final String TAG = OAuth2WebViewAuthzModule.class.getSimpleName();

    public OAuth2WebViewAuthzModule(OAuth2Properties config) {
        super(config);
    }

    @Override
    public void requestAccess(final Activity activity, final Callback<String> callback) {

        final String state = UUID.randomUUID().toString();

        final OAuth2AuthzService.AGAuthzServiceConnection connection = new OAuth2AuthzService.AGAuthzServiceConnection() {

            @Override
            public void onServiceConnected(ComponentName className, IBinder iBinder) {
                super.onServiceConnected(className, iBinder);
                doRequestAccess(state, activity, callback, this);
            }

        };

        activity.bindService(new Intent(activity.getApplicationContext(), OAuth2AuthzService.class
                ), connection, Context.BIND_AUTO_CREATE
                );

    }


    private void doRequestAccess(final String state, final Activity activity, final Callback<String> callback, final OAuth2AuthzService.AGAuthzServiceConnection instance) {

        service = instance.getService();

        if (isNullOrEmpty(accountId)) {
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
            try {
                callingActivity.unbindService(serviceConnection);
            } catch (IllegalArgumentException ignore) {}
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
                    try {
                        callingActivity.unbindService(serviceConnection);
                    } catch (IllegalArgumentException ignore) {}
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
            session.setClientId(clientId);
            service.addAccount(session);

            OAuth2FetchAccess fetcher = new OAuth2FetchAccess(service);
            fetcher.fetchAccessCode(accountId, config, new OAuth2AccessCallback(callingActivity, originalCallback, serviceConnection));
        }

        @Override
        public void onFailure(final Exception e) {
            myHandler.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        callingActivity.unbindService(serviceConnection);
                    } catch (IllegalArgumentException ignore) {}
                    originalCallback.onFailure(e);
                }
            });
        }
    }

}
