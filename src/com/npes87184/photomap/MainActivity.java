package com.npes87184.photomap;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import ru.bartwell.exfilepicker.ExFilePicker;
import ru.bartwell.exfilepicker.ExFilePickerParcelObject;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.media.ExifInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends PreferenceActivity {
	
	private static final String APP_NAME = "PhotoMap";
	private static final double version = 1.0;
	
	private static final String APP_ENTER_NUMBER = "enter_number";
	private static final String KEY_START = "Start_key";
	private static final String KEY_CHOOSE_NUMBER = "KEY_CHOOSE_NUMBER";
	private static final String KEY_ABOUT = "About_key";
	private static final String KEY_OTA = "Check_Update_Key";
	
	private static final int EX_FILE_PICKER_RESULT = 0;
	
	private String versionString = " ";
	private static int i;  //enter number
	private ProgressDialog progressDialog;
	
	private SharedPreferences prefs;
	private Preference startPreference;
	private Preference aboutPreference;
	private Preference otaPreference;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preference);
		
		prefs = getPreferenceManager().getSharedPreferences();
		
		i = prefs.getInt(APP_ENTER_NUMBER, 0);
		i++;
		
		if(i > 4) {
			i = 0;
			new Thread(new Runnable() {  //auto check version
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					try {
						ConnectivityManager CM = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
						NetworkInfo info = CM.getActiveNetworkInfo();
						if((info != null) && info.isConnected()) {
							BufferedReader reader = new BufferedReader(new InputStreamReader(getUrlData(),"BIG5"));
							String line;
							double temp_version = -1;
							while((line = reader.readLine())!=null) {
								String [] data = line.split(",");
								if(data[0].equals(APP_NAME)) {
									temp_version = Double.parseDouble(data[1]);
									System.out.println(temp_version);
									
									boolean first = true;
									boolean second = true;
									int i = 1;
									for(String aString : data) {
										if(first) {
											first = false;
											continue;
										}
										if(second) {
											versionString = versionString + getResources().getString(R.string.new_version_number) + aString;
											versionString = versionString + "\n\n ";
											second = false;
											continue;
										}
										versionString = versionString + "      " + i + "." +aString;
										versionString = versionString + "\n ";
										i++;
									}
									break;
								}
							}
							if(temp_version > version) {
								Message msg = new Message();
								msg.what = 3;
								mHandler.sendMessage(msg);
							}
							else {
								//do nothing
							}
						}
						else {
							//do nothing
						}
					} catch (IOException e) {
						System.out.println("IO");
					} catch (URISyntaxException e) {
						System.out.println("URL");
					}
				}
			}).start();
		}
		
		prefs.edit().putInt(APP_ENTER_NUMBER, i).commit();
		
		
		
		startPreference = findPreference(KEY_START);
		startPreference.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				Toast.makeText(MainActivity.this, getResources().getString(R.string.tip), Toast.LENGTH_LONG).show();
		        Intent intent = new Intent(getApplicationContext(), ru.bartwell.exfilepicker.ExFilePickerActivity.class);
				intent.putExtra(ExFilePicker.SET_CHOICE_TYPE, ExFilePicker.CHOICE_TYPE_FILES);
				intent.putExtra(ExFilePicker.SET_FILTER_LISTED, new String[] { "jpg", "jpeg", "png" });
				intent.putExtra(ExFilePicker.SET_START_DIRECTORY, Environment.getExternalStorageDirectory().getAbsolutePath() + "/DCIM/");
		        startActivityForResult(intent, EX_FILE_PICKER_RESULT);
        		return true;
			}
		});
		
		aboutPreference = findPreference(KEY_ABOUT);
		aboutPreference.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				View view = View.inflate(MainActivity.this, R.layout.about, null); 
				TextView textView = (TextView) view.findViewById(R.id.textView4);
				textView.setMovementMethod(LinkMovementMethod.getInstance());
			
				AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
				dialog.setTitle(getResources().getString(R.string.about)).setView(view)/*.setIcon(R.drawable.ic_menu_info_details)*/.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						// TODO Auto-generated method stub		
					}
				}).show();
				return true;
			}
		});
		
		otaPreference = findPreference(KEY_OTA);
		otaPreference.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				progressDialog = ProgressDialog.show(MainActivity.this,getResources().getString(R.string.check_update), getResources().getString(R.string.checking));
				progressDialog.setCancelable(true);
				new Thread(new Runnable() {
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						check_ota();
					}
				}).start();
				return true;
			}
		});
		
	}

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == EX_FILE_PICKER_RESULT) {
            if (data != null) {
                ExFilePickerParcelObject object = (ExFilePickerParcelObject) data.getParcelableExtra(ExFilePickerParcelObject.class.getCanonicalName());
                if (object.count > 0) {
                    // Here is object contains selected files names and path
                	for(int i=0;i < object.count;i++) {
                		prefs.edit().putString(String.valueOf(i) + "lat", String.valueOf(readGeoTagImage(object.path + object.names.get(i)).getLatitude())).commit();
                		prefs.edit().putString(String.valueOf(i) + "lon", String.valueOf(readGeoTagImage(object.path + object.names.get(i)).getLongitude())).commit();
                	}
                	prefs.edit().putInt(KEY_CHOOSE_NUMBER, object.count).commit();
                	Intent intent = new Intent(MainActivity.this, MapActivity.class);
                	startActivity(intent);
                }
            }
        }
    }
	
    
    /**
    * Read location information from image.
    * @param imagePath : image absolute path
    * @return : loation information
    */
    public Location readGeoTagImage(String imagePath) {
    	Location loc = new Location("");
    	try {
    		ExifInterface exif = new ExifInterface(imagePath);
    		float [] latlong = new float[2] ;
    		if(exif.getLatLong(latlong)){
    			loc.setLatitude(latlong[0]);
    			loc.setLongitude(latlong[1]);
    		}
    		String date = exif.getAttribute(ExifInterface.TAG_DATETIME);
    		SimpleDateFormat fmt_Exif = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");
    		loc.setTime(fmt_Exif.parse(date).getTime());

    	} catch (IOException e) {
    		e.printStackTrace();
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    	return loc;
    }



	Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 3: //have ota
				View view = View.inflate(MainActivity.this, R.layout.ota, null);
				TextView versionChange = (TextView) view.findViewById(R.id.textView1);
				versionChange.setText(versionString);
				versionString = " ";
				TextView textView = (TextView) view.findViewById(R.id.textView3);
				textView.setMovementMethod(LinkMovementMethod.getInstance());
			
				AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
				dialog.setTitle(getResources().getString(R.string.new_version)).setView(view).setPositiveButton("OK", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						// TODO Auto-generated method stub		
					}
				}).show();
				break;
			case 4: // do not have ota
				View view1 = View.inflate(MainActivity.this, R.layout.no_ota, null);
				TextView textview = (TextView) view1.findViewById(R.id.textView3);
				textview.setMovementMethod(LinkMovementMethod.getInstance());
			
				AlertDialog.Builder dialog1 = new AlertDialog.Builder(MainActivity.this);
				dialog1.setTitle(getResources().getString(R.string.no_version)).setView(view1).setPositiveButton("OK", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						// TODO Auto-generated method stub		
					}
				}).show();
				break;
            }
          super.handleMessage(msg);
		}
	};
	
	//get latest version code
	public InputStream getUrlData() throws URISyntaxException, ClientProtocolException, IOException {
		DefaultHttpClient client = new DefaultHttpClient();
		HttpGet method = new HttpGet(new URI("http://www.cmlab.csie.ntu.edu.tw/~npes87184/version.csv"));
		HttpResponse res = client.execute(method);
		return res.getEntity().getContent();
	}
	
	private void check_ota() {
		try {
			ConnectivityManager CM = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo info = CM.getActiveNetworkInfo();
			if((info != null) && info.isConnected()) {
				BufferedReader reader = new BufferedReader(new InputStreamReader(getUrlData(),"BIG5"));
				String line;
				double temp_version = -1;
				while((line = reader.readLine())!=null) {
					String [] data = line.split(",");
					if(data[0].equals(APP_NAME)) {
						temp_version = Double.parseDouble(data[1]);
						System.out.println(temp_version);
						
						boolean first = true;
						boolean second = true;
						int i = 1;
						for(String aString : data) {
							if(first) {
								first = false;
								continue;
							}
							if(second) {
								versionString = versionString + getResources().getString(R.string.new_version_number) + aString;
								versionString = versionString + "\n\n ";
								second = false;
								continue;
							}
							versionString = versionString + "      " + i + "." +aString;
							versionString = versionString + "\n ";
							i++;
						}
						break;
					}
				}
				progressDialog.dismiss();
				if(temp_version > version) {
					Message msg = new Message();
					msg.what = 3;
					mHandler.sendMessage(msg);
				}
				else {
					Message msg = new Message();
					msg.what = 4;
					mHandler.sendMessage(msg);
				}
			}
			else {
				Message msg = new Message();
				msg.what = 2;
				mHandler.sendMessage(msg);
			}
		} catch (IOException e) {
			System.out.println("IO");
		} catch (URISyntaxException e) {
			System.out.println("URL");
		}
	}
}
