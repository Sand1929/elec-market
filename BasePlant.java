package com.goodeast.economics;

/**
 * 
 * @author amsuh
 * 
 * A base load plant
 * 
 * Base load plants typically produce electricity at a very low cost
 * 
 * However, a base load plant cannot change its output rate very quickly,
 * so because the time-horizon of this model is so short, 
 * we assume a base load plant cannot change its output at all
 *
 */

public class BasePlant extends PowerPlant {
	// constructor
	public BasePlant(double rate, double max, double marg) {
		super(rate, max, marg);
	}
	
	// set output rate of plant
	// returns false because in this model, base plants can't change output
	public boolean setOutputRate(double rate) {
		return false;
	}
}
