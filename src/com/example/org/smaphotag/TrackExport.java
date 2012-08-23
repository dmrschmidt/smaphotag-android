package com.example.org.smaphotag;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import android.location.Location;

public class TrackExport {

	public static String locationListToString(List<Location> locations) {
		String res = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\" ?>\n";
		res += "<gpx creator=\"smaphotag\" version=\"1.0\" >\n";
		res += "<metadata>\n";
		res += "\t<link href=\"http://smaphotag.info/\">\n";
		res += "\t\t<text>smaphotag</text>\n";
		res += "\t</link>\n";
		res += "</metadata>\n";

		// TODO insert name
		res += "<trk>\n\t<name></name>\n\t<trkseg>\n";

		for (Location l : locations) {
			l.getLatitude();
			res += "\t\t<trkpt lat=\"" + l.getLatitude() + "\" lon=\"" + l.getLongitude() + "\">\n";
			String formatted = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").format(new Date(l.getTime()));

			res += "<time>" + formatted.substring(0, 22) + ":" + formatted.substring(22) + "</time>";
			res += "\t\t<ele>" + l.getAltitude() + "</ele>\n";
			res += "\t\t</trkpt>\n";

		}
		res += "\t</trkseg>\n</trk>\n</gpx>";

		return res;
	}

}
