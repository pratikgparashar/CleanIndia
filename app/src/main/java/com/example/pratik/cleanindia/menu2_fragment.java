package com.example.pratik.cleanindia;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Pratik on 04-Mar-15.
 */
public class menu2_fragment extends Fragment {
    TextView reqname, reqdate,reqtime;
    ImageView reqImage,reqImage2;
    View rootview;
    Button chksts;
    int id;
    int hours;
    long elapsedHours, elapsedDays;
    Cursor c;
    String encodedStringImage;
    Bitmap decodedStringImage;
    private static String KEY_SUCCESS = "success";
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootview = inflater.inflate(R.layout.menu2_layout, container, false);
        DBAdapter db = new DBAdapter(rootview.getContext());
        reqname = (TextView) rootview.findViewById(R.id.reqname);
        reqdate = (TextView) rootview.findViewById(R.id.reqdate);
        reqImage = (ImageView) rootview.findViewById(R.id.reqImage);
        reqImage2 = (ImageView) rootview.findViewById(R.id.reqImage2);
        reqtime = (TextView) rootview.findViewById(R.id.reqTime);

        chksts = (Button) rootview.findViewById(R.id.chksts);
        db.open();
        c = db.lastRequest();
        if (c.moveToFirst())
            try {
                DisplayRequest(c);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        else {
            Toast.makeText(getActivity(), "No requests to be shown", Toast.LENGTH_LONG).show();
               reqname.setVisibility(View.INVISIBLE);
                reqdate.setVisibility(View.INVISIBLE);
                  reqImage.setVisibility(View.INVISIBLE);

        }


        chksts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(chksts.getText().toString().equals("View Image"))
                {
                    decodedStringImage = getBitmapFromString(encodedStringImage);
                    reqImage2.setImageBitmap(decodedStringImage);
                    reqname.setVisibility(View.INVISIBLE);
                    reqdate.setVisibility(View.INVISIBLE);
                    chksts.setVisibility(View.INVISIBLE);
                }
                else if(chksts.getText().toString().equals("Lodge Complaint")){
                    Intent email = new Intent(Intent.ACTION_SEND);
                    email.putExtra(Intent.EXTRA_EMAIL,new String[] { "example@mail.com"});
                    email.putExtra(Intent.EXTRA_SUBJECT,"Complaint for request Number :"+ id);
                    email.putExtra(Intent.EXTRA_TEXT,"sent a message using the contact us ");

                    email.setType("message/rfc822");

                    startActivityForResult(Intent.createChooser(email, "Choose an Email client:"),
                            1);


                }
                else
                {
                    id = c.getInt(0);
                    NetAsync(v);

                }
            }
        });

        db.close();


        return rootview;
    }


    public void DisplayRequest(Cursor c) throws ParseException {
        String timeAcc = c.getString(4);
        android.text.format.DateFormat df = new android.text.format.DateFormat();
        String timeNow = df.format("dd/M/yyyy hh:mm:ss", new java.util.Date()).toString();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/M/yyyy hh:mm:ss");
        Date date1 = simpleDateFormat.parse(timeAcc);

        Date date2 = simpleDateFormat.parse(timeNow);
/*
        long difference = date2.getTime() - date1.getTime();
        int days = (int) (difference / (1000*60*60*24));
        hours = (int) ((difference - (1000*60*60*24*days)) / (1000*60*60));
        int min = (int) (difference - (1000*60*60*24*days) - (1000*60*60*hours)) / (1000*60);
        hours = (hours < 0 ? -hours : hours);
        Log.i("======= Hours"," :: "+hours);
*/

        long different = date2.getTime() - date1.getTime();

        long secondsInMilli = 1000;
        long minutesInMilli = secondsInMilli * 60;
        long hoursInMilli = minutesInMilli * 60;
        long daysInMilli = hoursInMilli * 24;

         elapsedDays = different / daysInMilli;
        different = different % daysInMilli;

         elapsedHours = different / hoursInMilli;
        different = different % hoursInMilli;

        long elapsedMinutes = different / minutesInMilli;
        different = different % minutesInMilli;

        long elapsedSeconds = different / secondsInMilli;

            reqtime.setText("Time Elapsed : " +elapsedDays +" Days "+ elapsedHours + " Hrs " + elapsedMinutes+" Mins ");
            reqname.setText("Name : " + c.getString(1));
            reqdate.setText("Date   : " + c.getString(2));
            Bitmap decodedimg = getBitmapFromString(c.getString(3));
            reqImage.setImageBitmap(decodedimg);

            }


    private Bitmap getBitmapFromString(String jsonString) {
    	/*
    	* This Function converts the String back to Bitmap
    	* */
        byte[] decodedString = Base64.decode(jsonString, Base64.DEFAULT);
        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
        return decodedByte;
    }

    public void NetAsync(View view) {
        new NetCheck().execute();
    }

    private class NetCheck extends AsyncTask<String, String, Boolean> {
        private ProgressDialog nDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            nDialog = new ProgressDialog(rootview.getContext());
            nDialog.setTitle("Checking Network");
            nDialog.setMessage("Loading..");
            nDialog.setIndeterminate(false);
            nDialog.setCancelable(false);
            nDialog.show();
        }

        /**
         * Gets current device state and checks for working internet connection by trying Google.
         */
        @Override
        protected Boolean doInBackground(String... args) {


            ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = cm.getActiveNetworkInfo();
            if (netInfo != null && netInfo.isConnected()) {
                try {
                    URL url = new URL("http://www.google.com");
                    HttpURLConnection urlc = (HttpURLConnection) url.openConnection();
                    urlc.setConnectTimeout(3000);
                    urlc.connect();
                    if (urlc.getResponseCode() == 200) {
                        return true;
                    }
                } catch (MalformedURLException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            return false;

        }

        @Override
        protected void onPostExecute(Boolean th) {

            if (th == true) {
                nDialog.dismiss();
                new ProcessLogin().execute();
            } else {
                nDialog.dismiss();
                Toast.makeText(rootview.getContext(), "Error in Network Connection", Toast.LENGTH_LONG).show();

            }
        }
    }

    private class ProcessLogin extends AsyncTask<String, String, JSONObject> {


        private ProgressDialog pDialog;


        @Override
        protected void onPreExecute() {
            super.onPreExecute();


            pDialog = new ProgressDialog(rootview.getContext());
            pDialog.setTitle("Contacting Servers");
            pDialog.setMessage("Sending Request");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected JSONObject doInBackground(String... args) {



            UserFunctionsCheckStatus userFunctionCheckStatus = new UserFunctionsCheckStatus();
            JSONObject json = userFunctionCheckStatus.checkStatus(id);
            Log.d("upload", "1");
            return json;
        }

        @Override
        protected void onPostExecute(JSONObject json) {
            try {
                if (json.getString(KEY_SUCCESS) != null) {

                    String res = json.getString(KEY_SUCCESS);

                    if (Integer.parseInt(res) == 1) {


                        String requestSts = json.getString("statuscode");
                         if(elapsedDays>=1 && requestSts.equals("0"))
                        {
                            reqname.setText("It's been more than 24 hours since you have made request.If still there is no action then you can lodge a complaint.");
                            chksts.setText("Lodge Complaint");
                            reqImage.setVisibility(View.INVISIBLE);
                            reqImage2.setImageResource(R.drawable.chksts3);

                        }
                        else if(requestSts.equals("0"))
                        {
                            reqImage.setVisibility(View.INVISIBLE);
                            reqImage2.setImageResource(R.drawable.chksts1);
                            Toast.makeText(rootview.getContext(), "Your request status is : "+requestSts, Toast.LENGTH_LONG).show();

                        }
                        else if(elapsedDays>=1 && requestSts.equals("1"))
                        {
                            reqname.setText("It's been more than 24 hours since you have made request.If still there is no action then you can lodge a complaint.");
                            chksts.setText("Lodge Complaint");
                            reqImage.setVisibility(View.INVISIBLE);
                            reqImage2.setImageResource(R.drawable.chksts3);

                        }
                        else if(requestSts.equals("1"))
                        {
                            reqImage.setVisibility(View.INVISIBLE);
                            reqImage2.setImageResource(R.drawable.chksts2);
                            Toast.makeText(rootview.getContext(), "Your request status is : "+requestSts, Toast.LENGTH_LONG).show();

                        }
                        else if(elapsedDays>=1 && requestSts.equals("2"))
                        {
                            reqname.setText("It's been more than 24 hours since you have made request.If still there is no action then you can lodge a complaint.");
                            chksts.setText("Lodge Complaint");
                            reqImage.setVisibility(View.INVISIBLE);
                            reqImage2.setImageResource(R.drawable.chksts3);

                        }
                        else if(requestSts.equals("2")) {
                            reqImage.setVisibility(View.INVISIBLE);
                            reqImage2.setImageResource(R.drawable.chksts3);
                            Toast.makeText(rootview.getContext(), "Your request status is : "+requestSts, Toast.LENGTH_LONG).show();


                        }
                        else if(requestSts.equals("3"))
                        {
                            encodedStringImage = json.getString("image");

                            reqImage.setVisibility(View.INVISIBLE);
                            reqImage2.setImageResource(R.drawable.chksts4);
                            Toast.makeText(rootview.getContext(), "Your request status is : "+requestSts, Toast.LENGTH_LONG).show();
                            chksts.setText("View Image");
                        }
                         else if(requestSts.equals("-1"))
                         {
                             reqImage.setVisibility(View.INVISIBLE);
                             reqImage2.setImageResource(R.drawable.chksts0);
                             Toast.makeText(rootview.getContext(), "Your request status is : "+requestSts, Toast.LENGTH_LONG).show();

                         }

                            pDialog.dismiss();




                    } else {

                        pDialog.dismiss();

                    }
                }
            } catch (NullPointerException e) {
                Log.d("Null Pointr", "null pointr");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
