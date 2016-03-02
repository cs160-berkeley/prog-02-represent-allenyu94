package com.yu.allen.represent;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

/**
 * Created by Allen on 2/29/2016.
 */
public class PresidentCampaignActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.president_campaign);

        String location = getIntent().getStringExtra("Location");

        TextView state = (TextView) findViewById(R.id.text_state);
        state.setText(location);

    }

}
