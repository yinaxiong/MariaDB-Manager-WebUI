package com.skysql.consolev.api;

import java.util.ArrayList;
import java.util.HashMap;

import com.vaadin.ui.Button;

public class UserChart extends ChartMappings {
	private static final long serialVersionUID = 0x4C656F6E6172646FL;

	public static final Integer COUNT_15 = 15;
	public static final Integer COUNT_30 = 30;
	public static final Integer COUNT_45 = 45;
	public static final Integer COUNT_60 = 60;

	public static Integer[] chartPoints() {
		Integer[] array = { COUNT_15, COUNT_30, COUNT_45, COUNT_60 };
		return array;
	}

	public static final String LINECHART = "LineChart";
	public static final String AREACHART = "AreaChart";

	public static String[] chartTypes() {
		String[] array = { LINECHART, AREACHART };
		return array;
	}

	private HashMap<String, Object> monitorsData;
	private Button deleteButton;

	public UserChart(String name, String description, String unit, String type, int points, ArrayList<String> monitorIDs) {
		super(name, description, unit, type, points, monitorIDs);
		this.monitorsData = new HashMap<String, Object>();
	}

	public UserChart(UserChart oldUserChart) {
		super(oldUserChart);
		this.monitorsData = oldUserChart.monitorsData;
		this.deleteButton = oldUserChart.deleteButton;
	}

	public UserChart(ChartMappings chartMappings) {
		super(chartMappings);
		this.monitorsData = new HashMap<String, Object>();
	}

	public Object getMonitorData(String id) {
		return monitorsData.get(id);
	}

	public void setMonitorData(String key, Object value) {
		this.monitorsData.put(key, value);
	}

	public void clearMonitorData() {
		this.monitorsData = new HashMap<String, Object>();
	}

	public Button getDeleteButton() {
		return deleteButton;
	}

	public void setDeleteButton(Button deleteButton) {
		this.deleteButton = deleteButton;
	}

}
