package com.yu.allen.represent;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.wearable.view.WearableListView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static android.view.LayoutInflater.*;

/**
 * Created by Allen on 2/29/2016.
 */
public class RepsFoundActivity extends Activity implements WearableListView.ClickListener {

    String TAG = "RepsFoundActivity";
    Context mContext;

    ArrayList<JSONObject> representatives;

    ArrayList<RepItem> listItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.reps_found_activity);

        mContext = this;
        listItems = new ArrayList<>();

        String url = getIntent().getStringExtra("url");
        String data = getIntent().getStringExtra("data");
        Log.e(TAG, "data is: " + data);
        //Log.e(TAG, "url: " + url);
        //new HandlerTask().execute(url);

        JSONArray arr = null;

        try {
            JSONObject json = new JSONObject(data);
            arr = json.getJSONArray("results");
        } catch (Exception e) {
            e.printStackTrace();
        }

        for (int i = 0; i < arr.length(); i++) {
            try {
                JSONObject currJSON = arr.getJSONObject(i);
                RepItem ritem = new RepItem();
                String bioId = currJSON.getString("bioguide_id");
                String name = currJSON.getString("first_name");
                name += " " + currJSON.getString("last_name");
                String party = (currJSON.getString("party").equals("R")) ? "Republican" : "Democrat";
                String email = currJSON.getString("oc_email");
                String website = currJSON.getString("website");
                String endDate = currJSON.getString("term_end");
                String twitter_id = currJSON.getString("twitter_id");

                ritem.bioId = bioId;
                ritem.name = name;
                ritem.party = party;
                ritem.email = email;
                ritem.website = website;
                ritem.endDate = endDate;
                ritem.twitter_id = twitter_id;

                listItems.add(ritem);
            } catch (Exception e) {
                e.getStackTrace();
            }
        }

        final String location = getIntent().getStringExtra("Location");

        // set button for president campaign activity
        Button presButton = (Button) findViewById(R.id.reps_found_pres_campaign);
        presButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent campaignActivity = new Intent(mContext, PresidentCampaignActivity.class);
                campaignActivity.putExtra("Location", location);
                startActivity(campaignActivity);
            }
        });

        // Get the list component from the layout of the activity
        WearableListView listView =
                (WearableListView) findViewById(R.id.reps_found_listview);

        // Assign an adapter to the list
        listView.setAdapter(new Adapter(this, listItems));

        // Set a click listener
        listView.setClickListener(this);
    }

    private class HandlerTask extends AsyncTask<String, Void, ArrayList<JSONObject>> {

        @Override
        protected ArrayList<JSONObject> doInBackground(String... urls) {
            try {
                URL url = new URL(urls[0]);

                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                InputStream is = connection.getInputStream();

                String result = readStream(is);
                Log.e(TAG, "result: " + result);
                JSONObject legislatorsJSON = new JSONObject(result);

                final JSONArray reps = legislatorsJSON.getJSONArray("results");

                representatives = new ArrayList<>();
                // populate the representatives data
                for (int i = 0; i < reps.length(); i++) {
                    Log.e(TAG, "rep: " + reps.getJSONObject(i));
                    representatives.add(reps.getJSONObject(i));
                }

                return representatives;

            } catch (Exception e) {
                Log.e(TAG, "error occurred in async task: " + e.getMessage());
                return null;
            }
        }

        @Override
        protected void onPostExecute(ArrayList<JSONObject> result) {
            for (JSONObject currJSON : result) {
                try {
                    RepItem ritem = new RepItem();
                    String bioId = currJSON.getString("bioguide_id");
                    String name = currJSON.getString("first_name");
                    name += " " + currJSON.getString("last_name");
                    String party = (currJSON.getString("party").equals("R")) ? "Republican" : "Democrat";
                    String email = currJSON.getString("oc_email");
                    String website = currJSON.getString("website");
                    String endDate = currJSON.getString("term_end");
                    String twitter_id = currJSON.getString("twitter_id");

                    ritem.bioId = bioId;
                    ritem.name = name;
                    ritem.party = party;
                    ritem.email = email;
                    ritem.website = website;
                    ritem.endDate = endDate;
                    ritem.twitter_id = twitter_id;

                    listItems.add(ritem);
                } catch (Exception e) {
                    e.getStackTrace();
                }
            }
            //mAdapter.notifyDataSetChanged();
        }
    }

    private String readStream(InputStream s) {
        try {
            ByteArrayOutputStream bo = new ByteArrayOutputStream();
            int i = s.read();
            while (i != -1) {
                bo.write(i);
                i = s.read();
            }
            return bo.toString();
        } catch (Exception e) {
            Log.e(TAG, "error in reading stream");
            return "";
        }
    }

    // WearableListView click listener
    @Override
    public void onClick(WearableListView.ViewHolder v) {
        Integer tag = (Integer) v.itemView.getTag();
        // use this data to complete some action ...
        Intent repSelected = new Intent(getBaseContext(), WatchToPhoneService.class);
        //repSelected.putExtra("REPS", "Barbara Lee");
        repSelected.putExtra("position", v.getAdapterPosition());
        startService(repSelected);
    }

    @Override
    public void onTopEmptyRegionClick() {
    }

    private static final class Adapter extends WearableListView.Adapter {
        private ArrayList<RepItem> mDataset;
        private final Context mContext;
        private final LayoutInflater mInflater;

        // Provide a suitable constructor (depends on the kind of dataset)
        public Adapter(Context context, ArrayList<RepItem> dataset) {
            mContext = context;
            mInflater = from(context);
            mDataset = dataset;
        }

        // Provide a reference to the type of views you're using
        public static class ItemViewHolder extends WearableListView.ViewHolder {
            private TextView name;
            private TextView party;
            private TextView email;
            private TextView website;

            public ItemViewHolder(View itemView) {
                super(itemView);
                // find the text view within the custom item's layout
                name = (TextView) itemView.findViewById(R.id.name);
                party = (TextView) itemView.findViewById(R.id.party);
                email = (TextView) itemView.findViewById(R.id.email);
                website = (TextView) itemView.findViewById(R.id.website);
            }
        }

        // Create new views for list items
        // (invoked by the WearableListView's layout manager)
        @Override
        public WearableListView.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                              int viewType) {
            // Inflate our custom layout for list items

            return new ItemViewHolder(mInflater.inflate(R.layout.reps_found_item, null));
        }

        // Replace the contents of a list item
        // Instead of creating new views, the list tries to recycle existing ones
        // (invoked by the WearableListView's layout manager)
        @Override
        public void onBindViewHolder(WearableListView.ViewHolder holder,
                                     int position) {
            // retrieve the text view
            ItemViewHolder itemHolder = (ItemViewHolder) holder;
            TextView item_name = itemHolder.name;
            TextView item_party = itemHolder.party;
            TextView item_email = itemHolder.email;
            TextView item_website = itemHolder.website;
            // replace text contents
            RepItem ritem = mDataset.get(position);

            item_name.setText(ritem.name);
            item_party.setText(ritem.party);
            item_email.setText(ritem.email);
            item_website.setText(ritem.website);

            // replace list item's metadata
            holder.itemView.setTag(position);
        }

        // Return the size of your dataset
        // (invoked by the WearableListView's layout manager)
        @Override
        public int getItemCount() {
            return mDataset.size();
        }
    }

    class RepItem {
        String bioId;
        String name;
        String party;
        String email;
        String website;
        String endDate;
        String twitter_id;
    }

}
