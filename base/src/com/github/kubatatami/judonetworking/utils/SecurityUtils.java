package com.github.kubatatami.judonetworking.utils;

import android.util.Base64;

import com.github.kubatatami.judonetworking.controllers.ProtocolController;

import java.io.IOException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by Kuba on 06/04/14.
 */
public class SecurityUtils {

    public static String getBasicAuthHeader(String login, String pass) {
        String source = login + ":" + pass;
        return "Basic " + Base64.encodeToString(source.getBytes(), Base64.NO_WRAP);
    }

    public static String getDigestAuthHeader(DigestAuth digestAuth, URL url,
                                             ProtocolController.RequestInfo requestInfo,
                                             String username, String password) throws IOException {
        digestAuth.digestCounter++;
        String cnonce = Base64.encodeToString((System.currentTimeMillis() + "").getBytes(), Base64.NO_WRAP);
        String base = url.getProtocol() + "://" + url.getHost();
        String uri = url.toString().substring(base.length());
        String nc = digestAuth.digestCounter + "";
        String method = requestInfo.entity != null ? "POST" : "GET";

        String response = digestAuth(username, password, nc,
                cnonce, method, uri, digestAuth, requestInfo);
        return "Digest username=\"" + username + "\", realm=\"" + digestAuth.realm + "\", nonce=\""
                + digestAuth.nonce + "\"," +
                " uri=\"" + uri + "\", cnonce=\"" + cnonce + "\"," +
                "nc=" + nc + ", qop=" + digestAuth.qop + ", response=\"" + response +
                "\", opaque=\"" + digestAuth.opaque +
                "\", algorithm=\"" + digestAuth.algorithm + "\"";

    }

    private static String digestAuth(String login, String pass, String nonceCount, String clientNonce,
                                     String method, String digestURI, DigestAuth digestAuth,
                                     ProtocolController.RequestInfo requestInfo) throws IOException {
        String source;
        String ha1 = digestAuthHa1(login, pass, clientNonce, digestAuth);
        String ha2 = digestAuthHa2(method, digestURI, digestAuth.qop, requestInfo);

        if (digestAuth.qop != null) {
            source = ha1 + ":" + digestAuth.nonce + ":" + nonceCount +
                    ":" + clientNonce + ":" + digestAuth.qop + ":" + ha2;
        } else {
            source = ha1 + ":" + digestAuth.nonce + ":" + ha2;
        }
        return md5(source);
    }

    private static String digestAuthHa1(String login, String pass, String clientNonce,
                                        DigestAuth digestAuth) {
        if (digestAuth.algorithm.equalsIgnoreCase("MD5-sess")) {
            return md5(md5(login + ":" + digestAuth.realm + ":" + pass) +
                    ":" + digestAuth.nonce + ":" + clientNonce);
        } else {
            return md5(login + ":" + digestAuth.realm + ":" + pass);
        }
    }

    private static String digestAuthHa2(String method, String digestURI, String qop,
                                        ProtocolController.RequestInfo requestInfo) throws IOException {
        if (qop.equalsIgnoreCase("auth-int")) {
            byte[] body = new byte[(int) requestInfo.entity.getContentLength()];
            requestInfo.entity.getContent().read(body);
            return md5(method + ":" + digestURI + ":" + md5(body));
        } else {
            return md5(method + ":" + digestURI);
        }
    }

    public static String md5(final String body) {
        return md5(body.getBytes());
    }

    public static String md5(final byte[] bytes) {
        final String MD5 = "MD5";
        try {
            // Create MD5 Hash
            MessageDigest digest = java.security.MessageDigest
                    .getInstance(MD5);
            digest.update(bytes);
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuilder hexString = new StringBuilder();
            for (byte aMessageDigest : messageDigest) {
                String h = Integer.toHexString(0xFF & aMessageDigest);
                while (h.length() < 2)
                    h = "0" + h;
                hexString.append(h);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static DigestAuth handleDigestAuth(String wwwAuthenticateHeader, int responseCode) {
        DigestAuth result = null;
        if (responseCode == 401) {
            if (wwwAuthenticateHeader != null && wwwAuthenticateHeader.indexOf("Digest") == 0) {
                result = new DigestAuth();

                for (String item : wwwAuthenticateHeader.substring(7).replaceAll(",", "").split(" ")) {
                    String name = item.split("=")[0];
                    String value = item.split("=")[1].replaceAll("\"", "");
                    switch (name) {
                        case "realm":
                            result.realm = value;
                            break;
                        case "nonce":
                            result.nonce = value;
                            break;
                        case "qop":
                            result.qop = value;
                            break;
                        case "opaque":
                            result.opaque = value;
                            break;
                        case "algorithm":
                            result.algorithm = value;
                            break;
                    }
                }
            }
        }
        return result;
    }

    public static class DigestAuth {
        public String realm;
        public String nonce;
        public String algorithm;
        public String opaque;
        public String qop;
        public int digestCounter = 0;
    }

}
