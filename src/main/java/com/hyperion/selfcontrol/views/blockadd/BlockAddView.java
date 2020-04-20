package com.hyperion.selfcontrol.views.blockadd;

import com.hyperion.selfcontrol.Pair;
import com.hyperion.selfcontrol.backend.CredentialService;
import com.hyperion.selfcontrol.backend.Utils;
import com.hyperion.selfcontrol.backend.Website;
import com.hyperion.selfcontrol.backend.jobs.NetNannyBaseJob;
import com.hyperion.selfcontrol.backend.jobs.NetNannyBlockAddJob;
import com.hyperion.selfcontrol.backend.jobs.pages.NetNannyBlockAddPage;
import com.hyperion.selfcontrol.backend.jobs.pages.NetNannyProfile;
import com.hyperion.selfcontrol.views.main.MainView;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
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

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Route(value = "blockadd", layout = MainView.class)
@PageTitle("Block/Add")
@CssImport(value = "styles/views/blockadd/blockadd-view.css", include = "lumo-badge")
@JsModule("@vaadin/vaadin-lumo-styles/badge.js")
public class BlockAddView extends Div implements AfterNavigationObserver {

    private static final Logger log = LoggerFactory.getLogger(BlockAddView.class);

    private CredentialService credentialService;

    private TextField newAllowed;
    private Button addAllowed;
    private Grid<Website> allowed;

    private TextField newBlocked;
    private Button addBlocked;
    private Grid<Website> blocked;

    @Autowired
    public BlockAddView(CredentialService credentialService) {
        this.credentialService = credentialService;
        setId("master-detail-view");
        setSizeFull();

        Div top = new Div();
        top.add(new H3("Allowed Websites"));
        top.setClassName("marginated");
        newAllowed = new TextField();
        addAllowed = new Button("Allow");
        addAllowed.addClickListener(event -> {
            String value = newAllowed.getValue();
            Consumer<WebDriver> function = driver -> NetNannyBaseJob.navigateToProfile(driver, credentialService)
                    .map(profile -> NetNannyBlockAddJob.addItem(profile, value, true));
            Runnable runnable = Utils.composeWithDriver(function);
            credentialService.runWithDelay("Allow website: " + value, runnable);
            newAllowed.clear();
        });
        addAllowed.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        allowed = new Grid<>();
        allowed.getStyle().set("width", "75%");
        allowed.addColumn(Website::getName).setHeader("Name");
        allowed.addColumn(new ComponentRenderer<>(item -> {
            Button remove = new Button("Remove");
            remove.addClickListener(buttonClickEvent -> {
                Function<WebDriver, Boolean> function = driver -> NetNannyBaseJob.navigateToProfile(driver, credentialService)
                        .map(profile -> NetNannyBlockAddJob.removeItem(profile, item.getName(), true)).orElse(false);
                Supplier<Boolean> supplier = Utils.composeWithDriver(function);
                if (supplier.get()) {
                    List<Website> collect = allowed.getDataProvider().fetch(new Query<>()).collect(Collectors.toList());
                    collect.remove(item);
                    allowed.setItems(collect);
                }
            });
            return remove;
        })).setHeader("Remove");
        HorizontalLayout buttonRow = new HorizontalLayout();
        buttonRow.add(newAllowed);
        buttonRow.add(addAllowed);
        top.add(buttonRow);
        top.add(allowed);
        add(top);

        Div bottom = new Div();
        bottom.add(new H3("Blocked Websites"));
        bottom.setClassName("marginated");
        newBlocked = new TextField();
        addBlocked = new Button("Block");
        addBlocked.addClickListener(event -> {
            String value = newBlocked.getValue();
            Function<WebDriver, Boolean> function = driver -> NetNannyBaseJob.navigateToProfile(driver, credentialService)
                    .map(profile -> NetNannyBlockAddJob.addItem(profile, value, false)).orElse(false);
            Supplier<Boolean> supplier = Utils.composeWithDriver(function);
            if (supplier.get()) {
                log.info("Adding item");
                Set<Website> collect = blocked.getDataProvider().fetch(new Query<>()).collect(Collectors.toSet());
                collect.add(new Website(value));
                blocked.setItems(new ArrayList<>(collect));
            }
            newBlocked.clear();
        });
        addBlocked.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        blocked = new Grid<>();
        blocked.getStyle().set("width", "75%");
        blocked.addColumn(Website::getName).setHeader("Name");
        blocked.addColumn(new ComponentRenderer<>(item -> {
            Button remove = new Button("Remove");
            remove.addClickListener(buttonClickEvent -> {
                Consumer<WebDriver> function = driver -> NetNannyBaseJob.navigateToProfile(driver, credentialService)
                        .flatMap(NetNannyProfile::clickBlockAdd).ifPresent(netNannyBlockAddPage -> netNannyBlockAddPage.removeItem(item.getName(), false));
                Runnable withDriver = Utils.composeWithDriver(function);
                credentialService.runWithDelay("Remove blocked site: " + item.getName(), withDriver);
                remove.setEnabled(false);
            });
            return remove;
        })).setHeader("Remove");
        HorizontalLayout bottomButtonRow = new HorizontalLayout();
        bottomButtonRow.add(newBlocked);
        bottomButtonRow.add(addBlocked);
        bottom.add(bottomButtonRow);
        bottom.add(blocked);
        add(bottom);
    }

    @Override
    public void afterNavigation(AfterNavigationEvent event) {
        Function<WebDriver, Pair<List<Website>, List<Website>>> function = driver -> NetNannyBaseJob.navigateToProfile(driver, credentialService)
                .map(NetNannyBlockAddJob::getBlockAddLists).orElse(new Pair<>(Collections.emptyList(), Collections.emptyList()));
        Supplier<Pair<List<Website>, List<Website>>> supplier = Utils.composeWithDriver(function);
        Pair<List<Website>, List<Website>> results = supplier.get();
        allowed.setItems(results.getFirst());
        blocked.setItems(results.getSecond());
    }
}
