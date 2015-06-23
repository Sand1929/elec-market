package com.goodeast.economics;

/**
 * 
 * @author amsuh
 * 
 * A market order for an economic market such as a stock market
 *
 */

public class MarketOrder {
	// price ordered
	protected double price;
	
	// quantity ordered
	protected double quantity;
	
	// constructor
	public MarketOrder(double dollars, double amount) {
		super();
		price = dollars;
		quantity = amount;
	}
	
	// accessors
	public double getPrice() {
		return price;
	}
	
	public double getQuantity() {
		return quantity;
	}
}
