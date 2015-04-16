package dk.aau.cs.psylog.sensor.module_calls_meta;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CallLog;
import android.util.Log;

import java.util.Date;

import dk.aau.cs.psylog.module_lib.DBAccessContract;
import dk.aau.cs.psylog.module_lib.IScheduledTask;

public class CallHistoryListener implements IScheduledTask {

    private ContentResolver contentResolver;
    private String[] callLogColumnProjection = {CallLog.Calls.CACHED_FORMATTED_NUMBER, CallLog.Calls.DATE, CallLog.Calls.DURATION, CallLog.Calls.TYPE};
    private static final String TABLE_NAME = "call_history";
    private static final String MODULE_NAME = "module_calls_meta";

    private static final String CALLER = "caller";
    private static final String DATE = "date";
    private static final String LENGTH = "length";
    private static final String INCOMING = "incoming";
    private static final String ANSWERED = "answered";

    public CallHistoryListener(Context context)
    {
        contentResolver = context.getContentResolver();
    }

    private long getTime()
    {
        Cursor cursor = contentResolver.query(Uri.parse(DBAccessContract.DBACCESS_CONTENTPROVIDER + MODULE_NAME + "_" + TABLE_NAME), new String[]{"MAX(" + DATE + ")"}, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            return cursor.getLong(0);
        }
        return 0;
    }

    @Override
    public void doTask() {
        ContentValues contentValues = new ContentValues();
        Cursor callLogCursor = contentResolver.query(CallLog.Calls.CONTENT_URI, callLogColumnProjection, CallLog.Calls.DATE + " > ?", new String[]{Long.toString(getTime())}, null);
        if (callLogCursor != null) {
            while (callLogCursor.moveToNext()) {
                contentValues.put(CALLER, callLogCursor.getString(callLogCursor.getColumnIndex(CallLog.Calls.CACHED_FORMATTED_NUMBER)));
                contentValues.put(DATE, callLogCursor.getString(callLogCursor.getColumnIndex(CallLog.Calls.DATE)));
                contentValues.put(LENGTH, callLogCursor.getString(callLogCursor.getColumnIndex(CallLog.Calls.DURATION)));
                switch (callLogCursor.getInt(callLogCursor.getColumnIndex(CallLog.Calls.TYPE))) {
                    case CallLog.Calls.INCOMING_TYPE: contentValues.put(INCOMING, 1); contentValues.put(ANSWERED, 0); break;
                    case CallLog.Calls.OUTGOING_TYPE: contentValues.put(INCOMING, 0); contentValues.put(ANSWERED, 0); break;
                    case CallLog.Calls.MISSED_TYPE: contentValues.put(ANSWERED, 0); contentValues.put(INCOMING, 1); break;
                    default: Log.d("module_calls_meta", "Unknown CallLog.Calls.TYPE.");
                }
                contentResolver.insert(Uri.parse(DBAccessContract.DBACCESS_CONTENTPROVIDER + MODULE_NAME + "_" + TABLE_NAME), contentValues);
            }
        }
    }

    @Override
    public void setParameters(Intent i) {

    }
}
