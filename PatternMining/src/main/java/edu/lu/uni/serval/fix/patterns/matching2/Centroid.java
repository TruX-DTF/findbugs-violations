package edu.lu.uni.serval.fix.patterns.matching2;

import java.util.List;

public class Centroid {

	public Double[] computerCentroid(List<Double[]> features) {
		int length = 300;
		Double[] centroid = new Double[length];
		int size = features.size();
		Double[] sum = new Double[length];
		for (int i = 0; i < length; i ++) {
			sum[i] = 0d;
		}
		
		for (Double[] feature : features) {
			for (int i = 0; i < length; i ++) {
				sum[i] += feature[i];
			}
		}
		
		for (int i = 0; i < length; i ++) {
			centroid[i] = sum[i] / size;
		}
		return centroid;
	}
}
