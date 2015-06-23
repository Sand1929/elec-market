package com.goodeast.economics;

/**
 * 
 * @author amsuh
 * Model of wholesale electricity market 
 *
 */

import java.util.*;

public class AuctionMarket {
	// list of bids 
	private List<ElectricityOrder> bids = new ArrayList<ElectricityOrder>();
	// list of asks
	private List<ElectricityOrder> asks = new ArrayList<ElectricityOrder>();
	
	// constructor
	public AuctionMarket() {
		super();
	}
	
	// places a buy order
	public void placeBuyOrder(double price, double quantity, ElectricityCompany company, Region region) {
		// make a new order
		ElectricityOrder newOrder = new ElectricityOrder(price, quantity, company, region);
		
		// add order to market 
		bids.add(newOrder);
		company.buyOrders.add(newOrder);
	}
	
	// cancels a buy order and erases it from company records
	// returns true on success, false otherwise
	public boolean cancelBuyOrder(ElectricityOrder order, ElectricityCompany company) {
		if(bids.remove(order)) {
			company.buyOrders.remove(order);
			return true;
		}
		else
			return false;
	}
	
	// method to view list of bids as two-dimensional array
	// record[x] represents bid x
	// record[x][0] is the price of bid x, record[x][1] is the quantity of bid x
	public double[][] viewBids() {
		// the array that will contain prices and quantities of bids
		double[][] record = new double[bids.size()][2];
		
		// counter to keep track of which order we are on (i.e., which index of record we are on)
		int counter = 0;
		
		// temporary storage
		ElectricityOrder tempOrder;
		
		// iterate over bids
		Iterator<ElectricityOrder> itr = bids.iterator();
		while(itr.hasNext()) {
			tempOrder = itr.next();
			
			// place price and quantity of bid into record
			record[counter][0] = tempOrder.getPrice();
			record[counter][1] = tempOrder.getQuantity();
			
			// increment counter
			++counter;
		}
		
		// return record
		return record;
	}
	
	// places a sell order
	public void placeSellOrder(double price, double quantity, ElectricityCompany company, Region region) {
		// make a new order
		ElectricityOrder newOrder = new ElectricityOrder(price, quantity, company, region);
				
		// add order to market and company records
		asks.add(newOrder);
		company.sellOrders.add(newOrder);
	}
	
	// cancels a sell order and erases it from company records
	// returns true on success, false otherwise
	public boolean cancelSellOrder(ElectricityOrder order, ElectricityCompany company) {
		if(asks.remove(order)) {
			company.sellOrders.remove(order);
			return true;
		}
		else
			return false;
	}
	
	// method to view list of asks as two-dimensional array
	// record[x] represents ask x
	// record[x][0] is the price of ask x, record[x][1] is the quantity of ask x
	public double[][] viewAsks() {
		// the array that will contain prices and quantities of asks
		double[][] record = new double[asks.size()][2];
		
		// counter to keep track of which order we are on (i.e., which index of record we are on)
		int counter = 0;
		
		// temporary storage
		ElectricityOrder tempOrder;
		
		// iterate over bids
		Iterator<ElectricityOrder> itr = asks.iterator();
		while(itr.hasNext()) {
			tempOrder = itr.next();
			
			// place price and quantity of bid into record
			record[counter][0] = tempOrder.getPrice();
			record[counter][1] = tempOrder.getQuantity();
			
			// increment counter
			++counter;
		}
		
		// return record
		return record;
	}
	
	// matches bids and asks to try to execute trades
	public void makeTrades() {
		// sort bids and asks in order of ascending price
		OrderPriceComparator priceOrder = new OrderPriceComparator();
		Collections.sort(bids, priceOrder);
		Collections.sort(asks, priceOrder);
		
		// temporary storage for information on trade being executed
		ElectricityOrder highestBid = null; 
		Set<Region> done = new HashSet<Region>();
		List<Region> path = new ArrayList<Region>();
		ArrayDeque<Region> checkPoints = new ArrayDeque<Region>();
		ArrayDeque<Region> deadEnds = new ArrayDeque<Region>();
		int checkPointsNumber = 0;
		int deadEndsNumber = 0;
		double amountOwed = 0;
		double amountSent = 0;
		double tradeQuantity = 0;
		double tradePrice = 0;
		
		executeTrades(asks, done, path, checkPoints, deadEnds, checkPointsNumber, deadEndsNumber, amountOwed, amountSent, tradeQuantity, tradePrice, highestBid);
	}
	
