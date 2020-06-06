package com.hyperion.selfcontrol.views.customfilters;

import com.hyperion.selfcontrol.backend.*;
import com.hyperion.selfcontrol.backend.config.job.DeleteCustomFilterJob;
import com.hyperion.selfcontrol.backend.config.job.ToggleFilterJob;
import com.hyperion.selfcontrol.backend.config.job.UpdateCustomFilterJob;
import com.hyperion.selfcontrol.backend.jobs.NetNannyBaseJob;
import com.hyperion.selfcontrol.backend.jobs.NetNannyStatusJob;
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
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.hyperion.selfcontrol.backend.AbstractFilterCategory.BLOCK;
import static com.hyperion.selfcontrol.backend.CustomFilterCategory.INACTIVE;
import static com.hyperion.selfcontrol.backend.Utils.getCurrentTimePlusDelay;
import static com.hyperion.selfcontrol.backend.jobs.pages.NetNannyFiltersPage.CUSTOM_CONTENT_FILTERS;
import static java.util.Collections.singletonList;

@Route(value = "customfilters", layout = MainView.class)
@PageTitle("Custom Filters")
@CssImport(value = "styles/views/customfilters/custom-filters-view.css", include = "lumo-badge")
@JsModule("@vaadin/vaadin-lumo-styles/badge.js")
public class CustomFiltersView extends Div implements AfterNavigationObserver {

    private static final Logger log = LoggerFactory.getLogger(CustomFiltersView.class);

    private final ConfigService configService;
    private final JobRunner jobRunner;

    private final Grid<CustomFilterCategory> statuses;
    private final Grid<Keyword> activeFilterKeywords;

    private final TextField name = new TextField();
    private final TextField status = new TextField();

    private final Button setActive = new Button("Set Active");
    private final Button setInactive = new Button("Set Inactive");
    private final Button createNew = new Button("Create");
    private final Button delete = new Button("Delete");

    private final Binder<CustomFilterCategory> binder;
    private Button add;
    private Button save;
    private TextField term;

    @Autowired
    public CustomFiltersView(ConfigService configService, JobRunner jobRunner) {
        this.jobRunner = jobRunner;
        this.configService = configService;
        setId("master-detail-view");

        activeFilterKeywords = new Grid<>();
        activeFilterKeywords.setHeight("30%");
        activeFilterKeywords.addThemeVariants(GridVariant.LUMO_NO_BORDER);
        activeFilterKeywords.addColumn(Keyword::getValue).setHeader("Keyword");
        activeFilterKeywords.addColumn(new ComponentRenderer<>(item -> {
            Button remove = new Button("Remove");
            remove.addClickListener(buttonClickEvent -> {
                List<Keyword> items = activeFilterKeywords.getDataProvider().fetch(new Query<>()).collect(Collectors.toList());
                items.remove(item);
                activeFilterKeywords.setItems(items);
            });
            return remove;
        })).setHeader("Action");

        // Configure Grid
        statuses = new Grid<>();
        statuses.addThemeVariants(GridVariant.LUMO_NO_BORDER);
        statuses.setHeight("50%");
        statuses.addColumn(CustomFilterCategory::getName).setHeader("Category");
        statuses.addColumn(new ComponentRenderer<>(item -> {
            Span span = new Span(item.getStatus());
            span.getElement().getThemeList().add(item.getTheme());
            return span;
        })).setFlexGrow(0).setWidth("100px").setHeader("Status");

        //when a row is selected or deselected, populate form
        statuses.asSingleSelect().addValueChangeListener(event -> {
            CustomFilterCategory filterCategory = event.getValue();
            populateForm(filterCategory);
            if (filterCategory != null) {
                activeFilterKeywords.setItems(filterCategory.getKeywords());
                add.setEnabled(true);
                save.setEnabled(true);
                delete.setEnabled(true);
                setActive.setEnabled(true);
                setInactive.setEnabled(true);
            } else {
                activeFilterKeywords.setItems(Collections.emptyList());
                add.setEnabled(false);
                save.setEnabled(false);
                delete.setEnabled(false);
                setActive.setEnabled(false);
                setInactive.setEnabled(false);
                term.clear();
            }
        });

        // Configure Form
        binder = new Binder<>(CustomFilterCategory.class);

        // Bind fields. This where you'd define e.g. validation rules
        binder.bindInstanceFields(this);

        // the grid valueChangeEvent will clear the form too
        setActive.addClickListener(e -> {
            CustomFilterCategory category = statuses.asSingleSelect().getValue();
            statuses.asSingleSelect().clear();
            ToggleFilterJob job = new ToggleFilterJob(null, "Toggle custom filter " + category.getName() + ", active",
                    CUSTOM_CONTENT_FILTERS, singletonList(new CustomFilterCategory(category.getName(), BLOCK)));
            boolean resultStatus = jobRunner.runJob(job);
            if (resultStatus) {
                category.setStatus(BLOCK);
            }
        });
        setActive.setEnabled(false);

        setInactive.addClickListener(e -> {
            CustomFilterCategory category = statuses.asSingleSelect().getValue();
            statuses.asSingleSelect().clear();
            ToggleFilterJob job = new ToggleFilterJob(getCurrentTimePlusDelay(configService), "Toggle custom filter " + category.getName() + ", inactive",
                    CUSTOM_CONTENT_FILTERS, singletonList(new CustomFilterCategory(category.getName(), INACTIVE)));
            jobRunner.queueJob(job);
        });
        setInactive.setEnabled(false);

        createNew.addClickListener(e -> {
            if (name.getValue() != null && !name.getValue().isEmpty() && !name.getValue().equals("")) {
                List<CustomFilterCategory> categories = statuses.getDataProvider().fetch(new Query<>()).collect(Collectors.toList());
                categories.add(new CustomFilterCategory(name.getValue(), INACTIVE, new ArrayList<>()));
                statuses.setItems(categories);
            }
        });

        delete.addClickListener(e -> {
            CustomFilterCategory category = statuses.asSingleSelect().getValue();
            DeleteCustomFilterJob job = new DeleteCustomFilterJob(getCurrentTimePlusDelay(configService), "Delete custom filter category " + category.getName(), category);
            jobRunner.queueJob(job);
        });
        delete.setEnabled(false);

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
        splitLayout.addToSecondary(editorDiv);
    }

