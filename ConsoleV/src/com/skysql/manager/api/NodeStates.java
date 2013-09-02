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

package com.skysql.manager.api;

import java.lang.reflect.Type;
import java.util.LinkedHashMap;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.skysql.manager.ui.ErrorDialog;

public class NodeStates {

	private static NodeStates nodeStates;
	private static LinkedHashMap<String, String> nodeStatesIcons;
	private static LinkedHashMap<String, String> nodeStatesDescriptions;

	public static String getNodeIcon(String state) {
		GetNodeStates();
		String icon = nodeStatesIcons.get(state);
		return (icon == null ? "invalid" : icon);
	}

	public static String getDescription(String state) {
		GetNodeStates();
		String description = nodeStatesDescriptions.get(state);
		return (description == null ? "Invalid" : description);
	}

	public static boolean load() {
		GetNodeStates();
		return (nodeStates != null);
	}

	private synchronized static void GetNodeStates() {
		if (nodeStates == null) {
			APIrestful api = new APIrestful();
			if (api.get("nodestate")) {
				try {
					nodeStates = APIrestful.getGson().fromJson(api.getResult(), NodeStates.class);
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

	protected void setNodeStatesIcons(LinkedHashMap<String, String> pairs) {
		NodeStates.nodeStatesIcons = pairs;
	}

	protected void setNodeStatesDescriptions(LinkedHashMap<String, String> pairs) {
		NodeStates.nodeStatesDescriptions = pairs;
	}
}

class NodeStatesDeserializer implements JsonDeserializer<NodeStates> {
	public NodeStates deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException, NullPointerException {
		NodeStates nodeStates = new NodeStates();

		JsonElement jsonElement = json.getAsJsonObject().get("nodestates");
		if (jsonElement.isJsonNull()) {
			nodeStates.setNodeStatesIcons(null);
			nodeStates.setNodeStatesDescriptions(null);
		} else {
			JsonArray array = jsonElement.getAsJsonArray();
			int length = array.size();

			LinkedHashMap<String, String> icons = new LinkedHashMap<String, String>(length);
			LinkedHashMap<String, String> descriptions = new LinkedHashMap<String, String>(length);
			for (int i = 0; i < length; i++) {
				JsonObject pair = array.get(i).getAsJsonObject();
				icons.put(pair.get("state").getAsString(), pair.get("icon").getAsString());
				descriptions.put(pair.get("state").getAsString(), pair.get("description").getAsString());
			}
			nodeStates.setNodeStatesIcons(icons);
			nodeStates.setNodeStatesDescriptions(descriptions);
		}

		return nodeStates;
	}

}