package com.hyperion.selfcontrol.views.home;

import com.hyperion.selfcontrol.backend.ConfigService;
import com.hyperion.selfcontrol.backend.Credentials;
import com.hyperion.selfcontrol.backend.JobRunner;
import com.hyperion.selfcontrol.backend.Utils;
import com.hyperion.selfcontrol.backend.config.bedtime.Bedtimes;
import com.hyperion.selfcontrol.backend.config.job.SaveBedtimesJob;
import com.hyperion.selfcontrol.backend.config.job.SetDelayJob;
import com.hyperion.selfcontrol.views.main.MainView;
import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.Component;
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
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.timepicker.TimePicker;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.Setter;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.function.ValueProvider;
import com.vaadin.flow.router.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.*;

import static com.hyperion.selfcontrol.backend.Utils.generatePassword;
import static com.hyperion.selfcontrol.backend.Utils.getCurrentTimePlusDelay;

@Route(value = "home", layout = MainView.class)
@RouteAlias(value = "", layout = MainView.class)
@PageTitle("Home")
@CssImport(value = "styles/views/home/home-view.css", include = "lumo-badge")
@JsModule("@vaadin/vaadin-lumo-styles/badge.js")
public class HomeView extends Div implements AfterNavigationObserver {

    private static final Logger log = LoggerFactory.getLogger(HomeView.class);

    private final ConfigService configService;
    private final JobRunner jobRunner;

    private final Grid<Credentials> credentials;
    private final Binder<Credentials> binder;

    private final TextField username = new TextField();
    private final TextField password = new TextField();
    private final TextField tag = new TextField();
    private final Button saveCredentials = new Button("Save");

    private final TextField delay = new TextField();
    private final Button saveDelay = new Button("Save");

    private final Binder<Bedtimes> bedtimesBinder;

    @Autowired
    public HomeView(ConfigService configService, JobRunner jobRunner) {
        this.configService = configService;
        this.jobRunner = jobRunner;
        setId("master-detail-view");
        // Configure Grid
        credentials = new Grid<>();
        credentials.addThemeVariants(GridVariant.LUMO_NO_BORDER);
        credentials.setHeight("50%");
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
        bedtimesBinder = new Binder<>(Bedtimes.class);

        // Bind fields. This where you'd define e.g. validation rules
        binder.bindInstanceFields(this);

        // the grid valueChangeEvent will clear the form too
        saveCredentials.addClickListener(e -> {
            Credentials credentials = new Credentials(password.getValue(), username.getValue(), tag.getValue());
            configService.setCredentials(credentials);
            this.credentials.setItems(configService.getCredentials());
            this.credentials.asSingleSelect().clear();
            password.clear();
            username.clear();
            tag.clear();
        });
        saveCredentials.setEnabled(configService.isEnabled());

        saveDelay.addClickListener(e -> {
            long delayMillis = Long.parseLong(delay.getValue());
            if (delayMillis > configService.getDelayMillis()) {
                configService.setDelay(delayMillis);
            } else {
                SetDelayJob delayJob = new SetDelayJob(getCurrentTimePlusDelay(configService), "Set new delay of " + delayMillis + "ms", delayMillis);
                jobRunner.queueJob(delayJob);
            }
        });
        delay.setValue("" + configService.getDelayMillis());

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
        Hr hr4 = new Hr();
        editorDiv.add(hr4);
        createRetryLayout(editorDiv);

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
        generate.setWidthFull();
        Label passwordLabel = new Label("Operation status will show here");
        passwordLabel.getStyle().set("text-align", "right");
        generate.addClickListener(buttonClickEvent -> {
            String password = generatePassword();
            int status = Utils.changeLocalAdminPassword(password);
            if (status == 0) {
                credentials.getDataProvider().fetch(new Query<>())
                        .filter(credentials -> credentials.getTag().contains("local"))
                        .findFirst()
                        .ifPresent(item -> {
                            item.setPassword(password);
                            configService.setCredentials(item);
                            passwordLabel.setText("Password changed successfully");
                        });
            } else {
                passwordLabel.setText("Check logs for error");
            }
        });
        if (!configService.isEnabled() && !configService.isHallPassUsed()) {
            generate.setEnabled(false);
        }
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
        Button activate = new Button("Activate Weekend Hall Pass");
        activate.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        activate.setWidthFull();
        LocalDateTime now = LocalDateTime.now(ZoneId.systemDefault());
        boolean isWeekend = EnumSet.of(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)
                .contains(now.getDayOfWeek());

        LocalDateTime fivePMOnFriday = LocalDateTime.of(now.getYear(), now.getMonth(), now.getDayOfMonth(), 17, 0);
        boolean afterFiveOnFriday = EnumSet.of(DayOfWeek.FRIDAY).contains(now.getDayOfWeek()) && now.isAfter(fivePMOnFriday);

        if (!configService.isHallPassUsed() && (isWeekend || afterFiveOnFriday)) {
            activate.setEnabled(true);
        } else {
            activate.setEnabled(false);
        }

        Label statusLabel = new Label("Operation status will show here");
        statusLabel.getStyle().set("text-align", "right");
        activate.addClickListener(buttonClickEvent -> {
            int status = Utils.changeLocalAdminPassword(ConfigService.STOCK_PASSWORD);
            if (status == 0) {
                credentials.getDataProvider().fetch(new Query<>())
                        .filter(credentials -> credentials.getTag().contains("local"))
                        .findFirst()
                        .ifPresent(item -> {
                            item.setPassword(ConfigService.STOCK_PASSWORD);
                            configService.setCredentials(item);
                            statusLabel.setText("Password changed to stock value of " + ConfigService.STOCK_PASSWORD);
                            configService.setHallPassUsed();
                        });
            } else {
                statusLabel.setText("Check logs for error");
            }
        });
        buttonLayout.add(activate);
        buttonLayout.add(statusLabel);
        editorDiv.add(buttonLayout);
    }

