package com.laomo.spamreport.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.laomo.spamreport.R;
import com.laomo.spamreport.SettingsActivity;
import com.laomo.spamreport.bean.SmsInfo;

public class Utils {

    public static final Uri SMS_PROVIDER = Uri.parse("content://sms/inbox");

    public static List<SmsInfo> getSmsInfoList(Context context) {
	/*
	 * 使用HashMap来处理相同号码的去重 
	 * TODO：不知道有没有什么好办法，查询时直接去重
	 */
	HashMap<String, SmsInfo> infos = new HashMap<String, SmsInfo>();
	String[] projection = new String[] { "_id", "thread_id", "address", "person", "body", "date", "type", "read" };
	Cursor cusor = context.getContentResolver().query(SMS_PROVIDER, projection, null, null, "date desc");
	int IdColumn = cusor.getColumnIndex("_id");
	int threadIdColumn = cusor.getColumnIndex("thread_id");
	int nameColumn = cusor.getColumnIndex("person");
	int phoneNumberColumn = cusor.getColumnIndex("address");
	int smsbodyColumn = cusor.getColumnIndex("body");
	int dateColumn = cusor.getColumnIndex("date");
	int typeColumn = cusor.getColumnIndex("type");
	int readColumn = cusor.getColumnIndex("read");
	if (cusor != null) {
	    SmsInfo smsinfo;
	    while (cusor.moveToNext()) {
		smsinfo = new SmsInfo();
		smsinfo.person = cusor.getInt(nameColumn);
		smsinfo.type = cusor.getInt(typeColumn);
		smsinfo.address = cusor.getString(phoneNumberColumn);

		/**
		 * person==0表示未保存联系人，未保存的联系人的短信才可能是垃圾短信，直接过滤掉已保存联系人 
		 * 简单过滤银行等服务号码，长度为5 
		 * TODO: 加强过滤条件，过滤掉肯定不是垃圾短信的部分
		 */
		if (smsinfo.person != 0 || smsinfo.address.length() == 5) {
		    continue;
		}
		smsinfo.id = cusor.getString(IdColumn);
		smsinfo.threadId = cusor.getString(threadIdColumn);
		smsinfo.date = cusor.getLong(dateColumn);
		smsinfo.body = cusor.getString(smsbodyColumn);
		smsinfo.read = cusor.getInt(readColumn);
		infos.put(smsinfo.address, smsinfo);
	    }
	    cusor.close();
	}
	return new ArrayList<SmsInfo>(infos.values());
    }

    /**
     * 删除举报的短信所在的整个会话
     * @param context
     * @param threadId
     */
    public static void deleteSms(Context context, String threadId) {
	//int result = resolver.delete(Uri.parse("content://sms"), "_id=?", new String[] { smsId });
	int result = context.getContentResolver().delete(Uri.parse("content://sms/conversations/" + threadId), null, null);
	Log.d("laomo", "delete result="+result);
    }

    public static void reportSms(Context context, SmsInfo smsinfo) {
	SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
	int operator = Integer
	    .parseInt(sharedPreferences.getString(context.getString(R.string.key_list_operator), "-1"));
	int send = Integer.parseInt(sharedPreferences.getString(context.getString(R.string.key_list_send), "-1"));
	boolean autoDelete = sharedPreferences.getBoolean(context.getString(R.string.key_auto_delete), true);
	String content = "";
	String phone = "";
	switch (operator) {
	    case Contants.CMCC_KEY:
		phone = Contants.CMCC_NUMBER;
		content = smsinfo.address + "*" + smsinfo.body;
		break;
	    case Contants.CUCC_KEY:
		//联通手机用户在收到垃圾短信时，可编辑短信“ljdxjb#被举报号码#举报内容”发到10010，中国联通将核实短信内容并根据相关规定进行处理。
		phone = Contants.CUCC_NUMBER;
		content = "ljdxjb#" + smsinfo.address + "#" + smsinfo.body;
		break;
	    case Contants.CT_KEY:
		//在您要举报的短信内容前面输入被举报的号码，再加“*”号以隔开后面的短信内容，然后发送到“12321”;
		phone = Contants.CT_NUMBER;
		content = smsinfo.address + "*" + smsinfo.body;
		break;
	    default:
		break;
	}
	//保证只发一条短信
	if (content.length() > 70) {
	    content = content.substring(0, 71);
	}
	switch (send) {
	    case Contants.SYS_SEND:
		Uri uri = Uri.parse("smsto:" + phone);
		Intent intent = new Intent(Intent.ACTION_SENDTO, uri);
		intent.putExtra("sms_body", content);
		context.startActivity(intent);
		break;
	    case Contants.APP_SEND:
		SmsManager smsManager = SmsManager.getDefault();
		smsManager.sendTextMessage(phone, null, content, null, null);
		break;
	    default:
		break;
	}
	//举报完成自动删除短信
	if (autoDelete) {
	    deleteSms(context, smsinfo.threadId);
	}
    }

    public static void showDialog(final Activity activity) {
	AlertDialog.Builder builder = new Builder(activity);
	builder.setMessage(R.string.dialog_content);
	builder.setTitle(R.string.tips);
	builder.setPositiveButton(R.string.dialog_ok, new OnClickListener() {
	    @Override
	    public void onClick(DialogInterface dialog, int which) {
		dialog.dismiss();
		activity.startActivity(new Intent(activity, SettingsActivity.class));
	    }
	}).setNegativeButton(R.string.dialog_cancel, new OnClickListener() {
	    @Override
	    public void onClick(DialogInterface dialog, int which) {
		dialog.dismiss();
	    }
	});
	builder.create().show();
    }

    /**
     * 获取手机卡类型，移动、联通、电信
     */
    public static int getMobileType(Context context) {
	int type = 0;
	TelephonyManager iPhoneManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
	String iNumeric = iPhoneManager.getSimOperator();
	if (iNumeric.length() > 0) {
	    if (iNumeric.equals("46000") || iNumeric.equals("46002")) {
		// 中国移动
		type = Contants.CMCC_KEY;
	    } else if (iNumeric.equals("46001")) {
		// 中国联通
		type = Contants.CUCC_KEY;
	    } else if (iNumeric.equals("46003")) {
		// 中国电信
		type = Contants.CT_KEY;
	    }
	}
	return type;
    }

}