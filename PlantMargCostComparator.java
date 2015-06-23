package com.goodeast.economics;

/**
 * 
 * @author amsuh
 * 
 * Orders power plants by marginal cost
 * Base-load plants are less than non-base-load plants, 
 * since they can't change output 
 * (so marginal cost is not applicable for them)
 * 
 * If two plants are of the same type, 
 * the one with the greater marginal cost is greater
 * 
 * Note: this comparator imposes orderings that are inconsistent with equals
 *
 */

import java.util.Comparator;

public class PlantMargCostComparator implements Comparator<PowerPlant> {
	@Override
	public int compare(PowerPlant o1, PowerPlant o2) {
		if(o1.getClass() == BasePlant.class && o2.getClass() != BasePlant.class) 
			return -1;
		if(o1.getClass() != BasePlant.class && o2.getClass() == BasePlant.class)
			return 1;
		return (int)(o1.getMargCost() - o2.getMargCost());
	}
}
