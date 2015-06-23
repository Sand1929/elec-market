package com.goodeast.economics;

/**
 * 
 * @author amsuh
 * 
 * Models an electricity company in the wholesale electricity market
 * 
 * In this model, each electricity company provides electricity for a given 
 * set of regions and owns a given set of power plants in each region. 
 * Each of the company's regions has a given level of demand for electricity 
 * that the company must meet. The company can meet this demand by producing
 * electricity with its power plants, transferring electricity between regions
 * it owns, buying electricity from other companies, or using some combination
 * of these methods. 
 * 
 * A company's objective is to minimize expenses while meeting the regions' 
 * demand for electricity. If a company has the capacity to produce excess
 * electricity, it can sell this electricity to other companies to further
 * minimize expenses. To buy and sell electricity, companies place orders in 
 * an auction market for electricity.
 * 
 * Suppose a company needs to buy electricity to meet its regions' demand, and 
 * there is only one company from which it can buy electricity. Then this sole
 * seller wants to charge an infinite price for its electricity, knowing
 * that the only way the other company can satisfy its regions' demand is by 
 * accepting this infinite price. However, this is unrealistic, so we assume 
 * that each company has a maximum price that it is willing to pay for 
 * electricity, and that it will choose to shut down its business rather than
 * pay an exorbitant price greater than this maximum price.
 * 
 */

import java.util.*;

public class ElectricityCompany {
	// max price that company will pay for electricity
	protected double maxPrice;
	
	// company's expenses 
	protected double expenses = 0;
	
	// the regions to which the company provides power
	List<Region> regions = new ArrayList<Region>();
	
	// list of buy orders
	List<ElectricityOrder> buyOrders = new ArrayList<ElectricityOrder>();
	// list of sell orders
	List<ElectricityOrder> sellOrders = new ArrayList<ElectricityOrder>();
	
	// constructor
	public ElectricityCompany(double price) {
		super();
		maxPrice = price;
	}
	
	// accessor and mutator methods
	public double getMaxPrice() {
		return maxPrice;
	}
	
	public double getExpenses() {
		return expenses;
	}
	
	public void updateExpenses(double change) {
		expenses += change;
	}
	
