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
package org.jboss.aerogear.android.authorization.oauth2.intent;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import java.util.UUID;
import org.jboss.aerogear.android.authorization.oauth2.OAuth2AuthzModule;
import org.jboss.aerogear.android.authorization.oauth2.OAuth2AuthzService;
import org.jboss.aerogear.android.authorization.oauth2.OAuth2FetchAccess;
import org.jboss.aerogear.android.authorization.oauth2.OAuth2Properties;
import org.jboss.aerogear.android.core.Callback;

/**
 * This is a generic AuthzModule which using intent processing to fetch tokens
 * to authorize requests.
 */
public class OAuth2IntentAuthzModule extends OAuth2AuthzModule {

    public OAuth2IntentAuthzModule(OAuth2Properties params) {
        super(params);
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

    private void doRequestAccess(String state, Activity activity, Callback<String> callback, OAuth2AuthzService.AGAuthzServiceConnection instance) {
        service = instance.getService();

        if (isNullOrEmpty(accountId)) {
            throw new IllegalArgumentException("need to have accountId set");
        }

        if (!service.hasAccount(accountId)) {

        } else {

            OAuth2FetchAccess fetcher = new OAuth2FetchAccess(service);
            fetcher.fetchAccessCode(accountId, config, callback);

        }
    }

}
