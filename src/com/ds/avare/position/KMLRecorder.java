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
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;

import android.annotation.SuppressLint;
import com.ds.avare.gps.GpsParams;

public class KMLRecorder {
	private long			mStartSpeed;			// Min speed to begin recording
	private GpsParams		mGpsParams;				// Current location information
	private BufferedWriter  mTracksFile;			// File handle to use for writing the data
    private File            mFile;					// core file handler
    private Timer           mTimer;					// background timer task object
    private LinkedList<Coordinate> mPositionHistory;// Stored GPS points 
    private boolean			mClearListOnStart = false;	// Option to clear the linked list at every start
	private URI 			mFileURI;				// The URI of the file created for these datapoints
	
    public static final String KMLFILENAMEFORMAT = "yyyy-MM-dd_HH-mm-ss";
    public static final String KMLFILENAMEEXTENTION = ".KML";
    public static final String KMLFILEPREFIX  = 
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

    public static final String KMLFILESUFFIX = 
            "				</coordinates>\n" +
    		"			</LineString>\n" +
    		"		</Placemark>\n" +
    		"	</Document>\n" +
    		"</kml>\n";

    /** 
     * Task to write our current position to a file. If the mTracksFile is open, then just 
     * write out the long,lat,alt to that file IF our current speed is greater than our 
     * start speed. This is set up to be called every [config] seconds. Google wants altitude in meters.
     */
    private class addPositionToKMLTask extends TimerTask {

        public void run() {
        	synchronized(this) {
	        	if((mTracksFile!= null) && (mGpsParams != null)) {
	        		// The output file is open and we have current location info
	        		//
	        		if(mGpsParams.getSpeed() >= mStartSpeed) {
	        			// We are above the min configured speed, so write a line out
	        			//
	        			try {
	        				mTracksFile.write ("\t\t\t\t\t" + mGpsParams.getLongitude() + "," + 
	        												  mGpsParams.getLatitude() + "," + 
	        												 (mGpsParams.getAltitude() * .3048) + "\n");

	        				// Add this position to our linked list for possible display
	        				// on the charts
	        				//
	        				Coordinate gpsPosition = new Coordinate(mGpsParams.getLongitude(), mGpsParams.getLatitude());
	        				mPositionHistory.add(gpsPosition);
	        			} catch (IOException ioe) { }
	        		}
	        	}
        	}
        }
    }

    public KMLRecorder(){
    	mPositionHistory = new LinkedList<Coordinate>();
    }
    
    /** 
     * Are we actively recording data to a file ? 
     * @return true if we are, false otherwise
     */
    public boolean isRecording() {
    	return mTracksFile != null;
    }
    
    /**
     * Stop saving datapoints to the file and the historical list
     * @return A URI of the file just closed
     */
    public URI stop(){
    	if(mTracksFile != null) {
    		// Close the file
    		// Turn off the timer for running the background thread
    		//
    		try {
        		mTracksFile.write(KMLFILESUFFIX);
    			mTracksFile.close();
    			if(mTimer != null) 
    				mTimer.cancel();
    		} catch (IOException ioe) { }

    		// Clear out our control objects
    		//
    		mTracksFile = null;
    		mTimer = null;
    		return mFileURI;
    	}
    	return null;
    }
    
    /**
     * Begin recording position points to a file and our memory linked list
     * @param folder Directory to store the KML file
     * @param updateTime Number of seconds between datapoints
     */
    @SuppressLint("SimpleDateFormat")
	public void start(String folder, long updateTime) {
			
		// Build the file name based upon the current date/time
		//
		String fileName = new SimpleDateFormat(KMLFILENAMEFORMAT).format(Calendar.getInstance().getTime()) + KMLFILENAMEEXTENTION;
    	mFile = new File(folder, fileName);
    	
    	// File handling can throw some exceptions
    	//
    	try {
    		// If the file does not exist, then create it. 
    		//
        	if(mFile.exists() == false){
        		mFile.mkdirs();			// Ensure the directory path exists
        		mFile.createNewFile();	// Create the new file
        	}

        	// Save off the URI of this file we just created. This value
        	// is returned at the stop() method.
        	//
        	mFileURI = mFile.toURI();
        	
        	// Create a new writer, then a buffered writer for this file
        	//
        	FileWriter fileWriter = new FileWriter(mFile);
    		mTracksFile = new BufferedWriter(fileWriter, 8192);

    		// Write out the opening file prefix
    		//
    		mTracksFile.write(KMLFILEPREFIX);

    		// Start a timer task that runs a thread in the background to
    		// record each position at the configured interval
    		//
            mTimer = new Timer();	// Create a timer for writing the tracks
            TimerTask taskTracks = new addPositionToKMLTask();	// The task thread that does the work
            mTimer.scheduleAtFixedRate(taskTracks, 0, updateTime * 1000);	// Set to run at the configured number of seconds

            // If we are supposed to clear the linked list each time
            // we start timing then do so now
            //
            if(mClearListOnStart == true) {
            	clearPositionHistory();
            }
    	} catch (IOException ioe) { }
    }
    
    /**
     * The positionhistory is a collection of points that a caller can use
     * to examine historical position information
     * @return LinkedList of coordinates of all previous positions. Most recent is
     * at the end.
     */
    public LinkedList<Coordinate> getPositionHistory() {
    	return mPositionHistory;
    }
    
    /**
     * Clear out the linked list of historical position data
     */
    public void clearPositionHistory() {
    	mPositionHistory.clear();
    }
    
    /**
     * This object requires notification of when the position changes. This is
     * done by the caller sending the information periodically via the GpsParams
     * object to this method.
     * @param gpsParams Current location information
     */
    public void setGpsParams(GpsParams gpsParams) {
    	synchronized(this) {
    		mGpsParams = gpsParams;
    	}
    }
    
    /**
     * Config setting to auto-clear the linked list everytime start
     * is called.
     * @param clearListOnStart boolean to indicate whether to clear list or keep it when start is called.
     */
    public void setClearListOnStart(boolean clearListOnStart){
    	mClearListOnStart = clearListOnStart;
    }
}
