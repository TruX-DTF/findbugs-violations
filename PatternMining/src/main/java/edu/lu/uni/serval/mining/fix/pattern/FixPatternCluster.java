package edu.lu.uni.serval.mining.fix.pattern;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import edu.lu.uni.serval.cluster.Cluster;
import edu.lu.uni.serval.config.Configuration;
import edu.lu.uni.serval.mining.utils.Classifier;
import edu.lu.uni.serval.mining.utils.DataPreparation;
import edu.lu.uni.serval.utils.FileHelper;

/**
 * Clustering of edit scripts with extracted features of edit scripts.
 * 
 * @author kui.liu
 *
 */
public class FixPatternCluster {

	public void cluster() {
		boolean isViolation = false;
		if (isViolation) {
			// classify data by alarm types.
			String featureFileName = "1_CNNoutput.csv";
			new Classifier().classifyDataByAlarmTypes2(featureFileName);
			Map<String, Integer> alarmTypes = new HashMap<>();
			alarmTypes = readAlarmTypes(Configuration.ALARMS + "AlarmTypesAmount.list");
			
			for (Map.Entry<String, Integer> entry : alarmTypes.entrySet()) {
				int amount = entry.getValue();
				if (amount >= 10) {
					Cluster cluster = new Cluster();
					cluster.cluster(amount, Configuration.ALARMS + entry.getKey() + "/", featureFileName);
				} else {
					Cluster cluster = new Cluster();
					cluster.cluster(amount * 2, Configuration.ALARMS + entry.getKey() + "/", featureFileName);
				}
			}
		} else {
			String clusterInput = Configuration.CLUSTER_INPUT;
			FileHelper.deleteFile(clusterInput);
			
			String featureFileName = "20_CNNoutput.csv";
			DataPreparation.prepareDataForClustering(featureFileName);
			
			String clusterOutput = Configuration.CLUSTER_OUTPUT;
			FileHelper.deleteFile(clusterOutput);

			Cluster cluster = new Cluster();
			cluster.cluster();
		}
	}

	public Map<String, Integer> readAlarmTypes(String fileName) {
		Map<String, Integer> alarmTypes = new HashMap<>();
		String content = FileHelper.readFile(fileName);
		BufferedReader reader = null;
		String line = null;
		try {
			reader = new BufferedReader(new StringReader(content));
			while((line = reader.readLine()) != null) {
				String[] alarms = line.split(" : ");
				String alarmType = alarms[0];
				int amount = Integer.parseInt(alarms[1]);
				alarmTypes.put(alarmType, amount);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		return alarmTypes;
	}

}
