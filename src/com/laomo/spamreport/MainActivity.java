package com.laomo.spamreport;

import java.util.List;

import android.app.ListActivity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;

import com.laomo.spamreport.adapter.SmsAdapter;
import com.laomo.spamreport.bean.SmsInfo;
import com.laomo.spamreport.utils.Utils;

public class MainActivity extends ListActivity {

	private SmsAdapter mSmsAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getListView().setOnCreateContextMenuListener(this);
		new MyAsyncTask().execute();
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View view,
			ContextMenuInfo menuInfo) {
		AdapterView.AdapterContextMenuInfo info;

		try {
			info = (AdapterView.AdapterContextMenuInfo) menuInfo;
		} catch (ClassCastException e) {
			return;
		}

		SmsInfo smsinfo = mSmsAdapter.getItem(info.position);

		// Setup the menu header
		menu.setHeaderTitle(smsinfo.address);

		// View account details
		menu.add(0, 0, 0, R.string.report);
		
		menu.add(0, 1, 0, R.string.delete);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {

		AdapterView.AdapterContextMenuInfo info;
		try {
			info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
		} catch (ClassCastException e) {
			return false;
		}
		SmsInfo smsinfo = mSmsAdapter.getItem(info.position);

		switch (item.getItemId()) {
		case 0:
			Utils.reportSms(smsinfo);
			return true;
		case 1:
			Utils.deleteSms(this, smsinfo.id);
			mSmsAdapter.deleteSms(info.position);
			return true;
		}
		return super.onContextItemSelected(item);
	}

	class MyAsyncTask extends AsyncTask<Void, Void, List<SmsInfo>> {

		@Override
		protected List<SmsInfo> doInBackground(Void... params) {
			return Utils.getSmsInfoList(MainActivity.this);
		}

		@Override
		protected void onPostExecute(List<SmsInfo> result) {
			mSmsAdapter = new SmsAdapter(MainActivity.this, result);
			setListAdapter(mSmsAdapter);
		}
	}
}
