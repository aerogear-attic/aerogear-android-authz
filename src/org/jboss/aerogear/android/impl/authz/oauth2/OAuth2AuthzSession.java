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

import android.os.Parcel;
import android.os.Parcelable;
import java.util.Date;
import java.util.Objects;
import org.jboss.aerogear.android.RecordId;

/**
 * This is a wrapper for various bits of authorization metadata.
 *
 * For details of the various fields, see the Oauth spec.
 */
public class OAuth2AuthzSession implements Parcelable {

    @RecordId
    private String accountId = "";

    private String cliendId = "";
    private String accessToken = "";
    private String authorizationCode = "";
    private String refreshToken = "";
    private long expires_on = 0;

    private OAuth2AuthzSession(Parcel in) {
        cliendId = in.readString();
        accessToken = in.readString();
        authorizationCode = in.readString();
        refreshToken = in.readString();
        accountId = in.readString();
        expires_on = in.readLong();
    }

    public OAuth2AuthzSession() {
    }

    public String getCliendId() {
        return cliendId;
    }

    public void setCliendId(String cliendId) {
        this.cliendId = cliendId;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getAuthorizationCode() {
        return authorizationCode;
    }

    public void setAuthorizationCode(String authorizationCode) {
        this.authorizationCode = authorizationCode;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public long getExpires_on() {
        return expires_on;
    }

    public void setExpires_on(long expires_on) {
        this.expires_on = expires_on;
    }

    /**
     * AccountId represents the ID of the account type used to fetch sessions
     * for the type
     *
     * @return the current account type.
     */
    public String getAccountId() {
        return accountId;
    }

    /**
     * AccountId represents the ID of the account type used to fetch sessions
     * for the type
     *
     * @param accountId an accountId
     */
    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 71 * hash + Objects.hashCode(this.cliendId);
        hash = 71 * hash + Objects.hashCode(this.accessToken);
        hash = 71 * hash + Objects.hashCode(this.authorizationCode);
        hash = 71 * hash + Objects.hashCode(this.refreshToken);
        hash = 71 * hash + Objects.hashCode(this.accountId);
        hash = 71 * hash + (int) (this.expires_on ^ (this.expires_on >>> 32));
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final OAuth2AuthzSession other = (OAuth2AuthzSession) obj;
        if (!Objects.equals(this.cliendId, other.cliendId)) {
            return false;
        }
        if (!Objects.equals(this.accessToken, other.accessToken)) {
            return false;
        }
        if (!Objects.equals(this.authorizationCode, other.authorizationCode)) {
            return false;
        }
        if (!Objects.equals(this.refreshToken, other.refreshToken)) {
            return false;
        }
        if (!Objects.equals(this.accountId, other.accountId)) {
            return false;
        }
        if (this.expires_on != other.expires_on) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "AuthzSession{" + "cliendId=" + cliendId + ", accessToken=" + accessToken + ", authorizationCode=" + authorizationCode + ", refreshToken=" + refreshToken
                + ", accountId=" + accountId + ", expires_on=" + expires_on + '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(cliendId);
        dest.writeString(accessToken);
        dest.writeString(authorizationCode);
        dest.writeString(refreshToken);
        dest.writeString(accountId);
        dest.writeLong(expires_on);
    }

    public static final Parcelable.Creator<OAuth2AuthzSession> CREATOR = new Parcelable.Creator<OAuth2AuthzSession>() {
        @Override
        public OAuth2AuthzSession createFromParcel(Parcel in) {
            return new OAuth2AuthzSession(in);
        }

        @Override
        public OAuth2AuthzSession[] newArray(int size) {
            return new OAuth2AuthzSession[size];
        }

    };

    public boolean tokenIsNotExpired() {
        if (expires_on == 0) {
            return true;
        }
        return (expires_on > new Date().getTime());
    }

}