    private void createRetryLayout(Div editorDiv) {
        VerticalLayout cancelLayout = new VerticalLayout();
        cancelLayout.setClassName("button-layout");
        cancelLayout.setWidthFull();
        cancelLayout.setSpacing(true);
        cancelLayout.getStyle().set("padding-right", "0");
        Button cancel = new Button("Cancel pending jobs");
        cancel.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        cancel.setWidthFull();
        cancel.addClickListener(buttonClickEvent -> {
            jobRunner.cancelPendingJobs();
        });
        if (!configService.hasCancellablePendingJobs()) {
            cancel.setEnabled(false);
        }
        cancelLayout.add(cancel);
        editorDiv.add(cancelLayout);

        VerticalLayout retryLayout = new VerticalLayout();
        retryLayout.setClassName("button-layout");
        retryLayout.setWidthFull();
        retryLayout.setSpacing(true);
        retryLayout.getStyle().set("padding-right", "0");
        Button retry = new Button("Retry failed jobs");
        retry.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        retry.setWidthFull();
        retry.addClickListener(buttonClickEvent -> {
            jobRunner.retryFailedJobs();
        });
        if (configService.getConfig().getRetryJobs().isEmpty()) {
            retry.setEnabled(false);
        }
        retryLayout.add(retry);
        editorDiv.add(retryLayout);

        VerticalLayout deleteLayout = new VerticalLayout();
        deleteLayout.setClassName("button-layout");
        deleteLayout.setWidthFull();
        deleteLayout.setSpacing(true);
        deleteLayout.getStyle().set("padding-right", "0");
        Button delete = new Button("Delete failed jobs");
        delete.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        delete.setWidthFull();
        delete.addClickListener(buttonClickEvent -> {
            jobRunner.deleteFailedJobs();
        });
        if (configService.getConfig().getRetryJobs().isEmpty()) {
            delete.setEnabled(false);
        }
        deleteLayout.add(delete);
        editorDiv.add(deleteLayout);
    }

