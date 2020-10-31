package com.app.gotobed.util;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;

import android.os.AsyncTask;

public class ApiCall extends AsyncTask<String, Void, String> {
	
	protected String host;
	protected String path;
	protected String postParameters;

	public ApiCall(String host, String path, String postParameters) {
		this.host=host;
		this.path=path;
		this.postParameters=postParameters;
	}
		
	public void execute() {
		this.execute(host);
	}
	
	@Override
	protected String doInBackground(String... hosts) {

		try {
			URL url = new URL("http://" + hosts[0] + path);
			HttpURLConnection uc = (HttpURLConnection)url.openConnection();
			Authorizer.authorizeUrlConnection(uc);
			uc.setUseCaches(false);
			
			if(postParameters!=null) {
				byte[] data=postParameters.getBytes(Charset.forName("UTF-8"));
				
				uc.setDoInput(true);
				uc.setRequestMethod("POST");
				uc.setRequestProperty( "Content-Type", "application/x-www-form-urlencoded"); 
				uc.setRequestProperty( "charset", "utf-8");
				uc.setRequestProperty( "Content-Length", Integer.toString(data.length));
				
				try {
					DataOutputStream dos=new DataOutputStream(uc.getOutputStream());
					dos.write(data);
					dos.flush();					
				} catch (IOException ioe) {
					ioe.printStackTrace();
				}

			}
			
			BufferedReader in = new BufferedReader(new InputStreamReader(uc.getInputStream()));
			String result = in.readLine();
			in.close();
			
			return result;

		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}
	
}
