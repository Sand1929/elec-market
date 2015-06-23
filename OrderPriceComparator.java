package com.goodeast.economics;

/**
 * 
 * @author amsuh
 * Compares market orders by price
 * Note: this comparator imposes orderings that are inconsistent with equals
 *
 */

import java.util.Comparator;

public class OrderPriceComparator implements Comparator<MarketOrder> {
	@Override
	public int compare(MarketOrder o1, MarketOrder o2) {
		return (int)(o1.getPrice() - o2.getPrice());
	}
}
