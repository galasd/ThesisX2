package com.galasd.thesisx.view;

import com.galasd.thesisx.service.ApiData;
import com.galasd.thesisx.service.NasaDataProvider;
import com.galasd.thesisx.service.NasaEntity;
import com.jayway.jsonpath.JsonPath;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLayout;
import org.apache.commons.lang3.math.NumberUtils;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;

/**
 * View for NASA API
 */
@Route(value = "nasa")
public class NasaView extends VerticalLayout implements RouterLayout {

    NasaData nasaData = new NasaData();
    private NasaDataProvider<NasaData> nasaDataProvider = new NasaDataProvider();
    private static SessionFactory factory;
    private VerticalLayout modalWindow;
    private FormLayout formLayout;
    private DatePicker dateFrom;
    private Button request;
    private Grid<JSONObject> grid;

    @Autowired
    public NasaView() {
        factory = buildSessionFactory();
        setSizeFull();
        VerticalLayout formAreaLayout = new VerticalLayout();
        formAreaLayout.setWidthFull();
        // Title layout
        VerticalLayout titleLayout = new VerticalLayout();
        Label apiTitle = new Label();
        apiTitle.setText("NASA NEO - Near Earth Objects");
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
        final Binder<NasaData> dataBinder = new Binder<>(NasaData.class);
        dataBinder.bind(dateFrom, NasaData::getDateFrom, NasaData::setDateFrom);
        dataBinder.bind(dateFrom, NasaData::getDateTo, NasaData::setDateTo);
        dateFrom.addValueChangeListener(valueChangeEvent -> {
            nasaData.setDateFrom(dateFrom.getValue());
            nasaData.setDateTo(dateFrom.getValue());
            dataBinder.setBean(nasaData);
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
        dateFrom = new DatePicker();
        dateFrom.setLabel("Select a date");
        dateFrom.setWeekNumbersVisible(false);
        request = new Button("Send request", VaadinIcon.SERVER.create());
        //saveAs = new com.vaadin.ui.Button("Save as", new ThemeResource("icons/save-as.png"));
        //export = new com.vaadin.ui.Button("Export", new ThemeResource("icons/excel.png"));
        request.setSizeUndefined();
        formLayout.add(dateFrom, request);
        //saveAs.setSizeUndefined();
        //export.setSizeUndefined();
    }

    // Create a grid to show query results
    private void createGrid() {
        //todo zapisovani do database pres hibernate NEFUNGUJE!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        grid = new Grid<>(JSONObject.class);
        grid.setDataProvider(nasaDataProvider);
        // Add a column to grid
        request.addClickListener(event -> {
            try {
                if (nasaData.getDateFrom() == null) {
                    showErrorWindow();
                    //todo modal window
                    //ui.add(modalWindow);
                } else {
                    // Show query result
                    nasaDataProvider.setFilter(nasaData);
                    nasaDataProvider.refreshAll();
                    grid.removeAllColumns();
                    nasaDataProvider.sendQuery(new Query<>());
                    JSONArray result = nasaDataProvider.getResultData();
                    // Grid rows according to a result
                    grid.addColumn(json -> {
                        try {
                            return getStringAttribute(json, nasaData.jsonKeys.get(0));
                        } catch (JSONException e1) {
                            throw new RuntimeException();
                        }
                    }).setHeader("Asteroid name");
                    grid.addColumn(json -> {
                        try {
                            return getStringAttribute(json, nasaData.jsonKeys.get(1));
                        } catch (JSONException e1) {
                            throw new RuntimeException();
                        }
                    }).setHeader("Relative velocity (km/s)");
                    grid.addColumn(json -> {
                        try {
                            return getNumAttribute(json, nasaData.jsonKeys.get(2));
                        } catch (JSONException e1) {
                            throw new RuntimeException();
                        }
                    }).setHeader("Minimal diameter (m)");
                    grid.addColumn(json -> {
                        try {
                            return getNumAttribute(json, nasaData.jsonKeys.get(3));
                        } catch (JSONException e1) {
                            throw new RuntimeException();
                        }
                    }).setHeader("Maximal diameter (m)");
                    grid.addColumn(json -> {
                        try {
                            return getStringAttribute(json, nasaData.jsonKeys.get(4));
                        } catch (JSONException e1) {
                            throw new RuntimeException();
                        }
                    }).setHeader("Distance from Earth (km)");
                    // Insert data into database
                    grid.setSizeFull();
                    for (int i = 0; i < result.length(); i++) {
                        dbInsert(getStringAttribute(result.getJSONObject(i), nasaData.jsonKeys.get(0)),
                                getStringAttribute(result.getJSONObject(i), nasaData.jsonKeys.get(4)));
                    }
                }
            } catch (Exception a) {
                throw new RuntimeException();
            }
        });
    }

    // Insert given data into database
    private void dbInsert(String name, String distance) {
        Session session = factory.openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            NasaEntity nasaEntity = new NasaEntity();
            nasaEntity.setName(name);
            nasaEntity.setEarthDistance(Math.round(NumberUtils.createFloat(distance)));
            session.save(nasaEntity);
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
    private Double getNumAttribute(JSONObject json, String path) {
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
     * Bean for NASA data
     */
    public class NasaData extends ApiData {

        // Filter parameter
        LocalDate dateTo;

        // Getters and setters
        public LocalDate getDateFrom() {
            return nasaDateFrom;
        }

        void setDateFrom(LocalDate dateFrom) {
            nasaDateFrom = dateFrom;
        }

        public LocalDate getDateTo() {
            return dateTo;
        }

        void setDateTo(LocalDate dateTo) {
            this.dateTo = dateTo;
        }

        NasaData() {
            super();
            // Keys for json data selection
            jsonKeys.add(0, "name");
            jsonKeys.add(1, "close_approach_data[0].relative_velocity.kilometers_per_second");
            jsonKeys.add(2, "estimated_diameter.meters.estimated_diameter_min");
            jsonKeys.add(3, "estimated_diameter.meters.estimated_diameter_max");
            jsonKeys.add(4, "close_approach_data[0].miss_distance.kilometers");
        }
    }
}
