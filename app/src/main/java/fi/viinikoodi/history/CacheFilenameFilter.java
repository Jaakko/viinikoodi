package fi.viinikoodi.history;

import java.io.File;
import java.io.FilenameFilter;
import java.util.List;

import android.util.Log;


public class CacheFilenameFilter implements FilenameFilter {

	private String[] items;
	private int size;
	
	public CacheFilenameFilter(List<String> files) {
	    size = files.size();
	    items = new String[size];
	    for (int i = 0; i < size; i++) {
	      items[i] = String.valueOf((files.get(i)).hashCode());
	      Log.d("CacheFilenameFilter", "items: " + items[i]);
	    }
		Log.d("CacheFilenameFilter", "length: " + size);
	}

	@Override
	public boolean accept(File dir, String name) {
		//int length = this.files.length;
		Log.d("CacheFilenameFilter", "name: " + name);
		for (int i = 0; i < size; i++) {
  	      if (this.items[i].equals(name)) {
  	    	Log.d("CacheFilenameFilter", "returning true");
  	    	  return true;
  	      }
  	    }
	    	Log.d("CacheFilenameFilter", "returning false");
		return false;
	}

}