	/*
	 * submits trades to the auction market, choosing trades that minimize the
	 * company's expenses
	 * 
	 * Unless firms are thinking strategically (i.e., not competitively), we 
	 * assume that sellers place orders at the minimum price they are willing 
	 * to offer and buyers place orders at the maximum price they are willing 
	 * to take. From this starting point, we calculate equilibrium prices for 
	 * the market (see the AuctinMarket class).
	 */
	public void orderTrades(AuctionMarket market) {
		// comparator for power plants
		PlantMargCostComparator margCostOrder = new PlantMargCostComparator();
		// temporary storage
		Region tempRegion;
		PowerPlant tempPlant;
		double surplus;
		
		// iterator for lists of power plants
		ListIterator<PowerPlant> plantItr;
		
		// iterate through regions
		Iterator<Region> regionItr = regions.iterator();
		while(regionItr.hasNext()) {
			tempRegion = regionItr.next();
			
			// sort the region's list of power plants by cost
			Collections.sort(tempRegion.powerPlants, margCostOrder); 
			
			// calculate region's surplus electricity
			surplus = tempRegion.getElectricity() - tempRegion.getDemand();
			
			// iterate through region's power plants, from most costly to least costly
			plantItr = tempRegion.powerPlants.listIterator(tempRegion.powerPlants.size());
			
			// if region has any surplus, sell the surplus:
			if(surplus > 0) {
				while(plantItr.hasPrevious()) {
					tempPlant = plantItr.previous();
					
					// if we have reached base-load plants, try to get rid of all surplus electricity
					if(tempPlant.getClass() == BasePlant.class) {
						// place a sell order for surplus 
						// price is 0 because that is point when region is indifferent about selling it
						market.placeSellOrder(0, surplus, this, tempRegion);
						// there is no more surplus, so break
						break;
					}
					// else plant is not base-load, so if surplus is less than or equal to output...
					else if(surplus <= tempPlant.getMaxOutputRate()) {
						// ... place a sell order for surplus, trying to sell it for at least what it costs to produce output 
						market.placeSellOrder(tempPlant.getMargCost(), surplus, this, tempRegion);
						// there is no more surplus, so...
						// if surplus was equal to output, break
						if(surplus == tempPlant.getMaxOutputRate())
							break;
						// else surplus was less than output, so change output, set plantItr to return this plant again, and then break
						else {
							tempPlant.setOutputRate(tempPlant.getMaxOutputRate() - surplus);
							plantItr.next();
							break;
						}
					}
					// else surplus must be greater than output, and plant is not base-load
					else {
						// place a sell order for plant's output, trying to sell it for at least what it costs to produce output
						market.placeSellOrder(tempPlant.getMargCost(), tempPlant.getMaxOutputRate(), this, tempRegion);
						// reduce surplus by amount sold
						surplus -= tempPlant.getMaxOutputRate();
					}
				}
			}
			// else if surplus is less than 0, there is a deficit
			else if(surplus < 0) {
				// TODO: first try to cover deficit with surplus from company's other regions
				// so try to buy electricity to cover deficit
				market.placeBuyOrder(maxPrice, -1 * surplus, this, tempRegion);
			}
			
			// if region is meeting demand with non-base-load plants, 
			// see if region can buy electricity instead of producing it with non-base-load plants:
			
			// iterate through remaining power plants 
			while(plantItr.hasPrevious()) {
				tempPlant = plantItr.previous();
				
				// if we reach base-load plants, stop
				if(tempPlant.getClass() == BasePlant.class)
					break;
				// else try to buy electricity for cheaper than plant's output cost
				else 
					market.placeBuyOrder(tempPlant.getMargCost(), tempPlant.getOutputRate(), this, tempRegion);
			}
		}
	}
	
	
	/*
	 * stops unnecessary output from company's power plants in a way that
	 * minimizes expected expenses
	 */
	public void stopExtraOutput() {
		// temporary storage
		Region tempRegion;
		PowerPlant tempPlant;
		double surplus;
		
		// iterator for power plants
		ListIterator<PowerPlant> plantItr;
		
		// iterate through regions
		Iterator<Region> regionItr = regions.iterator();
		while(regionItr.hasNext()) {
			// have region stop producing any surplus electricity:
			tempRegion = regionItr.next();
			
			// calculate region's surplus electricity
			surplus = tempRegion.getElectricity() - tempRegion.getDemand();
			
			// if there is no surplus electricity, don't reduce plants' output
			if(surplus <= 0)
				continue;
			
			// iterate through power plants in this region, from highest cost plants to lowest cost
			plantItr = tempRegion.powerPlants.listIterator(tempRegion.powerPlants.size());
			while(plantItr.hasPrevious()) {
				tempPlant = plantItr.previous();
				// if we have reached the base-load plants in list, stop; we cannot change the output of these plants
				if(tempPlant.getClass() == BasePlant.class) 
					break;
				
				// if surplus is less than or equal to output
				if(surplus <= tempPlant.getMaxOutputRate()) {
					// reduce plant's output
					tempPlant.setOutputRate(tempPlant.getMaxOutputRate() - surplus);
					// change electricity 
					tempRegion.changeElectricity(-1*surplus);
					// update expenses
					expenses -= surplus * tempPlant.getMargCost();
					// there is no more surplus, so break;
					break;
				}
				// else surplus must be greater than output
				else {
					// set plant's output to 0
					tempPlant.setOutputRate(0);
					// change electricity, expenses, and surplus
					tempRegion.changeElectricity(-1*tempPlant.getMaxOutputRate());
					expenses -= tempPlant.getMaxOutputRate() * tempPlant.getMargCost();
					surplus -= tempPlant.getMaxOutputRate();
				}
			}
		}
	}	
}
