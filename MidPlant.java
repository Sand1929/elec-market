package com.goodeast.economics;

/**
 * 
 * @author amsuh
 * 
 * A mid-merit plant, also called a load following plant
 * 
 * Mid-merit plants produce electricity at moderate cost and can change
 * output rate at a moderate speed. 
 * 
 * Basically, they strike a balance between base load and peaker plants
 * 
 * In this model, we assume a mid-merit plant is able to change output 
 * fast enough to go from producing 0 to producing at maxOutputRate 
 *
 */

public class MidPlant extends PowerPlant {
	// constructor
	public MidPlant(double rate, double max, double marg) {
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
