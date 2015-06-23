package com.goodeast.economics;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class StrategicElectricityCompany extends ElectricityCompany {
	// constructor
	public StrategicElectricityCompany(double price) {
		super(price);
	}
	
	/*
	 * simulates the strategic decisions a company can make 
	 * to isolate a company (and become a monopoly) or prevent itself from becoming isolated
	 * 
	 * TODO : not complete
	 */
	public void thinkStrategically() {
		// first, put company's regions into another list, which this method will manipulate
		List<Region> regionList = regions;
		// a list of which of this company's regions link to a region in question
		Set<Region> linkingCompanyRegions = new HashSet<Region>();
		// a list of other regions (not owned by this company) that link to a region in question
		Set<Region> linkingOtherRegions = new HashSet<Region>();
		// list of sharks: regions that can isolate or have isolated a region in question
		Set<Region> sharks = new HashSet<Region>();
		// list of minnows: regions that a region in question can isolate or has isolated
		Set<Region> minnows = new HashSet<Region>();
		
		// temporary storage
		Region tempRegion;
		Region otherRegion;
		TransmissionLine tempLine;
		Set<Region> tempMinnows = new HashSet<Region>();
		Set<Region> checked = new HashSet<Region>();
		double surplus;
		double minnowCapacity;
		double sharkCapacity;
		Set<Region> done = new HashSet<Region>();
		List<Region> path = new ArrayList<Region>();
		
		// iterators
		Iterator<Region> regionItr;
		Iterator<Region> otherItr;
		Iterator<TransmissionLine> lineItr;
		
		// iterate over company's regions
		regionItr = regionList.iterator();
		while(regionItr.hasNext()) {
			tempRegion = regionItr.next();
			
			// calculate region's surplus electricity
			// if surplus is negative, there is a deficit
			surplus = tempRegion.getElectricity() - tempRegion.getDemand();
			
			// have region check the area around it, looking for potential sharks and minnows: 
			// iterate over region's transmission lines
			lineItr = tempRegion.transmissionLines.iterator();
			while(lineItr.hasNext()) {
				tempLine = lineItr.next();
				
				// if this region is line's source, check vicinity from line's sink
				if(tempRegion == tempLine.getSource()) 
					tempLine.getSink().checkVicinity(this, linkingCompanyRegions, linkingOtherRegions, sharks, tempMinnows, checked, true);
				// else if this region is line's sink, keep checking vicinity from line's source
				else if(tempRegion == tempLine.getSink()) 
					tempLine.getSource().checkVicinity(this, linkingCompanyRegions, linkingOtherRegions, sharks, tempMinnows, checked, true);
				// else region is not connected to line, so do nothing
				
				// prepare for next call to checkVicinity()
				// clear "checked" 
				checked.clear();
				// add tempMinnows to minnows
				minnows.addAll(tempMinnows);
				// clear tempMinnows
				tempMinnows.clear();
			}
			
			// have region look for sharks:
			// iterate through potential sharks
			otherItr = sharks.iterator();
			while(otherItr.hasNext()) {
				otherRegion = otherItr.next();

			}
			
			// if regions finds potential sharks, record them:
			if(!sharks.isEmpty()) {
				// TODO
				
			}
			// else if region finds no sharks, have region look for minnows
			else {
				// iterate through potential minnows 
				otherItr = minnows.iterator();
				while(otherItr.hasNext()) {
					otherRegion = otherItr.next();
					
				}
			}
			// if region finds minnows and is not connected to other company-owned regions, 
			// isolate and exploit the minnows
			
			// if region finds minnows and is connected to other company-owned regions, 
			// check the other regions and coordinate with them
		}
	}
}
