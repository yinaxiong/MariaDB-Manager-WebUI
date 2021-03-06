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

package com.skysql.manager.ui;

import com.skysql.manager.ManagerUI;
import com.skysql.manager.MonitorRecord;
import com.skysql.manager.UserChart;
import com.skysql.manager.api.Monitors;
import com.skysql.manager.ui.components.ChartButton;
import com.skysql.manager.ui.components.ChartsLayout;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.NativeSelect;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

/**
 * The Class ChartPreviewLayout.
 */
public class ChartPreviewLayout extends VerticalLayout {

	private static final long serialVersionUID = 0x4C656F6E6172646FL;

	private ChartsLayout chartLayout;
	private UserChart userChart;
	private TextField chartName, chartDescription, chartUnit;
	private NativeSelect chartSelectType, selectCount;
	private boolean blockRefresh = false;
	private String time, interval;

	/**
	 * Instantiates a new chart preview layout.
	 *
	 * @param userChart the user chart
	 * @param time the time
	 * @param interval the interval
	 */
	public ChartPreviewLayout(final UserChart userChart, String time, String interval) {
		this.userChart = userChart;
		this.time = time;
		this.interval = interval;

		addStyleName("ChartPreviewLayout");
		setSpacing(true);
		setMargin(true);

		HorizontalLayout formDescription = new HorizontalLayout();
		formDescription.setSpacing(true);

		Embedded info = new Embedded(null, new ThemeResource("img/info.png"));
		info.addStyleName("infoButton");
		String infoText = "<table border=0 cellspacing=3 cellpadding=0 summary=\"\">\n" + "     <tr bgcolor=\"#ccccff\">" + "         <th align=left>Field"
				+ "         <th align=left>Description" + "     <tr>" + "         <td><code>Title</code>" + "         <td>Name of the Chart"
				+ "     <tr bgcolor=\"#eeeeff\">" + "         <td><code>Description</code>" + "         <td>Description of the Chart " + "     <tr>"
				+ "         <td nowrap><code>Measurement Unit</code>"
				+ "         <td>Unit of measurement for the data returned by the monitor, used as caption for the vertical axis of the chart"
				+ "     <tr bgcolor=\"#eeeeff\">" + "         <td><code>Type</code>" + "         <td>Chart type (LineChart, AreaChart)" + "     <tr>"
				+ "         <td><code>Points</code>" + "         <td>Number of data points displayed";
		infoText += " </table>" + " </blockquote>";
		info.setDescription(infoText);
		formDescription.addComponent(info);

		final Label monitorsLabel = new Label("Display as Chart");
		monitorsLabel.setStyleName("dialogLabel");
		formDescription.addComponent(monitorsLabel);
		formDescription.setComponentAlignment(monitorsLabel, Alignment.MIDDLE_LEFT);

		addComponent(formDescription);
		setComponentAlignment(formDescription, Alignment.TOP_CENTER);

		HorizontalLayout chartInfo = new HorizontalLayout();
		chartInfo.setSpacing(true);
		addComponent(chartInfo);
		setComponentAlignment(chartInfo, Alignment.MIDDLE_CENTER);

		FormLayout formLayout = new FormLayout();
		chartInfo.addComponent(formLayout);

		chartName = new TextField("Title");
		chartName.setImmediate(true);
		formLayout.addComponent(chartName);

		chartDescription = new TextField("Description");
		chartDescription.setWidth("25em");
		chartDescription.setImmediate(true);
		formLayout.addComponent(chartDescription);

		chartUnit = new TextField("Measurement Unit");
		chartUnit.setImmediate(true);
		formLayout.addComponent(chartUnit);

		chartSelectType = new NativeSelect("Type");
		chartSelectType.setImmediate(true);
		for (UserChart.ChartType type : UserChart.ChartType.values()) {
			chartSelectType.addItem(type.name());
		}
		chartSelectType.setNullSelectionAllowed(false);
		formLayout.addComponent(chartSelectType);

		selectCount = new NativeSelect("Points");
		selectCount.setImmediate(true);
		for (int points : UserChart.chartPoints()) {
			selectCount.addItem(points);
			selectCount.setItemCaption(points, String.valueOf(points));
		}
		selectCount.setNullSelectionAllowed(false);
		formLayout.addComponent(selectCount);

		updateChartInfo(userChart.getName(), userChart.getDescription(), userChart.getUnit(), userChart.getType(), userChart.getPoints());

		chartName.addValueChangeListener(new ValueChangeListener() {
			private static final long serialVersionUID = 0x4C656F6E6172646FL;

			public void valueChange(ValueChangeEvent event) {

				String chartName = (String) (event.getProperty()).getValue();
				userChart.setName(chartName);
				refreshChart();

			}
		});

		chartDescription.addValueChangeListener(new ValueChangeListener() {
			private static final long serialVersionUID = 0x4C656F6E6172646FL;

			public void valueChange(ValueChangeEvent event) {

				String value = (String) (event.getProperty()).getValue();
				userChart.setDescription(value);
				refreshChart();

			}
		});

		chartUnit.addValueChangeListener(new ValueChangeListener() {
			private static final long serialVersionUID = 0x4C656F6E6172646FL;

			public void valueChange(ValueChangeEvent event) {

				String value = (String) (event.getProperty()).getValue();
				userChart.setUnit(value);
				refreshChart();

			}
		});

		chartSelectType.addValueChangeListener(new ValueChangeListener() {
			private static final long serialVersionUID = 0x4C656F6E6172646FL;

			public void valueChange(ValueChangeEvent event) {

				String value = (String) (event.getProperty()).getValue();
				userChart.setType(value);
				refreshChart();

			}
		});

		selectCount.addValueChangeListener(new ValueChangeListener() {
			private static final long serialVersionUID = 0x4C656F6E6172646FL;

			public void valueChange(ValueChangeEvent event) {

				int points = (Integer) (event.getProperty()).getValue();
				userChart.setPoints(points);
				refreshChart();

			}
		});

		chartLayout = drawChart();
		addComponent(chartLayout);

	}

