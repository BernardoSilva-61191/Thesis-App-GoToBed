package com.app.gotobed.util;

import java.net.URLConnection;

import android.util.Base64;

public class Authorizer {

	final static String USERNAME="admin";
	final static String PASSWORD="admin";
	final static String AUTH_STRING = USERNAME + ":" + PASSWORD;
	final static String AUTH_STRING_ENCODED = Base64.encodeToString(AUTH_STRING.getBytes(), Base64.NO_WRAP);				

	public static void authorizeUrlConnection(URLConnection uc) {
		uc.setRequestProperty("Authorization", "Basic "+AUTH_STRING_ENCODED);
	}

}
