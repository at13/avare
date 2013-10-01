/*
Copyright (c) 2013, Avare software (apps4av@gmail.com) 
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

    * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
    *     * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
    *
    *     THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
package com.ds.avare.position;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;

import android.annotation.SuppressLint;
import com.ds.avare.LocationView;
import com.ds.avare.gps.GpsParams;

public class KMLRecorder {
	private LocationView 	mLV;					// Link back to location view for GPS and prefs
    private BufferedWriter  mTracksFile;			// File handle to use for writing the data
    private File            mFile;					// core file handler
    private Timer           mTimer;					// background timer task object
    private LinkedList<Coordinate> mPositionHistory;// Stored GPS points 
	
    public static final String mKMLFilePrefix = 
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
			"<kml xmlns=\"http://www.opengis.net/kml/2.2\">\n" +
			"	<Document>\n" +
			"		<name>Flight Data by Avare</name>\n" +
			"		<Style id=\"AircraftFlight\">\n" +
			"			<LineStyle>\n" +
			"				<color>ffff00ff</color>\n" +
			"				<width>4</width>\n" +
			"			</LineStyle>\n" +
			"			<PolyStyle>\n" +
			"				<color>7fcccccc</color>\n" +
			"			</PolyStyle>\n" +
			"		</Style>\n" +
			"		<Placemark>\n" +
			"			<name>Avare Flight Path</name>\n" +
			"			<visibility>1</visibility>\n" +
			"			<description>3-D Flight Position Data</description>\n" +
			"			<styleUrl>#AircraftFlight</styleUrl>\n" +
			"			<LineString>\n" +
			"				<extrude>1</extrude>\n" +
			"				<altitudeMode>absolute</altitudeMode>\n" +
			"				<coordinates>\n";

    public static final String mKMLFileSuffix = 
            "				</coordinates>\n" +
    		"			</LineString>\n" +
    		"		</Placemark>\n" +
    		"	</Document>\n" +
    		"</kml>\n";

    /* 
     * Task to write our current position to a file. If the mTracksFile is open, then just write out the long,lat,alt \n 
     * to that file IF our current speed is non zero. This is set up to be called every [config] seconds. Google wants altitude in meters.
     */
    private class addPositionToKMLTask extends TimerTask {

        public void run() {
        	if(mTracksFile!= null) {
        		GpsParams gpsParams = mLV.getGpsParams();
        		if(gpsParams.getSpeed() >= mLV.getPref().getTrackUpdateSpeed()) {
        			try {
        				mTracksFile.write ("\t\t\t\t\t" + gpsParams.getLongitude() + "," + gpsParams.getLatitude() + "," + (gpsParams.getAltitude() * .3048) + "\n");
        				Coordinate gpsPosition = new Coordinate(gpsParams.getLongitude(), gpsParams.getLatitude());
        				mPositionHistory.add(gpsPosition);
        			} catch (IOException ioe) { }
        		}
        	}
        }
    }

    public KMLRecorder(LocationView lv){
    	mLV = lv;
    	mPositionHistory = new LinkedList<Coordinate>();
    }
    
    public boolean isRecording() {
    	return mTracksFile != null;
    }
    
    public void stop(){
        /*
         * Irrespective of enable/disable, we need to close the current tracks file if it is
         * already open
         */
    	if(mTracksFile != null) {
    		// Close the file
    		// Turn off the timer for running the background thread
    		//
    		try {
        		mTracksFile.write(mKMLFileSuffix);
    			mTracksFile.close();
    			if(mTimer != null) 
    				mTimer.cancel();
    		} catch (IOException ioe) { }

    		// Clear out our control objects
    		//
    		mTracksFile = null;
    		mTimer = null;
    	}
    }
    
    @SuppressLint("SimpleDateFormat")
	public void start() {
			
		// Build the file name based upon the current date/time
		//
		String fileName = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(Calendar.getInstance().getTime()) + ".KML";
    	mFile = new File(mLV.getPref().mapsFolder(), fileName);

    	// File handling can throw some exceptions
    	//
    	try {
    		
    		// If the file does not exist, then create it. Get some
    		// writer objects from it
    		//
        	if(mFile.exists() == false){
        		mFile.createNewFile();
        	}
        	FileWriter fileWriter = new FileWriter(mFile);
    		mTracksFile = new BufferedWriter(fileWriter, 8192);

    		// Write out the opening file prefix
    		//
    		mTracksFile.write(mKMLFilePrefix);

    		// Start a timer task that runs a thread in the background to
    		// record each position at the configured interval
    		//
            mTimer = new Timer();	// Create a timer for writing the tracks
            TimerTask taskTracks = new addPositionToKMLTask();	// The task thread that does the work
            mTimer.scheduleAtFixedRate(taskTracks, 0, mLV.getPref().getTrackUpdateTime() * 1000);	// Set to run at the configured number of seconds

    	} catch (IOException ioe) { }
    }
    
    public LinkedList<Coordinate> getPositionHistory() {
    	return mPositionHistory;
    }
    
    public void clearPositionHistory() {
    	mPositionHistory.clear();
    }
}
