package com.goodeast.economics;

/**
 * 
 * @author amsuh
 * Models a participant in an economic market
 *
 */

public class Participant {
	// amount of good and dollars participant has
	private double goods;
	private double dollars;
	
	// accessor and mutator methods
	public double getGoods() {
		return goods;
	}
	
	public double getDollars() {
		return dollars;
	}
	
	// methods for buying/selling return true on success, false otherwise
	public boolean buyGoods(double quantity, double cost) {
		// if there is enough money for the purchase, make the transaction
		if(cost <= dollars + Math.ulp(dollars)) {
			goods += quantity;
			dollars -= cost;
			return true;
		}
		// else the transaction is invalid
		else
			return false;
	}
	
	public boolean sellGoods(double quantity, double cost) {
		// if there are enough goods to be sold, sell them
		if(quantity <= goods + Math.ulp(quantity)) {
			goods -= quantity;
			dollars += cost;
			return true;
		}
		// else the transaction is invalid
		else 
			return false;
	}
	
	// constructor
	public Participant(double amount, double cash) {
		super();
		goods = amount;
		dollars = cash;
	}
}
