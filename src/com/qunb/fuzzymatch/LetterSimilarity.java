package com.qunb.fuzzymatch;

import java.util.ArrayList;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;



public class LetterSimilarity {
	@Test
	public  void Test(){
		String input = "seconds";
		String result = "snd";
		boolean output = isSimilarEnough(input, result, CouplingLevel.LOW);
		System.out.println(output);
	}

	public static boolean isSimilarEnough(String original,String identified, CouplingLevel cp){
		
		//we don't care about capital letters
		original = original.toLowerCase();
		identified = identified.toLowerCase();

		int distance = StringUtils.getLevenshteinDistance(original,identified);

		double originalLength = original.length();
		double normalizedDistance = ((originalLength - distance) / originalLength);

		//We need to correct some coupling level if original length too short (we downgrade expectation)
		while(originalLength<cp.getSignificativeLength())
			cp = downgradeCouplingLevel(cp);
		if(original.contains(identified)&&identified.length()>1)
			return true;
		else {
			//repechage
			if(normalizedDistance >= cp.getCouplingrate()&&original.subSequence(0, 1).equals(identified.subSequence(0, 1))){
				return true;
			}
			else
				return false;

		}
	}

	private static CouplingLevel downgradeCouplingLevel(CouplingLevel cp) {
		int order = cp.getOrder();
		if(order>1)
			return CouplingLevel.returnByOrder(order-1);
		else
			return CouplingLevel.LOWEST;
	}

	/**
	 * We define here thresholds; ie % distance from which we consider matching as correct 
	 */
	public enum CouplingLevel{
		LOWEST		(1,	0.20,	1),			//correct if 20% of letters are similar
		LOW			(2,	0.50,	2),
		MODERATE	(3,	0.75,	4),
		HIGH		(4,	0.90,	10),			
		HIGHEST		(5,	0.95,	20),		//correct if 95% of letters are correct
		EXACT		(6,	1.00,	0);			//correct only if all letters similar

		private int order;
		private double couplingRate ;
		private int significativeLength; //minimum length to be valid

		public double getCouplingrate(){
			return couplingRate;}
		public int getSignificativeLength() {
			return significativeLength;}
		public int getOrder(){
			return order;}

		public static CouplingLevel returnByOrder(int i) {
			switch(i){
			case 1 : return LOWEST;
			case 2 : return LOW;
			case 3 : return MODERATE;
			case 4 : return HIGH;
			case 5 : return HIGHEST;
			case 6 : return EXACT;
			default:return null;
			}
		}

		CouplingLevel(int ord, double dbl, int n){
			order = ord;
			couplingRate = dbl;
			significativeLength = n;
		}


	}

}
