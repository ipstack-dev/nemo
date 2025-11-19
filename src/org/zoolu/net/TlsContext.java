/*
 * Copyright (C) 2007 Luca Veltri - University of Parma - Italy
 * 
 * This file is part of MjSip (http://www.mjsip.org)
 * 
 * MjSip is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * MjSip is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with MjSip; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 * 
 * Author(s):
 * Luca Veltri (luca.veltri@unipr.it)
 */

package org.zoolu.net;


import java.io.File;
import java.io.FileInputStream;
import java.security.Key;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;

import org.zoolu.util.log.DefaultLogger;
import org.zoolu.util.log.LoggerLevel;


/** TLS security context.
  */
public class TlsContext {
	
	/** Prints out a message. */
	private static void log(String str) {
		DefaultLogger.log(LoggerLevel.DEBUG,TlsContext.class,str);
	}
	
	public static boolean VERBOSE=false;

	
	/** Default cert file extensions */
	public static final String[] CERT_EXTENSIONS={ ".crt", ".pem" };
	
	/** Default password */
	public static final char[] DEFAULT_PASSWORD=("TLS_CONTEXT_PASSWD").toCharArray();

	/** KeyStore */
	KeyStore ks;

	/** Password */
	char[] passwd=DEFAULT_PASSWORD;

	/** Whether all certificates should be considered trusted.
	  * By default, trust_all_certificates=false. */
	boolean trust_all_certificates=false;

	/** Counter of trusted certificates. */
	int trust_count=0;


	
	/** Creates a new TlsContext. */
	public TlsContext() throws java.security.KeyStoreException, java.security.cert.CertificateException, java.security.NoSuchAlgorithmException, java.io.IOException {
		init();
	}


	/** Creates a new TlsContext. */
	public TlsContext(char[] passwd) throws java.security.KeyStoreException, java.security.cert.CertificateException, java.security.NoSuchAlgorithmException, java.io.IOException {
		if (passwd!=null) this.passwd=passwd;
		init();
	}


	/** Inits the TlsContext. */
	private void init() throws java.security.KeyStoreException, java.security.cert.CertificateException, java.security.NoSuchAlgorithmException, java.io.IOException {
		ks=KeyStore.getInstance(KeyStore.getDefaultType());
		ks.load(null,null);
	}


	/** Sets key certificate.
	 * @param key_file private key file
	 * @param cert_file certificate file
	 * @throws java.security.cert.CertificateException
	 * @throws java.io.FileNotFoundException
	 * @throws java.security.KeyStoreException
	 * @throws java.lang.Exception */
	public void setKeyCert(String key_file, String cert_file) throws java.security.cert.CertificateException, java.io.FileNotFoundException, java.security.KeyStoreException, java.lang.Exception {
		Key key=TlsKeyTool.importPrivateKeyBASE64(key_file);
		if (VERBOSE) log("key loaded: "+key_file);
		Certificate cert=CertificateFactory.getInstance("x509").generateCertificate(new FileInputStream(cert_file));
		if (VERBOSE) log("key cert loaded: "+cert_file);
		setKeyCert(key,cert);
	}


	/** Sets key certificate.
	 * @param key the private key
	 * @param cert the certificate
	 * @throws java.security.KeyStoreException */
	public void setKeyCert(Key key, Certificate cert) throws java.security.KeyStoreException {
		String alias="ssl";
		//printOut("DEBUG: key cert alias: "+alias);
		ks.setCertificateEntry(alias,cert);
		Certificate[] chain={ cert };
		ks.setKeyEntry(alias,key,passwd,chain);
	}


	/** Adds a trusted certificate.
	 * @param cert_file certificate file
	 * @throws java.security.cert.CertificateException
	 * @throws java.io.FileNotFoundException
	 * @throws java.security.KeyStoreException */
	public void addTrustCert(String cert_file) throws java.security.cert.CertificateException, java.io.FileNotFoundException, java.security.KeyStoreException {
		Certificate cert=CertificateFactory.getInstance("x509").generateCertificate(new FileInputStream(cert_file));
		if (VERBOSE) log("trusted cert loaded: "+cert_file);
		addTrustCert(cert);
	}


	/** Adds a trusted certificate.
	 * @param cert the certificate
	 * @throws java.security.KeyStoreException */
	public void addTrustCert(Certificate cert) throws java.security.KeyStoreException {
		String alias="ssl-trust-"+(trust_count/10)+((trust_count++)%10);
		//printOut("DEBUG: trusted cert alias: "+alias);
		ks.setCertificateEntry(alias,cert);
	}


	/** Adds all trusted certificates from the specified folder.
	 * @param cert_folder folder name
	 * @throws java.security.cert.CertificateException
	 * @throws java.io.FileNotFoundException
	 * @throws java.security.KeyStoreException */
	public void addTrustFolder(String cert_folder) throws java.security.cert.CertificateException, java.io.FileNotFoundException, java.security.KeyStoreException {
		File[] file_list=(new File(cert_folder)).listFiles();
		for (int i=0; i<file_list.length; i++) {
			File file=file_list[i];
			String file_name=file.getName();
			if (file_name.length()>4) {
				String extension=file_name.substring(file_name.length()-4);
				for (int k=0; k<CERT_EXTENSIONS.length; k++)
				if (extension.equalsIgnoreCase(CERT_EXTENSIONS[k])) {
					Certificate cert=CertificateFactory.getInstance("x509").generateCertificate(new FileInputStream(file));
					if (VERBOSE) log("trusted cert loaded: "+cert_folder+"/"+file_name);
					addTrustCert(cert);
					break;
				}
			}
		}
	}


	/** Sets trust-all mode.
	 * @param trust_all whether trust all certificates */
	public void setTrustAll(boolean trust_all) {
		this.trust_all_certificates=trust_all;
		if (VERBOSE) log("trust all: "+((trust_all)? "yes" : "no"));
					
	}


	/** Whether it's in trust-all mode. */
	public boolean isTrustAll() {
		return trust_all_certificates;
	}   


	/** Gets the key store.
	 * @return the key store
	 */
	public KeyStore getKeyStore() {
		return ks;
	}

		
}
