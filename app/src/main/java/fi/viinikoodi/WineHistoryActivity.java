package fi.viinikoodi;

import android.text.format.DateFormat;

import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import com.google.zxing.client.android.CaptureActivity;

import fi.viinikoodi.client.android.R;
import fi.viinikoodi.history.HistoryManager;
import fi.viinikoodi.history.ImageLoader;
import fi.viinikoodi.result.WineParsedResult;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class WineHistoryActivity extends ListActivity {
	
	private static final String TAG = WineInfoActivity.class.getSimpleName();

	private static final int SCAN_ID = Menu.FIRST;
	private static final int ORDER_ID = Menu.FIRST +1;
	private static final int SAVE_ID = Menu.FIRST +2;
	
    List<WineParsedResult> wineList;
    WineListAdapter wla;
    ListView listView;

	private static final int ORDER_BY_DATE = 0;
    private static final int ORDER_BY_RATING = 1;
    private static final int ORDER_BY_PRICE = 2;
    
    int orderBy = ORDER_BY_DATE;
    
    static final int WINE_INFO_REQUEST = 0;
    static final int WINE_INFO_CHANGED = 1;

    
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.winelist_layout);
        listView = getListView(); 
        
        wineList = HistoryManager.getHistoryItems(this);
        wla = new WineListAdapter(this, wineList);
        //wla.imageLoader.clearCache();

        listView.setAdapter(wla);
        
        OnItemClickListener oicl = new OnItemClickListener() {
        	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                
        		if (wineList.size() == 0) return;
        		
                Bundle wineResult = ((WineParsedResult) wineList.get(position)).getBundle();
                Log.d(TAG, "Got wine data: " + wineResult.getString(WineParsedResult.IMAGEURL));
                Intent in = new Intent(view.getContext(), WineInfoActivity.class);
                in.putExtra("data", wineResult);
                //startActivity(in);
                startActivityForResult(in,WINE_INFO_REQUEST);

        	}
        };
        listView.setOnItemClickListener(oicl);
	}
	  @Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    super.onCreateOptionsMenu(menu);  
	    menu.add(0, SCAN_ID, 0, R.string.menu_wineinfo_scan)
        .setIcon(R.drawable.ic_menu_scan);
	    
	    menu.add(0, ORDER_ID, 0, R.string.menu_wineinfo_order)
        .setIcon(android.R.drawable.ic_menu_sort_by_size);
	    
	    /*
	    menu.add(0, SAVE_ID, 0, R.string.menu_wineinfo_save)
        .setIcon(android.R.drawable.ic_menu_save);
	    */
	     return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
	    	case SCAN_ID: {
	        	Intent in = new Intent(this, CaptureActivity.class);
	        	startActivity(in);
	        	break;
	    	}
	    	case SAVE_ID: {    		
	    		Uri uri = HistoryManager.buildHistory(this);
	    		//Log.d(TAG,history.toString());
	    		//Uri uri = HistoryManager.saveHistory(history.toString());  	
	        	if (uri != null) Toast.makeText(getApplicationContext(), getResources().getString(R.string.msg_winehistory_saved) + " " +uri.getPath() ,Toast.LENGTH_LONG).show();
	        	else Toast.makeText(getApplicationContext(), getResources().getString(R.string.msg_unmount_usb),Toast.LENGTH_LONG).show();
	        	break;
	    	} case ORDER_ID: {
	    		final String items[] = {getResources().getString(R.string.alert_winehistory_time),
	    								getResources().getString(R.string.alert_winehistory_rating),
	    								getResources().getString(R.string.alert_winehistory_price)};

	    		AlertDialog.Builder ab = new AlertDialog.Builder(this);
	    		ab.setTitle(R.string.alert_winehistory_title);	

	    		//ab.setTitle(R.string.alert_winehistory_title);
	    		ab.setSingleChoiceItems(items, orderBy,new DialogInterface.OnClickListener() {
		    		public void onClick(DialogInterface dialog, int whichButton) {
		    			Log.d(TAG,"clicked: " + items[whichButton]);
		    			orderBy = whichButton;
		    		}
	    		})
	    		.setPositiveButton(R.string.alert_winehistory_ok, new DialogInterface.OnClickListener() {
	    			public void onClick(DialogInterface dialog, int whichButton) {
	    				orderListItems();
	    			}
	    		})
	    		.setNegativeButton(R.string.alert_winehistory_cancel, new DialogInterface.OnClickListener() {
		    		public void onClick(DialogInterface dialog, int whichButton) {
		    			Log.d(TAG,"Cancel clicked: " + whichButton);
		    		}
	    		});
	    		
	    		ab.show();
	    	}
	    }
	    return super.onOptionsItemSelected(item);
	  }
	  
    @Override
    public void onDestroy()
    {
        wla.imageLoader.stopThread();
        listView.setAdapter(null);
        super.onDestroy();
    }
    
    private void orderListItems(){
		switch (orderBy) {
			case ORDER_BY_DATE: {
				orderByDate();
				break;
			}
			case ORDER_BY_RATING: {
				orderByRating();
				break;
			}
			case ORDER_BY_PRICE: {
				orderByPrice();
				break;
			}
		}
    }
    private void orderByRating(){
    	Collections.sort(wineList, new Comparator<WineParsedResult>(){
          	 
            public int compare(WineParsedResult w1, WineParsedResult w2) {
               int wint1 = w1.getRating();
               int wint2 = w2.getRating();
               if (wint1 > wint2) return -1;
               if (wint2 < wint1) return 1;
               return 0;
            }
        });
		
		wla = new WineListAdapter(this, wineList);
    	listView.setAdapter(wla);
    	wla.notifyDataSetChanged();
    }
    
    private void orderByDate(){
    	Collections.sort(wineList, new Comparator<WineParsedResult>(){
          	 
            public int compare(WineParsedResult w1, WineParsedResult w2) {
               int wint1 = w1.getTimestamp();
               int wint2 = w2.getTimestamp();
               if (wint1 > wint2) return -1;
               if (wint2 < wint1) return 1;
               return 0;
            }
        });
		
		wla = new WineListAdapter(this, wineList);
    	listView.setAdapter(wla);
    	wla.notifyDataSetChanged();
    }

    private void orderByPrice(){
    	Collections.sort(wineList, new Comparator<WineParsedResult>(){
          	 
            public int compare(WineParsedResult w1, WineParsedResult w2) {
               Float wint1 = Float.parseFloat(w1.getPrice());
               Float wint2 = Float.parseFloat(w2.getPrice());
               return -1*wint1.compareTo(wint2);
            }
        });
		
		wla = new WineListAdapter(this, wineList);
    	listView.setAdapter(wla);
    	wla.notifyDataSetChanged();
    }
    protected void onActivityResult(int requestCode, int resultCode,
            Intent data) {
        if (requestCode == WINE_INFO_REQUEST) {
            if (resultCode == WINE_INFO_CHANGED) {
            	wineList = HistoryManager.getHistoryItems(this);
            	orderListItems();
            	wla = new WineListAdapter(this, wineList);
            	listView.setAdapter(wla);
            	wla.notifyDataSetChanged();
            }
        }
    }

    
	public static class WineListAdapter extends BaseAdapter {
    	private LayoutInflater mInflater;
    	private Activity activity;
    	private List<WineParsedResult> wineList;
    	public ImageLoader imageLoader;
    	Locale locale;
    	
    	public WineListAdapter(Activity activity, List<WineParsedResult> wineList) {
    		this.activity = activity;
    		this.mInflater = LayoutInflater.from(activity);	
    		this.wineList = wineList;
    		imageLoader=new ImageLoader(activity.getApplicationContext());
    		locale = Locale.getDefault();
    	}
    	public int getCount() {
    		return wineList.size();
    	}
    	
    	public Object getItem(int position) {
    		return position;
    	}
    	
    	public long getItemId(int position) {
    		return position;
    	}
    	
    	public View getView(int position, View convertView, ViewGroup parent) {
    		ViewHolder holder;
    		if (convertView == null) {
    			convertView = mInflater.inflate(R.layout.winelist_item, null);
    			holder = new ViewHolder();
    			holder.name = (TextView) convertView.findViewById(R.id.wine_name);
    			holder.type_country = (TextView) convertView.findViewById(R.id.wine_type_country);
    			holder.grape_region = (TextView) convertView.findViewById(R.id.wine_grape_region);
    			holder.desc = (TextView) convertView.findViewById(R.id.wine_desc);
    			holder.date = (TextView) convertView.findViewById(R.id.wine_date);
    			holder.price = (TextView) convertView.findViewById(R.id.wine_price);
    			
    			holder.image = (ImageView) convertView.findViewById(R.id.wine_image);
    			holder.notes = (ImageView) convertView.findViewById(R.id.notes_image);
    			convertView.setTag(holder);
    		} else {
    			holder = (ViewHolder) convertView.getTag();
    		}
    		
    		
    		holder.name.setText(wineList.get(position).getWineName());
    		holder.type_country.setText(wineList.get(position).getWineType() + ", " +
    									wineList.get(position).getCountry()); 
    		holder.price.setText(wineList.get(position).getPrice() + "€");
    		Integer ratingValue = wineList.get(position).getRating();
    		Log.d("winehistory",wineList.get(position).getWineName() + "id=" +wineList.get(position).getID() + " rating=" + ratingValue);
    		if (ratingValue != null && ratingValue.intValue() != WineParsedResult.NO_RATING) {
    			holder.rating = (RatingBar) convertView.findViewById(R.id.wine_rating_small);
    			holder.rating.setRating(WineParsedResult.getRatingBarRating(ratingValue));
    			holder.rating.setVisibility(View.VISIBLE);
    		} else {
    			holder.rating = (RatingBar) convertView.findViewById(R.id.wine_rating_small);
    			holder.rating.setRating(WineParsedResult.getRatingBarRating(ratingValue));
    			holder.rating.setVisibility(View.GONE);
    		}
    		
    		String notesString = wineList.get(position).getNotes();
    		Log.d("winehistory",wineList.get(position).getWineName() + "id=" +wineList.get(position).getID() + " notes=" + notesString);
    		if (notesString != null && !notesString.equals("")) {
    			holder.notes.setVisibility(View.VISIBLE);
    		} else {
    			holder.notes.setVisibility(View.GONE);
    		}
    		
    		holder.grape_region.setText(wineList.get(position).getGrapes());
    		holder.desc.setText(wineList.get(position).getDescription());
    		
    		Calendar cal = Calendar.getInstance();
    		cal.setTimeInMillis(((long) wineList.get(position).getTimestamp()) * 1000);
    		String date = DateFormat.format("dd MMMM h:mmaa",cal).toString();
    		holder.date.setText(date);
    		
    		holder.image.setTag(wineList.get(position).getImageUrl());
            imageLoader.DisplayImage(wineList.get(position).getImageUrl(), activity, holder.image);
    		return convertView;
    	}
    	
    	static class ViewHolder {
    		TextView name;
    		TextView type_country;
    		TextView grape_region;
    		TextView desc;
    		TextView price;
    		TextView date;
    		ImageView image;
    		RatingBar rating;
    		ImageView notes;
    	}
    }
}
