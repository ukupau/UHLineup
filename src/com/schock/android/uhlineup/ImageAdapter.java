package com.schock.android.uhlineup;

import android.R.color;
import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class ImageAdapter extends BaseAdapter {
    private Context mContext;

    public ImageAdapter(Context c) {
        mContext = c;
    }

    public int getCount() {
        return 100;
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return 0;
    }

    // create a new TextView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView textView;
        //ImageView textView;
        if (convertView == null) {  // if it's not recycled, initialize some attributes
            textView = new TextView(mContext);
        } else {
            textView = (TextView) convertView;
        }

        textView.setText(Integer.toString(position));
        textView.setTextSize(20);
        textView.setPadding(0, 5, 0, 5);
        //textView.setBackgroundResource(color.black);
        //textView.setTextColor(Color.WHITE);

        if (playerNumbers[position] == 0) {
            //textView.setVisibility(View.INVISIBLE);
            textView.setClickable(false);
            textView.setTextColor(Color.GRAY);
        }
        return textView;
    }
    
    public int[] playerNumbers;

}