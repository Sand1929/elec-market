package com.goodeast.economics;

/**
 * 
 * @author amsuh
 * 
 * An order that an electricity company can place in the wholesale
 * electricity market
 * 
 * Like a MarketOrder, an ElectricityOrder includes the price and quantity 
 * ordered, but it also includes the company placing the order and the region
 * for which the company is placing the order
 *
 */

public class ElectricityOrder extends MarketOrder {
	// electricity company placing order
	protected ElectricityCompany company;
	
	// region placing order
	protected Region location;
	
	// constructor
	public ElectricityOrder(double dollars, double amount, ElectricityCompany co, Region place) {
		super(dollars, amount);
		company = co;
		location = place;
	}
	
	// accessor methods
	public ElectricityCompany getCompany() {
		return company;
	}
	
	public Region getLocation() {
		return location;
	}
	
	// updates quantity when an order is partially filled
	// quantity can only be reduced and cannot be reduced to less than 0
	// returns true on success, false otherwise
	public boolean updateQuantity(double change) {
		if(change <= 0 && quantity + change >= -1*Math.ulp(quantity)) {
			quantity += change;
			return true;
		}
		else
			return false;
	}
}
