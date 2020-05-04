package com.hyperion.selfcontrol.views.credentials;

import com.hyperion.selfcontrol.backend.CredentialService;
import com.hyperion.selfcontrol.backend.Credentials;
import com.hyperion.selfcontrol.backend.Utils;
import com.hyperion.selfcontrol.views.main.MainView;
import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.Pre;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.router.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.EnumSet;
import java.util.Optional;
import java.util.Random;

@Route(value = "credentials", layout = MainView.class)
@RouteAlias(value = "", layout = MainView.class)
@PageTitle("Credentials")
@CssImport(value = "styles/views/credentials/credentials-view.css", include = "lumo-badge")
@JsModule("@vaadin/vaadin-lumo-styles/badge.js")
public class CredentialsView extends Div implements AfterNavigationObserver {

    private static final Logger log = LoggerFactory.getLogger(CredentialsView.class);

    private CredentialService credentialService;

    private Grid<Credentials> credentials;

    private TextField username = new TextField();
    private TextField password = new TextField();
    private TextField tag = new TextField();
    private Button saveCredentials = new Button("Save");

    private TextField delay = new TextField();
    private Button saveDelay = new Button("Save");

    private Binder<Credentials> binder;

    @Autowired
    public CredentialsView(CredentialService credentialService) {
        this.credentialService = credentialService;
        setId("master-detail-view");
        // Configure Grid
        credentials = new Grid<>();
        credentials.addThemeVariants(GridVariant.LUMO_NO_BORDER);
        credentials.setHeightFull();
        credentials.addColumn(Credentials::getUsername).setHeader("Username");
        credentials.addColumn(credentials -> {
            int length = credentials.getPassword().length();
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < length; i++) {
                builder.append("*");
            }
            return builder.toString();
        }).setHeader("Password");
        credentials.addColumn(Credentials::getTag).setHeader("Tag");

        //when a row is selected or deselected, populate form
        credentials.asSingleSelect().addValueChangeListener(event -> populateForm(event.getValue()));

        // Configure Form
        binder = new Binder<>(Credentials.class);

        // Bind fields. This where you'd define e.g. validation rules
        binder.bindInstanceFields(this);

        // the grid valueChangeEvent will clear the form too
        saveCredentials.addClickListener(e -> {
            Credentials credentials = new Credentials(password.getValue(), username.getValue(), tag.getValue());
            this.credentials.asSingleSelect().clear();
            credentialService.setCredentials(credentials);
            this.credentials.setItems(credentialService.getCredentials());
        });
        saveCredentials.setEnabled(credentialService.isEnabled());

        saveDelay.addClickListener(e -> {
            long delayMs = Long.parseLong(delay.getValue());
            credentialService.setDelay(delayMs);
        });
        delay.setValue("" + credentialService.getDelay());

        SplitLayout splitLayout = new SplitLayout();
        splitLayout.setSizeFull();

        createGridLayout(splitLayout);
        createEditorLayout(splitLayout);

