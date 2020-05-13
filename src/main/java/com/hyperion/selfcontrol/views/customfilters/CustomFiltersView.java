package com.hyperion.selfcontrol.views.customfilters;

import com.hyperion.selfcontrol.backend.ConfigService;
import com.hyperion.selfcontrol.backend.CustomFilterCategory;
import com.hyperion.selfcontrol.backend.Keyword;
import com.hyperion.selfcontrol.backend.Utils;
import com.hyperion.selfcontrol.backend.jobs.NetNannyBaseJob;
import com.hyperion.selfcontrol.backend.jobs.NetNannyCustomFiltersJob;
import com.hyperion.selfcontrol.backend.jobs.NetNannySetCategoryJob;
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
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
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
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.hyperion.selfcontrol.backend.jobs.pages.NetNannyFiltersPage.CUSTOM_CONTENT_FILTERS;

@Route(value = "customfilters", layout = MainView.class)
@PageTitle("Custom Filters")
@CssImport(value = "styles/views/customfilters/custom-filters-view.css", include = "lumo-badge")
@JsModule("@vaadin/vaadin-lumo-styles/badge.js")
public class CustomFiltersView extends Div implements AfterNavigationObserver {

    private static final Logger log = LoggerFactory.getLogger(CustomFiltersView.class);

    private final ConfigService configService;

    private final Grid<CustomFilterCategory> statuses;
    private final Grid<Keyword> activeFilterKeywords;

    private final TextField name = new TextField();
    private final TextField status = new TextField();

    private final Button setActive = new Button("Set Active");
    private final Button setInactive = new Button("Set Inactive");
    private final Button createNew = new Button("Create New Category");

    private final Binder<CustomFilterCategory> binder;
    private Button add;
    private Button save;
    private TextField term;

    @Autowired
    public CustomFiltersView(ConfigService configService) {
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
            } else {
                activeFilterKeywords.setItems(Collections.emptyList());
                add.setEnabled(false);
                save.setEnabled(false);
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

            Function<WebDriver, Optional<List<CustomFilterCategory>>> function = driver -> NetNannyBaseJob.navigateToProfile(driver, configService)
                    .flatMap(profile -> NetNannySetCategoryJob.setCategories(profile, configService, CUSTOM_CONTENT_FILTERS,
                            Collections.singletonList(new CustomFilterCategory(category.getName(), "block"))))
                    .map(NetNannyStatusJob::getNetNannyCustomStatuses);
            Supplier<Optional<List<CustomFilterCategory>>> composedFunction = Utils.composeWithDriver(function);
            composedFunction.get().ifPresent(statuses::setItems);
        });

        setInactive.addClickListener(e -> {
            CustomFilterCategory category = statuses.asSingleSelect().getValue();
            statuses.asSingleSelect().clear();

            Consumer<WebDriver> function = driver -> NetNannyBaseJob.navigateToProfile(driver, configService)
                    .ifPresent(profile -> NetNannySetCategoryJob.setCategories(profile, configService, CUSTOM_CONTENT_FILTERS,
                            Collections.singletonList(new CustomFilterCategory(category.getName(), "inactive"))));
            Runnable composedFunction = Utils.composeWithDriver(function);
            configService.runWithDelay("Set Category Allowed: " + category.getName(), composedFunction);
        });

        createNew.addClickListener(e -> {
            List<CustomFilterCategory> categories = statuses.getDataProvider().fetch(new Query<>()).collect(Collectors.toList());
            categories.add(new CustomFilterCategory(name.getValue(), CustomFilterCategory.INACTIVE, new ArrayList<>()));
            statuses.setItems(categories);
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
        splitLayout.addToSecondary(editorDiv);
    }

    private void createButtonLayout(Div editorDiv) {
        VerticalLayout buttonLayout = new VerticalLayout();
        buttonLayout.setId("button-layout");
        buttonLayout.setWidthFull();
        buttonLayout.setSpacing(true);
        buttonLayout.setPadding(false);
        setActive.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        setInactive.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        createNew.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        buttonLayout.add(createNew, setActive, setInactive);
        editorDiv.add(buttonLayout);
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
                Function<WebDriver, Boolean> function = driver -> NetNannyCustomFiltersJob.saveCustomFilters(driver, currentActive);
                Supplier<Boolean> booleanSupplier = Utils.composeWithDriver(function);
                booleanSupplier.get();
            } else if (removed.size() > 0) {
                Consumer<WebDriver> function = driver -> NetNannyCustomFiltersJob.saveCustomFilters(driver, currentActive);
                Runnable runnable = Utils.composeWithDriver(function);
                configService.runWithDelay("Save Custom Filter Category: " + currentActive.getName(), runnable);
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

        DesiredCapabilities capabilities = DesiredCapabilities.chrome();
        WebDriver driver = null;
        try {
            driver = new RemoteWebDriver(
                    new URL("http://0.0.0.0:4444/wd/hub"),
                    capabilities);

            doAfterNavigation(driver);
        } catch (MalformedURLException e) {
            log.error("Malformed selenium host url", e);
        } finally {
            if (driver != null) {
                driver.close();
            }
        }
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

