/*
 * Copyright (C) 2009 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fi.viinikoodi.history;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.nio.charset.Charset;
import java.text.DateFormat;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.Locale;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.json.JSONObject;

import android.util.Log;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.android.AndroidHttpClient;
import com.google.zxing.client.android.Intents;
import com.google.zxing.client.android.CaptureActivity;
import com.google.zxing.client.result.ParsedResultType;
import com.google.zxing.Result;

import fi.viinikoodi.client.android.R;
import fi.viinikoodi.result.WineParsedResult;

/**
 * <p>Manages functionality related to scan history.</p>
 * 
 * @author Sean Owen
 */
public final class HistoryManager {

  private static final String TAG = HistoryManager.class.getSimpleName();

  private static final int MAX_ITEMS = 40;
  private static final String[] GET_ITEM_COL_PROJECTION = {
	  DBHelper.ALKOID_COL,
	  DBHelper.NAME_COL,
      DBHelper.PRICE_COL,
      DBHelper.TYPE_COL,
      DBHelper.GRAPES_COL,
      DBHelper.COUNTRY_COL,
      DBHelper.URL_COL,
      DBHelper.IMAGEURL_COL,
      DBHelper.TIMESTAMP_COL,
      DBHelper.TEXT_COL,
      DBHelper.ID_COL,
      DBHelper.RATING_COL,
      DBHelper.NOTES_COL
  };
  private static final String[] EXPORT_COL_PROJECTION = {
	  DBHelper.ID_COL,
	  DBHelper.ALKOID_COL,
	  DBHelper.EAN_COL,
	  DBHelper.NAME_COL,
	  DBHelper.PRICE_COL,
	  DBHelper.COUNTRY_COL,
	  DBHelper.TYPE_COL,
	  DBHelper.GRAPES_COL,
	  DBHelper.TEXT_COL,
	  DBHelper.URL_COL,
      DBHelper.RATING_COL,
      DBHelper.NOTES_COL,
      DBHelper.TIMESTAMP_COL,
  };
  private static final String[] ID_COL_PROJECTION = { 
	  DBHelper.ID_COL, 
	  DBHelper.IMAGEURL_COL,
	  DBHelper.RATING_COL,
      DBHelper.NOTES_COL
  };
  
  private static final DateFormat EXPORT_DATE_TIME_FORMAT = DateFormat.getDateTimeInstance();
  
  private static final String USER_AGENT = "Viinikoodi (Android)";
  private NetworkThread networkThread;
  
  private final CaptureActivity activity;

  private final Handler handler = new Handler() {
    @Override
    public void handleMessage(Message message) {
      switch (message.what) {
        case R.id.search_wine_succeeded:
        	handleSearchResults((JSONObject) message.obj);
        	resetForNewQuery();
        	break;
        case R.id.search_wine_failed:
            resetForNewQuery();
            break;
      }
    }
  };
  
  public HistoryManager(CaptureActivity activity) {
    this.activity = activity;
  }

  public static List<WineParsedResult> getHistoryItems(Context context) {
    SQLiteOpenHelper helper = new DBHelper(context);
    List<WineParsedResult> items = new ArrayList<WineParsedResult>();
    SQLiteDatabase db = helper.getReadableDatabase();
    //db.delete(DBHelper.TABLE_NAME, null, null);
    Cursor cursor = null;
    try {
      cursor = db.query(DBHelper.TABLE_NAME,
                        GET_ITEM_COL_PROJECTION,
                        null, null, null, null,
                        DBHelper.TIMESTAMP_COL + " DESC");
      int count = 0;
      while (cursor.moveToNext()) {
    	  WineParsedResult result = new WineParsedResult(ParsedResultType.PRODUCT);
    	  result.setAlkoID(cursor.getString(0));
    	  result.setWineName(cursor.getString(1));
    	  result.setPrice(cursor.getString(2));
    	  result.setWineType(cursor.getString(3));
    	  result.setGrapes(cursor.getString(4));
    	  result.setCountry(cursor.getString(5));
    	  result.setUrl(cursor.getString(6));
    	  result.setImageUrl(cursor.getString(7));
    	  result.setTimestamp(cursor.getInt(8));
    	  result.setDescription(cursor.getString(9));
    	  result.setID(cursor.getInt(10));
    	  result.setRating(cursor.getInt(11));
    	  result.setNotes(cursor.getString(12));
    	  Log.d(TAG,"[" + count + "] getHistoryItems=" + cursor.getInt(11));
    	  count++;
    	  items.add(result);
      }
    } finally {
      if (cursor != null) {
        cursor.close();
      }
      db.close();
    }
    return items;
  }

