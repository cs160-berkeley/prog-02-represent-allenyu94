package com.yu.allen.represent;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.core.models.User;
import com.twitter.sdk.android.core.services.AccountService;
import com.twitter.sdk.android.core.services.StatusesService;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import io.fabric.sdk.android.Fabric;

/**
 * Created by Allen on 2/26/2016.
 */
public class RepresentativesActivity extends AppCompatActivity {

    String TAG = "RepresentativesActivity";

    Context mContext;
    double longitude;
    double latitude;
    String locateLegislators;
    ArrayList<JSONObject> representatives;
    ConcurrentHashMap<String, Bitmap> idToImg;
    ConcurrentHashMap<String, String> idToImgUrl;
    ConcurrentHashMap<String, String> idToTweet;
    ArrayList<String> texts;

    ArrayList<RepItem> listItems;
    ListAdapter mAdapter;
    JSONObject legislatorsJSON;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.content_reps_found);

        mContext = this;
        idToImg = new ConcurrentHashMap<>();
        idToTweet = new ConcurrentHashMap<>();
        texts = new ArrayList<>();

        TwitterAuthConfig authConfig = new TwitterAuthConfig(mContext.getString(R.string.twitter_api_key), mContext.getString(R.string.twitter_secret));
        Fabric.with(this, new Twitter(authConfig));

        latitude = getIntent().getDoubleExtra("latitude", -1.0);
        longitude= getIntent().getDoubleExtra("longitude", -1.0);
        String zip = getIntent().getStringExtra("zip");

        if (zip != null && !zip.isEmpty()) {
            locateLegislators = "http://congress.api.sunlightfoundation.com/legislators/locate?zip=" + zip + "&apikey=" + mContext.getString(R.string.sunlight_api_key);
        } else {
            locateLegislators = "http://congress.api.sunlightfoundation.com/legislators/locate?latitude=" + latitude + "&longitude=" + longitude + "&apikey=" + mContext.getString(R.string.sunlight_api_key);
        }

        listItems = new ArrayList<>();

        ListView lv = (ListView) findViewById(R.id.reps_found_listview);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent repsDetailActivity = new Intent(mContext, RepresentativeDetailActivity.class);
                for (JSONObject obj : representatives) {
                    RepItem ritem = listItems.get(position);
                    try {
                        if (obj.getString("bioguide_id").equals(ritem.bioId)) {
                            repsDetailActivity.putExtra("img", ritem.img);
                            repsDetailActivity.putExtra("bioId", ritem.bioId);
                            repsDetailActivity.putExtra("name", ritem.name);
                            repsDetailActivity.putExtra("party", ritem.party);
                            repsDetailActivity.putExtra("endDate", ritem.endDate);
                            Log.e(TAG, "ritem tweet: " + ritem.tweet);
                            repsDetailActivity.putExtra("tweet", ritem.tweet);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                startActivity(repsDetailActivity);
            }
        });

        new HandlerTask().execute(locateLegislators);
        mAdapter = new ListAdapter(this, R.layout.reps_found_item, listItems);

        mAdapter.notifyDataSetChanged();

        lv.setAdapter(mAdapter);
    }

    private class TwitterTask extends AsyncTask<String, Void, String> {
        String twitter_id;

        @Override
        protected String doInBackground(String... urls) {

            try {
                Log.e(TAG, "tweet url:" + urls[0]);
                Log.e(TAG, "twwt url: " + urls[1]);
                twitter_id = urls[1];
                URL tweetImgUrl = new URL(urls[0]);
                HttpURLConnection tweetImgConnection = (HttpURLConnection) tweetImgUrl.openConnection();
                tweetImgConnection.connect();
                InputStream input = tweetImgConnection.getInputStream();
                Log.e(TAG, "input " + input);

                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inScaled = false;
                Bitmap img = BitmapFactory.decodeStream(input, null, options);
                Log.e(TAG, "got img: " + img);
                idToImg.put(twitter_id, img);
                return twitter_id;
            } catch (Exception e) {
                e.printStackTrace();
                return "";
            }

        }

        @Override
        protected void onPostExecute(String result) {
            Log.e(TAG, "match this: " + twitter_id);
            for (RepItem ritem : listItems) {
                Log.e(TAG, "with this: " + ritem.twitter_id);
                if (ritem.twitter_id.equals(twitter_id)) {
                    ritem.img = idToImg.get(twitter_id);
                    break;
                }
            }
            mAdapter.notifyDataSetChanged();

        }
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
                legislatorsJSON = new JSONObject(result);

                final JSONArray reps = legislatorsJSON.getJSONArray("results");

                representatives = new ArrayList<>();
                // populate the representatives data
                for (int i = 0; i < reps.length(); i++) {
                    Log.e(TAG, "rep: " + reps.getJSONObject(i));
                    representatives.add(reps.getJSONObject(i));
                }

                TwitterSession session = Twitter.getSessionManager().getActiveSession();

                for (int j = 0; j < reps.length(); j++) {
                    final int currInt = j;
                    Log.e(TAG, "rep twitter id: " + reps.getJSONObject(j).getString("twitter_id"));
                    StatusesService statusService = Twitter.getApiClient(session).getStatusesService();
                    statusService.userTimeline(null, reps.getJSONObject(j).getString("twitter_id"), 1, null, null, null, null, null, null, new Callback<List<Tweet>>() {
                        @Override
                        public void success(Result<List<Tweet>> result) {

                            List<Tweet> tweets = result.data;
                            Tweet tweet = tweets.get(0);
                            try {
                                String twitter_id = reps.getJSONObject(currInt).getString("twitter_id");

                                String text = tweet.text;
                                String prof_img = tweet.user.profileImageUrl;

                                Log.e(TAG, "pre text: " + text);
                                Log.e(TAG, "pre img: " + prof_img);

                                Log.e(TAG, "putting fucking text in twitterid: " + text);
                                idToTweet.put(twitter_id, text);
                                texts.add(text);
                                Log.e(TAG, "idToTweet: " + idToTweet.get(twitter_id));

                                for (int i = 0; i < listItems.size(); i++) {
                                    if (listItems.get(i).twitter_id.equals(twitter_id)) {
                                        RepItem ritem = listItems.get(i);
                                        ritem.tweet = text;
                                        break;
                                    }
                                }

                                new TwitterTask().execute(new String[]{prof_img, twitter_id});

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void failure(TwitterException e) {
                            Log.e(TAG, "twitter call failed: " + e.getMessage());
                        }
                    });
                }

                return representatives;

            } catch (Exception e) {
                Log.e(TAG, "error occurred in async task: " + e.getMessage());
                return null;
            }
        }

        @Override
        protected void onPostExecute(ArrayList<JSONObject> result) {
            Intent sendIntent = new Intent(getBaseContext(), PhoneToWatchService.class);
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
            sendIntent.putExtra("data", legislatorsJSON.toString());
            startService(sendIntent);
            mAdapter.notifyDataSetChanged();
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

    private class ListAdapter extends ArrayAdapter<String> {

        private String TAG = "BasicListAdapter";

        private Context mContext;
        private Activity mActivity;
        private int resourceId;
        private ArrayList<RepItem> reps = new ArrayList<>();

        public ListAdapter(Context context, int resource, ArrayList<RepItem> data) {
            super(context, resource);
            mContext = context;
            resourceId = resource;
            this.reps = data;
        }

        @Override
        public int getCount() {
            return reps.size();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View row = convertView;
            ViewHolder holder = null;

            if (row == null) {
                row = LayoutInflater.from(mContext).inflate(resourceId, parent, false);
                holder = new ViewHolder();
                holder.img = (ImageView) row.findViewById(R.id.reps_found_item_img);
                holder.name = (TextView) row.findViewById(R.id.reps_found_item_name);
                holder.house = (TextView) row.findViewById(R.id.reps_found_item_house);
                holder.email = (TextView) row.findViewById(R.id.reps_found_item_email);
                holder.website = (TextView) row.findViewById(R.id.reps_found_item_website);
                row.setTag(holder);
            } else {
                holder = (ViewHolder) row.getTag();
            }

            RepItem item = listItems.get(position);

            holder.img.setImageBitmap(item.img);
            holder.name.setText(item.name);
            holder.house.setText(item.party);
            holder.email.setText(item.email);
            holder.website.setText(item.website);

            return row;

        }

    }

    class ViewHolder {
        ImageView img;
        TextView name;
        TextView house;
        TextView email;
        TextView website;
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