	/**
	 * Update chart info.
	 *
	 * @param name the name
	 * @param description the description
	 * @param unit the unit
	 * @param type the type
	 * @param points the points
	 */
	private void updateChartInfo(String name, String description, String unit, String type, int points) {
		blockRefresh = true;

		chartName.setValue(name != null ? name : "");
		userChart.setName(name);

		chartDescription.setValue(description != null ? description : "");
		userChart.setDescription(description);

		chartUnit.setValue(unit != null ? unit : "");
		userChart.setUnit(unit);

		chartSelectType.setValue(UserChart.DEFAULT_CHARTTYPE.name());
		try {
			if (type != null && UserChart.ChartType.valueOf(type) != null) {
				chartSelectType.setValue(type);
			}
		} catch (IllegalArgumentException e) {
			ManagerUI.error("unknow ChartType: " + type);
		}

		selectCount.setValue(points);

		blockRefresh = false;

	}

	/**
	 * Draw chart.
	 *
	 * @return the charts layout
	 */
	private ChartsLayout drawChart() {
		ChartsLayout newChartsLayout = new ChartsLayout(true, time, interval);
		newChartsLayout.addStyleName("chartPreview");
		userChart.clearMonitorData();
		ChartButton newChartButton = new ChartButton(userChart);
		newChartButton.setChartsLayout(newChartsLayout);
		newChartsLayout.addComponent(newChartButton);
		newChartsLayout.refreshCode(newChartButton, null);
		return newChartsLayout;
	}

	/**
	 * Refresh chart.
	 */
	public void refreshChart() {
		if (blockRefresh == false) {
			ChartsLayout newChartLayout = drawChart();
			replaceComponent(chartLayout, newChartLayout);
			chartLayout = newChartLayout;
		}
	}

	/**
	 * Refresh user chart.
	 */
	public void refreshUserChart() {
		if (userChart.getMonitorIDs().size() == 1) {
			// update chart fields from selection of Monitor only when it's the first and only monitor mapped to chart
			String monitorID = userChart.getMonitorIDs().get(0);
			MonitorRecord monitor = Monitors.getMonitor(monitorID);
			updateChartInfo(monitor.getName(), monitor.getDescription(), monitor.getUnit(), monitor.getChartType(), userChart.getPoints());
		}
	}
}