  public void addHistoryItem(Result result) {

    if (!activity.getIntent().getBooleanExtra(Intents.Scan.SAVE_HISTORY, true)) {
      return; // Do not save this item to the history.
    }
    
    startNetworkSearch(result.getText());
    //startNetworkSearch("3142804114020");
  }

  public static void trimHistory(Context context) {
    SQLiteOpenHelper helper = new DBHelper(context);
    SQLiteDatabase db = helper.getWritableDatabase();
    Cursor cursor = null;
    try {
    	cursor = db.query(DBHelper.TABLE_NAME,
                        ID_COL_PROJECTION,
                        null, null, null, null,
                        DBHelper.TIMESTAMP_COL + " DESC");
    	int count = 0;
    	List<String> keepItems = new ArrayList<String>();
    	while (count < MAX_ITEMS && cursor.moveToNext()) {
    		Log.d(TAG, "[" + count + "] Keep File: " + cursor.getString(0) + "," + cursor.getString(1));
    		keepItems.add(cursor.getString(1));
    		count++;
    	}
    	//List<String> items = new ArrayList<String>();
    	while (cursor.moveToNext()) {
    		Integer rating = cursor.getInt(2);
  		  	String notes = cursor.getString(3);
  		  	if ((rating == null || rating == WineParsedResult.NO_RATING) && (notes == null || notes.equals(""))) {
  		  		db.delete(DBHelper.TABLE_NAME, DBHelper.ID_COL + '=' + cursor.getString(0), null);
  		  		Log.d(TAG, "[" + count + "] To delete File: " + cursor.getString(0) + "," + cursor.getString(1) + ", " + rating + ", " + notes);
  		  		//items.add(cursor.getString(1));
  		  	} else {
  		  		keepItems.add(cursor.getString(1));
  		  		Log.d(TAG, "[" + count + "] NOT to delete File: " + cursor.getString(0) + "," + cursor.getString(1)+ ", " + rating + ", " + notes);
  		  	}
  		  	count++;
    	}
    	//if (!items.isEmpty()) ImageLoader.deleteFiles(items,context);
    	ImageLoader.keepFiles(keepItems,context);
    } finally {
    	if (cursor != null) {
    		cursor.close();
    	}
    	db.close();
    }
  }

  /**
   * <p>Builds a text representation of the scanning history. Each scan is encoded on one
   * line, terminated by a line break (\r\n). The values in each line are comma-separated,
   * and double-quoted. Double-quotes within values are escaped with a sequence of two
   * double-quotes. The fields output are:</p>
   *
   * <ul>
   *  <li>Raw text</li>
   *  <li>Display text</li>
   *  <li>Format (e.g. QR_CODE)</li>
   *  <li>Timestamp</li>
   *  <li>Formatted version of timestamp</li>
   * </ul>
   */
  public static Uri buildHistory(Context context) {
	File bsRoot = new File(Environment.getExternalStorageDirectory(), "Viinikoodi");
	File historyRoot = new File(bsRoot, "History");
	if (!historyRoot.exists() && !historyRoot.mkdirs()) {
	  Log.w(TAG, "Couldn't make dir " + historyRoot);
	  return null;
	}
	Calendar cal = Calendar.getInstance();	
	String date = android.text.format.DateFormat.format("MMddyy-hmmaa",cal).toString();
	
	File historyFile = new File(historyRoot, "history-" + date + ".csv");
	OutputStreamWriter out = null;
	try{
		out = new OutputStreamWriter(new FileOutputStream(historyFile), Charset.forName("UTF-8"));
	} catch(Exception ex){
		return null;
	}
	//StringBuilder historyText = new StringBuilder(1000);
    SQLiteOpenHelper helper = new DBHelper(context);
    SQLiteDatabase db = helper.getReadableDatabase();
    Cursor cursor = null;
    try {
      cursor = db.query(DBHelper.TABLE_NAME,
                        EXPORT_COL_PROJECTION,
                        null, null, null, null,
                        DBHelper.TIMESTAMP_COL + " DESC");
      while (cursor.moveToNext()) {
    	  StringBuilder historyText = new StringBuilder(1000);
    	  for (int col = 0; col < EXPORT_COL_PROJECTION.length; col++) {
    		  historyText.append('"').append(massageHistoryField(cursor.getString(col))).append("\",");
    	  }
    	  // Add timestamp again, formatted
    	  long timestamp = cursor.getLong(EXPORT_COL_PROJECTION.length - 1);
    	  historyText.append('"').append(massageHistoryField(
            EXPORT_DATE_TIME_FORMAT.format(new Date(timestamp)))).append("\"\r\n");
    	  //Log.d(TAG, historyText.toString());
          out.write(historyText.toString());
      }
    } catch (IOException ioe) {
          Log.w(TAG, "Couldn't access file " + historyFile + " due to " + ioe);
          return null;
    } finally {
      if (cursor != null) {
        cursor.close();
      }
      db.close();
      if (out != null) {
          try {
            out.close();
          } catch (IOException ioe) {
            // do nothing
          }
      }
    }
    return Uri.parse("file://" + historyFile.getAbsolutePath());
    //return historyText;
  }
/*
  public static Uri saveHistory(String history) {
    File bsRoot = new File(Environment.getExternalStorageDirectory(), "Viinikoodi");
    File historyRoot = new File(bsRoot, "History");
    if (!historyRoot.exists() && !historyRoot.mkdirs()) {
      Log.w(TAG, "Couldn't make dir " + historyRoot);
      return null;
    }
    File historyFile = new File(historyRoot, "history-" + System.currentTimeMillis() + ".csv");
    OutputStreamWriter out = null;
    try {
      out = new OutputStreamWriter(new FileOutputStream(historyFile), Charset.forName("UTF-8"));
      out.write(history);
      return Uri.parse("file://" + historyFile.getAbsolutePath());
    } catch (IOException ioe) {
      Log.w(TAG, "Couldn't access file " + historyFile + " due to " + ioe);
      return null;
    } finally {
      if (out != null) {
        try {
          out.close();
        } catch (IOException ioe) {
          // do nothing
        }
      }
    }
  }
*/
  private static String massageHistoryField(String value) {
	  if (value == null) return "";
	  return value.replace("\"","\"\"");
  }

