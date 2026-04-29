package com.pharmacy.util;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class SuggestMoney {

	private static final long[] DENOMS = { 1000, 2000, 5000, 10000, 20000, 50000, 100000, 200000, 500000, 1000000,
			2000000, 5000000, 10000000, 2000000, 5000000, 10000000, 20000000, 50000000, 100000000 };

	public static List<Long> suggest(long amount) {
		Set<Long> result = new LinkedHashSet<>();

		for (long M : DENOMS) {
			long S = ((amount + M - 1) / M) * M;

			if (S > amount)
				result.add(S);

			if (result.size() >= 6)
				break;
		}

		return new ArrayList<>(result);
	}
}
