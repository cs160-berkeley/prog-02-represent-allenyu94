package com.yu.allen.represent;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;

import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.core.models.User;
import com.twitter.sdk.android.core.services.StatusesService;

import org.json.JSONArray;
import org.json.JSONObject;

import io.fabric.sdk.android.Fabric;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    // Note: Your consumer key and secret should be obfuscated in your source code before shipping.
    private static final String TWITTER_KEY = "3UjQ2pwsvztXp2ulitpauEk2I";
    private static final String TWITTER_SECRET = "t1Hwj3AuqD2XyUzQggW5MNX3ujXjgIviyYCj9Jr9W17PiCs8Fe";


    String TAG = "MainActivity";
    Context mContext;
    GoogleApiClient mGoogleApiClient;
    LocationRequest mLocationRequest;
    Location activeLocation;

    String currZip;
    ArrayList<RepItem> listItems;

    Geocoder geocoder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TwitterAuthConfig authConfig = new TwitterAuthConfig(TWITTER_KEY, TWITTER_SECRET);
        Fabric.with(this, new Crashlytics(), new Twitter(authConfig));

        mContext = this;

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        createLocationRequest();

        geocoder = new Geocoder(this);

        listItems = new ArrayList<>();

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Button search = (Button) findViewById(R.id.search_search_btn);
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText locationInput = (EditText) findViewById(R.id.search_zip_value);
                final String zip = locationInput.getText().toString();
                if (zip != null && !zip.isEmpty()) {
                    currZip = zip;
                    Intent sendIntent = new Intent(getBaseContext(), PhoneToWatchService.class);
                    try {
                        List<Address> addresses = geocoder.getFromLocationName(zip, 1);
                        Address address = addresses.get(0);
                        Log.e(TAG, "" + address);
                        sendIntent.putExtra("latitude", address.getLatitude());
                        sendIntent.putExtra("longitude", address.getLongitude());
                    } catch (IOException e) {
                        e.getStackTrace();
                    }
                    sendIntent.putExtra("Location", zip);
                    String url = "http://congress.api.sunlightfoundation.com/legislators/locate?zip=" + zip + "&apikey=" + mContext.getString(R.string.sunlight_api_key);
                    sendIntent.putExtra("url", url);
                    //new HandlerTask().execute(url);
                    sendIntent.putExtra("REPS", "Search");
                    Log.e(TAG, "send sendIntent");
                    //startService(sendIntent);
                    Intent repsFoundActivity = new Intent(mContext, RepresentativesActivity.class);
                    //repsFoundActivity.putExtra("latitude", activeLocation.getLatitude());
                    //repsFoundActivity.putExtra("longitude", activeLocation.getLongitude());
                    repsFoundActivity.putExtra("zip", zip);
                    startActivity(repsFoundActivity);
                } else {
                    Toast.makeText(MainActivity.this, "Please Input Location", Toast.LENGTH_SHORT).show();
                }
            }
        });

        Button currLocationButton = (Button) findViewById(R.id.search_current_location);
        currLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent sendIntent = new Intent(getBaseContext(), PhoneToWatchService.class);
                sendIntent.putExtra("Location", "Berkeley");
                Log.e(TAG, "in curr loc btn: lat: " + activeLocation.getLatitude() + " long: " + activeLocation.getLongitude());
                sendIntent.putExtra("latitude", activeLocation.getLatitude());
                sendIntent.putExtra("longitude", activeLocation.getLongitude());
                sendIntent.putExtra("REPS", "Search");
                //startService(sendIntent);
                Intent repsFoundActivity = new Intent(mContext, RepresentativesActivity.class);
                repsFoundActivity.putExtra("latitude", activeLocation.getLatitude());
                repsFoundActivity.putExtra("longitude", activeLocation.getLongitude());
                startActivity(repsFoundActivity);
            }
        });

    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        Log.e(TAG, "connected to google api client");
        Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
            Log.e(TAG, "lat: " + mLastLocation.getLatitude() + " + long: " + mLastLocation.getLongitude());
            activeLocation = mLastLocation;
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.e(TAG, result.toString());
    }

    @Override
    public void onConnectionSuspended(int cause) {

    }

    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    class RepItem {
        Bitmap img;
        String tweet;
        String bioId;
        String name;
        String party;
        String email;
        String website;
        String endDate;
        String twitter_id;
    }
}