        add(splitLayout);
    }

    private void createEditorLayout(SplitLayout splitLayout) {
        Div editorDiv = new Div();
        editorDiv.setId("editor-layout");
        FormLayout formLayout = new FormLayout();
        addFormItem(editorDiv, formLayout, username, "Username");
        addFormItem(editorDiv, formLayout, password, "Password");
        addFormItem(editorDiv, formLayout, tag, "Tag");
        createButtonLayout(editorDiv);

        // insert a break between form sections
        Hr hr = new Hr();
        editorDiv.add(hr);

        FormLayout delayLayout = new FormLayout();
        addFormItem(editorDiv, delayLayout, delay, "Delay (ms)");
        createDelayButtonLayout(editorDiv);

        VerticalLayout commonValuesLayout = new VerticalLayout();
        commonValuesLayout.setWidthFull();
        commonValuesLayout.setPadding(false);
        Pre pre = new Pre("Common delay values (ms)\n" +
                "30 minutes:     1800000\n" +
                "1 hour:         3600000\n" +
                "2 hours:        7200000\n" +
                "3 hours:        10800000\n" +
                "5 hours:        18000000\n" +
                "10 hours:       36000000");
        pre.getStyle().set("margin-left", "auto");
        commonValuesLayout.add(pre);
        editorDiv.add(commonValuesLayout);

        // insert a break between form sections
        Hr hr2 = new Hr();
        editorDiv.add(hr2);
        createPasswordGenerationLayout(editorDiv);

        Hr hr3 = new Hr();
        editorDiv.add(hr3);
        createWeekendHallPassLayout(editorDiv);

        splitLayout.addToSecondary(editorDiv);
    }

    private void createButtonLayout(Div editorDiv) {
        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.setClassName("button-layout");
        buttonLayout.setWidthFull();
        buttonLayout.setSpacing(true);
        saveCredentials.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        buttonLayout.add(saveCredentials);
        editorDiv.add(buttonLayout);
    }

    private void createDelayButtonLayout(Div editorDiv) {
        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.setClassName("button-layout");
        buttonLayout.setWidthFull();
        buttonLayout.setSpacing(true);
        saveDelay.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        buttonLayout.add(saveDelay);
        editorDiv.add(buttonLayout);
    }

    private void createPasswordGenerationLayout(Div editorDiv) {
        VerticalLayout buttonLayout = new VerticalLayout();
        buttonLayout.setClassName("button-layout");
        buttonLayout.setWidthFull();
        buttonLayout.setSpacing(true);
        buttonLayout.getStyle().set("padding-right", "0");
        Button generate = new Button("Change Admin Password");
        generate.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        Label passwordLabel = new Label("Operation status will show here");
        passwordLabel.getStyle().set("text-align", "right");
        generate.addClickListener(buttonClickEvent -> {
            String password = generatePassword();
            int status = Utils.changePassword(password);
            if (status == 0) {
                credentials.getDataProvider().fetch(new Query<>())
                        .filter(credentials -> credentials.getTag().contains("local"))
                        .findFirst()
                        .ifPresent(item -> {
                            item.setPassword(password);
                            credentialService.setCredentials(item);
                            passwordLabel.setText("Password changed successfully");
                        });
            } else {
                passwordLabel.setText("Check logs for error");
            }
        });
        buttonLayout.add(generate);
        buttonLayout.add(passwordLabel);
        editorDiv.add(buttonLayout);
    }

    private void createWeekendHallPassLayout(Div editorDiv) {
        VerticalLayout buttonLayout = new VerticalLayout();
        buttonLayout.setClassName("button-layout");
        buttonLayout.setWidthFull();
        buttonLayout.setSpacing(true);
        buttonLayout.getStyle().set("padding-right", "0");
        Button generate = new Button("Activate Weekend Hall Pass");
        generate.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        LocalDateTime now = LocalDateTime.now(ZoneId.systemDefault());
        boolean isWeekend = EnumSet.of(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)
                .contains(now.getDayOfWeek());

        LocalDateTime fivePMOnFriday = LocalDateTime.of(now.getYear(), now.getMonth(), now.getDayOfMonth(), 17, 0);
        boolean afterFiveOnFriday = EnumSet.of(DayOfWeek.FRIDAY).contains(now.getDayOfWeek()) && now.isAfter(fivePMOnFriday);

        if (!credentialService.isHallPassUsed() && (isWeekend || afterFiveOnFriday)) {
            generate.setEnabled(true);
        } else {
            generate.setEnabled(false);
        }

        Label statusLabel = new Label("Operation status will show here");
        statusLabel.getStyle().set("text-align", "right");
        generate.addClickListener(buttonClickEvent -> {
            int status = Utils.changePassword(CredentialService.STOCK_PASSWORD);
            if (status == 0) {
                credentials.getDataProvider().fetch(new Query<>())
                        .filter(credentials -> credentials.getTag().contains("local"))
                        .findFirst()
                        .ifPresent(item -> {
                            item.setPassword(CredentialService.STOCK_PASSWORD);
                            credentialService.setCredentials(item);
                            statusLabel.setText("Password changed to stock value of " + CredentialService.STOCK_PASSWORD);
                            credentialService.setHallPassUsed();
                        });
            } else {
                statusLabel.setText("Check logs for error");
            }
        });
        buttonLayout.add(generate);
        buttonLayout.add(statusLabel);
        editorDiv.add(buttonLayout);
    }

    private void createGridLayout(SplitLayout splitLayout) {
        Div wrapper = new Div();
        wrapper.setId("wrapper");
        wrapper.setWidthFull();
        splitLayout.addToPrimary(wrapper);
        wrapper.add(credentials);
    }

    private void addFormItem(Div wrapper, FormLayout formLayout,
                             AbstractField field, String fieldName) {
        formLayout.addFormItem(field, fieldName);
        wrapper.add(formLayout);
        field.getElement().getClassList().add("full-width");
    }

    private String generatePassword() {
        int leftLimit = 33; // numeral '0'
        int rightLimit = 126; // character '~'
        int targetStringLength = 12;
        Random random = new Random();

        return random.ints(leftLimit, rightLimit + 1)
                .limit(targetStringLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }

    @Override
    public void afterNavigation(AfterNavigationEvent event) {

        // Lazy init of the grid items, happens only when we are sure the view will be
        // shown to the user
        if (credentialService.isEnabled()) {
            credentials.setItems(credentialService.getCredentials());
        }
    }

    private void populateForm(Credentials value) {
        // Value can be null as well, that clears the form
        binder.readBean(value);
    }
}

