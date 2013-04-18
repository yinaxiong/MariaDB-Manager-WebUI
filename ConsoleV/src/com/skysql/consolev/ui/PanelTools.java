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

package com.skysql.consolev.ui;

import java.util.LinkedHashMap;

import com.skysql.consolev.api.ClusterComponent;
import com.skysql.consolev.api.NodeInfo;
import com.skysql.consolev.api.SystemInfo;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.ThemeResource;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Link;
import com.vaadin.ui.VerticalLayout;

public class PanelTools extends HorizontalLayout {
	private static final long serialVersionUID = 0x4C656F6E6172646FL;

	private Link phpLink, monyogLink;
	private String phpUrl;

	PanelTools() {

		// thisTab.setSizeFull();
		// thisTab.setWidth(Sizeable.SIZE_UNDEFINED, 0); // Default
		setHeight("200px");
		setSpacing(true);

		// External Tools Vertical Module
		SystemInfo systemInfo = VaadinSession.getCurrent().getAttribute(SystemInfo.class);
		LinkedHashMap<String, String> properties = systemInfo.getProperties();
		if (properties != null) {
			VerticalLayout externalsLayout = new VerticalLayout();
			externalsLayout.setWidth("150px");
			externalsLayout.addStyleName("externalsLayout");
			externalsLayout.setSpacing(true);

			String EIP = properties.get(SystemInfo.PROPERTY_EIP);
			String MONyog = properties.get(SystemInfo.PROPERTY_MONYOG);
			if (EIP != null && MONyog != null) {
				String url = "http://" + EIP + MONyog;
				monyogLink = new Link("MONyog", new ExternalResource(url));
				monyogLink.setTargetName("_blank");
				monyogLink.setDescription("Open MONyog for the whole system");
				monyogLink.setIcon(new ThemeResource("img/externalLink.png"));
				monyogLink.addStyleName("icon-after-caption");
				externalsLayout.addComponent(monyogLink);
				externalsLayout.setComponentAlignment(monyogLink, Alignment.BOTTOM_CENTER);
			}

			phpUrl = properties.get(SystemInfo.PROPERTY_PHPMYADMIN);
			if (phpUrl != null) {
				phpLink = new Link("phpMyAdmin", null);
				phpLink.setTargetName("_blank");
				phpLink.setDescription("Open phpMyAdmin for the selected node");
				phpLink.setIcon(new ThemeResource("img/externalLink.png"));
				phpLink.addStyleName("icon-after-caption");
				externalsLayout.addComponent(phpLink);
				externalsLayout.setComponentAlignment(phpLink, Alignment.BOTTOM_CENTER);
			}

			addComponent(externalsLayout);
			setComponentAlignment(externalsLayout, Alignment.MIDDLE_CENTER);

		}

		{
			Label spacer = new Label();
			spacer.setWidth("40px");
			addComponent(spacer);
		}

		// Scripting layout placeholder
		VerticalLayout placeholderLayout = new VerticalLayout();
		placeholderLayout.addStyleName("placeholderLayout");
		placeholderLayout.setSizeUndefined();

		Label placeholderLabel = new Label("Links to external tools");
		placeholderLabel.addStyleName("instructions");
		placeholderLayout.addComponent(placeholderLabel);

		addComponent(placeholderLayout);
		setComponentAlignment(placeholderLayout, Alignment.MIDDLE_CENTER);

	}

	public void refresh() {

		ClusterComponent componentInfo = VaadinSession.getCurrent().getAttribute(ClusterComponent.class);

		switch (componentInfo.getType()) {
		case system:
			if (phpLink != null) {
				phpLink.setVisible(false);
			}
			break;

		case node:
			if (phpLink != null) {
				NodeInfo nodeInfo = (NodeInfo) componentInfo;
				String url = "http://" + nodeInfo.getPublicIP() + phpUrl;
				phpLink.setResource(new ExternalResource(url));
				phpLink.setVisible(true);
			}
			break;
		}

	}

}
