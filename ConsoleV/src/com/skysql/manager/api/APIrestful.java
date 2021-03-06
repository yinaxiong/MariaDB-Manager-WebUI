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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.MalformedJsonException;
import com.skysql.manager.AppData.Debug;
import com.skysql.manager.Commands;
import com.skysql.manager.ManagerUI;
import com.skysql.manager.MonitorLatest;
import com.skysql.manager.ui.ErrorDialog;
import com.vaadin.ui.Notification;

/**
 * The Class APIrestful handles communication with the API.
 */
public class APIrestful {

	/** The Constant sdf defines the format that date fields are expected to be received in from the API. */
	private static final SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.ENGLISH);

	public static final String apiVERSION = "1.1";
	private static String apiURI;

	/** The key ID and code for the authentication header */
	private static String keyID, keyCode;

	/** The gson class for parsing the API responses */
	private static Gson gson;

	/** The api class singleton. */
	private static APIrestful api;

	/** The success code. */
	protected boolean success;

	/** The response code. */
	private int responseCode = 200;

	/** The last call to the API. */
	private String lastCall;

	/** The last result from the API. */
	private String result;

	/** The last errors from the API. */
	private String errors;

	/** The API version as returned by the API. */
	private static String version;

	/**
	 * Gets the API uri.
	 *
	 * @return the uri
	 */
	public static String getURI() {
		return apiURI;
	}

	/**
	 * Gets the API key.
	 *
	 * @return the key
	 */
	public static String getKey() {
		return keyCode;
	}

	/**
	 * Creates or obtains the instance of the api singleton.
	 *
	 * @param URI the uri
	 * @param keys the keys ID and code table
	 * @return the api instance
	 */
	public static APIrestful newInstance(String URI, String id, String key) {
		if (api == null) {
			api = new APIrestful();
			apiURI = URI;
			keyID = id;
			keyCode = key;
			api.get("apidate");
			String apiDate = api.result.substring(api.result.indexOf(":") + 2);
			apiDate = apiDate.substring(0, apiDate.indexOf("\""));
			version += " (" + apiDate + ")";
		}

		return api;
	}

	/**
	 * Gets the gson class.
	 *
	 * @return the gson
	 */
	public static Gson getGson() {
		if (gson == null) {
			GsonBuilder gsonBuilder = new GsonBuilder();
			gsonBuilder.registerTypeAdapter(APIrestful.class, new APIrestfulDeserializer());
			gsonBuilder.registerTypeAdapter(Backups.class, new BackupsDeserializer());
			gsonBuilder.registerTypeAdapter(BackupStates.class, new BackupStatesDeserializer());
			gsonBuilder.registerTypeAdapter(Commands.class, new CommandsDeserializer());
			gsonBuilder.registerTypeAdapter(CommandStates.class, new CommandStatesDeserializer());
			gsonBuilder.registerTypeAdapter(Monitors.class, new MonitorsDeserializer());
			gsonBuilder.registerTypeAdapter(MonitorData.class, new MonitorDataDeserializer());
			gsonBuilder.registerTypeAdapter(MonitorDataRaw.class, new MonitorDataRawDeserializer());
			gsonBuilder.registerTypeAdapter(MonitorLatest.class, new ObjectDeserializer());
			gsonBuilder.registerTypeAdapter(NodeInfo.class, new NodeInfoDeserializer());
			gsonBuilder.registerTypeAdapter(NodeStates.class, new NodeStatesDeserializer());
			gsonBuilder.registerTypeAdapter(Schedule.class, new ScheduleDeserializer());
			gsonBuilder.registerTypeAdapter(SettingsValues.class, new SettingsValuesDeserializer());
			gsonBuilder.registerTypeAdapter(Steps.class, new StepsDeserializer());
			gsonBuilder.registerTypeAdapter(SystemInfo.class, new SystemInfoDeserializer());
			gsonBuilder.registerTypeAdapter(SystemTypes.class, new SystemTypesDeserializer());
			gsonBuilder.registerTypeAdapter(TaskInfo.class, new TaskInfoDeserializer());
			gsonBuilder.registerTypeAdapter(UserInfo.class, new UserInfoDeserializer());
			gsonBuilder.registerTypeAdapter(UserObject.class, new UserObjectDeserializer());
			gsonBuilder.registerTypeAdapter(Versions.class, new VersionsDeserializer());
			gsonBuilder.registerTypeAdapter(WriteResponse.class, new ResponseDeserializer());

			//
			gsonBuilder.registerTypeAdapter(ChartProperties.class, new ChartPropertiesDeserializer());

			gson = gsonBuilder.create();
		}
		return gson;
	}

	/**
	 * The Enum CallType.
	 */
	protected enum CallType {
		GET, PUT, POST, DELETE;
	}

	/**
	 * Checks if is successful.
	 *
	 * @return true, if is success
	 */
	public boolean isSuccess() {
		return success;
	}

	/**
	 * Gets the result.
	 *
	 * @return the result
	 */
	public String getResult() {
		return result;
	}

	/**
	 * Gets the errors.
	 *
	 * @return the errors
	 */
	public String getErrors() {
		return errors;
	}

	/**
	 * Gets the call.
	 *
	 * @return the call
	 */
	public String getCall() {
		return lastCall;
	}

	/**
	 * Gets the version.
	 *
	 * @return the version
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * Sets the success.
	 *
	 * @param success the new success
	 */
	protected void setSuccess(boolean success) {
		this.success = success;
	}

	/**
	 * Sets the result.
	 *
	 * @param result the new result
	 */
	protected void setResult(String result) {
		this.result = result;
	}

	/**
	 * Sets the errors.
	 *
	 * @param errors the new errors
	 */
	protected void setErrors(String errors) {
		this.errors = errors;
	}

	/**
	 * Error string.
	 *
	 * @return the string
	 */
	public String errorString() {
		return "<p class=\"api-response\">" + getCall() + "</p>" + "HTTP " + responseCode + " - "
				+ (getResult() != null ? "Response: <p class=\"api-response\">" + getResult() + "</p>" : "")
				+ (getErrors() != null ? "Errors: <p class=\"api-response\">" + getErrors() + "</p>" : "");
	}

	/**
	 * Convenience call for <code>get(uri, null, null)</code>.
	 * No parameters will be passed to the API for this
	 * GET request, and no If-Modified-Since header will
	 * be set.
	 * @param uri			the API uri request
	 * @return true, if successful
	 */
	public boolean get(String uri) {

		return get(uri, null);

	}

	/**
	 * Convenience call for <code>get(uri, value, null)</code>,
	 * i.e. no If-Modified-Since header will be set.
	 * @param uri			the API uri request
	 * @param value			the parameters string
	 * @return true, if successful
	 */
	public boolean get(String uri, String value) {

		return get(uri, value, null);

	}

	/**
	 * Issues a GET request to the API, sending some parameter.
	 * The parameters must be given in the form
	 * <code>field1=value1&field2=value2</code>, or it can be <code>null</code>.
	 * A date can also be provided, to set the If-Modified-Since header
	 * in the rfc-2822 format. If it is null or empty, the header will
	 * not be set.
	 * @param uri				the API uri request
	 * @param value				the parameters string
	 * @param lastModified		the date of last modified, in rfc-2822 format
	 * @return
	 */
	public boolean get(String uri, String value, String lastModified) {
		return call(uri, CallType.GET, value, lastModified);
	}

	/**
	 * Performs a PUT
	 *
	 * @param uri the uri
	 * @param value the value
	 * @return true, if successful
	 */
	public boolean put(String uri, String value) {

		return call(uri, CallType.PUT, value);

	}

	/**
	 * Performs a POST
	 *
	 * @param uri the uri
	 * @param value the value
	 * @return true, if successful
	 */
	public boolean post(String uri, String value) {

		return call(uri, CallType.POST, value);

	}

	/**
	 * Performs a DELETE.
	 *
	 * @param uri the uri
	 * @return true, if successful
	 */
	public boolean delete(String uri) {

		return call(uri, CallType.DELETE, null);

	}

	/**
	 * Convenience call for <code>call(uri, type, value, null)</code>.
	 * It does not set the if-Modified-Since header.
	 * Avoid calling this method for a GET.
	 * @param uri			the API uri request
	 * @param type			the request type (GET, POST, PUT, DELETE)
	 * @param value			the parameters string
	 * @return true, if successful
	 */
	private boolean call(String uri, CallType type, String value) {
		return call(uri, type, value, null);
	}

	private boolean call(String uri, CallType type, String value, String lastModified) {

		HttpURLConnection httpConnection = null;
		URL url = null;
		long startTime = System.nanoTime();

		try {
			url = new URL(apiURI + "/" + uri + ((type == CallType.GET && value != null) ? value : ""));
			lastCall = type + " " + url.toString() + (type != CallType.GET && value != null ? " parameters: " + value : "");
			ManagerUI.log("API " + lastCall);
			URLConnection sc = url.openConnection();
			httpConnection = (HttpURLConnection) sc;
			String date = sdf.format(new Date());

			MessageDigest md = MessageDigest.getInstance("MD5");
			byte[] mdbytes = md.digest((uri + keyCode + date).getBytes("UTF-8"));
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < mdbytes.length; i++) {
				sb.append(Integer.toString((mdbytes[i] & 0xff) + 0x100, 16).substring(1));
			}
			sc.setRequestProperty("Authorization", "api-auth-" + keyID + "-" + sb.toString());
			sc.setRequestProperty("Date", date);
			sc.setRequestProperty("Accept", "application/json");
			sc.setRequestProperty("X-SkySQL-API-Version", apiVERSION);

			OutputStreamWriter out;

			switch (type) {
			case GET:
				//httpConnection.setRequestProperty("Accept-Encoding", "gzip");
				if (lastModified != null && !lastModified.isEmpty()) {
					httpConnection.setRequestProperty("If-Modified-Since", lastModified);
				}
				break;

			case PUT:
				httpConnection.setDoOutput(true);
				httpConnection.setRequestProperty("Content-Type", "application/json");
				httpConnection.setRequestProperty("charset", "utf-8");
				String length = Integer.toString(value.getBytes().length);
				httpConnection.setRequestProperty("Content-Length", "" + length);
				httpConnection.setUseCaches(false);
				httpConnection.setRequestMethod(type.toString());
				out = new OutputStreamWriter(httpConnection.getOutputStream(), "UTF-8");
				out.write(value);
				out.close();
				break;

			case POST:
				httpConnection.setDoOutput(true);
				httpConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
				httpConnection.setRequestProperty("charset", "utf-8");
				httpConnection.setRequestProperty("Content-Length", "" + Integer.toString(value.getBytes().length));
				httpConnection.setUseCaches(false);
				httpConnection.setRequestMethod(type.toString());
				out = new OutputStreamWriter(httpConnection.getOutputStream());
				out.write(value);
				out.close();
				break;

			case DELETE:
				httpConnection.setRequestMethod(type.toString());
				break;
			}

			if (api.version == null) {
				Map<String, List<String>> headers = httpConnection.getHeaderFields();
				List<String> version = headers.get("X-SkySQL-API-Version");
				api.version = version.get(0);
			}

			responseCode = httpConnection.getResponseCode();
			long estimatedTime = (System.nanoTime() - startTime) / 1000000;

			if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED) {
				BufferedReader in = new BufferedReader(new InputStreamReader(sc.getInputStream()));
				result = in.readLine();
				in.close();
				ManagerUI.log("Response Time: " + estimatedTime + "ms, inputStream: " + result);

				APIrestful api = getGson().fromJson(result, APIrestful.class);
				return api.success;

			} else if (responseCode == HttpURLConnection.HTTP_NOT_MODIFIED) {
				ManagerUI.log("If-Modified-Since worked!");
				APIrestful api = getGson().fromJson("", APIrestful.class);
				return true;
			} else {
				BufferedReader in = new BufferedReader(new InputStreamReader(httpConnection.getErrorStream()));
				errors = in.readLine();
				in.close();

				String msg = "Response Time: " + estimatedTime + "ms, responseCode: " + responseCode + ", errorStream: " + errors;
				if (Debug.ON) {
					ManagerUI.log(msg);
				} else {
					ManagerUI.error("API " + lastCall);
					ManagerUI.error(msg);
				}

				APIrestful api = getGson().fromJson(errors, APIrestful.class);
				errors = "API error: HTTP " + responseCode + " - " + api.getErrors();

				switch (responseCode) {
				case HttpURLConnection.HTTP_INTERNAL_ERROR:
				case HttpURLConnection.HTTP_UNAUTHORIZED:
					Notification.show(errors, Notification.Type.ERROR_MESSAGE);
					throw new RuntimeException("API error");

				case HttpURLConnection.HTTP_BAD_REQUEST:
				case HttpURLConnection.HTTP_NOT_FOUND:
				case HttpURLConnection.HTTP_CONFLICT:
					Notification.show(errors, Notification.Type.ERROR_MESSAGE);
					return false;

				default:
					Notification.show(errors, Notification.Type.ERROR_MESSAGE);
					return false;
				}

			}

		} catch (NoSuchAlgorithmException e) {
			new ErrorDialog(e, "Could not use MD5 to encode HTTP request header");
			throw new RuntimeException();
		} catch (MalformedURLException e) {
			new ErrorDialog(e, "Bad URL: " + url.toString());
			throw new RuntimeException();
		} catch (ConnectException e) {
			new ErrorDialog(e, "API not responding at: " + getURI());
			throw new RuntimeException("API not responding");
		} catch (MalformedJsonException e) {
			new ErrorDialog(e, "API did not return JSON for: " + api.errorString());
			throw new RuntimeException("MalformedJson inputStream");
		} catch (IOException e) {
			new ErrorDialog(e, "IOException while calling the API" + errorString());
			throw new RuntimeException("IOException");
		} catch (JsonSyntaxException e) {
			new ErrorDialog(e, "API did not return valid JSON for:" + api.errorString());
			throw new RuntimeException("JsonSyntax inputStream");
		} catch (Exception e) {
			new ErrorDialog(e, "API call: " + url.toString());
			throw new RuntimeException("API error");
		}

	}
}

/**
 * The Class APIrestfulDeserializer reads the JSON returned by the API.
 */
class APIrestfulDeserializer implements JsonDeserializer<APIrestful> {
	public APIrestful deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {

		APIrestful response = new APIrestful();

		if (!json.isJsonObject()) {
			response.setSuccess(false);
			return response;
		}

		JsonObject jsonObject = json.getAsJsonObject();

		if (jsonObject.has("result")) {
			response.setResult(jsonObject.get("result").toString());
			response.setSuccess(true);
		} else if (jsonObject.has("errors")) {
			JsonArray array = jsonObject.get("errors").getAsJsonArray();
			int length = array.size();
			StringBuffer errors = new StringBuffer();
			for (int i = 0; i < length; i++) {
				errors.append(array.get(i).getAsString());
				errors.append(",");
			}
			if (errors.length() > 0) {
				errors.deleteCharAt(errors.length() - 1);
			}
			response.setErrors(errors.toString());
			response.setSuccess(false);
		} else {
			// response does not have either and is valid JSON, so let processing continue
			response.setSuccess(true);
		}

		return response;
	}
}
