package com.goodeast.economics;

/**
 * 
 * @author amsuh
 * Contains information for one region in a simulation of the wholesale 
 * electricity market
 * 
 */

import java.util.*;

public class Region {
	// company that provides electricity to this region
	protected ElectricityCompany company;
	
	// amount of electricity currently available for this region
	protected double electricity;
	
	// amount of electricity that region is trying to pass along as part of a transaction
	protected double excessElectricity = 0;
	
	// record which regions this region is connected to via transmission lines
	protected List<TransmissionLine> transmissionLines = new ArrayList<TransmissionLine>();
	
	// list of lines designated as two-way lines for a transaction
	protected List<TransmissionLine> twoWayLines = new ArrayList<TransmissionLine>();
	
	// record power plants available to this region
	protected List<PowerPlant> powerPlants = new ArrayList<PowerPlant>();
	
	// demand in this region
	protected double demand;	
	
	// potential minnows that region could target
	protected List<Region> minnows = new ArrayList<Region>();
	
	// constructor
	public Region(ElectricityCompany owner, double amount) {
		super();
		company = owner;
		demand = amount;
	}
	
	// accessor and mutator methods
	public ElectricityCompany getCompany() {
		return company;
	}
	
	public double getElectricity() {
		return electricity;
	}
	
	// set electricity available to region
	public void setAvailableElectricity() {
		// set electricity to 0
		electricity = 0;
		
		// temporary storage
		PowerPlant temp;
		
		// iterate through power plants
		Iterator<PowerPlant> itr = powerPlants.iterator();
		while(itr.hasNext()) {
			temp = itr.next();
			// if temp is not a base load plant, set its output to max
			if(temp.getClass() != BasePlant.class) 
				temp.setOutputRate(temp.getMaxOutputRate());
			
			// add output of this power plant to available electricity
			electricity += temp.getOutputRate();
			
			// add to company's expenses
			company.updateExpenses(temp.getOutputRate() * temp.getMargCost());
		}
	}
	
	// method for increasing or decreasing electricity
	// returns false if the change is invalid
	public boolean changeElectricity(double change) {
		// if this change would give the region negative electricity
		if(electricity + change < -1*Math.ulp(electricity))
			return false;
		// else make the change
		else {
			electricity += change;
			return true;
		}
	}
	
	public double getExcessElectricity() {
		return excessElectricity;
	}
	
	// method for increasing or decreasing excess electricity
	// returns false if the change is invalid
	public boolean changeExcessElectricity(double change) {
		// if this change would make excessElectricity an invalid value, stop
		if(excessElectricity + change < -1*Math.ulp(excessElectricity) || excessElectricity + change > electricity + Math.ulp(electricity))
			return false;
		// else make the change
		else {
			excessElectricity += change;
			return true;
		}
	}
	
	// method to find transmission lines in region with free capacity for a transaction
	public boolean findTransmissionLines(List<Region> path, Region destination) {
		// indicates whether any of this region's lines can lead to destination
		boolean leadsToDest = false;
		
		// if path does not already include region, this path is not a loop, so continue
		if(!path.contains(this)) {
			// put this region into path
			path.add(this);
			
			// if this region is destination, we're done
			if(this == destination) 
				return true;
			
			// temporary storage
			TransmissionLine temp;
			
			// iterate through transmission lines in region
			Iterator<TransmissionLine> itr = transmissionLines.iterator();
			while(itr.hasNext()) {
				temp = itr.next();
				
				// if this region is line's source, find lines from line's sink
				if(this == temp.getSource()) {
					// if there is a path to destination through temp
					if(temp.getSink().findTransmissionLines(path, destination)) {
						// give temp a transmission request
						temp.setRequestedForward(true);
						// this region does have a path to destination
						leadsToDest = true;
					}
				}
				// else this region must be line's sink, so find lines from line's source
				else {
					// if there is a path to destination through temp
					if(temp.getSource().findTransmissionLines(path, destination)) {
						// give temp a transmission request
						temp.setRequestedBackward(true);
						// this region does have a path to destination
						leadsToDest = true;
					}
				}
				
				// clear regions in path that were added after this region
				path.subList(path.indexOf(this) + 1, path.size());
			}
		}
		
		// return whether this region has a path to destination
		return leadsToDest;
	}
	
