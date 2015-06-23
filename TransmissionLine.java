package com.goodeast.economics;

/**
 * 
 * @author amsuh
 * A transmission line for the wholesale electricity market
 *
 */

public class TransmissionLine {
	// the capacity of the line
	private double capacity;
	
	// the regions providing and receiving electricity
	// electricity can run from sink to source; the naming of the two regions is arbitrary
	private Region source, sink;
	
	// the amount of electricity running through the line from source to sink
	// if negative, current is from sink to source
	private double current = 0;
	
	// net amount of electricity sent in transmissions for current transactions
	// if negative, amount is from sink to source
	private double transmissionsAmount = 0;
	
	// whether the transmission line is asked to complete a transaction from source to sink or sink to source
	private boolean requestedForward = false;
	private boolean	requestedBackward = false;
	
	// constructor
	public TransmissionLine(double a, Region b, Region c) {
		super();
		capacity = a;
		source = b;
		sink = c;
	}
	
	// accessor and mutator methods
	public double getCapacity() {
		return capacity;
	}
	
	public Region getSource() {
		return source;
	}
	
	public Region getSink() {
		return sink;
	}
	
	public double getCurrent() {
		return current;
	}
	
	public double getTransmissionsAmount() {
		return transmissionsAmount;
	}
	
	public boolean getRequestedForward() {
		return requestedForward;
	}
	
	public void setRequestedForward(boolean b) {
		requestedForward = b;
	}
	
	public boolean getRequestedBackward() {
		return requestedBackward;
	}
	
	public void setRequestedBackward(boolean b) {
		requestedBackward = b;
	}
	
	// request capacity of line in a given direction
	public double requestFreeCapacity(Region start, Region end) {
		// if request is in same direction as current
		if(start == source && end == sink) 
			return capacity - current;
		// else if request is in opposite direction
		else if(end == source && start == sink) 
			return capacity + current;
		// else inputs were invalid; return 0
		else
			return 0;
	}
	
	// tries to add requested current to line 
	// must input a region, whether region is sending or receiving electricity, and amount to be sent
	// returns the amount of requested current that could not be sent
	public double requestTransmission(Region start, Region end, double amount) {
		// make sure start and end are the line's source and sink 
		if((start != source && start != sink) || (end != source && end != sink) || start == end) 
			return amount; // if not, stop transmission
		
		// amount of requested current that could not be sent, if any
		double failedAmount = amount;
		
		// if line does not have sufficient capacity or sending region does not have sufficient electricity,
		// reduce amount to be sent across line:
		
		// if request is from source to sink
		if(start == source) { 
			// check for spare capacity
			if(capacity < amount + current)
				amount = capacity - current;
			// make sure sending region has enough electricity
			if(amount > source.getElectricity() - source.getDemand())
				amount = source.getElectricity() - source.getDemand();
		}
		// else request must be from sink to source
		else {
			// check for spare capacity
			if(capacity < amount - current)
				amount = capacity + current;
			// make sure sending region has enough electricity
			if(amount > sink.getElectricity() - sink.getDemand())
				amount = sink.getElectricity() - sink.getDemand();
		}
		
		// amount must be greater than 0
		if(amount <= 0)
			return failedAmount;
		
		// execute transmission:
		
		// transfer electricity between regions:
		// if request is from source to sink
		if(start == source) {
			// transfer electricity
			if(source.changeElectricity(-1*amount) && source.changeExcessElectricity(-1*amount) && sink.changeElectricity(amount) && sink.changeExcessElectricity(amount)) {
				// if transfer is successful, 
				// update current and transmissionsAmount and calculate requested current not sent
				current += amount;
				transmissionsAmount += amount;
				failedAmount -= amount;
			}
			// if transfer fails, print error
			else 
				System.out.println("An error occurred while sending electricity across regions");
		}
		// else request must be from sink to source
		else {
			// transfer electricity
			if(sink.changeElectricity(-1*amount) && sink.changeExcessElectricity(-1*amount) && source.changeElectricity(amount) && source.changeExcessElectricity(amount)) {
				// if transfer is successful, 
				// update current and transmissionsAmount and calculate requested current not sent
				current -= amount;
				transmissionsAmount -= amount;
				failedAmount -= amount;
			}
			// if transfer fails, print error
			else 
				System.out.println("An error occurred while sending electricity across regions");
		}
		
		// return the amount of requested current that could not be sent
		return failedAmount;
	}
	
	// clear transaction-related information from line
	public void clearTransactionInfo() {
		transmissionsAmount = 0;
		requestedForward = false;
		requestedBackward = false;
	}
}
