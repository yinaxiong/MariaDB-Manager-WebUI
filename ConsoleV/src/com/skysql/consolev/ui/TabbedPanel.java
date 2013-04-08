package com.skysql.consolev.ui;

import java.io.Serializable;

import com.skysql.consolev.api.ClusterComponent;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.SelectedTabChangeEvent;

public class TabbedPanel implements Serializable {
	private static final long serialVersionUID = 0x4C656F6E6172646FL;

	private Component currentTab;
	private TabSheet tabsheet;
	private PanelInfo panelInfo;
	private PanelControl panelControl;
	private PanelBackup panelBackup;
	private PanelTools panelTools;
	private HorizontalLayout backupTab, toolsTab;

	public TabbedPanel() {

		// Set another root layout for the middle panels section.
		tabsheet = new TabSheet();
		tabsheet.setImmediate(true);
		tabsheet.setSizeFull();

		// INFO TAB
		panelInfo = new PanelInfo();
		tabsheet.addTab(panelInfo).setCaption("Info");
		currentTab = panelInfo;

		// CONTROL TAB
		panelControl = new PanelControl();
		tabsheet.addTab(panelControl).setCaption("Control");

		// BACKUP TAB
		backupTab = new HorizontalLayout();
		backupTab.setImmediate(true);
		panelBackup = new PanelBackup(backupTab);
		tabsheet.addTab(backupTab).setCaption("Backup");

		// TOOLS TAB
		toolsTab = new HorizontalLayout();
		toolsTab.setImmediate(true);
		panelTools = new PanelTools(toolsTab);
		tabsheet.addTab(toolsTab).setCaption("Tools");

		// ADD LISTENERS TO TABS
		tabsheet.addSelectedTabChangeListener(new TabSheet.SelectedTabChangeListener() {
			private static final long serialVersionUID = 0x4C656F6E6172646FL;

			public void selectedTabChange(SelectedTabChangeEvent event) {
				final TabSheet source = (TabSheet) event.getSource();
				if (source == tabsheet) {
					Component selectedTab = source.getSelectedTab();
					if (selectedTab != currentTab) {
						currentTab = selectedTab;
						refresh();
					}
				}
			}
		});
	}

	public TabSheet getTabSheet() {
		return this.tabsheet;
	}

	public void refresh() {
		ClusterComponent componentInfo = VaadinSession.getCurrent().getAttribute(ClusterComponent.class);

		tabsheet.getTab(backupTab).setVisible(componentInfo.getType() == ClusterComponent.CCType.system ? false : true);
		tabsheet.getTab(panelControl).setVisible(componentInfo.getType() == ClusterComponent.CCType.system ? false : true);

		if (currentTab == panelInfo) {
			panelInfo.refresh();
		} else if (currentTab == panelControl) {
			panelControl.refresh();
		} else if (currentTab == backupTab) {
			panelBackup.refresh();
		} else if (currentTab == toolsTab) {
			panelTools.refresh();
		}

	}
}
