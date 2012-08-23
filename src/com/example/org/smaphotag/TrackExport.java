package com.example.org.smaphotag;

import java.util.List;


import android.location.Location;

public class TrackExport {

	public static String locationListToString(List<Location> locations) {
		String res="<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\" ?>\n";
		res+="<gpx creator=\"smaphotag\" version=\"1.0\" >\n";
		res+="<metadata>\n";
		res+="\t<link href=\"http://smaphotag.info/\">\n";
		res+="\t\t<text>smaphotag</text>\n";
		res+="\t</link>\n";
		res+="</metadata>\n";

		res+="<trk>\n\t<name>FlightPlan</name>\n\t<trkseg>\n";
		
		for (Location loc:locations )
		{
			res+="\t\t<trkpt lat=\"" +loc.getLatitude()+"\" lon=\"" + loc.getLongitude() + "\">\n";
			res+="\t\t<ele>" + loc.getAltitude()+"</ele>\n";
			res+="\t\t</trkpt>\n";
		}
		res+="\t</trkseg>\n</trk>\n</gpx>";

		return res;
	}
	
	
}