    private void createButtonLayout(Div editorDiv) {
        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.setWidthFull();
        buttonLayout.setSpacing(true);
        buttonLayout.setPadding(false);
        buttonLayout.setClassName("button-layout");
        setActive.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        setActive.getStyle().set("width", "7em");
        setInactive.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        setInactive.getStyle().set("width", "7em");
        buttonLayout.add(setActive, setInactive);
        editorDiv.add(buttonLayout);
        HorizontalLayout secondRow = new HorizontalLayout();
        secondRow.setClassName("button-layout");
        secondRow.setWidthFull();
        secondRow.setSpacing(true);
        secondRow.setPadding(false);
        createNew.getStyle().set("width", "7em");
        delete.getStyle().set("width", "7em");
        secondRow.add(createNew, delete);
        editorDiv.add(secondRow);
    }

    private void createGridLayout(SplitLayout splitLayout) {
        Div wrapper = new Div();
        wrapper.setId("wrapper");
        wrapper.setWidthFull();
        splitLayout.addToPrimary(wrapper);
        wrapper.add(statuses);

        Hr hr = new Hr();
        wrapper.add(hr);

        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.setPadding(true);
        buttonLayout.setWidthFull();
        term = new TextField();
        term.setPlaceholder("Add term");
        buttonLayout.add(term);
        add = new Button("Add");
        add.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        add.setWidth("5em");
        add.addClickListener(event -> {
            List<Keyword> items = activeFilterKeywords.getDataProvider().fetch(new Query<>()).collect(Collectors.toList());
            if (term.getValue() != null && !term.getValue().isEmpty()) {
                items.add(new Keyword(term.getValue()));
            }
            activeFilterKeywords.setItems(items);
            term.clear();
        });
        add.setEnabled(false);
        buttonLayout.add(add);
        save = new Button("Save");
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        save.setWidth("5em");
        save.addClickListener(event -> {
            CustomFilterCategory currentActive = statuses.asSingleSelect().getValue();
            List<Keyword> updatedList = activeFilterKeywords.getDataProvider().fetch(new Query<>()).collect(Collectors.toList());
            List<Keyword> oldList = currentActive.getKeywords();

            List<Keyword> added = updatedList.stream().filter(keyword -> !oldList.contains(keyword)).collect(Collectors.toList());
            List<Keyword> removed = oldList.stream().filter(keyword -> !updatedList.contains(keyword)).collect(Collectors.toList());

            currentActive.setKeywords(updatedList);

            if (removed.size() == 0 && added.size() > 0) {
                UpdateCustomFilterJob job = new UpdateCustomFilterJob(null, "Add keywords to custom filter " + currentActive.getName(), currentActive);
                jobRunner.runJob(job);
            } else if (removed.size() > 0) {
                UpdateCustomFilterJob job = new UpdateCustomFilterJob(getCurrentTimePlusDelay(configService), "Add/remove keywords to custom filter " + currentActive.getName(), currentActive);
                jobRunner.queueJob(job);
            }
        });
        save.setEnabled(false);
        buttonLayout.add(save);
        wrapper.add(buttonLayout);
        wrapper.add(activeFilterKeywords);
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
        Optional<List<CustomFilterCategory>> filterCategories = NetNannyBaseJob.navigateToProfile(driver, configService)
                .map(NetNannyStatusJob::getNetNannyCustomStatuses);
        filterCategories.ifPresent(statuses::setItems);
    }

    private void populateForm(CustomFilterCategory value) {
        // Value can be null as well, that clears the form
        binder.readBean(value);
    }
}