    private void createGridLayout(SplitLayout splitLayout) {
        Div wrapper = new Div();
        wrapper.setId("wrapper");
        wrapper.setWidthFull();
        wrapper.add(credentials);

        Hr hr = new Hr();
        wrapper.add(hr);

        Tabs days = new Tabs();
        Tab sunday = new Tab("Sunday");
        Tab monday = new Tab("Monday");
        Tab tuesday = new Tab("Tuesday");
        Tab wednesday = new Tab("Wednesday");
        Tab thursday = new Tab("Thursday");
        Tab friday = new Tab("Friday");
        Tab saturday = new Tab("Saturday");
        List<Tab> tabs = Arrays.asList(sunday, monday, tuesday, wednesday, thursday, friday, saturday);

        VerticalLayout tabActivePage = new VerticalLayout();
        tabActivePage.setPadding(true);
        Map<Tab, Component> tabsToPages = new HashMap<>();
        for (int i = 0; i < tabs.size(); i++) {
            Tab tab = tabs.get(i);
            Div page = generateTabPage(tab.getLabel());
            tabsToPages.put(tab, page);
            if (i == 0) {
                tabActivePage.add(page);
            }
        }
        days.add(tabs.toArray(new Tab[0]));
        days.setFlexGrowForEnclosedTabs(1);
        days.addSelectedChangeListener(event -> {
            Component selectedPage = tabsToPages.get(days.getSelectedTab());
            selectedPage.setVisible(true);
            tabActivePage.removeAll();
            tabActivePage.add(selectedPage);
        });

        wrapper.add(days);
        wrapper.add(tabActivePage);

        VerticalLayout buttonLayout = new VerticalLayout();
        buttonLayout.setPadding(true);
        Button saveButton = new Button("Save");
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveButton.addClickListener(event -> {
            Bedtimes bedtimes = bedtimesBinder.getBean();
            SaveBedtimesJob bedtimesJob = new SaveBedtimesJob(getCurrentTimePlusDelay(configService), "Update bedtimes job", bedtimes);
            jobRunner.queueJob(bedtimesJob);
        });
        buttonLayout.add(saveButton);
        wrapper.add(buttonLayout);

        splitLayout.addToPrimary(wrapper);
    }

    private Div generateTabPage(String dayLabel) {
        Div page = new Div();
        FormLayout layoutWithFormItems = new FormLayout();
        TimePicker timePicker = new TimePicker();
        bedtimesBinder.forField(timePicker).bind(getBedtimeValueProvider(dayLabel), setBedtimeValueProvider(dayLabel));
        layoutWithFormItems.addFormItem(timePicker, "Choose internet shutoff time for " + dayLabel + ":");
        page.add(layoutWithFormItems);
        return page;
    }

    private ValueProvider<Bedtimes, LocalTime> getBedtimeValueProvider(String dayLabel) {
        return bedtimes -> {
            try {
                Method getter = Bedtimes.class.getDeclaredMethod("get" + dayLabel);
                return (LocalTime) getter.invoke(bedtimes);
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ignored) {
                return null;
            }
        };
    }

    private Setter<Bedtimes, LocalTime> setBedtimeValueProvider(String dayLabel) {
        return (bedtimes, time) -> {
            try {
                Method getter = Bedtimes.class.getDeclaredMethod("set" + dayLabel, LocalTime.class);
                getter.invoke(bedtimes, time);
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ignored) {}
        };
    }

    private void addFormItem(Div wrapper, FormLayout formLayout,
                             AbstractField field, String fieldName) {
        formLayout.addFormItem(field, fieldName);
        wrapper.add(formLayout);
        field.getElement().getClassList().add("full-width");
    }

    @Override
    public void afterNavigation(AfterNavigationEvent event) {

        // Lazy init of the grid items, happens only when we are sure the view will be
        // shown to the user
        if (configService.isEnabled()) {
            credentials.setItems(configService.getCredentials());
        }

        Bedtimes bedtimes = configService.getConfig().getBedtimes();
        if (bedtimes == null) {
            bedtimes = new Bedtimes();
        }
        bedtimesBinder.setBean(bedtimes);
    }

    private void populateForm(Credentials value) {
        // Value can be null as well, that clears the form
        binder.readBean(value);
    }
}

