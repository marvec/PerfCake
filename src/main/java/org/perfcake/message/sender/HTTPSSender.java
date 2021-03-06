/*
 * Copyright 2010-2013 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.perfcake.message.sender;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.net.URL;
import java.security.KeyStore;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

import org.apache.log4j.Logger;
import org.perfcake.PerfCakeException;
import org.perfcake.message.Message;

/**
 * 
 * @author Martin Večeřa <marvenec@gmail.com>
 * @author Pavel Macík <pavel.macik@gmail.com>
 * @author Filip Eliáš <elfilip01@gmail.com>
 */
public class HTTPSSender extends AbstractSender {

   // static {
   // //for localhost testing only
   // javax.net.ssl.HttpsURLConnection.setDefaultHostnameVerifier(
   // new javax.net.ssl.HostnameVerifier(){
   //
   // public boolean verify(String hostname,
   // javax.net.ssl.SSLSession sslSession) {
   // if (hostname.equals("localhost")) {
   // return true;
   // }
   // return false;
   // }
   // });
   // }

   private URL url;

   private String method = "POST";

   private static final Logger log = Logger.getLogger(HTTPSSender.class);

   private static final int DEFAULT_EXPECTED_CODE = 200;

   private String address = "";

   private List<Integer> expectedCodes = Arrays.asList(DEFAULT_EXPECTED_CODE);

   private String keyStoreStr;

   private String keyStorePassword;

   private String trustStoreStr;

   private String trustStorePassword;

   private SSLSocketFactory sslFactory;

   private HttpsURLConnection rc;

   private String reqStr;

   private int len;

   @Override
   public void setProperty(String prop, String value) {
      if ("method".equals(prop)) {
         method = value;
      } else if ("address".equals(prop)) {
         this.address = value;
      } else if ("expectedResponseCode".equals(prop)) {
         this.expectedCodes = setExpectedCodes(value.split(","));
      } else if ("keyStore".equals(prop)) {
         this.keyStoreStr = value;
      } else if ("keyStorePassword".equals(prop)) {
         this.keyStorePassword = value;
      } else if ("trustStore".equals(prop)) {
         this.trustStoreStr = value;
      } else if ("trustStorePassword".equals(prop)) {
         this.trustStorePassword = value;
      }
   }

   @Override
   public void init() throws Exception {
      url = new URL(address);
      sslFactory = initKeyStores();
   }

   @Override
   public void close() {
   }

   @Override
   public void preSend(Message message, Map<String, String> properties) throws Exception {
      rc = (HttpsURLConnection) url.openConnection();
      rc.setSSLSocketFactory(sslFactory);

      rc.setRequestMethod(method);
      rc.setDoOutput(true);
      rc.setDoInput(true);
      rc.setRequestProperty("Content-Type", "text/xml; charset=utf-8");
      String reqStr = message.getPayload().toString();
      int len = reqStr.length();
      rc.setRequestProperty("Content-Length", Integer.toString(len));

      Set<String> propertyNameSet = message.getProperties().stringPropertyNames();
      for (String property : propertyNameSet) {
         rc.setRequestProperty(property, message.getProperty(property));
         System.out.println("property " + property + "  " + message.getProperty(property));
      }
      // set additional properties
      if (log.isDebugEnabled()) {
         log.debug("Setting HTTP headers");
      }
      if (properties != null) {
         propertyNameSet = properties.keySet();
         for (String property : propertyNameSet) {
            String pValue = (properties.get(property));
            rc.setRequestProperty(property, pValue);
            if (log.isDebugEnabled()) {
               log.debug(property + ": " + pValue);
            }
         }
      }

      if (message.getHeaders().size() > 0) {
         Set<String> headerNameSet = message.getHeaders().stringPropertyNames();
         for (String header : headerNameSet) {
            String hValue = message.getHeader(header);
            if (log.isDebugEnabled()) {
               log.debug(header + ": " + hValue);
            }
            rc.setRequestProperty(header, hValue);
         }
      }

   }

   @Override
   public Serializable doSend(Message message, Map<String, String> properties) throws Exception {

      rc.connect();

      OutputStreamWriter out = new OutputStreamWriter(rc.getOutputStream());
      out.write(reqStr, 0, len);
      out.flush();

      rc.getOutputStream().close();

      int respCode = -1;
      respCode = rc.getResponseCode();
      if (!checkResponseCode(respCode)) {
         String errorMess = "The server returned an unexpected HTTP response code: " + respCode + " " + "\"" + rc.getResponseMessage() + "\". Expected HTTP codes are ";
         for (int code : expectedCodes) {
            errorMess += Integer.toString(code) + ", ";
         }
         throw new PerfCakeException(errorMess.substring(0, errorMess.length() - 2) + ".");
      }

      InputStream rcis = null;
      InputStreamReader read = null;
      char[] cbuf = new char[10 * 1024];

      if (respCode < 400) {
         rcis = rc.getInputStream();
      } else {
         rcis = rc.getErrorStream();
      }
      read = new InputStreamReader(rcis);
      StringBuilder sb = new StringBuilder();
      int ch = read.read(cbuf);
      while (ch != -1) {
         sb.append(cbuf, 0, ch);
         ch = read.read(cbuf);
      }
      read.close();
      rcis.close();

      String payload = sb.toString();
      return payload;
   }

   @Override
   public void postSend(Message message) {
      rc.disconnect();
   }

   @Override
   public Serializable doSend(Message message) throws Exception {
      return null;
   }

   private LinkedList<Integer> setExpectedCodes(String[] codes) {
      LinkedList<Integer> numCodes = new LinkedList<Integer>();
      for (int i = 0; i < codes.length; i++) {
         numCodes.add(Integer.parseInt(codes[i].trim()));
      }
      return numCodes;
   }

   private boolean checkResponseCode(int code) {
      for (int i : expectedCodes) {
         if (i == code) {
            return true;
         }
      }
      return false;
   }

   private SSLSocketFactory initKeyStores() throws Exception {
      KeyStore keyStore = null;
      KeyStore trustStore = null;
      KeyManagerFactory keyManager = null;
      TrustManagerFactory trustManager = null;

      if (keyStoreStr != null) {
         if (keyStorePassword == null) {
            throw new PerfCakeException("The keyStore password is not set. (Use keyStorePassword property of the HTTPSSender to set it!)");
         } else {
            keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            java.io.FileInputStream fis = new java.io.FileInputStream("." + File.separator + "resources" + File.separator + "keystores" + File.separator + keyStoreStr);
            keyStore.load(fis, keyStorePassword.toCharArray());
            fis.close();
            keyManager = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManager.init(keyStore, keyStorePassword.toCharArray());
         }
      }
      if (trustStoreStr != null) {
         if (trustStorePassword == null) {
            throw new PerfCakeException("The trustStore password is not set. (Use trustStorePassword property of the HTTPSSender to set it!)");
         } else {
            trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            java.io.FileInputStream fis = new java.io.FileInputStream("." + File.separator + "resources" + File.separator + "keystores" + File.separator + trustStoreStr);
            trustStore.load(fis, trustStorePassword.toCharArray());
            fis.close();
            trustManager = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManager.init(trustStore);
         }
      }
      SSLContext ctx = SSLContext.getInstance("TLS");
      ctx.init(keyManager == null ? null : keyManager.getKeyManagers(), trustManager == null ? null : trustManager.getTrustManagers(), null);
      return ctx.getSocketFactory();
   }

}
