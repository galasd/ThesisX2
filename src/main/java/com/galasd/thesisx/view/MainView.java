package com.galasd.thesisx.view;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLayout;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.PWA;
import com.vaadin.flow.server.VaadinServlet;
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
@Route("")
@PWA(name = "Vaadin Application",
        shortName = "Vaadin App",
        startPath = "login",
        description = "This is an example Vaadin application.")
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
        Tab nasaTab = new Tab(new RouterLink("Nasa_API", NasaView.class));
        Tab mapboxTab = new Tab(new RouterLink("Mapbox_API", MapboxView.class));
        final String contextPath = VaadinServlet.getCurrent().getServletContext().getContextPath();
        Tab logoutTab = new Tab(createLogoutLink(contextPath));
        tabs.add(nasaTab, mapboxTab, logoutTab);
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
        Label author = new Label("© David Galaš 2020");
        footLine.add(author);
        author.setSizeUndefined();
        footLine.setVerticalComponentAlignment(Alignment.BASELINE, author);
        add(footLine);
    }

    private static Anchor createLogoutLink(String contextPath) {
        final Anchor a = populateLink(new Anchor(),"Logout");
        a.setHref(contextPath + "/logout");
        return a;
    }

    private static <T extends HasComponents> T populateLink(T a, String title) {
        a.add(title);
        return a;
    }
}
