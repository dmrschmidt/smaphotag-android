package com.example.org.smaphotag;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.android.maps.MapActivity;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class MainActivity extends MapActivity implements LocationListener {

  private LocationManager lm = null;
  private LayoutInflater li;
  
  private boolean recording_mode=false;
  
  private LinearLayout header_view;
  private List<Location> track=new ArrayList<Location>();
  private View started_header;
  private Date track_start_date;
  private SimpleDateFormat dateformat;
	private Handler hndl=new Handler();
  private Location last_known_location;
  
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		dateformat=new SimpleDateFormat("yyyy/MM/dd hh:mm:ss");
		
		ListView lv = (ListView) this.findViewById(R.id.listView1);
		
		li=(LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		
		header_view=new LinearLayout(this);
		
		adjustHeader();
		
		lv.addHeaderView(header_view);
				
		lv.setAdapter(new TrackAdapter(this, R.layout.track_item, new TrackInfo[] {

		new TrackInfo("test", new Date(), new Date())

		}));
		
		this.setTitle("Smaphotag");
	}
	
	public void adjustHeader() {
		header_view.removeAllViews();
		
		if (!recording_mode) {
			View stopped_header=li.inflate(R.layout.stopped_header,null);
			Button btn=(Button)stopped_header.findViewById(R.id.add_button);
			
			btn.setOnClickListener(new OnClickListener()  {
	
				@Override
				public void onClick(View v) {
					recording_mode=true;
					track_start_date=new Date();
					adjustHeader();
				}
				
			});
			
			header_view.addView(stopped_header);
		} else { // recording mode
			if (started_header==null)
				started_header=li.inflate(R.layout.recording_header,null);
			
			
		
			
			Runnable update_runnable=new Runnable () {

				@Override
				public void run() {
					TextView started_tv=(TextView)started_header.findViewById(R.id.start_time_tv);
					started_tv.setText(dateformat.format(track_start_date));		
					
					TextView stopped_tv=(TextView)started_header.findViewById(R.id.stop_time_tv);
					stopped_tv.setText(dateformat.format(new Date()));		
					hndl.postDelayed(this, 100);
				}
				
			};
			
			hndl.post(update_runnable);
		
			
			Button btn=(Button)started_header.findViewById(R.id.stop_btn);
			
			btn.setOnClickListener(new OnClickListener()  {
	
				@Override
				public void onClick(View v) {
					recording_mode=false;
					adjustHeader();
					File f=new File("/sdcard/smaphotag/test.gpx");
					try {
						f.createNewFile();
				
					FileWriter sgf_writer = new FileWriter(f);

					BufferedWriter out = new BufferedWriter(sgf_writer);
					
					out.write(TrackExport.locationListToString(track));
					
					out.close();
					
					
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					track.clear();
					
					if (last_known_location!=null)
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

	@Override
	public void onLocationChanged(Location location) {
		
		Log.i("smaphotag","got location" + location.getLatitude());
		last_known_location=location;
		
		track.add(location);
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
