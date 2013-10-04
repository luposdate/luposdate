/**
 * Copyright (c) 2013, Institute of Information Systems (Sven Groppe and contributors of LUPOSDATE), University of Luebeck
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 *
 * 	- Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 * 	  disclaimer.
 * 	- Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
 * 	  following disclaimer in the documentation and/or other materials provided with the distribution.
 * 	- Neither the name of the University of Luebeck nor the names of its contributors may be used to endorse or promote
 * 	  products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
 * GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package lupos.geo.geosparql;

import lupos.geo.geosparql.functions.GeofBoundary;
import lupos.geo.geosparql.functions.GeofConvexHull;
import lupos.geo.geosparql.functions.GeofDifference;
import lupos.geo.geosparql.functions.GeofDistance;
import lupos.geo.geosparql.functions.GeofEnvelope;
import lupos.geo.geosparql.functions.GeofGetSRID;
import lupos.geo.geosparql.functions.GeofIntersection;
import lupos.geo.geosparql.functions.GeofRelate;
import lupos.geo.geosparql.functions.GeofSymDifference;
import lupos.geo.geosparql.functions.GeofUnion;
import lupos.geo.geosparql.functions.eh.GeofEhContains;
import lupos.geo.geosparql.functions.eh.GeofEhCoveredBy;
import lupos.geo.geosparql.functions.eh.GeofEhCovers;
import lupos.geo.geosparql.functions.eh.GeofEhInside;
import lupos.geo.geosparql.functions.rcc8.GeofRcc8dc;
import lupos.geo.geosparql.functions.rcc8.GeofRcc8ec;
import lupos.geo.geosparql.functions.rcc8.GeofRcc8ntpp;
import lupos.geo.geosparql.functions.rcc8.GeofRcc8ntppi;
import lupos.geo.geosparql.functions.rcc8.GeofRcc8po;
import lupos.geo.geosparql.functions.rcc8.GeofRcc8tpp;
import lupos.geo.geosparql.functions.rcc8.GeofRcc8tppi;
import lupos.geo.geosparql.functions.sf.GeofSfContains;
import lupos.geo.geosparql.functions.sf.GeofSfCrosses;
import lupos.geo.geosparql.functions.sf.GeofSfIntersects;
import lupos.geo.geosparql.functions.sf.GeofSfWithin;
import lupos.geo.geosparql.functions.sf_eh.GeofDisjoint;
import lupos.geo.geosparql.functions.sf_eh.GeofOverlaps;
import lupos.geo.geosparql.functions.sf_eh.GeofSfTouchesEhMeet;

/**
 * Richard Mietz
 * Date: 21.02.13
 */
public class GeoFunctionRegisterer {

    public static void registerGeoFunctions() {
        GeofBoundary.register();
        GeofConvexHull.register();
        GeofDifference.register();
        GeofDistance.register();
        GeofEnvelope.register();
        GeofGetSRID.register();
        GeofIntersection.register();
        GeofRelate.register();
        GeofSymDifference.register();
        GeofUnion.register();

        //eh package
        GeofEhContains.register();
        GeofEhCoveredBy.register();
        GeofEhCovers.register();
        GeofEhInside.register();

        //rcc8 package
        GeofRcc8dc.register();
        GeofRcc8ec.register();
        GeofRcc8ntpp.register();
        GeofRcc8ntppi.register();
        GeofRcc8po.register();
        GeofRcc8tpp.register();
        GeofRcc8tppi.register();

        //sf package
        GeofSfContains.register();
        GeofSfCrosses.register();
        GeofSfIntersects.register();
        GeofSfWithin.register();

        //sf_eh package
        GeofDisjoint.register();
        GeofOverlaps.register();
        GeofSfTouchesEhMeet.register();
    }
}
