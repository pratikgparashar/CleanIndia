package com.example.pratik.cleanindia;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;

/**
 * Created by Pratik on 04-Mar-15.
 */
public class menu1_fragment extends Fragment implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {


    private static final int CAMERA_REQUEST = 1888;
    private long id;
    double latitude;
    double longitude;
    View rootview;

    TextView clickPhoto,location;
    private static String KEY_SUCCESS = "success";
    EditText name, email, phone;
    ImageView srcimg;
    String encodedImage;
    Button submit;
    private static final String TAG = MainActivity.class.getSimpleName();

    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;

    private Location mLastLocation;

    // Google client to interact with Google API
    private GoogleApiClient mGoogleApiClient;
    String dateString;
    // boolean flag to toggle periodic location updates
    private boolean mRequestingLocationUpdates = false;

    private LocationRequest mLocationRequest;

    // Location updates intervals in sec
    private static int UPDATE_INTERVAL = 5000; // 10 sec
    private static int FATEST_INTERVAL = 2000; // 5 sec
    private static int DISPLACEMENT = 10; // 10 meters


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {


        LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);;
        rootview = inflater.inflate(R.layout.menu1_layout, container, false);
        clickPhoto = (TextView) rootview.findViewById(R.id.clickPhoto);
        srcimg = (ImageView) rootview.findViewById(R.id.SrcImage);
        name = (EditText) rootview.findViewById(R.id.name);
        email = (EditText) rootview.findViewById(R.id.email);
        phone = (EditText) rootview.findViewById(R.id.phone);
        submit = (Button) rootview.findViewById(R.id.submit);

        UserEmailFetcher fetchEmail = new UserEmailFetcher();
        String accName = fetchEmail.getEmail(rootview.getContext());
        email.setText(accName);
        Calendar c = Calendar.getInstance();
        int mYear = c.get(Calendar.YEAR);
        int mMonth = c.get(Calendar.MONTH);
        int mDay = c.get(Calendar.DAY_OF_MONTH);
        dateString = String.valueOf(mDay)+" / " + String.valueOf(mMonth)+" / " + String.valueOf(mYear) ;
        location = (TextView) rootview.findViewById(R.id.location);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
        else {

        }
       if (checkPlayServices()) {

            // Building the GoogleApi client
           buildGoogleApiClient();

          createLocationRequest();

      }
       //  new ProgressTask().execute();
          displayLocation();



        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (name.getText().toString().equals("") || phone.getText().toString().equals("")) {
                    Toast.makeText(rootview.getContext(), "Please fill all details.", Toast.LENGTH_LONG).show();
                } else {

                    Bitmap viewBitmap = Bitmap.createBitmap(srcimg.getWidth(), srcimg.getHeight(), Bitmap.Config.ARGB_8888);//i is imageview whch u want to convert in bitmap
                    Canvas canvas = new Canvas(viewBitmap);

                    srcimg.draw(canvas);
                    encodedImage = getStringFromBitmap(viewBitmap);
                    displayLocation();
                    NetAsync(v);
                }
            }
        });
        clickPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_REQUEST);
            }
        });


        return rootview;


    }

    private String getStringFromBitmap(Bitmap bitmapPicture) {
    	 /*
    	 * This functions converts Bitmap picture to a string which can be
    	 * JSONified.
    	 * */
        final int COMPRESSION_QUALITY = 100;
        String encodedImage;
        ByteArrayOutputStream byteArrayBitmapStream = new ByteArrayOutputStream();
        bitmapPicture.compress(Bitmap.CompressFormat.PNG, COMPRESSION_QUALITY,
                byteArrayBitmapStream);
        byte[] b = byteArrayBitmapStream.toByteArray();
        encodedImage = Base64.encodeToString(b, Base64.DEFAULT);
        return encodedImage;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            srcimg.setImageBitmap(photo);
        }
    }

    public void NetAsync(View view) {
        new NetCheck().execute();
    }


    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil
                .isGooglePlayServicesAvailable(rootview.getContext());
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, getActivity(),
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Toast.makeText(rootview.getContext(),
                        "This device is not supported.", Toast.LENGTH_LONG)
                        .show();
                getActivity().finish();
            }
            return false;
        }
        return true;
    }

    public void onStart() {
        super.onStart();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    /**
     * Method to display the location on UI
     * */
    private void displayLocation() {

        mLastLocation = LocationServices.FusedLocationApi
                .getLastLocation(mGoogleApiClient);

        if (mLastLocation != null) {
             latitude = mLastLocation.getLatitude();
             longitude = mLastLocation.getLongitude();

            location.setText("Your Location : \n"+"Latitude :"+latitude + ", "+"Longitude :" + longitude);

        } else {

            location
                    .setText("(Couldn't get the location. Make sure location is enabled on the device)");
        }
    }

    /**
     * Creating google api client object
     * */
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
    }

    /**
     * Creating location request object
     * */
    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FATEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);
    }


    @Override
    public void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        displayLocation();

    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onLocationChanged(Location location) {

        Log.d("location ","changing location ");
      //  displayLocation();
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = "
                + result.getErrorCode());

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
            String name1 = name.getText().toString();
            String email1 = email.getText().toString();
            String phone1 = phone.getText().toString();

            String caption = "New";
            UserFunctions userFunction = new UserFunctions();
            JSONObject json = userFunction.uploadimage(caption, encodedImage, name1, email1, phone1,longitude,latitude);
            Log.d("upload", "1");
            return json;
        }

        @Override
        protected void onPostExecute(JSONObject json) {
            try {
                if (json.getString(KEY_SUCCESS) != null) {

                    String res = json.getString(KEY_SUCCESS);

                    if (Integer.parseInt(res) == 1) {

                        DBAdapter db = new DBAdapter(rootview.getContext()); // calls the constructor > which calls the onCreate()

                        String receivedImage =json.getString("image");
                        String requestid = json.getString("id");
                        //---add a contact---
                        android.text.format.DateFormat df = new android.text.format.DateFormat();
                        String timeAcc = df.format("dd/M/yyyy hh:mm:ss", new java.util.Date()).toString();
                        db.open();
                        id = db.insertRequest(Integer.parseInt(requestid),name.getText().toString(),dateString,receivedImage,timeAcc);
                        Log.d("Time",timeAcc);
                        db.close();

                        Toast.makeText(rootview.getContext(), "Your request id is : "+requestid, Toast.LENGTH_LONG).show();
                        Toast.makeText(rootview.getContext(), "Your request has been submitted successfully. Than You! ", Toast.LENGTH_LONG).show();
                        final FragmentTransaction ft = getFragmentManager().beginTransaction();
                        Fragment thankyou = null ;
                        thankyou = new menu5_fragment();
                        ft.replace(R.id.container,thankyou);
                        ft.commit();

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

