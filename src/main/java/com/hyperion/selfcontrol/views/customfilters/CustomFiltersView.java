package com.hyperion.selfcontrol.views.customfilters;

import com.hyperion.selfcontrol.backend.CredentialService;
import com.hyperion.selfcontrol.backend.CustomFilterCategory;
import com.hyperion.selfcontrol.backend.Utils;
import com.hyperion.selfcontrol.backend.jobs.NetNannyBaseJob;
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
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.hyperion.selfcontrol.backend.jobs.pages.NetNannyFiltersPage.CUSTOM_CONTENT_FILTERS;

@Route(value = "customfilters", layout = MainView.class)
@PageTitle("Custom Filters")
@CssImport(value = "styles/views/customfilters/custom-filters-view.css", include = "lumo-badge")
@JsModule("@vaadin/vaadin-lumo-styles/badge.js")
public class CustomFiltersView extends Div implements AfterNavigationObserver {

    private static final Logger log = LoggerFactory.getLogger(CustomFiltersView.class);

    private CredentialService credentialService;

    private Grid<CustomFilterCategory> statuses;

    private TextField name = new TextField();
    private TextField status = new TextField();

    private Button setActive = new Button("Set Active");
    private Button setInactive = new Button("Set Inactive");

    private Binder<CustomFilterCategory> binder;

    @Autowired
    public CustomFiltersView(CredentialService credentialService) {
        this.credentialService = credentialService;
        setId("master-detail-view");
        // Configure Grid
        statuses = new Grid<>();
        statuses.addThemeVariants(GridVariant.LUMO_NO_BORDER);
        statuses.setHeightFull();
        statuses.addColumn(CustomFilterCategory::getName).setHeader("Category");
        statuses.addColumn(new ComponentRenderer<>(item -> {
            Span span = new Span(item.getStatus());
            span.getElement().getThemeList().add(item.getTheme());
            return span;
        })).setFlexGrow(0).setWidth("100px").setHeader("Status");

        //when a row is selected or deselected, populate form
        statuses.asSingleSelect().addValueChangeListener(event -> populateForm(event.getValue()));

        // Configure Form
        binder = new Binder<>(CustomFilterCategory.class);

        // Bind fields. This where you'd define e.g. validation rules
        binder.bindInstanceFields(this);

        // the grid valueChangeEvent will clear the form too
        setActive.addClickListener(e -> {
            CustomFilterCategory category = statuses.asSingleSelect().getValue();
            statuses.asSingleSelect().clear();

            Function<WebDriver, Optional<List<CustomFilterCategory>>> function = driver -> NetNannyBaseJob.navigateToProfile(driver, credentialService)
                    .flatMap(profile -> NetNannySetCategoryJob.setCategories(profile, credentialService, CUSTOM_CONTENT_FILTERS,
                            Collections.singletonList(new CustomFilterCategory(category.getName(), "block"))))
                    .map(NetNannyStatusJob::getNetNannyCustomStatuses);
            Supplier<Optional<List<CustomFilterCategory>>> composedFunction = Utils.composeWithDriver(function);
            composedFunction.get().ifPresent(items -> statuses.setItems(items));
        });

        setInactive.addClickListener(e -> {
            CustomFilterCategory category = statuses.asSingleSelect().getValue();
            statuses.asSingleSelect().clear();

            Consumer<WebDriver> function = driver -> NetNannyBaseJob.navigateToProfile(driver, credentialService)
                    .ifPresent(profile -> NetNannySetCategoryJob.setCategories(profile, credentialService, CUSTOM_CONTENT_FILTERS,
                            Collections.singletonList(new CustomFilterCategory(category.getName(), "inactive"))));
            Runnable composedFunction = Utils.composeWithDriver(function);
            credentialService.runWithDelay("Set Category Allowed: " + category.getName(), composedFunction);
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
        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.setId("button-layout");
        buttonLayout.setWidthFull();
        buttonLayout.setSpacing(true);
        setActive.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        setInactive.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        buttonLayout.add(setActive, setInactive);
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
        Optional<List<CustomFilterCategory>> filterCategories = NetNannyBaseJob.navigateToProfile(driver, credentialService)
                .map(NetNannyStatusJob::getNetNannyCustomStatuses);
        filterCategories.ifPresent(categories -> statuses.setItems(categories));
    }

    private void populateForm(CustomFilterCategory value) {
        // Value can be null as well, that clears the form
        binder.readBean(value);
    }
}

