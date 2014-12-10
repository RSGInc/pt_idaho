/*
 * Copyright  2005 PB Consult Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package com.pb.models.pt.tripmodes;

import com.pb.models.pt.Mode;
import com.pb.models.pt.TripModeType;
import com.pb.models.pt.Activity;
import com.pb.models.pt.TourModeType;
import com.pb.models.pt.TripModePersonAttributes;
import com.pb.models.pt.TripModeParameters;
import com.pb.models.pt.ZoneAttributes;
import com.pb.models.pt.util.TravelTimeAndCost;

import org.apache.log4j.Logger;
/**
 * Passenger (three+ person shared ride) mode
 *
 * @author Joel Freedman
 * @version 1.0 12/01/2003
 *
 */
public class SharedRide3Plus extends Mode {
    final static Logger logger = Logger.getLogger("com.pb.idaho.pt.default");

//     public boolean isAvailable=true;
//     public boolean hasUtility=false;
    //     double utility=0;
    public SharedRide3Plus(){
        isAvailable = true;
        hasUtility = false;
        utility = 0.0D;
        alternativeName= "SharedRide3Plus";
        type=TripModeType.SR3P;
    }

    /** Calculates utility of two person shared ride mode
     *
     * @param tc - travel time and cost
     * @param zOrigin - ZoneAttributes at origin(Currently parking cost and terminal time)
     * @param zDestination - ZoneAttributes at destination(Currently parking cost and terminal time)
     * @param p - TourModeParameters
     * @param a - PersonTourModeAttributes
     * @param tourMode - tourMode
     * @param thisActivity - activity
     */

    public void calcUtility(TravelTimeAndCost tc, ZoneAttributes zOrigin, ZoneAttributes zDestination, float[] p,
                            TripModePersonAttributes a, Mode tourMode, Activity thisActivity){

        hasUtility = false;
        utility=-999;
        isAvailable = true;

        if(tc.sharedRide3Time==0) isAvailable=false;
        if(tourMode.type!=TourModeType.AUTODRIVER && tourMode.type!=TourModeType.AUTOPASSENGER
                && tourMode.type!=TourModeType.TRANSITPASSENGER && tourMode.type!=TourModeType.PASSENGERTRANSIT)
            isAvailable=false;

        int autoDriver=0;
        int autoPassenger=0;
        int transitPassenger=0;
        int passengerTransit=0;
        if(tourMode.type==TourModeType.AUTODRIVER)
            autoDriver=1;
        else if(tourMode.type==TourModeType.AUTOPASSENGER)
            autoPassenger=1;
        else if(tourMode.type==TourModeType.TRANSITPASSENGER)
            transitPassenger=1;
        else if(tourMode.type==TourModeType.PASSENGERTRANSIT)
            passengerTransit=1;

        if(isAvailable){
            time=tc.sharedRide3Time; 
            //if duration is zero, round it to 1 for parking costs
            short duration = thisActivity.duration == 0 ? 60 : thisActivity.duration;
            utility=(
                    p[TripModeParameters.IVT]*tc.sharedRide3Time
                            + p[TripModeParameters.OPCPAS]*tc.sharedRide3Cost
                            + p[TripModeParameters.OPCPAS]*(zDestination.parkingCost*(duration/60))
                            + p[TripModeParameters.WLK]*(zOrigin.terminalTime)
                            + p[TripModeParameters.WLK]*(zDestination.terminalTime)
                            + p[TripModeParameters.SR3HH3P]*a.size3p
                            + p[TripModeParameters.DRIVERSR3P]*autoDriver
                            + p[TripModeParameters.PASSSR3P]*autoPassenger
                            + p[TripModeParameters.TRANPASSSR3P]*transitPassenger
                            + p[TripModeParameters.PASSTRANSR3P]*passengerTransit
            );
            if(utility == Double.POSITIVE_INFINITY || utility == Double.NEGATIVE_INFINITY){
                utility = -999;
                isAvailable = false;
            }

            if (trace) {
                logger.info("shared-ride 3p activity duration: " + duration + "/60");
                logger.info("shared-ride 3p parking cost: " + zDestination.parkingCost);
                logger.info("shared-ride 3p utility: " + utility);
                logger.info("\t" + p[TripModeParameters.IVT] + " * "+ tc.sharedRide3Time);
                logger.info("\t" + p[TripModeParameters.OPCPAS] + " * " + tc.sharedRide3Cost  );
                logger.info("\t" + p[TripModeParameters.OPCPAS] + " * " + (zDestination.parkingCost*(duration/60)));
                logger.info("\t" + p[TripModeParameters.WLK] + " * " + zDestination.terminalTime);
                logger.info("\t" + p[TripModeParameters.WLK] + " * " + zOrigin.terminalTime);
                logger.info("\t" + p[TripModeParameters.SR3HH3P] + " * " + a.size3p );
                logger.info("\t" + p[TripModeParameters.DRIVERSR3P] + " * " + autoDriver );
                logger.info("\t" + p[TripModeParameters.PASSSR3P] + " * " + autoPassenger );
                logger.info("\t" + p[TripModeParameters.TRANPASSSR3P] + " * " + transitPassenger );
                logger.info("\t" + p[TripModeParameters.PASSTRANSR3P] + " * " + passengerTransit );
            }
            hasUtility=true;
        }

    }

    public double getUtility(){
        if(!hasUtility){
            logger.fatal("Error: Utility not calculated for "+alternativeName+"\n");
            //TODO - log this error to the node exception file
            System.exit(1);
        }
        return utility;
    }


}

