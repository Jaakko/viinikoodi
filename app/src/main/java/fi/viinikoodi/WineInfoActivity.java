package fi.viinikoodi;

import com.google.zxing.client.android.CaptureActivity;

import fi.viinikoodi.client.android.R;
import fi.viinikoodi.history.DBHelper;
import fi.viinikoodi.history.HistoryManager;
import fi.viinikoodi.history.ImageLoader;
import fi.viinikoodi.result.WineParsedResult;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.View.OnKeyListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class WineInfoActivity extends Activity implements RatingBar.OnRatingBarChangeListener{
	
	private static final int SCAN_ID = Menu.FIRST;
	private static final int DELETE_ID = Menu.FIRST + 1;
	private static final int SHARE_ID = Menu.FIRST + 2;
	private static final int ERASE_ID = Menu.FIRST + 3;
	private static final int WEB_ID = Menu.FIRST + 4;
	private static final int HISTORY_ID = Menu.FIRST + 5;
	  
	
	private ImageLoader imageLoader;
	private static final String TAG = WineInfoActivity.class.getSimpleName();
	private TextView ratingText;
	private int id;
	private String notes;
	private Uri url;
	Bundle bundle;
	
	private EditText edittext;
	private RatingBar wineRatingBar;
	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        
        setContentView(R.layout.wineinfo);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.wineinfo_title);
        
               
        TextView title = (TextView) findViewById(R.id.wineinfo_title_name);
        title.setText(R.string.app_name);
        
        Bundle extras = getIntent().getExtras();
        if(extras !=null)
        {
        	//String value = extras.getString("keyName");
        	//value = value + " " + extras.getString("deviceIndex");
        	bundle = extras.getBundle("data");
        	String imageUrl = bundle.getString(WineParsedResult.IMAGEURL);

        	this.id = bundle.getInt(WineParsedResult.ID);
        	
        	TextView price = (TextView) findViewById(R.id.wineinfo_price);
        	price.setText(bundle.getString(WineParsedResult.PRICE) + "€");

        	String wineName = bundle.getString(WineParsedResult.NAME);        	
        	TextView name = (TextView) findViewById(R.id.wineinfo_name);
        	name.setText(wineName);
        	Log.d(TAG, "Got wine data: " + wineName + "," + this.id + ", " + imageUrl);
        	
        	TextView desc = (TextView) findViewById(R.id.wineinfo_desc);
        	desc.setText(bundle.getString(WineParsedResult.DESCRIPTION));
        	
        	TextView type_country = (TextView) findViewById(R.id.wineinfo_type_country);
        	type_country.setText(bundle.getString(WineParsedResult.TYPE) + ", " + 
        					     bundle.getString(WineParsedResult.COUNTRY));
        	
        	TextView grape = (TextView) findViewById(R.id.wineinfo_grape);
        	grape.setText(bundle.getString(WineParsedResult.GRAPES));
        	
        	String region = bundle.getString(WineParsedResult.REGION);
        	if (region != null && !region.equals("")) ((TextView) findViewById(R.id.wineinfo_region)).setText(region);
        	
        	ImageView image = (ImageView) findViewById(R.id.wineinfo_image);
        	imageLoader = new ImageLoader(this.getApplicationContext());
        	image.setTag(imageUrl);
            imageLoader.DisplayImage(imageUrl, this, image);
            
            int rating = bundle.getInt(WineParsedResult.RATING);
            
            wineRatingBar = (RatingBar)findViewById(R.id.ratingbar);
            if (rating != 0) wineRatingBar.setRating(WineParsedResult.getRatingBarRating(rating));

    		ratingText = (TextView)findViewById(R.id.rating_text);
    		
    		//	Set appropriate listener to listen required events
    		wineRatingBar.setOnRatingBarChangeListener(this);
    		
    		edittext = (EditText) findViewById(R.id.editwinenotes);
    		notes = bundle.getString(WineParsedResult.NOTES);
    		if (notes != null && !notes.equals("")) {
    			edittext.setText(notes);
    		}
    	    this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    		

    		
    	    edittext.addTextChangedListener(new TextWatcher() {
    			 
    		    public void afterTextChanged(Editable s) {
    		        setResult (WineHistoryActivity.WINE_INFO_CHANGED);
    		    }
    		 
    		    public void beforeTextChanged(CharSequence s, int start, int count,
    		            int after) {
    		        // TODO Auto-generated method stub
    		 
    		    }
    		 
    		    public void onTextChanged(CharSequence s, int start, int before,
    		            int count) {
    		        // TODO Auto-generated method stub
    		 
    		    }
    		 
    		});
    	    try{
    	    	this.url = Uri.parse(bundle.getString(WineParsedResult.URL));
    	    	ImageButton helmet = (ImageButton) findViewById(R.id.button_wineinfo_title);
    	    	helmet.setOnClickListener(new View.OnClickListener() {
    	    		public void onClick(View view) {
    	    			startActivity(new Intent(Intent.ACTION_VIEW, url));
    	    		}
    	    	});
    	    } catch(Exception ex) {
    	    	
    	    }
        }
	    ScrollView wineinfoScrollView = (ScrollView)findViewById(R.id.wineinfo_scrollview);
        wineinfoScrollView.fullScroll(ScrollView.FOCUS_UP);
	}

	 public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromTouch) {
	        ratingText.setText(rating + " " + getResources().getString(R.string.msg_wine_rating_stored));
	        updateRating(id,WineParsedResult.getStorageRating(rating));
	        Log.d(TAG, "Update : " + id + ", " + WineParsedResult.getStorageRating(rating));
	        setResult (WineHistoryActivity.WINE_INFO_CHANGED);
	    }

	 
	 private void updateRating(int id,int rating){
		   ContentValues args = new ContentValues();
	       args.put(DBHelper.RATING_COL, rating);
	       int millis = (int) (System.currentTimeMillis() / 1000);
	       args.put(DBHelper.TIMESTAMP_COL, millis);
	       String where = DBHelper.ID_COL + "='" + id +"'";
	       SQLiteOpenHelper helper = new DBHelper(this);
	       SQLiteDatabase db = helper.getWritableDatabase();
	       db.update(DBHelper.TABLE_NAME, args, where, null);
	       db.close();
	  }

	 private void updateNotes(int id,String n){
		   ContentValues args = new ContentValues();
	       args.put(DBHelper.NOTES_COL, n);
	       int millis = (int) (System.currentTimeMillis() / 1000);
	       args.put(DBHelper.TIMESTAMP_COL, millis);
	       String where = DBHelper.ID_COL + "='" + id +"'";
	       SQLiteOpenHelper helper = new DBHelper(this);
	       SQLiteDatabase db = helper.getWritableDatabase();
	       db.update(DBHelper.TABLE_NAME, args, where, null);
	       db.close();
	  }
	 
	  @Override
	  public boolean onCreateOptionsMenu(Menu menu) {
	    super.onCreateOptionsMenu(menu);
	  
	    menu.add(0, SCAN_ID, 0, R.string.menu_wineinfo_scan)
        .setIcon(R.drawable.ic_menu_scan);
	    
	    menu.add(0, WEB_ID, 0, R.string.menu_wineinfo_web)
    	.setIcon(R.drawable.ic_menu_web);
	    
	    menu.add(0, HISTORY_ID, 0, R.string.menu_history)
        .setIcon(android.R.drawable.ic_menu_recent_history);
	    
	    menu.add(0, SHARE_ID, 0, R.string.menu_wineinfo_share)
	        .setIcon(android.R.drawable.ic_menu_share);
	    
	    if (wineRatingBar.getRating() > 0 || (notes != null && !notes.equals("")))	menu.add(0, ERASE_ID, 0, R.string.menu_wineinfo_erase_rating)
    	.setIcon(R.drawable.ic_menu_erase);
	    
	    menu.add(0, DELETE_ID, 0, R.string.menu_wineinfo_delete)
	    	.setIcon(android.R.drawable.ic_menu_delete);
	     return true;
	  }

	 private void deleteWine(int id){
	       SQLiteOpenHelper helper = new DBHelper(this);
	       SQLiteDatabase db = helper.getWritableDatabase();
	       String idString = (new Integer(id)).toString();
	       db.delete(DBHelper.TABLE_NAME, DBHelper.ID_COL + "=?", new String[] { idString });
	       db.close();
	       setResult (WineHistoryActivity.WINE_INFO_CHANGED);
	  }
	 
	  @Override
	  public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	    case SCAN_ID: {
            Intent in = new Intent(this, CaptureActivity.class);
            startActivity(in);
	        break;
	      }
	    case ERASE_ID:{
	    	edittext.setText("");
	    	updateRating(id,WineParsedResult.NO_RATING);
	    	wineRatingBar.setRating(0);
	    	updateNotes(id,"");
	    	Toast.makeText(getApplicationContext(), R.string.msg_wine_rating_removed ,Toast.LENGTH_LONG).show();
	    	setResult (WineHistoryActivity.WINE_INFO_CHANGED);
	    	break;
	    }
	    case DELETE_ID: {
	    	deleteWine(this.id);
    		Toast.makeText(getApplicationContext(), R.string.msg_wine_removed ,Toast.LENGTH_LONG).show();
    		finish();
	        break;
	    }
	    case SHARE_ID: {
	        shareWine();
	        break;
	    }
	    case WEB_ID: {
	    	try{
	    		Intent intent = new Intent(Intent.ACTION_VIEW, this.url);
	    		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
    	    	startActivity(intent);
    	    } catch(Exception ex) {
    	    	
    	    }
	        break;
	    }
	    case HISTORY_ID: {
	    	Intent in = new Intent(this, WineHistoryActivity.class);
	    	startActivityForResult(in,0);
	    	break;
        }
	    
	    }
	    return super.onOptionsItemSelected(item);
	  }

	  public void shareWine() {
			Intent shareIntent = new Intent(android.content.Intent.ACTION_SEND);   	   
			shareIntent.setType("text/plain");
			shareIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, R.string.msg_wine_share_title);
			String entry = bundle.getString(WineParsedResult.NAME) + "\n";
			entry += bundle.getString(WineParsedResult.TYPE) + ", " + bundle.getString(WineParsedResult.COUNTRY) + "\n";
			entry += bundle.getString(WineParsedResult.PRICE)+ "€\n";
			if (wineRatingBar.getRating() > 0) {
				entry += wineRatingBar.getRating() + " " + getResources().getString(R.string.msg_wine_share_star_rating) + "\n";
			}
			if (notes != null && !notes.equals("")) {
				entry += notes + "\n";
			}
			shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, entry);
			
			startActivity(Intent.createChooser(shareIntent, getText(R.string.menu_wineinfo_share_intent)));      
		}  
	
	  private void checkWineNotes(){
		  String editText = edittext.getText().toString();
		  Log.d(TAG, "edittext: " + editText + "," + notes);
		  if (editText != null && !editText.equals("")) {
			  if ((notes == null) || (notes != null && !notes.equals(editText))) {
				  updateNotes(this.id, editText);
				  setResult (WineHistoryActivity.WINE_INFO_CHANGED);
				  notes = editText;
				  Log.d(TAG, "notes updated: " + editText);
				  
			  }
		  }  
	  }
	  
	  @Override
	  public void onStop()
    {
		super.onStop(); 
		checkWineNotes();
		Log.d(TAG, "onStop");
    }  
	
	  @Override
	  public void onPause()
    {
		super.onPause(); 
		checkWineNotes();
		Log.d(TAG, "onPause");
    }    
	  
	@Override
    public void onDestroy()
    {
        super.onDestroy();
    }
}