	// send electricity across transmission lines for a transaction
	// transmission lines for transaction are determined by findTransmissionLines method
	public void sendElectricity(Region destination, ArrayDeque<Region> checkPoints, ArrayDeque<Region> deadEnds) {
		// if this region is destination, it doesn't need to send any electricity
		if(this == destination) 
			return;
		
		// temporary storage
		TransmissionLine tempLine;
		double tempAmount;
		double tempNumber = Math.ulp(excessElectricity);
		double errorBound;
		
		// amount that region failed to send in a given transmission
		double unsent;
		
		// make a list of the lines in the region designated by findTransmissionLines:
		ArrayList<TransmissionLine> tempList = new ArrayList<TransmissionLine>();
		// iterate over region's lines
		Iterator<TransmissionLine> itr = transmissionLines.iterator();
		while(itr.hasNext()) {
			tempLine = itr.next();
			
			// if tempLine sends electricity both to and from region, region is a check point
			if(tempLine.getRequestedForward() && tempLine.getRequestedBackward()) {
				// add this line to twoWayLines and make sure checkPoints contains this region
				twoWayLines.add(tempLine);
				if(!checkPoints.contains(this))
					checkPoints.add(this);
			}
			
			// if region is tempLine's source 
			if(this == tempLine.getSource()) { 
				// and if tempLine is able to carry current away from region
				if(tempLine.getRequestedForward() && tempLine.requestFreeCapacity(this, tempLine.getSink()) > 0) 
					tempList.add(tempLine); // put tempLine into tempList
			}
			// else region must be tempLine's sink
			else {
				// and if tempLine is able to carry current away from region
				if(tempLine.getRequestedBackward() && tempLine.requestFreeCapacity(this, tempLine.getSource()) > 0) 
					tempList.add(tempLine); // put tempLine into tempList
			}
		}
		
		// have region send electricity until it either cannot or has sent all of the amount to be sent
		while(!tempList.isEmpty() && excessElectricity > tempNumber) {
			// distribute amount to be sent evenly among lines in tempList
			tempAmount = excessElectricity / tempList.size();
			// for comparing doubles
			errorBound = Math.ulp(tempAmount);
			
			// send transmission requests to lines:
			
			// iterate through tempList
			itr = tempList.iterator();
			while(itr.hasNext()) {
				tempLine = itr.next();
				
				// if tempLine runs in both directions, skip it
				if(tempLine.getRequestedForward() && tempLine.getRequestedBackward()) 
					continue;
				
				// if region is tempLine's source 
				if(this == tempLine.getSource()) { 
					// request transmission and record amount that fails to send
					unsent = tempLine.requestTransmission(this, tempLine.getSink(), tempAmount); 
					// if transmission succeeded, have the next region make transmission requests
					if(unsent < tempAmount) 
						tempLine.getSink().sendElectricity(destination, checkPoints, deadEnds);
					// if request was only partially filled
					if(unsent > errorBound) {
						// this line must be at full capacity, so stop requesting it and remove line from tempList
						tempLine.setRequestedForward(false);
						itr.remove();
					}
				}
				// else region must be tempLine's sink
				else {
					// request transmission and record amount that fails to send
					unsent = tempLine.requestTransmission(this, tempLine.getSource(), tempAmount); 
					// if transmission succeeded, have the next region make transmission requests
					if(unsent < tempAmount) 
						tempLine.getSource().sendElectricity(destination, checkPoints, deadEnds);
					// if request was only partially filled
					if(unsent > errorBound) {
						// this line must be at full capacity, so stop requesting it and remove line from tempList
						tempLine.setRequestedBackward(false);
						itr.remove();
					}
				}
			}
		}
		
		// if region has excess electricity and is not a check point, it's a dead end
		if(excessElectricity > tempNumber && !checkPoints.contains(this) && !deadEnds.contains(this))
			deadEnds.add(this);
	}
	
