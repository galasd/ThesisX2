package com.galasd.thesisx.view;

import com.galasd.thesisx.service.GreetService;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLayout;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.PWA;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * A sample Vaadin view class.
 * <p>
 * To implement a Vaadin view just extend any Vaadin component and
 * use @Route annotation to announce it in a URL as a Spring managed
 * bean.
 * Use the @PWA annotation make the application installable on phones,
 * tablets and some desktop browsers.
 * <p>
 * A new instance of this class is created for every new user and every
 * browser tab/window.
 */
@Route(value = "main")
@PWA(name = "Vaadin Application",
        shortName = "Vaadin App",
        description = "This is an example Vaadin application.",
        enableInstallPrompt = true)
@CssImport("./styles/shared-styles.css")
@CssImport(value = "./styles/vaadin-text-field-styles.css", themeFor = "vaadin-text-field")

public class MainView extends VerticalLayout implements RouterLayout {

    /**
     * Construct a new Vaadin view.
     * <p>
     * Build the initial UI state for the user accessing the application.
     *
     */
    @Autowired
    public MainView() {
        setSizeFull();
        VerticalLayout rootLayout = new VerticalLayout();
        rootLayout.setSizeFull();
        VerticalLayout tabsLayout = new VerticalLayout();
        tabsLayout.setSizeFull();
        VerticalLayout welcomeLayout = new VerticalLayout();
        welcomeLayout.setSizeFull();
        welcomeLayout.setAlignItems(Alignment.CENTER);
        rootLayout.add(tabsLayout, welcomeLayout);
        //Tabs
        Tabs tabs = new Tabs();
        Tab nasaTab = new Tab("NASA_API");
        Tab mapboxTab = new Tab("Mapbox_API");
        RouterLink nasaLink = new RouterLink(null, NasaView.class);
        RouterLink mapboxLink = new RouterLink(null, MapboxView.class);
        nasaTab.add(nasaLink);
        mapboxTab.add(mapboxLink);
        tabs.add(nasaTab, mapboxTab);
        tabs.setWidthFull();
        tabsLayout.add(tabs);
        Label welcomeLabel = new Label();
        welcomeLabel.setText("Select a REST API");
        welcomeLabel.getStyle().set("font-size", "48px");
        welcomeLayout.add(welcomeLabel);
        add(rootLayout);
        // Add the copyright line
        HorizontalLayout footLine = new HorizontalLayout();
        footLine.setWidth("100%");
        Label author = new Label("© David Galaš 2019");
        footLine.add(author);
        author.setSizeUndefined();
        footLine.setVerticalComponentAlignment(Alignment.BASELINE, author);
        add(footLine);
    }
}
