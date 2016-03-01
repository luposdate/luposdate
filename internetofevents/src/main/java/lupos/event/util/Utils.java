/**
 * Copyright (c) 2007-2015, Institute of Information Systems (Sven Groppe and contributors of LUPOSDATE), University of Luebeck
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 *
 * 	- Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 * 	  disclaimer.
 * 	- Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
 * 	  following disclaimer in the documentation and/or other materials provided with the distribution.
 * 	- Neither the name of the University of Luebeck nor the names of its contributors may be used to endorse or promote
 * 	  products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
 * GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package lupos.event.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
public final class Utils {

	private Utils() {
	}

	/**
	 * Checks, if obj is a list and only contains elements of the same type
	 *
	 * @param obj a {@link java.lang.Object} object.
	 * @param elementType a {@link java.lang.Class} object.
	 * @return a boolean.
	 */
	public static boolean isHomogenousList(Object obj, @SuppressWarnings("rawtypes") Class elementType) {
		if(!(obj instanceof List<?>))
			return false;
		
		List<?> l = (List<?>)obj;
			
		for(Object o : l) {
			if(!elementType.isInstance(o))
				return false;
		}
		
		return true;
	}
	
	/**
	 * <p>escape.</p>
	 *
	 * @param s a {@link java.lang.String} object.
	 * @return a {@link java.lang.String} object.
	 */
	public static String escape(String s){
		StringBuilder result = new StringBuilder();
		boolean escaped = false;
		for(char c: s.toCharArray()){
			// remove returns
			if(c!='\n' && c!='\r'){
				if(c=='"' && !escaped){
					// escape quotes
					result.append("\\\"");
				} else {
					result.append(c);
					// check escaping!
					if(c=='\\'){
						escaped = !escaped;
					} else {
						escaped = false;
					}
				}
			}
		}
		return result.toString();
	}
	
	
	/**
	 * <p>createURIString.</p>
	 *
	 * @param namespace a {@link java.lang.String} object.
	 * @param str a {@link java.lang.String} object.
	 * @return a {@link java.lang.String} object.
	 */
	public static String createURIString(String namespace, String str) {
		return "<"+namespace+str+">";
	}
	
	
	/**
	 * <p>httpGet.</p>
	 *
	 * @param url a {@link java.lang.String} object.
	 * @return a {@link java.lang.String} object.
	 * @throws java.lang.Exception if any.
	 */
	@SuppressWarnings("deprecation")
	public static String httpGet(String url) throws Exception {
		
		DefaultHttpClient base = new DefaultHttpClient();
		
		SSLContext ctx = SSLContext.getInstance("TLS");
		X509TrustManager tm = new X509TrustManager() {

		    @Override
			public void checkClientTrusted(X509Certificate[] xcs, String string) throws CertificateException {
		    	// just ignore => dirty trick, better import certificate in your trust store!
		    }

		    @Override
			public void checkServerTrusted(X509Certificate[] xcs, String string) throws CertificateException {
		    	// just ignore => dirty trick, better import certificate in your trust store!
		    }

		    @Override
			public X509Certificate[] getAcceptedIssuers() {
		        return null;
		    }
		};
		ctx.init(null, new TrustManager[]{tm}, null);
		
		SSLSocketFactory ssf = new SSLSocketFactory(ctx);
		ssf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
		ClientConnectionManager ccm = base.getConnectionManager();
		SchemeRegistry sr = ccm.getSchemeRegistry();
		sr.register(new Scheme("https", ssf, 443));

		DefaultHttpClient client = new DefaultHttpClient(ccm, base.getParams());
		
		final HttpUriRequest httpurirequest = new HttpGet(url);
		HttpResponse response = client.execute(httpurirequest);
		HttpEntity entity = response.getEntity();
		
        BufferedReader in = new BufferedReader(new InputStreamReader(entity.getContent()));
        StringBuilder sb = new StringBuilder();
        String str;

        while ((str = in.readLine()) != null) {
    		sb.append(str);
        }

        in.close();

        return sb.toString();
	}
}
