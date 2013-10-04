/*
Copyright (c) 2013, Avare software (apps4av@gmail.com) 
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

    * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
    *     * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
    *
    *     THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
package com.ds.avare.flightLog;

import com.ds.avare.gps.GpsParams;

/**
 * The flight log class is an attempt to implement an automated flight log.
 * Certain assumptions can be made just by watching the GPS position information.
 * 1) Once flight has begun (speed > threshold) then look for the airport nearest 
 *	  the current position. That is our start point for the flight. Note the time
 *    and heading.
 * 2) If the speed drops below the threshold, then note position and search for the
 *    nearest airport. If our current altitude is "close" to the altitude of the
 *    airport, then this is our destination. Note the current time and heading, 
 *    calculate elapsed time from start and make an entry into the log book table.
 *    
 * @author Ron
 *
 */
public class FlightLog  {
	private GpsParams mGpsParams;
	
	public FlightLog(){
	}
	
	public void setGpsParams(GpsParams gpsParams){
		mGpsParams = gpsParams;
	}
}
