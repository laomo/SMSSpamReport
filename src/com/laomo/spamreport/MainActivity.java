package com.laomo.spamreport;

import java.util.List;

import android.app.ListActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ProgressBar;

import com.laomo.spamreport.adapter.SmsAdapter;
import com.laomo.spamreport.bean.SmsInfo;
import com.laomo.spamreport.utils.Contants;
import com.laomo.spamreport.utils.Utils;

public class MainActivity extends ListActivity {

    private SmsAdapter mSmsAdapter;
    private ProgressBar mProgressBar;
    private SharedPreferences mSharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.activity_main);
	mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
	getListView().setOnCreateContextMenuListener(this);
	mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
    }

    @Override
    protected void onResume() {
	super.onResume();
	boolean isInit = mSharedPreferences.getBoolean(Contants.IS_INIT, true);
	if (isInit) {
	    mSharedPreferences.edit().putBoolean(Contants.IS_INIT, false)
		.putString(getString(R.string.key_list_operator), String.valueOf(Utils.getMobileType(this))).commit();
	    Utils.showDialog(this);
	} else {
	    new MyAsyncTask().execute();
	}
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
	menu.add(0, 1, 1, R.string.settings).setIcon(android.R.drawable.ic_menu_preferences);
	return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
	startActivity(new Intent(this, SettingsActivity.class));
	return true;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo) {
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
		Utils.reportSms(this, smsinfo);
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		boolean autoDelete = sharedPreferences.getBoolean(getString(R.string.key_auto_delete), false);
		if (autoDelete) {
		    mSmsAdapter.deleteSms(info.position);
		}
		return true;
	    case 1:
		Utils.deleteSms(this, smsinfo.threadId);
		mSmsAdapter.deleteSms(info.position);
		return true;
	}
	return super.onContextItemSelected(item);
    }

    class MyAsyncTask extends AsyncTask<Void, Void, List<SmsInfo>> {

	@Override
	protected void onPreExecute() {
	    super.onPreExecute();
	    mProgressBar.setVisibility(View.VISIBLE);
	}

	@Override
	protected List<SmsInfo> doInBackground(Void... params) {
	    return Utils.getSmsInfoList(MainActivity.this);
	}

	@Override
	protected void onPostExecute(List<SmsInfo> result) {
	    mSmsAdapter = new SmsAdapter(MainActivity.this, result);
	    setListAdapter(mSmsAdapter);
	    mProgressBar.setVisibility(View.GONE);
	}
    }
}
