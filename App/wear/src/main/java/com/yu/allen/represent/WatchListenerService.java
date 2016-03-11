package com.yu.allen.represent;

import android.content.Intent;
import android.util.Log;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

import java.nio.charset.StandardCharsets;

/**
 * Created by joleary and noon on 2/19/16 at very late in the night. (early in the morning?)
 */
public class WatchListenerService extends WearableListenerService {
    // In PhoneToWatchService, we passed in a path, either "/FRED" or "/LEXY"
    // These paths serve to differentiate different phone-to-watch messages
    private static final String URL = "/Data";
    private static final String LEXY_FEED = "/Lexy";

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        Log.e("asdf", "messageEvent: " + messageEvent);
        Log.e("T", "in WatchListenerService, got: " + messageEvent.getPath());
        //use the 'path' field in sendmessage to differentiate use cases
        //(here, fred vs lexy)

        if( messageEvent.getPath().equalsIgnoreCase( URL ) ) {
            String value = new String(messageEvent.getData(), StandardCharsets.UTF_8);
            Intent intent = new Intent(this, RepsFoundActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            //you need to add this flag since you're starting a new activity from a service
            //intent.putExtra("CAT_NAME", "Fred");
            Log.e("T", "url value is: " + value);
            intent.putExtra("data", value);
            startActivity(intent);
        } else if (messageEvent.getPath().equalsIgnoreCase( LEXY_FEED )) {
            String value = new String(messageEvent.getData(), StandardCharsets.UTF_8);
            Intent intent = new Intent(this, MainActivity.class );
            intent.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
            //you need to add this flag since you're starting a new activity from a service
            intent.putExtra("CAT_NAME", "Lexy");
            Log.d("T", "about to start watch MainActivity with CAT_NAME: Lexy");
            startActivity(intent);
        } else {
            super.onMessageReceived( messageEvent );
        }

    }
}