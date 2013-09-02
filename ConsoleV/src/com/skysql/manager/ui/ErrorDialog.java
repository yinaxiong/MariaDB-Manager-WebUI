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

package com.skysql.manager.ui;

import java.io.PrintWriter;
import java.io.StringWriter;

import com.vaadin.server.ThemeResource;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

public class ErrorDialog {

	Window dialogWindow;

	public ErrorDialog(Exception e, String humanizedError) {

		dialogWindow = new ChartWindow("An Error has occurred");
		UI.getCurrent().addWindow(dialogWindow);

		HorizontalLayout wrapper = new HorizontalLayout();
		wrapper.setWidth("100%");
		wrapper.setMargin(true);

		VerticalLayout iconLayout = new VerticalLayout();
		iconLayout.setWidth("100px");
		wrapper.addComponent(iconLayout);
		Embedded image = new Embedded(null, new ThemeResource("img/error.png"));
		iconLayout.addComponent(image);

		VerticalLayout textLayout = new VerticalLayout();
		textLayout.setSpacing(true);
		wrapper.addComponent(textLayout);
		wrapper.setExpandRatio(textLayout, 1.0f);

		if (humanizedError != null || e != null) {
			String error = (humanizedError != null) ? humanizedError : e.toString();
			System.err.println(error);
			Label label = new Label(error, ContentMode.HTML);
			label.addStyleName("warning");
			textLayout.addComponent(label);
			textLayout.setComponentAlignment(label, Alignment.TOP_CENTER);
		}

		if (e != null) {
			TextArea stackTrace = new TextArea("Error Log");
			stackTrace.setSizeFull();
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			stackTrace.setValue(sw.toString());
			textLayout.addComponent(stackTrace);
			textLayout.setComponentAlignment(stackTrace, Alignment.TOP_LEFT);
			textLayout.setExpandRatio(stackTrace, 1.0f);
		}

		HorizontalLayout buttonsBar = new HorizontalLayout();
		buttonsBar.setStyleName("buttonsBar");
		buttonsBar.setSizeFull();
		buttonsBar.setSpacing(true);
		buttonsBar.setMargin(true);
		buttonsBar.setHeight("49px");

		Label filler = new Label();
		buttonsBar.addComponent(filler);
		buttonsBar.setExpandRatio(filler, 1.0f);

		Button cancelButton = new Button("Close");
		buttonsBar.addComponent(cancelButton);
		buttonsBar.setComponentAlignment(cancelButton, Alignment.MIDDLE_RIGHT);

		cancelButton.addClickListener(new Button.ClickListener() {
			private static final long serialVersionUID = 0x4C656F6E6172646FL;

			public void buttonClick(ClickEvent event) {
				dialogWindow.close();
			}
		});

		Button okButton = new Button("Send Error");
		okButton.setEnabled(false);
		okButton.addClickListener(new Button.ClickListener() {
			private static final long serialVersionUID = 0x4C656F6E6172646FL;

			public void buttonClick(ClickEvent event) {
				dialogWindow.close();
			}
		});
		buttonsBar.addComponent(okButton);
		buttonsBar.setComponentAlignment(okButton, Alignment.MIDDLE_RIGHT);

		VerticalLayout windowLayout = (VerticalLayout) dialogWindow.getContent();
		windowLayout.setSpacing(false);
		windowLayout.setMargin(false);
		windowLayout.addComponent(wrapper);
		windowLayout.addComponent(buttonsBar);

	}
}

class ErrorWindow extends Window {
	private static final long serialVersionUID = 0x4C656F6E6172646FL;

	public ErrorWindow(String caption) {
		setModal(true);
		setWidth("900px");
		center();
		setCaption(caption);
		VerticalLayout layout = new VerticalLayout();
		setContent(layout);
		layout.setSpacing(true);
		layout.setMargin(true);
	}
}