	// after calling sendElectricity(), some regions have excess electricity that they cannot send and are "dead ends"
	// send this excess electricity on a different path or send it back to its original region
	public static int returnElectricity(Region origin, Region destination, ArrayDeque<Region> checkPoints, ArrayDeque<Region> deadEnds) {
		// number of dead ends given as input
		int input = deadEnds.size();
		
		// temporary storage
		Region tempRegion;
		TransmissionLine tempLine;
		double tempAmount;
		double tempExcess;
		
		// amount of electricity that came into region
		double in = 0;
		
		// transmission lines by which excess electricity will be exiting region
		Queue<TransmissionLine> exits = new ArrayDeque<TransmissionLine>();
		
		// iterator for transmission lines
		Iterator<TransmissionLine> lineItr;
		
		// iterate through list of dead ends
		while(!deadEnds.isEmpty()) {
			tempRegion = deadEnds.poll();
			
			// store region's initial excess electricity 
			tempExcess = tempRegion.excessElectricity;
			
			// iterate through transmission lines for region
			lineItr = tempRegion.transmissionLines.iterator();
			while(lineItr.hasNext()) {
				tempLine = lineItr.next();
				
				// record how much electricity came into region:
				// if tempLine has sent electricity into region, increase "in" and add tempLine to exits
				if(tempRegion == tempLine.getSink() && tempLine.getTransmissionsAmount() > 0) {
					in += tempLine.getTransmissionsAmount();
					exits.add(tempLine);
				}
				else if(tempRegion == tempLine.getSource() && tempLine.getTransmissionsAmount() < 0) {
					in -= tempLine.getTransmissionsAmount();
					exits.add(tempLine);
				}
			}
			
			// iterate over exits
			while(true) {
				tempLine = exits.poll();
				if(tempLine == null)
					break;
				
				// have amount of electricity exiting through this line 
				// be proportional to amount sent in through this line:
				
				// if tempRegion is tempLine's source
				if(tempRegion == tempLine.getSource()) {
					// calculate exiting amount
					tempAmount = -1 * tempExcess * tempLine.getTransmissionsAmount() / in;
					// request transmission
					if(tempLine.requestTransmission(tempRegion, tempLine.getSink(), tempAmount) > 0) {
						// error-checking
						System.out.println("An error occurred in dealing with dead ends in a transmission line path");
					}
					// this line leads to a dead end, so stop requesting it
					tempLine.setRequestedBackward(false);
					// have receiving region find a new path for electricity
					tempLine.getSink().sendElectricity(destination, checkPoints, deadEnds);
				}
				// else tempRegion must be sink
				else {
					// calculate exiting amount
					tempAmount = tempExcess * tempLine.getTransmissionsAmount() / in;
					// request transmission
					if(tempLine.requestTransmission(tempRegion, tempLine.getSource(), tempAmount) > 0) {
						// error-checking
						System.out.println("An error occurred in dealing with dead ends in a transmission line path");
					}
					// this line leads to a dead end, so stop requesting it
					tempLine.setRequestedForward(false);
					// have receiving region find a new path for electricity
					tempLine.getSink().sendElectricity(destination, checkPoints, deadEnds);
				}
			}
		}
		
		// return the number of dead ends we started with
		return input;
	}
	
