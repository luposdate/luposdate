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
