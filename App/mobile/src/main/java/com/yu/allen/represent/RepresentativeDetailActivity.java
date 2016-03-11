package com.yu.allen.represent;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.TwitterApiClient;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by Allen on 2/26/2016.
 */
public class RepresentativeDetailActivity extends AppCompatActivity {

    String TAG = "RepresentativeDetailActivity";

    Bitmap img;
    String bioId;
    String name;
    String party;
    String endDate;
    String tweet;

    Context mContext;
    ArrayList<String> committeeList;
    ArrayList<BillItem> billsList;

    CommitteeAdapter cmAdapter;
    BillsAdapter bAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_rep_detail);

        mContext = this;

        img = getIntent().getParcelableExtra("img");
        bioId = getIntent().getStringExtra("bioId");
        name = getIntent().getStringExtra("name");
        party = getIntent().getStringExtra("party");
        endDate = getIntent().getStringExtra("endDate");
        tweet = getIntent().getStringExtra("tweet");

        Log.e(TAG, "tweetwet: " + tweet);

        ImageView rep_img = (ImageView) findViewById(R.id.reps_detail_item_img);
        TextView rep_tweet = (TextView) findViewById(R.id.reps_detail_item_tweet);
        TextView rep_name = (TextView) findViewById(R.id.reps_detail_item_name);
        TextView rep_party = (TextView) findViewById(R.id.reps_detail_item_party);
        TextView rep_endDate = (TextView) findViewById(R.id.reps_detail_item_date);

        if (img != null) {
            rep_img.setImageBitmap(img);
        }
        if (name != null) {
            rep_name.setText(name);
        }
        if (party != null) {
            rep_party.setText(party);
        }
        if (endDate != null) {
            rep_endDate.setText("end date: " + endDate);
        }
        if (tweet != null) {
            rep_tweet.setText(tweet);
        }

        ToggleButton committeeToggle = (ToggleButton) findViewById(R.id.toggle_committees);
        ToggleButton billsToggle = (ToggleButton) findViewById(R.id.toggle_bills);

        committeeToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                ListView cList = (ListView) findViewById(R.id.rep_details_committees_list);
                if (isChecked) {
                    cList.setVisibility(View.VISIBLE);
                } else {
                    cList.setVisibility(View.GONE);
                }
            }
        });

        billsToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                ListView bList = (ListView) findViewById(R.id.rep_details_bills_list);
                if (isChecked) {
                    bList.setVisibility(View.VISIBLE);
                } else {
                    bList.setVisibility(View.GONE);
                }
            }
        });

        String committeeUrl = "http://congress.api.sunlightfoundation.com/committees?member_ids=" + bioId + "&apikey=" + mContext.getString(R.string.sunlight_api_key);
        new HandlerTask().execute(committeeUrl);

        TextView tweet = (TextView) findViewById(R.id.reps_detail_item_tweet);

        ListView committeeLV = (ListView) findViewById(R.id.rep_details_committees_list);
        ListView billsLV = (ListView) findViewById(R.id.rep_details_bills_list);

        committeeList = new ArrayList<>();

        billsList = new ArrayList<>();

        cmAdapter = new CommitteeAdapter(this, R.layout.rep_detail_committee_item, committeeList);
        bAdapter = new BillsAdapter(this, R.layout.rep_detail_bill_item, billsList);

        cmAdapter.notifyDataSetChanged();
        bAdapter.notifyDataSetChanged();

        committeeLV.setAdapter(cmAdapter);
        billsLV.setAdapter(bAdapter);

        cmAdapter.notifyDataSetChanged();
        bAdapter.notifyDataSetChanged();

    }

    private class HandlerTask extends AsyncTask<String, Void, Boolean> {
        @Override
        protected Boolean doInBackground(String... urls) {
            try {
                URL url = new URL(urls[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                InputStream is = connection.getInputStream();

                String result = readStream(is);
                Log.e(TAG, "result: " + result);
                JSONObject committeesJSON = new JSONObject(result);

                JSONArray committees = committeesJSON.getJSONArray("results");

                for (int i = 0; i < committees.length(); i++) {
                    JSONObject currCommittee = committees.getJSONObject(i);
                    committeeList.add(currCommittee.getString("name"));
                }

                String billsHttp = "http://congress.api.sunlightfoundation.com/bills?sponsor_id=" + bioId + "&apikey=" + mContext.getString(R.string.sunlight_api_key);
                URL billUrl = new URL(billsHttp);
                connection = (HttpURLConnection) billUrl.openConnection();
                connection.connect();

                is = connection.getInputStream();

                result = readStream(is);

                JSONObject billsJSON = new JSONObject(result);
                JSONArray bills = billsJSON.getJSONArray("results");

                for (int j = 0; j < bills.length(); j++) {
                    JSONObject currBill = bills.getJSONObject(j);
                    BillItem b = new BillItem();
                    b.name = currBill.getString("short_title");
                    b.introDate = "introduced on: " + currBill.getString("introduced_on");
                    if (!b.name.equals("null")) {
                        billsList.add(b);
                    }
                }

                return true;

            } catch (Exception e) {
                Log.e(TAG, "error occurred in async task: " + e.getMessage());
                return null;
            }
        }

        @Override
        protected void onPostExecute(Boolean b) {
            cmAdapter.notifyDataSetChanged();
            bAdapter.notifyDataSetChanged();
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

    private class CommitteeAdapter extends ArrayAdapter<String> {

        private String TAG = "BasicListAdapter";

        private Context mContext;
        private Activity mActivity;
        private int resourceId;
        private ArrayList<String> committees = new ArrayList<>();

        public CommitteeAdapter(Context context, int resource, ArrayList<String> data) {
            super(context, resource, data);
            mContext = context;
            resourceId = resource;
            this.committees = data;
        }

        @Override
        public int getCount() {
            return committees.size();
        }

        @Override
        public String getItem(int position) {
            return committees.get(position);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View row = convertView;
            CommitteeHolder holder = null;

            if (row == null) {
                row = LayoutInflater.from(mContext).inflate(resourceId, parent, false);
                holder = new CommitteeHolder();
                holder.name = (TextView) row.findViewById(R.id.rep_details_committee_item_name);
                row.setTag(holder);
            } else {
                holder = (CommitteeHolder) row.getTag();
            }

            String item = committeeList.get(position);

            holder.name.setText(item);

            return row;

        }

    }

    class CommitteeHolder {
        TextView name;
    }

    private class BillsAdapter extends ArrayAdapter<String> {

        private String TAG = "BasicListAdapter";

        private Context mContext;
        private Activity mActivity;
        private int resourceId;
        private ArrayList<BillItem> bills = new ArrayList<>();

        public BillsAdapter(Context context, int resource, ArrayList<BillItem> data) {
            super(context, resource);
            mContext = context;
            resourceId = resource;
            this.bills = data;
        }

        @Override
        public int getCount() {
            return bills.size();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View row = convertView;
            BillsHolder holder = null;

            if (row == null) {
                row = LayoutInflater.from(mContext).inflate(resourceId, parent, false);
                holder = new BillsHolder();
                holder.name = (TextView) row.findViewById(R.id.rep_details_bill_item_name);
                holder.date = (TextView) row.findViewById(R.id.rep_details_bill_item_date);
                row.setTag(holder);
            } else {
                holder = (BillsHolder) row.getTag();
            }

            BillItem item = billsList.get(position);

            holder.name.setText(item.name);
            holder.date.setText(item.introDate);

            return row;

        }

    }

    class BillsHolder {
        TextView name;
        TextView date;
    }

    class BillItem {
        String name;
        String introDate;
    }
}
