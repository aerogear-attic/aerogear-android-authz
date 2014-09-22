/*
 * Copyright 2014 JBoss by Red Hat.
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
package org.jboss.aerogear.android.impl.authz;

import android.util.Pair;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.jboss.aerogear.android.Config;
import org.jboss.aerogear.android.authorization.AuthzModule;
import org.jboss.aerogear.android.impl.authz.oauth2.OAuth2AuthzModule;

public class OAuth2AuthorizationConfiguration extends AuthorizationConfiguration<OAuth2AuthorizationConfiguration> implements Config<OAuth2AuthorizationConfiguration>{
    private String authzEndpoint = "";
    private String redirectURL = "";
    private String accessTokenEndpoint = "";
    private List<String> scopes = new ArrayList<String>();
    private String clientId = "";
    private String clientSecret = "";
    private String accountId = "";
    private Set<Pair<String, String>> additionalAuthorizationParams = new HashSet<Pair<String, String>>();
    private Set<Pair<String, String>> additionalAccessParams = new HashSet<Pair<String, String>>();

    public String getAuthzEndpoint() {
        return authzEndpoint;
    }

    public AuthorizationConfiguration setAuthzEndpoint(String authzEndpoint) {
        this.authzEndpoint = authzEndpoint;
        return this;
    }

    public String getRedirectURL() {
        return redirectURL;
    }

    public AuthorizationConfiguration setRedirectURL(String redirectURL) {
        this.redirectURL = redirectURL;
        return this;
    }

    public String getAccessTokenEndpoint() {
        return accessTokenEndpoint;
    }

    public AuthorizationConfiguration setAccessTokenEndpoint(String accessTokenEndpoint) {
        this.accessTokenEndpoint = accessTokenEndpoint;
        return this;
    }

    public List<String> getScopes() {
        return scopes;
    }

    public AuthorizationConfiguration setScopes(List<String> scopes) {
        this.scopes = scopes;
        return this;        
    }

    public String getClientId() {
        return clientId;
    }

    public AuthorizationConfiguration setClientId(String clientId) {
        this.clientId = clientId;
        return this;        
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public AuthorizationConfiguration setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
        return this;        
    }

    public String getAccountId() {
        return accountId;
    }

    public AuthorizationConfiguration setAccountId(String accountId) {
        this.accountId = accountId;
        return this;        
    }

    public Set<Pair<String, String>> getAdditionalAuthorizationParams() {
        return additionalAuthorizationParams;
    }

    public AuthorizationConfiguration setAdditionalAuthorizationParams(Set<Pair<String, String>> additionalAuthorizationParams) {
        this.additionalAuthorizationParams = additionalAuthorizationParams;
        return this;        
    }

    public Set<Pair<String, String>> getAdditionalAccessParams() {
        return additionalAccessParams;
    }

    public AuthorizationConfiguration setAdditionalAccessParams(Set<Pair<String, String>> additionalAccessParams) {
        this.additionalAccessParams = additionalAccessParams;
        return this;        
    }

    @Override
    protected AuthzModule buildModule() {
        return new OAuth2AuthzModule(null);
    }

    
    
}