  void clearHistory() {
    SQLiteOpenHelper helper = new DBHelper(activity);
    SQLiteDatabase db = helper.getWritableDatabase();
    try {
      db.delete(DBHelper.TABLE_NAME, null, null);
    } finally {
      db.close();
    }
  }

  // Currently there is no way to distinguish between a query which had no results and a book
  // which is not searchable - both return zero results.
  private void handleSearchResults(JSONObject json) {
	
	  WineParsedResult wpr = new WineParsedResult();
	  if (!wpr.parse(json)) {
		  Handler activityHandler = activity.getHandler();
		  String query_url = wpr.getQuery_url();
		  if (query_url != null && !query_url.equals("")) {
			  Message message = Message.obtain(activityHandler, R.id.search_wine_failed);
			  Log.d(TAG, "Got wine query url: " + query_url);
			  message.obj = query_url;
			  message.sendToTarget();
			  return;
		  }
		  String alkoID = wpr.getAlkoID();
		  if (alkoID == null || alkoID.equals("")) {
			  Message message = Message.obtain(activityHandler, R.id.search_wine_error);
			  Log.d(TAG, "Got search wine error" + alkoID);
			  message.sendToTarget();
			  return;
		  }
		  
		  String alkoUrl = wpr.getUrl();
		  if (alkoUrl != null && !alkoUrl.equals("")) {
			  Message message = Message.obtain(activityHandler, R.id.search_wine_failed);
			  Log.d(TAG, "Got wine query url: " + alkoUrl);
			  message.obj = alkoUrl;
			  message.sendToTarget();
			  return;
		  }

	  }
	  
      SQLiteOpenHelper helper = new DBHelper(activity);
      SQLiteDatabase db = helper.getWritableDatabase();
      try {
    	  String where = DBHelper.EAN_COL + "='" + wpr.getEan() + "'";
		  Cursor cursor = db.query(DBHelper.TABLE_NAME, new String[] { DBHelper.ID_COL,DBHelper.RATING_COL,DBHelper.NOTES_COL }, where, null, null, null, null, "1");
		  Integer id = null;
		  Integer rating = null;
		  String notes = null;
		  if (cursor.moveToFirst()) {
		         do {
		            id = cursor.getInt(0);
		            rating = cursor.getInt(1);
		            notes = cursor.getString(2);
		         } while (cursor.moveToNext());
		  }
	   	  if (cursor != null && !cursor.isClosed()) {
	   			cursor.close();
	   	  }
        // Delete if already exists
	   	  if ((rating == null || rating == WineParsedResult.NO_RATING) && (notes == null || notes.equals(""))) {
	   		  Log.d(TAG, "Rating NULL");
	   		  db.delete(DBHelper.TABLE_NAME, DBHelper.EAN_COL + "=?", new String[] { wpr.getEan() });
	   		  // Insert
	          ContentValues values = new ContentValues();
	          values.put(DBHelper.NAME_COL, wpr.getWineName());
	          values.put(DBHelper.ALKOID_COL, wpr.getAlkoID());
	          values.put(DBHelper.EAN_COL, wpr.getEan());
	          values.put(DBHelper.URL_COL, wpr.getUrl());
	          values.put(DBHelper.IMAGEURL_COL, wpr.getImageUrl());
	          values.put(DBHelper.COUNTRY_COL, wpr.getCountry());
	          values.put(DBHelper.REGION_COL, wpr.getRegion());
	          values.put(DBHelper.TYPE_COL, wpr.getWineType());
	          values.put(DBHelper.GRAPES_COL, wpr.getGrapes());
	          values.put(DBHelper.PRICE_COL, wpr.getPrice());
	          values.put(DBHelper.TEXT_COL, wpr.getDescription());
	          int millis = (int) (System.currentTimeMillis() / 1000);
	          values.put(DBHelper.TIMESTAMP_COL, millis);
	          long rowId = db.insert(DBHelper.TABLE_NAME, DBHelper.TIMESTAMP_COL, values);
	          if (rowId != -1) id = (int) rowId;
	   	  } else {
	   		  Log.d(TAG, "Rating=" + rating + ", id=" + id);
			  ContentValues args = new ContentValues();
			  int millis = (int) (System.currentTimeMillis() / 1000);
			  args.put(DBHelper.TIMESTAMP_COL, millis);
			  String w = DBHelper.ID_COL + "='" + id +"'";
			  db.update(DBHelper.TABLE_NAME, args, w, null);
	   	  }
		  if (id != null) wpr.setID(id);
		  if (rating != null) wpr.setRating(rating);
		  if (notes != null) wpr.setNotes(notes);
		  
	   	  Handler activityHandler = activity.getHandler();
		  Message message = Message.obtain(activityHandler, R.id.search_wine_result);
		  Bundle bundle = wpr.getBundle();
	      message.obj = bundle;
	      message.sendToTarget();
      } finally {
        db.close();
      }

  }
  
