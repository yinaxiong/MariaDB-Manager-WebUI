package com.skysql.consolev.api;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

public class MonitorData {

	private String[][] dataPoints;
	  
	public String[][] getDataPoints() {
		return dataPoints;
	}
	public void setDataPoints(String[][] dataPoints) {
		this.dataPoints = dataPoints;
	}

	public boolean equals(Object ob) {
		    if (!(ob instanceof MonitorData)) 
		    	return false;
		    MonitorData other = (MonitorData)ob;
		    if (!java.util.Arrays.deepEquals(dataPoints, other.dataPoints))
		    	return false;
		    
		    return true;
	}

	public MonitorData() {
	}

	public MonitorData(String monitor, String system, String node, String time, String interval, String count) {
		
    	String inputLine = null;
        try {
        	URL url = new URI("http", "localhost", "/consoleAPI/monitorinfo.php", "monitor="+monitor+"&system="+system+"&node="+node+"&time="+time+"&interval="+interval+"&count="+count, null).toURL();
        	URLConnection sc = url.openConnection();
        	BufferedReader in = new BufferedReader(new InputStreamReader(sc.getInputStream()));
        	inputLine = in.readLine();
        	in.close();
        } catch (Exception e) {
        	e.printStackTrace();
        	throw new RuntimeException("Could not get response from API");
        }

		Gson gson = AppData.getGson();
		MonitorData monitorData = gson.fromJson(inputLine, MonitorData.class);
		this.dataPoints = monitorData.dataPoints;
		monitorData = null;
	}
	  
}

class MonitorDataDeserializer implements JsonDeserializer<MonitorData> {
		  public MonitorData deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
		      throws JsonParseException
		 {
			MonitorData monitorData = new MonitorData();
					    
			JsonElement jsonElement = json.getAsJsonObject().get("monitor_data");
			if (jsonElement == null || jsonElement.isJsonNull()) {
				monitorData.setDataPoints(null);
			} else {
		    	JsonArray array = jsonElement.getAsJsonArray();
		    	int length = array.size();
		    	
		    	String[][] points = new String[length][2];
		    	for (int i = 0; i < length; i++) {
		    		JsonObject point = array.get(i).getAsJsonObject();
		    		points[i][0] = point.get("time").getAsString();
		    		points[i][1] = point.get("value").getAsString();		       
		    	}
		    	monitorData.setDataPoints(points);
		    }
		    return monitorData;
		  }
		
}
