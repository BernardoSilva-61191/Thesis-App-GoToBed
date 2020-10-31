package com.app.gotobed.util;

import android.webkit.WebView;

public class JavascriptExecutor {

	public static void execute(WebView webView, String callbackJsFunctionName, String data) {

		StringBuilder sb=new StringBuilder("javascript:");
		sb.append(callbackJsFunctionName);
		sb.append("(\"");
		sb.append(data.replaceAll("\\\"", "\\\\\""));
		sb.append("\");");
		webView.loadUrl(sb.toString());

	}
	
}
