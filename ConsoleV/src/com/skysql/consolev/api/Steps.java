/*
 * This file is distributed as part of the SkySQL Cloud Data Suite.  It is free
 * software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation,
 * version 2.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Copyright 2012-2013 SkySQL Ab
 */

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
import com.skysql.consolev.StepRecord;

public class Steps {

	LinkedHashMap<String, StepRecord> stepsList;

	public LinkedHashMap<String, StepRecord> getStepsList() {
		return stepsList;
	}

	public void setStepsList(LinkedHashMap<String, StepRecord> stepsList) {
		this.stepsList = stepsList;
	}

	public Steps() {

	}

	public Steps(String dummy) {

		String inputLine = null;
		try {
			URL url = new URI("http", "localhost", "/consoleAPI/steps.php", null, null).toURL();
			URLConnection sc = url.openConnection();
			BufferedReader in = new BufferedReader(new InputStreamReader(sc.getInputStream()));
			inputLine = in.readLine();
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Could not get response from API");
		}

		Gson gson = AppData.getGson();
		Steps steps = gson.fromJson(inputLine, Steps.class);
		this.stepsList = steps.stepsList;
		steps = null;
	}

}

class StepsDeserializer implements JsonDeserializer<Steps> {
	public Steps deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {

		Steps steps = new Steps();

		JsonElement jsonElement = json.getAsJsonObject().get("steps");
		if (jsonElement == null || jsonElement.isJsonNull()) {
			steps.setStepsList(null);
		} else {
			JsonArray array = jsonElement.getAsJsonArray();
			int length = array.size();

			LinkedHashMap<String, StepRecord> backupsList = new LinkedHashMap<String, StepRecord>(length);
			for (int i = 0; i < length; i++) {
				JsonObject backupJson = array.get(i).getAsJsonObject();

				JsonElement element;
				String id = (element = backupJson.get("id")).isJsonNull() ? null : element.getAsString();
				String script = (element = backupJson.get("script")).isJsonNull() ? null : element.getAsString();
				String icon = (element = backupJson.get("icon")).isJsonNull() ? null : element.getAsString();
				String description = (element = backupJson.get("description")).isJsonNull() ? null : element.getAsString();
				StepRecord stepRecord = new StepRecord(script, icon, description);
				backupsList.put(id, stepRecord);
			}
			steps.setStepsList(backupsList);
		}
		return steps;

	}

}
