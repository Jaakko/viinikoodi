package fi.viinikoodi.result;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.util.Log;

import com.google.zxing.client.result.ParsedResult;
import com.google.zxing.client.result.ParsedResultType;

import fi.viinikoodi.history.HistoryManager;

public final class WineParsedResult extends ParsedResult {

	public static final String ID = "id";
	public static final String ALKOID = "alko_id";
	public static final String NAME = "name";
	public static final String COUNTRY = "country";
	public static final String TYPE = "type";
	public static final String PRICE = "price";
	public static final String EAN = "ean";
	public static final String URL = "url";
	public static final String IMAGEURL = "image_url";
	public static final String GRAPES = "grapes";
	public static final String REGION = "region";
	public static final String DESCRIPTION = "description";
	public static final String RATING = "rating";
	public static final String NOTES = "notes";
	
	public static final int NO_RATING = 0;
	
	private int id;
	private String alko_id;
	private String wineName;
	private String ean;
	private String wineType;
	private String country;
	private String price;
	private String imageUrl;
	private String grapes;
	private String region;
	private int timestamp;
	private String description;
	private String url;
	private int rating;
	private String notes;
	
	private String query_url;
	
	
	private static final String TAG = WineParsedResult.class.getSimpleName();
	  
	public WineParsedResult(ParsedResultType type) {
		super(type);
	}
	
	public WineParsedResult() {
		super(ParsedResultType.PRODUCT);
	}
	
	public boolean parse(JSONObject json){
		try {
		      this.alko_id = json.getString("alko_id");
		      this.imageUrl = json.getString("image_url");
		      this.url = json.getString("url");
		      this.ean = json.getString("ean");
		      this.wineName = json.getString("name");
		      this.country = json.getString("country");
		      this.wineType = json.getString("type");
		      this.price = json.getString("price");
		      this.grapes = json.getString("grapes");
		      this.region = json.getString("region");
		      this.description = json.getString("description");
		      Log.d(TAG, "Got JSON: " + this.alko_id + "," + this.ean + "," + this.wineName + "," + this.country + "," + this.wineType + "," + this.price + "," + this.grapes + "," + this.region);
			  return true;
		} catch (JSONException e) {	    	
	        Log.w(TAG, "Bad JSON from wine search", e);
	    }
	    
	    try {
	    	this.setQuery_url(json.getString("query_url"));
	    } catch (JSONException e){
	    	Log.w(TAG, "Bad JSON from wine query url search", e);
	    }
	    return false;
	}
	
	public Bundle getBundle(){
		Bundle bundle = new Bundle();
		bundle.putInt(WineParsedResult.ID, this.id);
		bundle.putInt(WineParsedResult.RATING, this.rating);
		bundle.putCharSequence(WineParsedResult.ALKOID, this.alko_id);
		bundle.putCharSequence(WineParsedResult.COUNTRY, this.country);
		bundle.putCharSequence(WineParsedResult.DESCRIPTION, this.description);
		bundle.putCharSequence(WineParsedResult.EAN, this.ean);
		bundle.putCharSequence(WineParsedResult.GRAPES, this.grapes);
		bundle.putCharSequence(WineParsedResult.IMAGEURL, this.imageUrl);
		bundle.putCharSequence(WineParsedResult.NAME, this.wineName);
		bundle.putCharSequence(WineParsedResult.PRICE, this.price);
		bundle.putCharSequence(WineParsedResult.REGION, this.region);
		bundle.putCharSequence(WineParsedResult.URL, this.url);
		bundle.putCharSequence(WineParsedResult.TYPE, this.wineType);
		bundle.putCharSequence(WineParsedResult.NOTES, this.notes);
		return bundle;
	}

	public void setID(int id){
		this.id = id;
	}
	
	public int getID(){
		return this.id;
	}
	
	public void setAlkoID(String alko_id){
		this.alko_id = alko_id;
	}
	
	public String getAlkoID(){
		return this.alko_id;
	}
	
	public void setWineName(String name) {
		this.wineName = name;
	}

	public String getWineName() {
		return wineName;
	}

	public void setWineType(String wineType) {
		this.wineType = wineType;
	}

	public String getWineType() {
		return wineType;
	}

	public void setDescription(String desc) {
		this.description = desc;
	}

	public String getDescription() {
		return description;
	}
	
	public void setCountry(String country) {
		this.country = country;
	}

	public String getCountry() {
		return country;
	}

	public void setPrice(String price) {
		this.price = price;
	}

	public String getPrice() {
		return price;
	}
	
	public void setEan(String ean) {
		this.ean = ean;
	}

	public String getEan() {
		return ean;
	}

	public void setGrapes(String grape) {
		this.grapes = grape;
	}

	public String getGrapes() {
		return grapes;
	}

	public void setRegion(String region) {
		this.region = region;
	}

	public String getRegion() {
		return region;
	}

	public String getText() {
		/*
		if (this.name != null && this.price != null){
			return this.name + " " + this.price + "€";
		} else if (this.ean != null) {
			return this.ean;
		} else {
			return "No stored data";
		}
		*/
		return "";
	}
	
	public void setTimestamp(int timestamp) {
		this.timestamp = timestamp;
	}

	public int getTimestamp() {
		return timestamp;
	}

	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}

	public String getImageUrl() {
		return imageUrl;
	}

	public String getUrl() {
		return url;
	}
	
	public void setUrl(String url) {
		this.url = url;
	}
	
	public void setQuery_url(String query_url) {
		this.query_url = query_url;
	}

	public String getQuery_url() {
		return query_url;
	}

	public void setRating(int rating){
		this.rating = rating;
	}
	
	public Integer getRating(){
		return new Integer(this.rating);
	}

	
	public void setNotes(String notes) {
		this.notes = notes;
	}

	public String getNotes() {
		return notes;
	}

	public static float getRatingBarRating(int rating){
		return ((float) rating)/20;
	}
	
	public static int getStorageRating(float rating){
		return (new Float(rating*20)).intValue();
	}
	
	@Override
	public String getDisplayResult() {
		// TODO Auto-generated method stub
		return null;
	}

}
