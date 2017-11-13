package com.example.pratik.cleanindia;

import android.util.Log;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Pratik on 25-Mar-15.
 */
public class UserFunctionsCheckStatus {


        private JASONParser jsonParser;
        private static String loginURL ="http://cleanindia.net16.net/app/chkstatus.php";/*"http://itsatechware.wc.lt/cleanIndia.php";*/

        // private static String loginURL = "http://192.168.1.101:8080/imgtst/";
        public UserFunctionsCheckStatus(){
            jsonParser = new JASONParser();
        }


        public JSONObject checkStatus(int id){
            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();

            params.add(new BasicNameValuePair("chkid", String.valueOf(id)));

            Log.d("img Submission", params.toString());
            JSONObject json = jsonParser.getJSONFromUrl(loginURL, params);
            return json;
        }
    }