	public static int doCheckPoints(Region destination, ArrayDeque<Region> checkPoints, ArrayDeque<Region> deadEnds) {
		// number of check points given as input
		int input = checkPoints.size();
		
		// temporary storage
		Region tempRegion;
		TransmissionLine tempLine;
		double tempAmount;
		double errorBound;
		Map.Entry<TransmissionLine, Double> tempEntry;
		
		// map of check points and their initial amounts of excess electricity
		HashMap<Region, Double> initialAmounts = new HashMap<Region, Double>();
		
		// map of transmission lines that seem to be blocked, plus the amounts they are trying to send 
		// if an amount is negative, line was trying to send from sink to source
		LinkedHashMap<TransmissionLine, Double> blocked = new LinkedHashMap<TransmissionLine, Double>();
		
		// amount that region failed to send in a given transmission
		double unsent;
		
		// iterators
		Iterator<Region> regionItr;
		ListIterator<TransmissionLine> lineItr;
		Iterator<Map.Entry<TransmissionLine, Double>> setItr;
		
		// whether check points have reached equilibrium with two-way lines
		boolean atEquilibrium;
		// whether no more electricity can be sent through blocked lines
		boolean noMore;
		// whether we are on first iteration through blocked lines
		boolean firstTime;
		
		// send electricity through two-way lines until check points reach equilibrium:
		do {
			// have atEquilibrium and firstTime initially set to true
			atEquilibrium = true;
			firstTime = true;
			
			// iterate over check points to record initial excess electricity
			regionItr = checkPoints.iterator();
			while(regionItr.hasNext()) {
				tempRegion = regionItr.next();
				
				// record check point's initial excess electricity 
				initialAmounts.put(tempRegion, tempRegion.excessElectricity);
			}
			
			// iterate over check points again to send electricity
			for(int i = 0; i < checkPoints.size(); ++i) {
				tempRegion = checkPoints.remove();
				// move tempRegion to back of deque
				checkPoints.add(tempRegion);
				
				// distribute amount to be sent evenly among two-way lines
				tempAmount = tempRegion.excessElectricity / tempRegion.twoWayLines.size();
				// for comparing doubles
				errorBound = Math.ulp(tempAmount);
				
				// iterate over check point's two-way lines to send electricity through each one
				lineItr = tempRegion.twoWayLines.listIterator();
				while(lineItr.hasNext()) {
					tempLine = lineItr.next();
					
					// send electricity:
					// if region is tempLine's source
					if(tempRegion == tempLine.getSource()) {
						// request transmission:
						// if tempLine was blocked, add blocked amount to tempAmount for request
						if(blocked.containsKey(tempLine)) 
							unsent = tempLine.requestTransmission(tempRegion, tempLine.getSink(), tempAmount + blocked.get(tempLine));
						// else use tempAmount for request
						else
							unsent = tempLine.requestTransmission(tempRegion, tempLine.getSink(), tempAmount);
						
						// if transmission was not completely successful, add tempLine to "blocked"
						if(unsent > errorBound) 
							blocked.put(tempLine, unsent);
						
						// make sure receiving region is in checkPoints
						// because it has a two-way line
						if(!checkPoints.contains(tempLine.getSink())) {
							checkPoints.push(tempLine.getSink());
						}
					}
					// else region must be tempLine's sink
					else {
						// request transmission:
						// if tempLine was blocked, add blocked amount to tempAmount for request
						if(blocked.containsKey(tempLine)) 
							unsent = tempLine.requestTransmission(tempRegion, tempLine.getSource(), tempAmount - blocked.get(tempLine));
						// else use tempAmount for request
						else
							unsent = tempLine.requestTransmission(tempRegion, tempLine.getSource(), tempAmount);
						
						// if transmission was not completely successful, add tempLine to "blocked"
						if(unsent > errorBound) 
							blocked.put(tempLine, -1 * unsent);
						
						// make sure receiving region is in checkPoints
						// because it has a two-way line
						if(!checkPoints.contains(tempLine.getSource())) {
							checkPoints.push(tempLine.getSource());
						}
					}
				}
			}
			
			// try to send electricity over blocked lines until no more electricity can be sent:
			do {
				// initially set noMore to true
				noMore = true;
				// iterate over blocked lines to try to send electricity again
				setItr = blocked.entrySet().iterator();
				while(setItr.hasNext()) {
					tempEntry = setItr.next();
					tempLine = tempEntry.getKey();
					tempAmount = tempEntry.getValue();
					errorBound = Math.ulp(tempAmount);
					
					// send electricity:
					// if tempLine was sending from source to sink
					if(tempAmount > 0) {
						// request transmission
						unsent = tempLine.requestTransmission(tempLine.getSource(), tempLine.getSink(), tempAmount);
						// if transmission was completely successful
						if(unsent <= errorBound) 
							setItr.remove(); // remove tempLine from "blocked"
						
						// else if transmission was partially successful
						else if(unsent > errorBound && unsent < tempAmount) {
							// update the value for tempEntry
							tempEntry.setValue(unsent);
							// there is still more electricity to be sent
							noMore = false;
						}
						// else if transmission was completely unsuccessful and this was the first iteration 
						else if(firstTime) {
							// the line is staying at full capacity, so remove line from region's two-way lines and "blocked"
							tempLine.getSource().twoWayLines.remove(tempLine);
							setItr.remove();
						}
					}
					// else region must be tempLine's sink
					else {
						// request transmission
						unsent = tempLine.requestTransmission(tempLine.getSink(), tempLine.getSource(), tempAmount);
						// if transmission was completely successful
						if(unsent <= errorBound) 
							setItr.remove(); // remove tempLine from "blocked"
						
						// else if transmission was partially successful
						else if(unsent > errorBound && unsent < tempAmount) {
							// update the value for tempEntry
							tempEntry.setValue(-1 * unsent);
							// there is still more electricity to be sent
							noMore = false;
						}
						// else if transmission was completely unsuccessful and this was the first iteration 
						else if(firstTime) {
							// the line is staying at full capacity, so remove line from region's two-way lines and "blocked"
							tempLine.getSource().twoWayLines.remove(tempLine);
							setItr.remove();
						}
					}	
				}
				// first iteration is done by now
				firstTime = false;
			}
			while(!noMore);
			
			// check whether check points have reached equilibrium:
			// iterate through check points
			regionItr = checkPoints.iterator();
			while(regionItr.hasNext()) {
				tempRegion = regionItr.next();
				
				// if check point's current excess electricity differs from its initial excess electricity,
				// we are not at equilibrium yet
				if(tempRegion.excessElectricity > initialAmounts.get(tempRegion) + 5 * Math.ulp(initialAmounts.get(tempRegion)) || tempRegion.excessElectricity < initialAmounts.get(tempRegion) - 5 * Math.ulp(initialAmounts.get(tempRegion))) {
					atEquilibrium = false;
					break;
				}
			}
		}
		while(!atEquilibrium);
		
		// iterate over check points again and have each one try to send electricity through one-way lines:
		// first, find current size of checkPoints
		int size = checkPoints.size();
		// iterate
		for(int i = 0; i < size; ++i) {
			// remove this check point from list
			tempRegion = checkPoints.remove();
			
			// remove two-way lines from check point's list of lines
			tempRegion.transmissionLines.removeAll(tempRegion.twoWayLines);
			
			// send electricity through check point's one-way lines
			tempRegion.sendElectricity(destination, checkPoints, deadEnds);
			
			// put two-way lines back into check point's list of lines
			tempRegion.transmissionLines.addAll(tempRegion.twoWayLines);
		}
		
		// return the number of check points we started with
		return input;
	}
	
