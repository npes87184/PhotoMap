package com.npes87184.photomap;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

public class MapActivity extends Activity {

	private static final String MAP_URL = "file:///android_asset/new.html";
	private static final String KEY_CHOOSE_NUMBER = "KEY_CHOOSE_NUMBER";
	
	private WebView webView;
	private SharedPreferences prefs;
	String [] latString;
	String [] lonString;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_map);
		webView = (WebView) findViewById(R.id.webView1);
		webView.getSettings().setJavaScriptEnabled(true);
	    webView.loadUrl(MAP_URL);   
	    webView.addJavascriptInterface(MapActivity.this , "AndroidFunction");
		
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		
		latString = new String[prefs.getInt(KEY_CHOOSE_NUMBER, 1)];
		lonString = new String[prefs.getInt(KEY_CHOOSE_NUMBER, 1)];
		
		for(int i=0;i<prefs.getInt(KEY_CHOOSE_NUMBER, 1);i++) {
			latString[i] = prefs.getString(String.valueOf(i)+ "lat", "0");
			lonString[i] = prefs.getString(String.valueOf(i)+ "lon", "0");
		}
	}
	
	@JavascriptInterface
	public String getLat(int i) {
		return latString[i];
	}
	
	@JavascriptInterface
	public String getLon(int i) {
		return lonString[i];
	}
	
	@JavascriptInterface
	public String getNumber() {
		return String.valueOf(prefs.getInt(KEY_CHOOSE_NUMBER, 1));
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.map, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_share) {
			webView.setDrawingCacheEnabled(true);
			saveImage(webView.getDrawingCache());
	        Intent shareIntent = new Intent(Intent.ACTION_SEND);  
	        shareIntent.setType("image/*");  
	        File capImgFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
	                        + "/PhotoMap/captured_Bitmap.png");
	        Uri uri = Uri.fromFile(capImgFile);  
	        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);  
	        startActivity(Intent.createChooser(shareIntent, "½Ð¿ï¾Ü"));  
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	 protected void saveImage(Bitmap bmScreen2) {
	        // TODO Auto-generated method stub

	        // String fname = "Upload.png";
	        File saved_image_file = new File(
	                Environment.getExternalStorageDirectory().getAbsolutePath()
	                        + "/PhotoMap/captured_Bitmap.png");
	        if (saved_image_file.exists())
	        	System.out.println("exist");
	            saved_image_file.delete();
	        if(!saved_image_file.exists()) {
	        	saved_image_file.getParentFile().mkdirs();
	        	try {
	        		saved_image_file.createNewFile();
	        	} catch (IOException e) {
	        		// TODO Auto-generated catch block
	        		e.printStackTrace();
	        	}
	        }
	        try {
	            FileOutputStream out = new FileOutputStream(saved_image_file);
	            bmScreen2.compress(Bitmap.CompressFormat.PNG, 100, out);
	            out.flush();
	            out.close();
	            MediaScannerConnection.scanFile(getApplicationContext(), new String[]{Environment.getExternalStorageDirectory().getAbsolutePath()
                    + "/PhotoMap/captured_Bitmap.png"}, null, null);

	        } catch (Exception e) {
	            e.printStackTrace();
	        }

	    }
}
