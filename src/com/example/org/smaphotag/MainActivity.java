package com.example.org.smaphotag;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;

public class MainActivity extends MapActivity implements LocationListener {

	private LocationManager lm = null;
	private LayoutInflater li;

	private MapView map;
	
	private boolean recording_mode = false;

	private LinearLayout header_view;
	private List<Location> track = new ArrayList<Location>();
	private View started_header;
	private Date track_start_date;
	private SimpleDateFormat dateformat;
	private Handler hndl = new Handler();
	private Location last_known_location;
	//private String gpx_path="/sdcard/smaphotag/";
	private ListView lv;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		dateformat = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss");

		lv = (ListView) this.findViewById(R.id.listView1);

		li = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		header_view = new LinearLayout(this);
		header_view.setLayoutParams(new AbsListView.LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT));

		adjustHeader();

		lv.addHeaderView(header_view);

		this.setTitle("Smaphotag");
		
		generateAdapter();
		
	}
	
	private File[] files;
	public void generateAdapter() {
		ArrayList<TrackInfo> track_list=new ArrayList<TrackInfo>();
		
		File dir=new File(SmaphotagEnv.path);
		files=dir.listFiles();
		
		for (File f:files) {
			if (f.getAbsolutePath().endsWith(".gpx"))
				track_list.add(0,new TrackInfo(f.getName(),new Date(),new Date()));
		}
		
		
		lv.setAdapter(new TrackAdapter(this, R.layout.track_item, track_list.toArray(new TrackInfo[] {})));
		lv.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				Intent it = new Intent(Intent.ACTION_SEND);   
				it.putExtra(Intent.EXTRA_SUBJECT, "GPX created with Smaphotag");   
				it.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://"+files[arg2]));   
				it.setType("application/gpx+xml");   
				startActivity(Intent.createChooser(it, "Choose how to send the GPX"));
				return false;
			}
			
		});
	}
	
	public String findNextFreeFilename() {
		int i=0;
		
		while (new File(SmaphotagEnv.path+"track"+i+".gpx").exists()) {
			i++;
		};
		
		return SmaphotagEnv.path+"track"+i+".gpx";

	}

	public void adjustHeader() {
		header_view.removeAllViews();

		if (!recording_mode) {
			View stopped_header = li.inflate(R.layout.stopped_header, null);
			Button btn = (Button) stopped_header.findViewById(R.id.add_button);

			btn.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					recording_mode = true;
					track_start_date = new Date();
					adjustHeader();
				}

			});

			header_view.addView(stopped_header);
		} else { // recording mode
			if (started_header == null) {
				started_header = li.inflate(R.layout.recording_header, null);
				map=(MapView)started_header.findViewById(R.id.mapview);
			}
			Runnable update_runnable = new Runnable() {

				@Override
				public void run() {
					TextView started_tv = (TextView) started_header.findViewById(R.id.start_time_tv);
					started_tv.setText(dateformat.format(track_start_date));

					TextView stopped_tv = (TextView) started_header.findViewById(R.id.stop_time_tv);
					stopped_tv.setText(dateformat.format(new Date()));
					hndl.postDelayed(this, 100);
				}

			};

			hndl.post(update_runnable);

			Button btn = (Button) started_header.findViewById(R.id.stop_btn);

			btn.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					recording_mode = false;
					adjustHeader();
					File f = new File(findNextFreeFilename());
					try {
						f.createNewFile();

						FileWriter sgf_writer = new FileWriter(f);

						BufferedWriter out = new BufferedWriter(sgf_writer);

						out.write(TrackExport.locationListToString(track));

						out.close();

						generateAdapter(); // refresh the adapter
						
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					track.clear();

					if (last_known_location != null)
						track.add(last_known_location);

				}

			});

			header_view.addView(started_header);
		}

	}

	@Override
	protected void onStart() {
		super.onStart();

		if (lm == null) {
			lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
			lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000L, 5.0f, this);
			lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000L, 5.0f, this);
			
		}
	}

	private class TrackAdapter extends ArrayAdapter<TrackInfo> {

		private LayoutInflater li;

		public TrackAdapter(Context context, int textViewResourceId, TrackInfo[] objects) {
			super(context, textViewResourceId, objects);
			li = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = li.inflate(R.layout.track_item, null);
			TrackInfo item = getItem(position);
			((TextView) view.findViewById(R.id.title_tv)).setText(item.getName());
			((TextView) view.findViewById(R.id.start_time_tv)).setText(item.getStartAsString());
			((TextView) view.findViewById(R.id.stop_time_tv)).setText(item.getStopString());
			return view;
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	 public static GeoPoint location2GeoPoint(Location l) {
     return new GeoPoint((int) (l.getLatitude() * 1E6), (int) (l.getLongitude() * 1E6));
	 }

	 
	@Override
	public void onLocationChanged(Location location) {

		Log.i("smaphotag", "got location" + location.getLatitude());
		last_known_location = location;

		track.add(location);
		if (map!=null) {
			map.getController().setCenter(location2GeoPoint(location));
			map.getController().setZoom(20);
		}
	}

	@Override
	public void onProviderDisabled(String provider) {

	}

	@Override
	public void onProviderEnabled(String provider) {

	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {

	}

	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}

}
