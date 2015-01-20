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
package lupos.geo.stsparql.functions;

import com.vividsolutions.jts.geom.Geometry;
import lupos.datastructures.items.literal.LiteralFactory;
import lupos.engine.operators.singleinput.filter.expressionevaluation.EvaluationVisitorImplementation;
import lupos.engine.operators.singleinput.filter.expressionevaluation.ExternalFunction;
import lupos.engine.operators.singleinput.TypeErrorException;
import lupos.geo.GeoHelper;

/**
 * Richard Mietz
 * Date: 20.02.13
 */
public class Distance implements ExternalFunction
{
    @Override
    public Object evaluate(Object[] args) throws TypeErrorException
    {
        if(args.length==2)
        {
            Geometry geo1 = GeoHelper.getStSPARQLGeometry(args[0]);
            Geometry geo2 = GeoHelper.getStSPARQLGeometry(args[1]);
            Double distance = geo1.distance(geo2);
            return LiteralFactory.createTypedLiteralWithoutException(String.valueOf(distance), "<http://www.w3.org/2001/XMLSchema#double>");
        }
        else
        {
            throw new TypeErrorException("Distance Function expects exactly 2 arguments: two geometry objects.");
        }
    }

    public static void register(){
        EvaluationVisitorImplementation.registerExternalFunction(LiteralFactory.createURILiteralWithoutLazyLiteralWithoutException("<" + GeoHelper.stSPARQLFunctionUri + "union>"), new Distance());
    }
}
