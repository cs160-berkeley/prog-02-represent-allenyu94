package com.yu.allen.represent;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
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

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Allen on 2/26/2016.
 */
public class RepresentativesActivity extends AppCompatActivity {

    Context mContext;

    ArrayList<String> listItems;
    ListAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.content_reps_found);

        mContext = this;

        listItems = new ArrayList<String>();
        listItems.add("placeholder");

        ListView lv = (ListView) findViewById(R.id.reps_found_listview);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent repsDetailActivity = new Intent(mContext, RepresentativeDetailActivity.class);
                startActivity(repsDetailActivity);
            }
        });

        mAdapter = new ListAdapter(this, R.layout.reps_found_item, listItems);

        mAdapter.notifyDataSetChanged();

        lv.setAdapter(mAdapter);
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
        private ArrayList<String> exercises = new ArrayList<>();

        public ListAdapter(Context context, int resource, ArrayList<String> data) {
            super(context, resource, data);
            mContext = context;
            resourceId = resource;
            this.exercises = data;
        }

        @Override
        public int getCount() {
            return exercises.size();
        }

        @Override
        public String getItem(int position) {
            return exercises.get(position);
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
                row.setTag(holder);
            } else {
                holder = (ViewHolder) row.getTag();
            }

            String item = listItems.get(position);

            holder.img.setImageResource(R.drawable.barbara_lee);
            holder.name.setText("Barbara Lee");
            holder.house.setText("Democrat");
            holder.email.setText("primaryemail@email.com");

            return row;

        }

    }

    class ViewHolder {
        ImageView img;
        TextView name;
        TextView house;
        TextView email;
    }

}
