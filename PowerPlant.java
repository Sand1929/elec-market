package com.goodeast.economics;

/**
 * 
 * @author amsuh
 * 
 * A power plant for simulation of the wholesale electricity market
 * 
 * Power plants generate electricity at a rate equal to outputRate
 * outputRate cannot exceed maxOutputRate
 * 
 * We assume marginal cost of output remains constant for each plant
 *
 */

public abstract class PowerPlant {
	// current rate of electricity output
	protected double outputRate;
	
	// maximum rate of electricity output
	protected double maxOutputRate;
	
	// marginal cost of output
	protected double margCost;
	
	// constructor
	public PowerPlant(double rate, double max, double marg) {
		super();
		outputRate = rate;
		maxOutputRate = max;
		margCost = marg;
	}
	
	// accessor and mutator methods
	public double getOutputRate() {
		return outputRate;
	}
	
	public double getMaxOutputRate() {
		return maxOutputRate;
	}
	
	public double getMargCost() {
		return margCost;
	}
	
	// set output rate of plant
	// return true on success, false otherwise
	public abstract boolean setOutputRate(double rate);
}
