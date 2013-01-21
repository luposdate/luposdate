/**
 * Copyright (c) 2013, Institute of Information Systems (Sven Groppe), University of Luebeck
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
package lupos.optimizations.logical.statistics;

import java.util.HashMap;
import java.util.Map;

import lupos.datastructures.items.Variable;

public class Statistics {

	public static Map<Variable, VarBucket> estimateJoinSelectivity(
			final Map<Variable, VarBucket> partner1,
			final Map<Variable, VarBucket> partner2) {
		if (partner2 == null || partner2.size() == 0)
			return partner1;
		if (partner1 == null || partner1.size() == 0)
			return partner2;
		final Map<Variable, VarBucket> result = new HashMap<Variable, VarBucket>();
		for (final Variable v : partner1.keySet()) {
			final VarBucket vb = partner1.get(v);
			result.put(v, (VarBucket) vb.clone());
		}

		int factor = 0;
		boolean cartesianProduct = true;
		for (final Variable v : partner2.keySet()) {
			if (result.containsKey(v)) {
				cartesianProduct = false;
				factor++;
			}
		}

		if (!cartesianProduct) {
			for (final Variable v : partner2.keySet()) {
				if (result.containsKey(v)) {
					final VarBucket vb = result.get(v);
					final double sumBefore = vb.getSum();
					vb.multiplySelectivities(partner2.get(v), factor);
					final double factorOtherVariables = (sumBefore == 0) ? vb
							.getSum() : vb.getSum() / sumBefore;
					for (final VarBucket vb2 : result.values()) {
						if (vb.equals(vb2))
							continue;
						vb2.multiplySelectivities(factorOtherVariables);
					}
				} else {
					final double sumBefore1 = partner1.values().iterator()
							.next().getSum();
					final double sumBefore2 = partner2.get(v).getSum();
					if (sumBefore2 < sumBefore1) {
						final double factorOtherVariables = (sumBefore1 == 0) ? sumBefore2
								: sumBefore2 / sumBefore1;
						for (final VarBucket vb2 : result.values()) {
							vb2.multiplySelectivities(factorOtherVariables);
						}
						final Map<Variable, VarBucket> zPartner2 = new HashMap<Variable, VarBucket>();
						for (final Variable v2 : partner2.keySet()) {
							final VarBucket vb = partner2.get(v2);
							zPartner2.put(v2, (VarBucket) vb.clone());
						}
						result.putAll(zPartner2);
					} else {
						final double factorOtherVariables = (sumBefore2 == 0) ? sumBefore1
								: sumBefore1 / sumBefore2;
						final VarBucket vb = (VarBucket) partner2.get(v)
								.clone();
						vb.multiplySelectivities(factorOtherVariables);
						result.put(v, vb);
					}
				}
			}
		} else {
			for (final Variable v : partner2.keySet()) {
				// cartesian product
				final double sum1 = partner2.get(v).getSum();
				final double sum2 = result.values().iterator().next().getSum();
				for (final VarBucket vb : result.values()) {
					vb.multiplySelectivities(sum1);
				}
				final Map<Variable, VarBucket> zPartner2 = new HashMap<Variable, VarBucket>();
				for (final Variable v2 : partner2.keySet()) {
					final VarBucket vb = partner2.get(v2);
					zPartner2.put(v2, (VarBucket) vb.clone());
				}
				zPartner2.get(v).multiplySelectivities(sum2);
				result.putAll(zPartner2);
			}
		}
		return result;
	}
}
