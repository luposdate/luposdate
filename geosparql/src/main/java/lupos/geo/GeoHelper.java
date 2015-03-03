/**
 * Copyright (c) 2007-2015, Institute of Information Systems (Sven Groppe and contributors of LUPOSDATE), University of Luebeck
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
package lupos.geo;

import lupos.datastructures.items.literal.TypedLiteral;
import lupos.engine.operators.singleinput.TypeErrorException;
import lupos.engine.operators.singleinput.filter.expressionevaluation.Helper;
import lupos.geo.deserializer.GeoSPARQLWktDeserializer;
import lupos.geo.deserializer.GmlDeserializer;
import lupos.geo.deserializer.StSPARQLWktDeserializer;

import com.vividsolutions.jts.geom.Geometry;

/**
 * Richard Mietz
 * Date: 21.02.13
 *
 * @author groppe
 * @version $Id: $Id
 */
public class GeoHelper
{
    /** Constant <code>geoSPARQLFunctionUri="http://www.opengis.net/def/function/geo"{trunked}</code> */
    public static final String geoSPARQLFunctionUri = "http://www.opengis.net/def/function/geosparql/";
    /** Constant <code>geoSPARQLwktDataTypeURI="http://www.opengis.net/ont/geosparql#wk"{trunked}</code> */
    public static final String geoSPARQLwktDataTypeURI = "http://www.opengis.net/ont/geosparql#wktLiteral";
    /** Constant <code>geoSPARQLgmlDataTypeURI="http://www.opengis.net/ont/geosparql#gm"{trunked}</code> */
    public static final String geoSPARQLgmlDataTypeURI = "http://www.opengis.net/ont/geosparql#gmlLiteral";

    /** Constant <code>stURI="http://strdf.di.uoa.gr/ontology#"</code> */
    public static final String stURI = "http://strdf.di.uoa.gr/ontology#";
    /** Constant <code>stWktDataTypeURI="stURI + WKT"</code> */
    public static final String stWktDataTypeURI = stURI + "WKT";
    /** Constant <code>stGeometryDataTypeURI="stURI + geometry"</code> */
    public static final String stGeometryDataTypeURI = stURI + "geometry";
    /** Constant <code>stGmlDataTypeURI="stURI + GML"</code> */
    public static final String stGmlDataTypeURI = stURI + "GML";
    /** Constant <code>stSPARQLFunctionUri="stURI"</code> */
    public static final String stSPARQLFunctionUri = stURI;

    /**
     * <p>getGeoSPARQLGeometry.</p>
     *
     * @param a a {@link java.lang.Object} object.
     * @return a {@link com.vividsolutions.jts.geom.Geometry} object.
     * @throws lupos.engine.operators.singleinput.TypeErrorException if any.
     */
    public static Geometry getGeoSPARQLGeometry(Object a) throws TypeErrorException
    {
    	a = Helper.unlazy(a);
        if (a instanceof TypedLiteral) {
            final TypedLiteral tl = (TypedLiteral) a;
            String content = tl.getContent();
            if(content.startsWith("\"")){
                content = tl.getContent().substring(1, tl.getContent().length() - 1);
            }

            if (tl.getType().compareTo("<" + geoSPARQLwktDataTypeURI + ">") == 0){
                return new GeoSPARQLWktDeserializer().toGeometry(content);
            }
            else if(tl.getType().compareTo("<" + geoSPARQLgmlDataTypeURI + ">") == 0){
                return new GmlDeserializer().toGeometry(content);
            }
            throw new TypeErrorException();
        }
        throw new TypeErrorException();
    }

    /**
     * <p>getStSPARQLGeometry.</p>
     *
     * @param a a {@link java.lang.Object} object.
     * @return a {@link com.vividsolutions.jts.geom.Geometry} object.
     * @throws lupos.engine.operators.singleinput.TypeErrorException if any.
     */
    public static Geometry getStSPARQLGeometry(Object a) throws TypeErrorException {
    	a = Helper.unlazy(a);
        if (a instanceof TypedLiteral) {
            final TypedLiteral tl = (TypedLiteral) a;
            String content = tl.getContent();
            if(content.startsWith("\"")){
                content = tl.getContent().substring(1, tl.getContent().length() - 1);
            }
            if (tl.getType().compareTo("<" + stWktDataTypeURI + ">") == 0 || tl.getType().compareTo("<" + stGeometryDataTypeURI + ">") == 0){
                return new StSPARQLWktDeserializer().toGeometry(content);
            }
            else if(tl.getType().compareTo("<" + stGmlDataTypeURI + ">") == 0){
                return new GmlDeserializer().toGeometry(content);
            }
            throw new TypeErrorException();
        }
        throw new TypeErrorException();
    }


}