  private void resetForNewQuery() {
	    networkThread = null;
	    //queryTextView.setEnabled(true);
	    //queryTextView.selectAll();
	    //queryButton.setEnabled(true);
  }
  
  private void startNetworkSearch(String query) {
    if (networkThread == null) {
      if (query != null && query.length() > 0) {
        networkThread = new NetworkThread(query, handler);
        networkThread.start();
      }
    }
  }
  
  private static final class NetworkThread extends Thread {
	    private final String query;
	    private final Handler handler;

	    NetworkThread(String query, Handler handler) {
	      this.query = query;
	      this.handler = handler;
	    }

	    @Override
	    public void run() {
	      AndroidHttpClient client = null;
	      try {
	    	  Locale l = Locale.getDefault();
	  	      String lang = l.getLanguage();
	    	  URI uri= new URI("http", null, "www.viinikoodi.fi", -1, "/haku.php", "lang=" + lang + "&out=json&koodi=" + query, null);
	        
	          HttpUriRequest get = new HttpGet(uri);
	          //get.setHeader("cookie", getCookie(uri.toString()));
	          client = AndroidHttpClient.newInstance(USER_AGENT);
	          HttpResponse response = client.execute(get);
	          if (response.getStatusLine().getStatusCode() == 200) {
		          HttpEntity entity = response.getEntity();
		          ByteArrayOutputStream jsonHolder = new ByteArrayOutputStream();
		          entity.writeTo(jsonHolder);
		          jsonHolder.flush();
		          JSONObject json = new JSONObject(jsonHolder.toString());
		          jsonHolder.close();
	
		          Message message = Message.obtain(handler, R.id.search_wine_succeeded);
		          message.obj = json;
		          message.sendToTarget();
	          } else {
		          Log.w(TAG, "HTTP returned " + response.getStatusLine().getStatusCode() + " for " + uri);
		          Message message = Message.obtain(handler, R.id.search_wine_failed);
		          message.sendToTarget();
	        }
	      } catch (Exception e) {
	        Log.w(TAG, "Error accessing wine search", e);
	        Message message = Message.obtain(handler, R.id.search_wine_failed);
	        message.sendToTarget();
	      } finally {
	        if (client != null) {
	          client.close();
	        }
	      }
	    }
  }
}
