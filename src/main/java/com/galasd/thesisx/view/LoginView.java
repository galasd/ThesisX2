package com.galasd.thesisx.view;

import com.galasd.thesisx.security.SecurityUtils;
import com.vaadin.flow.component.login.LoginI18n;
import com.vaadin.flow.component.login.LoginOverlay;
import com.vaadin.flow.router.*;
import org.springframework.beans.factory.annotation.Autowired;

@Route
public class LoginView extends LoginOverlay implements AfterNavigationObserver, BeforeEnterObserver {

    @Autowired
    public LoginView() {
        LoginI18n i18n = LoginI18n.createDefault();
        i18n.setHeader(new LoginI18n.Header());
        i18n.getHeader().setTitle("Thesis X app");
        i18n.getHeader().setDescription("admin@vaadin.com + admin common@vaadin.com + common");
        i18n.setAdditionalInformation(null);
        i18n.setForm(new LoginI18n.Form());
        i18n.getForm().setSubmit("Sign in");
        i18n.getForm().setTitle("Sign in");
        i18n.getForm().setUsername("Username");
        i18n.getForm().setPassword("Password");
        setI18n(i18n);
        setForgotPasswordButtonVisible(false);
        setAction("login");
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (SecurityUtils.isUserLoggedIn()) {
            event.forwardTo(MainView.class);
        } else {
            setOpened(true);
        }
    }

    @Override
    public void afterNavigation(AfterNavigationEvent event) {
        setError(event.getLocation().getQueryParameters().getParameters().containsKey("error"));
    }
}


