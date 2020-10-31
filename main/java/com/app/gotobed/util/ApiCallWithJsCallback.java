package com.app.gotobed.util;

import android.webkit.WebView;

public class ApiCallWithJsCallback extends ApiCall {
	
	private WebView webView;
	private String callbackJsFunctionName;
	
	public ApiCallWithJsCallback(String host, String path, String postParameters, WebView webView, String callbackJsFunctionName) {
		super(host, path, postParameters);		
		this.webView=webView;
		this.callbackJsFunctionName=callbackJsFunctionName;
	}

	@Override
	protected void onPostExecute(String result) {
		JavascriptExecutor.execute(webView,  callbackJsFunctionName, result);
	}

}
