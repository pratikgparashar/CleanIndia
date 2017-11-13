package com.example.pratik.cleanindia;

import android.util.Log;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class UserFunctions {
	 
	 private JASONParser jsonParser;
	 private static String loginURL ="http://cleanindia.net16.net/app/cleanIndia1.php";/*"http://itsatechware.wc.lt/cleanIndia.php";*/

   // private static String loginURL = "http://192.168.1.101:8080/imgtst/";
	public UserFunctions(){
	        jsonParser = new JASONParser();
	    }
	
	
	 public JSONObject uploadimage(String caption, String image,String name1,String email1,String phone1,Double longitude,Double latitude){
	        // Building Parameters
	        List<NameValuePair> params = new ArrayList<NameValuePair>();
	        
	        params.add(new BasicNameValuePair("caption", caption));
	       params.add(new BasicNameValuePair("image",image));
         params.add(new BasicNameValuePair("name", name1));
         params.add(new BasicNameValuePair("email", email1));
         params.add(new BasicNameValuePair("phone", phone1));
         params.add(new BasicNameValuePair("longitude", longitude.toString()));
         params.add(new BasicNameValuePair("latitude", latitude.toString()));

	          Log.d("img Submission", params.toString());
	        JSONObject json = jsonParser.getJSONFromUrl(loginURL, params);
	        return json;
	    }
}
