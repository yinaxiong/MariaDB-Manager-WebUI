package com.skysql.consolev.api;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.LinkedHashMap;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.skysql.consolev.BackupRecord;

public class Backups {

	LinkedHashMap<String, BackupRecord> backupsList;

	public LinkedHashMap<String, BackupRecord> getBackupsList() {
		return backupsList;
	}
	
	public void setBackupsList(LinkedHashMap<String, BackupRecord> backupsList) {
		this.backupsList = backupsList;
	}

	public Backups() {
		
	}
	
	public Backups(String system, String date) {
		
    	String inputLine = null;
        try {
        	URL url = new URI("http", "localhost", "/consoleAPI/backups.php", "system=" + system + "&date=" + date, null).toURL();
        	URLConnection sc = url.openConnection();
        	BufferedReader in = new BufferedReader(new InputStreamReader(sc.getInputStream()));
        	inputLine = in.readLine();
        	in.close();
        } catch (Exception e) {
        	e.printStackTrace();
        	throw new RuntimeException("Could not get response from API");
        }

		Gson gson = AppData.getGson();
		Backups backups = gson.fromJson(inputLine, Backups.class);
		this.backupsList = backups.backupsList;
		backups = null;
	}
	
}

class BackupsDeserializer implements JsonDeserializer<Backups> {
	  public Backups deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
	      throws JsonParseException
	  {
		
		Backups backups = new Backups();
		
		JsonElement jsonElement = json.getAsJsonObject().get("backups");
		if (jsonElement == null || jsonElement.isJsonNull()) {
	    	backups.setBackupsList(null);
		} else {
	    	JsonArray array = jsonElement.getAsJsonArray();
	    	int length = array.size();

		    LinkedHashMap<String, BackupRecord> backupsList = new LinkedHashMap<String, BackupRecord>(length);
		    for (int i = 0; i < length; i++) {
		    	JsonObject backupJson = array.get(i).getAsJsonObject();
		    	JsonElement element;
		    	String id = (element = backupJson.get("id")).isJsonNull() ? null : element.getAsString();
		    	String status = (element = backupJson.get("status")).isJsonNull() ? null : element.getAsString();
		    	String started = (element = backupJson.get("started")).isJsonNull() ? null : element.getAsString();
		    	String updated = (element = backupJson.get("updated")).isJsonNull() ? null : element.getAsString();
		    	String level = (element = backupJson.get("level")).isJsonNull() ? null : element.getAsString();
		    	String node = (element = backupJson.get("node")).isJsonNull() ? null : element.getAsString();
		    	String size = (element = backupJson.get("size")).isJsonNull() ? null : element.getAsString();
		    	String storage = (element = backupJson.get("storage")).isJsonNull() ? null : element.getAsString();
		    	String restored = (element = backupJson.get("restored")).isJsonNull() ? null : element.getAsString();
		    	String log = (element = backupJson.get("log")).isJsonNull() ? null : element.getAsString();
		    	BackupRecord backupRecord = new BackupRecord(id, status, started, updated, level, node, size, storage, restored, log);
		    	backupsList.put(id, backupRecord);
		    }
		    backups.setBackupsList(backupsList);
	    }
	    return backups;	    
	    
	  }
	
}

