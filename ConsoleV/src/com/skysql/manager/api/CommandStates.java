/*
 * This file is distributed as part of the MariaDB Manager.  It is free
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
 * Copyright 2012-2014 SkySQL Corporation Ab
 */

package com.skysql.manager.api;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.skysql.manager.ui.ErrorDialog;

/**
 * The Class CommandStates.
 */
public class CommandStates {

	/**
	 * Possible command states as per the API; some of these may be obsolete already or not yet implemented.
	 */
	public enum States {
		running, paused, stopped, done, error, cancelled, missing;
	}

	private static CommandStates commandStates;
	private static LinkedHashMap<String, String> descriptions;

	/**
	 * Gets the descriptions.
	 *
	 * @return the descriptions
	 */
	public static LinkedHashMap<String, String> getDescriptions() {
		GetCommandStates();
		return CommandStates.descriptions;
	}

	/**
	 * Attempts to load the command states and returns true if successful
	 *
	 * @return true, if successful
	 */
	public static boolean load() {
		GetCommandStates();
		return (commandStates != null);
	}

	/**
	 * Gets the command states from the API.
	 */
	private static void GetCommandStates() {
		if (commandStates == null) {
			APIrestful api = new APIrestful();
			if (api.get("command/state")) {
				try {
					commandStates = APIrestful.getGson().fromJson(api.getResult(), CommandStates.class);
					// TODO: verify states against enum and throw error or log warning depending on discrepancy (missing state: error, new unknown state: warning)

				} catch (NullPointerException e) {
					new ErrorDialog(e, "API did not return expected result for:" + api.errorString());
					throw new RuntimeException("API response");
				} catch (JsonParseException e) {
					new ErrorDialog(e, "JSON parse error in API results for:" + api.errorString());
					throw new RuntimeException("API response");
				}
			}
		}
	}

	/**
	 * Sets the descriptions.
	 *
	 * @param pairs the pairs
	 */
	protected void setDescriptions(LinkedHashMap<String, String> pairs) {
		CommandStates.descriptions = pairs;
	}

}

// {"commandStates":[{"state":"running","description":"Running"},{"state":"paused","description":"Paused"},{"state":"stopped","description":"Stopped"},{"state":"done","description":"Done"},{"state":"error","description":"Error"}]}

class CommandStatesDeserializer implements JsonDeserializer<CommandStates> {
	public CommandStates deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException, NullPointerException {
		CommandStates commandStates = new CommandStates();

		JsonElement jsonElement = json.getAsJsonObject().get("commandStates");
		if (jsonElement.isJsonNull()) {
			commandStates.setDescriptions(null);
		} else {
			JsonArray array = jsonElement.getAsJsonArray();
			int length = array.size();

			LinkedHashMap<String, String> descriptions = new LinkedHashMap<String, String>(length);
			List<CommandStates.States> validStates = Arrays.asList(CommandStates.States.values());
			for (int i = 0; i < length; i++) {
				JsonObject pair = array.get(i).getAsJsonObject();
				String state = pair.get("state").getAsString();
				try {
					if (validStates.contains(CommandStates.States.valueOf(state))) {
						descriptions.put(state, pair.get("description").getAsString());
					}
				} catch (IllegalArgumentException e) {
					new ErrorDialog(e, "Unknown Command State (" + state + ") found in API response");
					throw new RuntimeException("Unknown Command State (" + state + ") found in API response");
				}
			}
			commandStates.setDescriptions(descriptions);
		}

		return commandStates;
	}
}
