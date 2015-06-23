package com.goodeast.economics;

/**
 * 
 * @author amsuh
 * 
 * A peaker plant
 * 
 * Producing electricity with a peaker plant is very costly. 
 * 
 * To make up for its costs, however, a peaker plant is able to change its 
 * output rate very quickly, so we assume that a peaker plant can change its 
 * output rate from 0 to maxOutputRate within the time frame of the model
 *
 */

public class PeakerPlant extends PowerPlant {
	// constructor
	public PeakerPlant(double rate, double max, double marg) {
		super(rate, max, marg);
	}
	
	// set output rate of plant
	// rate must be non-negative and less than or equal to maxOutputRate
	// return true on success, false otherwise
	public boolean setOutputRate(double rate) {
		if(rate >= 0 && rate <= maxOutputRate) {
			outputRate = rate;
			return true;
		}
		else
			return false;
	}
}
