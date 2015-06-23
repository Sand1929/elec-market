package com.goodeast.economics;

/**
 * 
 * @author amsuh
 * Simulation modeling the wholesale electricity market
 *
 */

import java.io.*;
import java.util.*;

public class ElectricitySimulation {

	public static void main(String[] args) throws FileNotFoundException, NumberFormatException, IndexOutOfBoundsException {
		// check usage
		if(args.length != 1) { 
			// if user did not provide input file, print error and stop
			System.out.println("Usage: ElectricitySimulation fileName");
			return;
		}
		
		// storage for inputs
		String line;
		boolean strategic;
		List<ElectricityCompany> companies = new ArrayList<ElectricityCompany>();
		List<ElectricityCompany> shuffledCompanies = new ArrayList<ElectricityCompany>();
		Region tempRegion1 = null;
		Region tempRegion2 = null;
		TransmissionLine tempLine;
		ElectricityCompany tempCompany;
		int companiesCounter = 1, regionsCounter = 1, plantsCounter = 1, transmissionCounter = 1;
		String[] input;
		int i1 = 0;
		double d1 = 0;
		double d2 = 0;
		double d3 = 0;
		
		// iterators
		Iterator<ElectricityCompany> companyItr;
		Iterator<Region> regionItr;
		
		// keep track of line of input file that scanner is on
		int lineCounter = 0;
		
		// get inputs:
		// create a scanner
		Scanner s = null;
		try {
			s = new Scanner(new BufferedReader(new FileReader(args[0])));
		}
		catch(FileNotFoundException ex) {
			System.err.println("Error: the file path you entered could not be found");
		}
		
		// see whether companies are thinking competitively or strategically
		if(s.hasNextLine()) {
			line = s.nextLine().trim();
			// update line count
			++lineCounter;
			
			// get input of first line
			if(line.equalsIgnoreCase("Companies think competitively or strategically: competitively"))
				strategic = false;
			else if(line.equalsIgnoreCase("Companies think competitively or strategically: strategically"))
				strategic = true;
			// else input is invalid
			else {
				System.out.println("Error: invalid input on line " + lineCounter);
				s.close();
				return;
			}
		}
		
		// read input file line by line
		while(s.hasNextLine()) {
			line = s.nextLine().trim();
			// update line count
			++lineCounter;
			
			// if this line has only whitespace 
			if(line.equals("")) {
				// skip it
				continue;
			}
			// if this line declares a power plant correctly
			else if(line.equalsIgnoreCase("Power plant " + plantsCounter)) {
				// get inputs for plant:
				// type
				if(s.hasNextLine()) {
					line = s.nextLine().trim();
					++lineCounter;
					if(line.equalsIgnoreCase("Type is base load, load following, or peaker: base load"))
						i1 = 1;
					else if(line.equalsIgnoreCase("Type is base load, load following, or peaker: load following"))
						i1 = 2;
					else if(line.equalsIgnoreCase("Type is base load, load following, or peaker: peaker"))
						i1 = 3;
					else {
						System.out.println("Error: invalid input on line " + lineCounter);
						s.close();
						return;
					}
				}
				else {
					System.out.println("Error: invalid input on line " + lineCounter);
					s.close();
					return;
				}
				
				// output rate
				if(s.hasNextLine()) {
					line = s.nextLine();
					++lineCounter;
					input = line.split("\\s+");
					if(line.startsWith("If type is base load, output rate is:")) {
						if(i1 == 1 && input.length == 9) {
							try {
								d1 = Double.parseDouble(input[8]);
							}
							catch(NumberFormatException ex) {
								System.err.println("Error: invalid input on line " + lineCounter);
								s.close();
							}
						}
						else if(i1 != 1 && input.length == 8)
							d2 = 0;
						else {
							System.out.println("Error: invalid input on line " + lineCounter);
							s.close();
							return;
						}
					}
					else {
						System.out.println("Error: invalid input on line " + lineCounter);
						s.close();
						return;
					}
				}
				else {
					System.out.println("Error: invalid input on line " + lineCounter);
					s.close();
					return;
				}
				
				// Maximum output rate
				if(s.hasNextLine()) {
					line = s.nextLine();
					++lineCounter;
					input = line.split("\\s+");
					if(line.startsWith("Maximum output rate:") && input.length == 4) {
						try {
							d2 = Double.parseDouble(input[3]);
						}
						catch(NumberFormatException ex) {
							System.err.println("Error: invalid input on line " + lineCounter);
							s.close();
						}
					}
					else {
						System.out.println("Error: invalid input on line " + lineCounter);
						s.close();
						return;
					}
				}
				else {
					System.out.println("Error: invalid input on line " + lineCounter);
					s.close();
					return;
				}
				
				// marginal cost
				if(s.hasNextLine()) {
					line = s.nextLine();
					++lineCounter;
					input = line.split("\\s+");
					if(line.startsWith("Marginal cost:") && input.length == 3) {
						try {
							d3 = Double.parseDouble(input[2]);
						}
						catch(NumberFormatException ex) {
							System.err.println("Error: invalid input on line " + lineCounter);
							s.close();
						}
					}
					else {
						System.out.println("Error: invalid input on line " + lineCounter);
						s.close();
						return;
					}
				}
				else {
					System.out.println("Error: invalid input on line " + lineCounter);
					s.close();
					return;
				}	
				
				// create new power plant
				if(companiesCounter > 1 && regionsCounter > 1) {
					switch (i1) {
						case 1: companies.get(companiesCounter - 2).
								regions.get(regionsCounter - 2).
								powerPlants.add(new BasePlant(d1, d2, d3));
								break;
						
						case 2: companies.get(companiesCounter - 2).
								regions.get(regionsCounter - 2).
								powerPlants.add(new MidPlant(d1, d2, d3));
								break;
							
						case 3: companies.get(companiesCounter - 2).
								regions.get(regionsCounter - 2).
								powerPlants.add(new PeakerPlant(d1, d2, d3));
								break;
					}
				
					++plantsCounter;
				}
				else {
					System.out.println("Error: power plant declared without a company or region");
					s.close();
					return;
				}
			}
			// else if this line declares a region correctly
			else if(line.equalsIgnoreCase("Region " + regionsCounter)) {
				// get input for region
				if(s.hasNextLine()) {
					line = s.nextLine();
					++lineCounter;
					input = line.split("\\s+");
					if(line.startsWith("Demand for electricity for this hour:") && input.length == 7) {
						try {
							d1 = Double.parseDouble(input[6]);
						}
						catch(NumberFormatException ex) {
							System.err.println("Error: invalid input on line " + lineCounter);
							s.close();
						}
					}
					else {
						System.out.println("Error: invalid input on line " + lineCounter);
						s.close();
						return;
					}
				}
				else {
					System.out.println("Error: invalid input on line " + lineCounter);
					s.close();
					return;
				}
				// initialize last region if there is one and create new region
				if(companiesCounter == 2 && regionsCounter == 1) {
					tempRegion1 = new Region(companies.get(0), d1);
					companies.get(0).regions.add(tempRegion1);	
					++regionsCounter;
					plantsCounter = 1;
				}
				else if(companiesCounter > 1) {
					tempRegion1.setAvailableElectricity();
					tempRegion1 = new Region(companies.get(companiesCounter - 2), d1);
					companies.get(companiesCounter - 2).regions.add(tempRegion1);	
					++regionsCounter;
					plantsCounter = 1;
				}
				else {
					System.out.println("Error: region declared without a company");
					s.close();
					return;
				}
			}
			// else if this line declares a company correctly
			else if(line.equalsIgnoreCase("Company " + companiesCounter)) {
				// get input for region
				if(s.hasNextLine()) {
					line = s.nextLine();
					++lineCounter;
					input = line.split("\\s+");
					if(line.startsWith("The maximum price the company will pay when buying electricity:") && input.length == 11) {
						try {
							d1 = Double.parseDouble(input[10]);
						}
						catch(NumberFormatException ex) {
							System.err.println("Error: invalid input on line " + lineCounter);
							s.close();
						}
					}
					else {
						System.out.println("Error: invalid input on line " + lineCounter);
						s.close();
						return;
					}
				}
				else {
					System.out.println("Error: invalid input on line " + lineCounter);
					s.close();
					return;
				}
				// create new company
				companies.add(new ElectricityCompany(d1));
				++companiesCounter;
				regionsCounter = 1;
			}
			// else if this line declares the start of transmission line data
			else if(line.equalsIgnoreCase("Transmission lines")) {
				// initialize last region
				tempRegion1.setAvailableElectricity();
				
				// read input file for transmission lines
				while(s.hasNextLine()) {
					line = s.nextLine().trim();
					// update line count
					++lineCounter;
					
					// if this line has only whitespace 
					if(line.equals("")) {
						// skip it
						continue;
					}
					// if this line correctly declares a transmission line
					else if(line.equalsIgnoreCase("Line " + transmissionCounter)) {
						// get inputs for line:
						// capacity
						if(s.hasNextLine()) {
							line = s.nextLine();
							++lineCounter;
							input = line.split("\\s+");
							if(line.startsWith("Capacity:") && input.length == 2) {
								try {
									d1 = Double.parseDouble(input[1]);
								}
								catch(NumberFormatException ex) {
									System.err.println("Error: invalid input on line " + lineCounter);
									s.close();
								}
							}
							else {
								System.out.println("Error: invalid input on line " + lineCounter);
								s.close();
								return;
							}
						}
						else {
							System.out.println("Error: invalid input on line " + lineCounter);
							s.close();
							return;
						}
						
						// source
						if(s.hasNextLine()) {
							line = s.nextLine();
							++lineCounter;
							input = line.split("\\s+");
							if(line.startsWith("Source: Company") && input.length == 5 && input[3].equalsIgnoreCase("Region")) {
								try {
									tempRegion1 = companies.get(Integer.parseInt(input[2]) - 1).regions.get(Integer.parseInt(input[4]) - 1);
								}
								catch(IndexOutOfBoundsException ex1) {
									System.err.println("Error: invalid source or sink for transmission line on line " + lineCounter);
									s.close();
								}
								catch(NumberFormatException ex2) {
									System.err.println("Error: invalid input on line " + lineCounter);
									s.close();
								}
							}
							else {
								System.out.println("Error: invalid input on line " + lineCounter);
								s.close();
								return;
							}
						}
						else {
							System.out.println("Error: invalid input on line " + lineCounter);
							s.close();
							return;
						}
						
						// sink
						if(s.hasNextLine()) {
							line = s.nextLine();
							++lineCounter;
							input = line.split("\\s+");
							if(line.startsWith("Sink: Company") && input.length == 5 && input[3].equalsIgnoreCase("Region")) {
								try {
									tempRegion2 = companies.get(Integer.parseInt(input[2]) - 1).regions.get(Integer.parseInt(input[4]) - 1);
								}
								catch(IndexOutOfBoundsException ex1) {
									System.err.println("Error: invalid source or sink for transmission line on line " + lineCounter);
									s.close();
								}
								catch(NumberFormatException ex2) {
									System.err.println("Error: invalid input on line " + lineCounter);
									s.close();
								}
							}
							else {
								System.out.println("Error: invalid input on line " + lineCounter);
								s.close();
								return;
							}
						}
						else {
							System.out.println("Error: invalid input on line " + lineCounter);
							s.close();
							return;
						}
						
						// create new line
						tempLine = new TransmissionLine(d1, tempRegion1, tempRegion2);
						tempRegion1.transmissionLines.add(tempLine);
						tempRegion2.transmissionLines.add(tempLine);
						++transmissionCounter;
					}
				}
			}
			// else input is invalid
			else {
				System.out.println("Error: invalid input on line " + lineCounter);
				s.close();
				return;
			}
		}
		s.close();
		
		// start simulation:
		// create new auction market for electricity
		AuctionMarket market = new AuctionMarket();
		
		// shuffle companies
		shuffledCompanies.addAll(companies);
		long seed = System.nanoTime();
		Collections.shuffle(shuffledCompanies, new Random(seed));
		// iterate through companies to make trades
		companyItr = shuffledCompanies.iterator();
		while(companyItr.hasNext()) {
			tempCompany = companyItr.next();
			
			// have companies order trades
			tempCompany.orderTrades(market);
			// market tries to execute trades
			market.makeTrades();
		}
		
		// iterate through companies again to make final adjustments
		companyItr = companies.iterator();
		while(companyItr.hasNext()) {
			tempCompany = companyItr.next();
			
			// have companies order trades
			tempCompany.stopExtraOutput();;
		}
		
		// print results:
		// keep track of which company we're on
		companiesCounter = 0; 
		// iterate through companies
		companyItr = companies.iterator();
		while(companyItr.hasNext()) {
			tempCompany = companyItr.next();
			++companiesCounter;
			regionsCounter = 0; // keep track of which region we're on
			System.out.println("Company " + companiesCounter + ":");
			
			// print company's expenses
			System.out.println("Expenses: " + tempCompany.getExpenses());
			
			// iterate through company's regions
			regionItr = tempCompany.regions.iterator();
			while(regionItr.hasNext()) {
				tempRegion1 = regionItr.next();
				++regionsCounter;
				System.out.println("Region " + regionsCounter + " -");
				
				// print region's electricity and demand
				System.out.println("Electricity: " + tempRegion1.getElectricity());
				System.out.println("Demand: " + tempRegion1.getDemand());
			}
		}
	}

}