	// clear transaction-related data from regions and transmission lines for next transaction
	public void clearInfo(Set<Region> done) {
		// try to add this region to "done"
		if(!done.add(this)) // if region is already in "done," we don't need to do anything
			return; 
		// else, clear region's transaction-related data:
		
		// temporary storage
		TransmissionLine temp;
		
		// clear excessElectricity and twoWayLines
		excessElectricity = 0;
		twoWayLines.clear();
		
		// iterate over region's transmission lines
		Iterator<TransmissionLine> itr = transmissionLines.iterator();
		while(itr.hasNext()) {
			temp = itr.next();
			
			// clear requests and transmission records from this line
			temp.clearTransactionInfo();
			
			// if this region is line's source, cleanUp line's sink
			if(this == temp.getSource())
				temp.getSink().clearInfo(done);
			// else if region is line's sink, cleanUp line's source
			else if(this == temp.getSink())
				temp.getSource().clearInfo(done);
			// else region is not connected to line, so remove line from transmissionLines
			else
				itr.remove();
		}
	}
	
	public double getDemand() {
		return demand;
	}
	
	// method for checking surrounding regions for "sharks" and "minnows"
	// used by electric companies for thinkStrategically()
	// parameter "original" tells you whether this is first call to checkVicinity()
	// parameter "checked" records regions already checked by method
	// see thinkStrategically() for descriptions of other parameters
	// NOTE: method currently assumes companies have perfect info on each other's electricity
	// this assumption may be changed in the future
	public void checkVicinity(ElectricityCompany company, Set<Region> linkingCompanyRegions, Set<Region> linkingOtherRegions, Set<Region> sharks, Set<Region> minnows, Set<Region> checked, boolean original) {
		// if both sharks and minnows contain null, there are no sharks or minnows, so stop checking
		if(sharks.contains(null) && minnows.contains(null))
			return;
		
		// if "checked" already includes region, stop
		// else region is added to "checked"
		if(!checked.add(this)) 
			return;
		
		// if this is first call to method, check whether this region belongs to company
		if(original) {
			// if it does, add it to linkingCompanyRegions and stop
			if(company.regions.contains(this)) {
				linkingCompanyRegions.add(this);
				return;
			}
			// else add it to linkingOtherRegions and continue
			else
				linkingOtherRegions.add(this);
		}
		
		// temporary storage
		TransmissionLine temp;
		double surplus;
		boolean add;
		
		// iterators
		Iterator<TransmissionLine> lineItr;
		Iterator<Region> regionItr;
		
		// if region is not from "company," check whether region is a potential shark or minnow:
		if(this.company != company) {
			// figure out how much electricity and demand region has
			surplus = electricity - demand;
			
			// if region has surplus electricity, this region might be a shark
			if(surplus > Math.ulp(electricity)) {
				// if sharks contains null, that is a signal that there are no sharks, 
				// so this region is not a shark; do nothing
				if(sharks.contains(null))
					;
				// else check whether this region and other potential sharks (if any) are from the same company
				else {
					// initially set add to true
					add = true;
					
					// iterate over other potential sharks
					regionItr = sharks.iterator();
					while(regionItr.hasNext()) {
						// if shark in question and this shark are from different companies,
						// then there are no sharks because there are multiple companies in vicinity with surplus
						if(this.company != regionItr.next().company) {
							// so clear sharks, give sharks a null element, set add to false, and break
							sharks.clear();
							sharks.add(null);
							add = false;
							break;
						}
					}
					// if this region is from the same company as all other potential sharks,
					// add this region to sharks
					if(add)
						sharks.add(this);
				}
				// region has surplus electricity, so there are no minnows
				// clear minnows and give it a null element
				minnows.clear();
				minnows.add(null);
			}
			// else if region lacks electricity and minnows does not contain null, region might be a minnow
			// if minnows contains null, that is a signal that there are no minnows
			else if(surplus < -1 * Math.ulp(electricity) && !minnows.contains(null))
				minnows.add(this);
		}
					
		// iterate through transmission lines in region
		lineItr = transmissionLines.iterator();
		while(lineItr.hasNext()) {
			temp = lineItr.next();
			
			// if this region is line's source, keep checking vicinity from line's sink
			if(this == temp.getSource()) 
				temp.getSink().checkVicinity(company, linkingCompanyRegions, linkingOtherRegions, sharks, minnows, checked, false);
			// else if this region is line's sink, keep checking vicinity from line's source
			else if(this == temp.getSink())
				temp.getSource().checkVicinity(company, linkingCompanyRegions, linkingOtherRegions, sharks, minnows, checked, false);
			// else region is not connected to line, so do nothing
		}
	}
}
