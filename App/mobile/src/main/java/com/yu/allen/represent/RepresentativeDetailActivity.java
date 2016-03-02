package com.yu.allen.represent;

import android.app.Activity;
import android.content.Context;
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
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Allen on 2/26/2016.
 */
public class RepresentativeDetailActivity extends AppCompatActivity {

    ArrayList<String> committeeList;
    ArrayList<String> billsList;

    CommitteeAdapter cmAdapter;
    BillsAdapter bAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_rep_detail);

        ListView committeeLV = (ListView) findViewById(R.id.rep_details_committees_list);
        ListView billsLV = (ListView) findViewById(R.id.rep_details_bills_list);

        committeeList = new ArrayList<>();
        committeeList.add("committee 1");

        billsList = new ArrayList<>();
        billsList.add("bill 1");

        cmAdapter = new CommitteeAdapter(this, R.layout.rep_detail_committee_item, committeeList);
        bAdapter = new BillsAdapter(this, R.layout.rep_detail_bill_item, billsList);

        cmAdapter.notifyDataSetChanged();
        bAdapter.notifyDataSetChanged();

        committeeLV.setAdapter(cmAdapter);
        billsLV.setAdapter(bAdapter);


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

            holder.name.setText("Committee 1");

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
        private ArrayList<String> bills = new ArrayList<>();

        public BillsAdapter(Context context, int resource, ArrayList<String> data) {
            super(context, resource, data);
            mContext = context;
            resourceId = resource;
            this.bills = data;
        }

        @Override
        public int getCount() {
            return bills.size();
        }

        @Override
        public String getItem(int position) {
            return bills.get(position);
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

            String item = billsList.get(position);

            holder.name.setText("Bill 1");
            holder.date.setText("02/29/16");

            return row;

        }

    }

    class BillsHolder {
        TextView name;
        TextView date;
    }
}
