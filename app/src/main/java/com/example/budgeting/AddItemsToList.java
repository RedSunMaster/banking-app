package com.example.budgeting;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

public class AddItemsToList extends BaseAdapter {

    private JSONArray mItems;

    public AddItemsToList(JSONArray items) {
        mItems = items;
    }

    @Override
    public int getCount() {
        return mItems.length();
    }

    @Override
    public Object getItem(int position) {
        return mItems.optJSONObject(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.balance_list_fragment, parent, false);

        JSONObject item = (JSONObject) getItem(position);

        TextView textView = view.findViewById(R.id.categoryText);
        TextView textView2 = view.findViewById(R.id.amountText);
        textView.setText(item.optString("Category") + ": ");
        textView2.setText("$" + item.optString("Amount"));

        return view;
    }
}