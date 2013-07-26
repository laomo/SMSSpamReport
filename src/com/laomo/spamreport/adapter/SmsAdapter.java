package com.laomo.spamreport.adapter;

import java.util.List;

import android.content.Context;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.laomo.spamreport.R;
import com.laomo.spamreport.bean.SmsInfo;

public class SmsAdapter extends BaseAdapter {
    private List<SmsInfo> list;
    @SuppressWarnings("unused")
    private Context mContext;
    private LayoutInflater mInflater;


    public SmsAdapter(Context context, List<SmsInfo> list) {
	mContext = context;
	mInflater = LayoutInflater.from(context);
	this.list = list;
    }

    /**
     * 删除已选中的短信
     */
	public void deleteSms(int position) {
	list.remove(position);
	notifyDataSetChanged();
    }

    @Override
    public int getCount() {
	return list.size();
    }

    @Override
    public SmsInfo getItem(int position) {
	return list.get(position);
    }

    @Override
    public long getItemId(int position) {
	return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
	ViewHolder holder;
	if (convertView == null) {
	    holder = new ViewHolder();
	    convertView = mInflater.inflate(R.layout.item_smsbackup, null);
	    holder.nameView = (TextView) convertView.findViewById(R.id.name);
	    holder.contentView = (TextView) convertView.findViewById(R.id.content);
	    convertView.setTag(holder);
	} else {
	    holder = (ViewHolder) convertView.getTag();
	}
	SmsInfo sms = getItem(position);
	holder.nameView.setText(sms.address);
	Linkify.addLinks(holder.nameView, Linkify.ALL);
	holder.contentView.setText(sms.body);
	Linkify.addLinks(holder.contentView, Linkify.ALL);
	return convertView;
    }

    class ViewHolder {
	TextView nameView;
	TextView contentView;
    }
}
