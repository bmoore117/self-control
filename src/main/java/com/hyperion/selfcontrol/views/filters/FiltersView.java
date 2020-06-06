package com.hyperion.selfcontrol.views.filters;

import com.hyperion.selfcontrol.Pair;
import com.hyperion.selfcontrol.backend.ConfigService;
import com.hyperion.selfcontrol.backend.FilterCategory;
import com.hyperion.selfcontrol.backend.JobRunner;
import com.hyperion.selfcontrol.backend.Utils;
import com.hyperion.selfcontrol.backend.config.job.ToggleFilterJob;
import com.hyperion.selfcontrol.backend.config.job.ToggleSafeSearchJob;
import com.hyperion.selfcontrol.backend.jobs.NetNannyBaseJob;
import com.hyperion.selfcontrol.backend.jobs.NetNannyStatusJob;
import com.hyperion.selfcontrol.views.main.MainView;
import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import static com.helger.css.propertyvalue.CCSSValue.BLOCK;
import static com.hyperion.selfcontrol.backend.AbstractFilterCategory.ALLOW;
import static com.hyperion.selfcontrol.backend.Utils.getCurrentTimePlusDelay;
import static com.hyperion.selfcontrol.backend.jobs.pages.NetNannyFiltersPage.CONTENT_FILTERS;

@Route(value = "filters", layout = MainView.class)
@PageTitle("Master-Detail")
@CssImport(value = "styles/views/filters/filters-view.css", include = "lumo-badge")
@JsModule("@vaadin/vaadin-lumo-styles/badge.js")
public class FiltersView extends Div implements AfterNavigationObserver {

    private static final Logger log = LoggerFactory.getLogger(FiltersView.class);

    private final ConfigService configService;
    private final JobRunner jobRunner;

    private Grid<FilterCategory> statuses;

    private TextField name = new TextField();
    private TextField status = new TextField();
    private Checkbox checkbox;

    private Button setAllowed = new Button("Set Allowed");
    private Button setBlocked = new Button("Set Blocked");

    private Binder<FilterCategory> binder;

    @Autowired
    public FiltersView(ConfigService configService, JobRunner jobRunner) {
        this.configService = configService;
        this.jobRunner = jobRunner;
        setId("master-detail-view");
        // Configure Grid
        statuses = new Grid<>();
        statuses.addThemeVariants(GridVariant.LUMO_NO_BORDER);
        statuses.setHeightFull();
        statuses.addColumn(FilterCategory::getName).setHeader("Category");
        statuses.addColumn(new ComponentRenderer<>(item -> {
            Span span = new Span(item.getStatus());
            span.getElement().getThemeList().add(item.getTheme());
            return span;
        })).setFlexGrow(0).setWidth("100px").setHeader("Status");

        //when a row is selected or deselected, populate form
        statuses.asSingleSelect().addValueChangeListener(event -> populateForm(event.getValue()));

        // Configure Form
        binder = new Binder<>(FilterCategory.class);

        // Bind fields. This where you'd define e.g. validation rules
        binder.bindInstanceFields(this);

        // the grid valueChangeEvent will clear the form too
        setAllowed.addClickListener(e -> {
            FilterCategory category = statuses.asSingleSelect().getValue();
            statuses.asSingleSelect().clear();
            ToggleFilterJob job = new ToggleFilterJob(getCurrentTimePlusDelay(configService), "Toggle filter " + category.getName() + ", allowed",
                    CONTENT_FILTERS, Collections.singletonList(new FilterCategory(category.getName(), ALLOW)));
            jobRunner.queueJob(job);
        });

        setBlocked.addClickListener(e -> {
            FilterCategory category = statuses.asSingleSelect().getValue();
            statuses.asSingleSelect().clear();
            ToggleFilterJob job = new ToggleFilterJob(LocalDateTime.now(), "Toggle filter " + category.getName() + ", blocked",
                    CONTENT_FILTERS, Collections.singletonList(new FilterCategory(category.getName(), BLOCK)));
            boolean resultStatus = jobRunner.runJob(job);
            if (resultStatus) {
                category.setStatus(BLOCK);
            }
        });

        checkbox = new Checkbox();
        checkbox.addClickListener(e -> {
            // value here is new value, not old value - it is the value after clicking
            if (!checkbox.getValue()) {
                // disable on delay
                ToggleSafeSearchJob job = new ToggleSafeSearchJob(getCurrentTimePlusDelay(configService), "Toggle safesearch off", false);
                jobRunner.queueJob(job);
            } else {
                // if safe search not currently enabled, run immediately and enable
                ToggleSafeSearchJob job = new ToggleSafeSearchJob(null, "Toggle safesearch off", false);
                boolean statusOnServer = jobRunner.runJob(job);
                checkbox.setValue(statusOnServer);
            }
        });

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
        addFormItem(editorDiv, formLayout, name, "Category");
        addFormItem(editorDiv, formLayout, status, "Status");
        createButtonLayout(editorDiv);
        FormLayout safeSearchLayout = new FormLayout();
        addFormItem(editorDiv, safeSearchLayout, checkbox, "Force SafeSearch");
        splitLayout.addToSecondary(editorDiv);
    }

    private void createButtonLayout(Div editorDiv) {
        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.setId("button-layout");
        buttonLayout.setWidthFull();
        buttonLayout.setSpacing(true);
        setAllowed.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        setBlocked.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        buttonLayout.add(setAllowed, setBlocked);
        editorDiv.add(buttonLayout);
    }

    private void createGridLayout(SplitLayout splitLayout) {
        Div wrapper = new Div();
        wrapper.setId("wrapper");
        wrapper.setWidthFull();
        splitLayout.addToPrimary(wrapper);
        wrapper.add(statuses);
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
        Consumer<WebDriver> driverConsumer = this::doAfterNavigation;
        Runnable withDriver = Utils.composeWithDriver(driverConsumer);
        withDriver.run();
    }

    public void doAfterNavigation(WebDriver driver) {
        // insert here
        Optional<Pair<Boolean, List<FilterCategory>>> resultsOpt = NetNannyBaseJob.navigateToProfile(driver, configService)
                .map(profile -> new Pair<>(profile.isForceSafeSearch(), NetNannyStatusJob.getNetNannyStatuses(profile)));
        resultsOpt.ifPresent(results -> {
            checkbox.setValue(results.getFirst());
            if (!results.getFirst()) {
                checkbox.setEnabled(true);
            }
            statuses.setItems(results.getSecond());
        });
    }

    private void populateForm(FilterCategory value) {
        // Value can be null as well, that clears the form
        binder.readBean(value);
    }
}