	// helper function for makeTrades(); actual execution of trades
	public void executeTrades(List<ElectricityOrder> askList, Set<Region> done, List<Region> path, ArrayDeque<Region> checkPoints, ArrayDeque<Region> deadEnds, int checkPointsNumber, int deadEndsNumber, double amountOwed, double amountSent, double tradeQuantity, double tradePrice, ElectricityOrder highestBid) {
		// iterator for asks and counter for bids
		Iterator<ElectricityOrder> askItr = askList.iterator();
		int bidsCounter = bids.size() - 1;
		
		// boolean to see if current ask order has been completed
		boolean askFilled = true;
		
		// temporary storage for lowest ask
		ElectricityOrder lowestAsk = null;
		
		// iterate through asks
		while(askItr.hasNext()) {		
			// if the last sell order was filled
			if(askFilled) {
				// get next lowest ask from sorted lists
				lowestAsk = askItr.next();
				// reset bidsCounter
				bidsCounter = bids.size() - 1;
			}
			
			// set the bid to be used in this iteration, assuming there are bids left
			if(bidsCounter > 0)
				highestBid = bids.get(bidsCounter);
			// else we've gone through all the bids, so we're done with this ask order
			else {
				askFilled = true;
				continue;
			}
			
			// if highest bid is greater than or equal to lowest ask
			if(highestBid.getPrice() >= lowestAsk.getPrice()) {
				// try to execute trade:
				// determine quantity to trade
				if(highestBid.getQuantity() >= lowestAsk.getQuantity()) {
					// if buyer's desired quantity is greater than or equal to seller's
					// use seller's quantity
					tradeQuantity = lowestAsk.getQuantity();
				}
				else {
					// else use buyer's quantity
					tradeQuantity = highestBid.getQuantity();
				}
				
				// determine price of trade
				tradePrice = (highestBid.getPrice() + lowestAsk.getPrice()) / 2;
				
				// transfer electricity:
				lowestAsk.getLocation().clearInfo(done);
				lowestAsk.getLocation().changeExcessElectricity(tradeQuantity);
				lowestAsk.getLocation().findTransmissionLines(path, highestBid.getLocation());
				lowestAsk.getLocation().sendElectricity(highestBid.getLocation(), checkPoints, deadEnds);
				do {
					// while there are still dead ends and check points in network of regions, there is still electricity to send
					deadEndsNumber = Region.returnElectricity(lowestAsk.getLocation(), highestBid.getLocation(), checkPoints, deadEnds);
					checkPointsNumber = Region.doCheckPoints(highestBid.getLocation(), checkPoints, deadEnds);
				}
				while(deadEndsNumber > 0 && checkPointsNumber > 0);
				// clear path and done for next trade
				path.clear();
				done.clear();
				
				// transfer money:
				// amount of electricity successfully sent
				amountSent = tradeQuantity - lowestAsk.getLocation().getExcessElectricity();
				// amount of money owed
				amountOwed = tradePrice * amountSent;
				// update companies' expenses
				lowestAsk.getCompany().updateExpenses(-1 * amountOwed);
				highestBid.getCompany().updateExpenses(amountOwed);
				
				// if buy order was only partially completed, update order quantity
				if(amountSent < highestBid.getQuantity() - Math.ulp(highestBid.getQuantity())) 
					highestBid.updateQuantity(-1*amountSent);
				// else buy order was completed, so remove buy order from bids
				else 
					bids.remove(bidsCounter);
				// if any electricity was sent, go through failed orders again
				if(amountSent > Math.ulp(tradeQuantity))
					executeTrades(askList.subList(0, askList.indexOf(lowestAsk)), done, path, checkPoints, deadEnds, checkPointsNumber, deadEndsNumber, amountOwed, amountSent, tradeQuantity, tradePrice, highestBid);
				// if sell order was only partially completed
				if(amountSent < lowestAsk.getQuantity() - Math.ulp(lowestAsk.getQuantity())) {
					// ask order was not filled
					askFilled = false;
					// update order quantity
					lowestAsk.updateQuantity(-1*amountSent);
				}
				// else ask order was completed
				else {
					askFilled = true;
					// remove sell order from asks
					askItr.remove();
				}
				
				// go to next bid
				--bidsCounter;
			}
		}		
	}
}
