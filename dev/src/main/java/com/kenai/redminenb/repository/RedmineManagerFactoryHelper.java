/*
 * Copyright 2015 Matthias Bläsing.
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

package com.kenai.redminenb.repository;

import com.taskadapter.redmineapi.RedmineManager;
import com.taskadapter.redmineapi.TransportConfiguration;
import com.taskadapter.redmineapi.internal.Transport;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.ProxySelector;
import java.security.AccessController;
import java.security.NoSuchAlgorithmException;
import java.security.PrivilegedAction;
import java.security.cert.X509Certificate;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.SystemDefaultRoutePlanner;

class RedmineManagerFactoryHelper {

    public static TransportConfiguration getTransportConfig() {
        /**
     * Implement a minimal hostname verifier. This is needed to be able to use
     * hosts with certificates, that don't match the used hostname (VServer).
         *
         * This is implemented by first trying the "Browser compatible" hostname
         * verifier and if that fails, fall back to the default java hostname
         * verifier.
         *
         * If the default case the hostname verifier in java always rejects, but
         * for netbeans the "SSL Certificate Exception" module is available that
         * catches this and turns a failure into a request to the GUI user.
         */
        X509HostnameVerifier hostnameverified = new X509HostnameVerifier() {
            @Override
            public void verify(String string, SSLSocket ssls) throws IOException {
                if(SSLSocketFactory.BROWSER_COMPATIBLE_HOSTNAME_VERIFIER.verify(string, ssls.getSession())) {
                    return;
                }
                if (!HttpsURLConnection.getDefaultHostnameVerifier().verify(string, ssls.getSession())) {
                    throw new SSLException("Hostname did not verify");
                }
            }

            @Override
            public void verify(String string, X509Certificate xc) throws SSLException {
                throw new SSLException("Check not implemented yet");
            }

            @Override
            public void verify(String string, String[] strings, String[] strings1) throws SSLException {
                throw new SSLException("Check not implemented yet");
            }

            @Override
            public boolean verify(String string, SSLSession ssls) {
                if (SSLSocketFactory.BROWSER_COMPATIBLE_HOSTNAME_VERIFIER.verify(string, ssls)) {
                    return true;
                }
                return HttpsURLConnection.getDefaultHostnameVerifier().verify(string, ssls);
            }
        };

        try {
            SSLConnectionSocketFactory scsf = new SSLConnectionSocketFactory(SSLContext.getDefault(), hostnameverified);

            HttpClient hc = HttpClientBuilder.create()
                    .setRoutePlanner(new SystemDefaultRoutePlanner(ProxySelector.getDefault()))
                    .setSSLSocketFactory(scsf)
                    .build();

            return TransportConfiguration.create(hc, null);
        } catch (NoSuchAlgorithmException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    public static Transport getTransportFromManager(final RedmineManager rm) {
        return AccessController.doPrivileged(new PrivilegedAction<Transport>() {

            @Override
            public Transport run() {
                try {
                    Field transportField = RedmineManager.class.getDeclaredField("transport");
                    transportField.setAccessible(true);
                    return (Transport) transportField.get(rm);
                } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException ex) {
                    throw new RuntimeException("Failed to get transport from redmine manager", ex);
                }
            }
        });
    }
}
