package com.galasd.thesisx.view;

import com.galasd.thesisx.service.ApiData;
import com.galasd.thesisx.service.CapitalsService;
import com.galasd.thesisx.service.MapboxDataProvider;
import com.galasd.thesisx.service.MapboxEntity;
import com.jayway.jsonpath.JsonPath;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLayout;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * View for Mapbox API
 */
@Route(value = "mapbox")
public class MapboxView extends VerticalLayout implements RouterLayout {

    private MapboxDataProvider<MapboxData> mapboxDataProvider = new MapboxDataProvider();
    private static SessionFactory factory;
    private CapitalsService capitalsService = new CapitalsService();
    private VerticalLayout modalWindow;
    private FormLayout formLayout;
    private ComboBox<String> cities;
    private Button request;
    private Grid<JSONObject> grid;
    MapboxData mapboxData = new MapboxData();

    @Autowired
    public MapboxView() {
        factory = buildSessionFactory();
        setSizeFull();
        VerticalLayout formAreaLayout = new VerticalLayout();
        formAreaLayout.setWidthFull();
        // Title layout
        VerticalLayout titleLayout = new VerticalLayout();
        Label apiTitle = new Label();
        apiTitle.setText("Mapbox - Geocoding");
        apiTitle.getStyle().set("font-size", "24px");
        titleLayout.add(apiTitle);
        titleLayout.setAlignItems(Alignment.CENTER);
        // Layout for form and buttons
        formLayout = new FormLayout();
        formLayout.setWidthFull();
        // Create form
        createForm();
        formAreaLayout.add(titleLayout, formLayout);
        // Layout for grid
        VerticalLayout gridAreaLayout = new VerticalLayout();
        gridAreaLayout.setSizeFull();
        //formAreaLayout.setVerticalComponentAlignment(Alignment.END, buttonsLayout);
        //formAreaLayout.setVerticalComponentAlignment(Alignment.START, titleLayout);
        // Binder for form
        final Binder<MapboxData> dataBinder = new Binder<>(MapboxData.class);
        dataBinder.bind(cities, MapboxData::getSearchText, MapboxData::setSearchText);
        cities.addValueChangeListener(valueChangeEvent -> {
            mapboxData.setSearchText(cities.getValue());
            dataBinder.setBean(mapboxData);
        });
        // Grid
        createGrid();
        gridAreaLayout.add(grid);
        //todo exports
        add(formAreaLayout, gridAreaLayout);
    }

    // Create a form to send queries
    private void createForm() {
        // Form elements
        cities = new ComboBox<>();
        cities.setLabel("Select a city");
        cities.setItems(capitalsService.getCities());
        request = new Button("Send request", VaadinIcon.SERVER.create());
        //saveAs = new com.vaadin.ui.Button("Save as", new ThemeResource("icons/save-as.png"));
        //export = new com.vaadin.ui.Button("Export", new ThemeResource("icons/excel.png"));
        request.setSizeUndefined();
        formLayout.add(cities, request);
        //saveAs.setSizeUndefined();
        //export.setSizeUndefined();
    }

    // Create a grid to show query results
    private void createGrid() {
        grid = new Grid<>(JSONObject.class);
        grid.setSizeFull();
        grid.setDataProvider(mapboxDataProvider);
        // Add a column to grid
        request.addClickListener(event -> {
            try {
                if (mapboxData.getSearchText() == null) {
                    showErrorWindow();
                    //todo modalWindow
                    //ui.add(modalWindow);
                } else {
                    // Show query result
                    mapboxDataProvider.setFilter(mapboxData);
                    mapboxDataProvider.refreshAll();
                    grid.removeAllColumns();
                    mapboxDataProvider.sendQuery(new Query<>());
                    JSONObject result = mapboxDataProvider.getResultData();
                    // Grid rows according to a result
                    grid.addColumn(json -> {
                        try {
                            return getStringAttribute(json, mapboxData.jsonKeys.get(0));
                        } catch (JSONException e1) {
                            throw new RuntimeException();
                        }
                    }).setHeader("Name a country");
                    grid.addColumn(json -> {
                        try {
                            return getNumAttribute(json, mapboxData.jsonKeys.get(1));
                        } catch (JSONException e1) {
                            throw new RuntimeException();
                        }
                    }).setHeader("Latitude");
                    grid.addColumn(json -> {
                        try {
                            return getNumAttribute(json, mapboxData.jsonKeys.get(2));
                        } catch (JSONException e1) {
                            throw new RuntimeException();
                        }
                    }).setHeader("Longitude");
                    // Insert data into database
                    dbInsert(String.valueOf(result.get(mapboxData.jsonKeys.get(0))),
                            Double.valueOf(getNumAttribute(result, mapboxData.jsonKeys.get(1)).toString()),
                            Double.valueOf(getNumAttribute(result, mapboxData.jsonKeys.get(2)).toString()));
                }
            } catch (Exception a) {
                throw new RuntimeException();
            }
        });
    }

    // Insert given data into database
    private void dbInsert(String name, Double latitude, Double longitude) {
        Session session = factory.openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            MapboxEntity mapboxEntity = new MapboxEntity();
            mapboxEntity.setName(name);
            mapboxEntity.setLatitude(latitude);
            mapboxEntity.setLongitude(longitude);
            session.save(mapboxEntity);
            tx.commit();
        } catch (HibernateException e) {
            if (tx != null)
                tx.rollback();
            throw new HibernateException(e);
        } finally {
            session.close();
        }
    }

    // Build a session factory for transactions
    private SessionFactory buildSessionFactory() {
        return new Configuration().configure("hibernate.cfg.xml").buildSessionFactory();
    }

    // Get a given String attribute from JSONObjectu
    private String getStringAttribute(JSONObject json, String path) {
        return JsonPath.read(json.toString(), path);
    }

    // Get a given Number attribute from JSONObjectu
    private Number getNumAttribute(JSONObject json, String path) {
        return JsonPath.read(json.toString(), path);
    }

    // Show modal window
    private void showErrorWindow() {
        VerticalLayout vertLayout = new VerticalLayout();
        Label info = new Label("No city selected.");
        info.setClassName("info-style");
        info.setSizeUndefined();
        vertLayout.add(info);
        vertLayout.setHorizontalComponentAlignment(Alignment.CENTER, info);
        modalWindow = new VerticalLayout(vertLayout);
        modalWindow.setWidth("300px");
        modalWindow.setHeight("200px");
        modalWindow.setClassName("popup-style");
    }

    /**
     * Bean for Mapbox data
     */
    public class MapboxData extends ApiData {
        // Filter parameter
        String searchText;

        // Getter and setter
        public String getSearchText() {
            return searchText;
        }

        void setSearchText(String searchText) {
            this.searchText = searchText;
        }

        MapboxData() {
            super();
            // Keys for json data selection
            jsonKeys.add(0, "place_name");
            jsonKeys.add(1, "center[0]");
            jsonKeys.add(2, "center[1]");
        }
    }

}
