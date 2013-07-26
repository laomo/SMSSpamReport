package com.laomo.spamreport.utils;

import java.util.ArrayList;
import java.util.List;

import com.laomo.spamreport.bean.SmsInfo;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.telephony.SmsManager;

public class Utils {

	public static final Uri SMS_PROVIDER = Uri.parse("content://sms/inbox");

	public static List<SmsInfo> getSmsInfoList(Context context) {
		List<SmsInfo> infos = new ArrayList<SmsInfo>();
		String[] projection = new String[] { "_id", "thread_id", "address",
				"person", "body", "date", "type", "read" };
		Cursor cusor = context.getContentResolver().query(SMS_PROVIDER,
				projection, null, null, "date desc");
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
				 * 未保存的联系人的短信才可能是垃圾短信
				 */
				if (smsinfo.person != 0) {// 跳过已保存的联系人，暂时认为不是垃圾短信
					continue;
				}
				smsinfo.id = cusor.getString(IdColumn);
				smsinfo.threadId = cusor.getInt(threadIdColumn);
				smsinfo.date = cusor.getLong(dateColumn);
				smsinfo.body = cusor.getString(smsbodyColumn);
				smsinfo.read = cusor.getInt(readColumn);
				infos.add(smsinfo);
			}
			cusor.close();
		}
		return infos;
	}

	public static void deleteSms(Context context, String smsId) {
		ContentResolver resolver = context.getContentResolver();
		resolver.delete(Uri.parse("content://sms"), "_id=?",
				new String[] { smsId });
		// resolver.delete(Uri.parse("content://sms/"+smsId), null, null);
	}

	@SuppressLint("UnlocalizedSms") 
	public static void reportSms(SmsInfo smsinfo) {
		String content = smsinfo.address + "*" + smsinfo.body;
		SmsManager smsManager = SmsManager.getDefault();
		List<String> divideContents = smsManager.divideMessage(content);
		smsManager.sendTextMessage("10086999", null, divideContents.get(0),
				null, null);
	}

}