package com.hyperion.selfcontrol.views.blockadd;

import com.hyperion.selfcontrol.Pair;
import com.hyperion.selfcontrol.backend.ConfigService;
import com.hyperion.selfcontrol.backend.JobRunner;
import com.hyperion.selfcontrol.backend.Utils;
import com.hyperion.selfcontrol.backend.Website;
import com.hyperion.selfcontrol.backend.config.job.AddHostJob;
import com.hyperion.selfcontrol.backend.config.job.RemoveHostJob;
import com.hyperion.selfcontrol.backend.jobs.NetNannyBaseJob;
import com.hyperion.selfcontrol.backend.jobs.NetNannyBlockAddJob;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.hyperion.selfcontrol.backend.Utils.getCurrentTimePlusDelay;

@Route(value = "blockadd", layout = MainView.class)
@PageTitle("Block/Add")
@CssImport(value = "styles/views/blockadd/blockadd-view.css", include = "lumo-badge")
@JsModule("@vaadin/vaadin-lumo-styles/badge.js")
public class BlockAddView extends Div implements AfterNavigationObserver {

    private static final Logger log = LoggerFactory.getLogger(BlockAddView.class);

    private final ConfigService configService;
    private final JobRunner jobRunner;

    private TextField newAllowed;
    private Grid<Website> allowed;

    private TextField newBlocked;
    private Grid<Website> blocked;

    @Autowired
    public BlockAddView(ConfigService configService, JobRunner jobRunner) {
        this.jobRunner = jobRunner;
        this.configService = configService;
        setId("master-detail-view");
        setSizeFull();

        Div top = new Div();
        top.add(new H3("Allowed Websites"));
        top.setClassName("marginated");
        newAllowed = new TextField();
        Button addAllowed = new Button("Allow");
        addAllowed.addClickListener(event -> {
            String value = newAllowed.getValue();
            AddHostJob job = new AddHostJob(getCurrentTimePlusDelay(configService), "Allow host " + value, value, true);
            jobRunner.queueJob(job);
            newAllowed.clear();
        });
        addAllowed.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        allowed = new Grid<>();
        allowed.getStyle().set("width", "75%");
        allowed.addColumn(Website::getName).setHeader("Name");
        allowed.addColumn(new ComponentRenderer<>(item -> {
            Button remove = new Button("Remove");
            remove.addClickListener(buttonClickEvent -> {
                RemoveHostJob job = new RemoveHostJob(null, "Remove allowed host " + item.getName(), item.getName(), true);
                boolean resultStatus = jobRunner.runJob(job);
                if (resultStatus) {
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
        Button addBlocked = new Button("Block");
        addBlocked.addClickListener(event -> {
            String value = newBlocked.getValue();
            AddHostJob job = new AddHostJob(null, "Block host " + value, value, false);
            boolean resultStatus = jobRunner.runJob(job);
            if (resultStatus) {
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
                RemoveHostJob job = new RemoveHostJob(getCurrentTimePlusDelay(configService), "Remove blocked host " + item.getName(), item.getName(), false);
                jobRunner.queueJob(job);
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
        Function<WebDriver, Pair<List<Website>, List<Website>>> function = driver -> NetNannyBaseJob.navigateToProfile(driver, configService)
                .map(NetNannyBlockAddJob::getBlockAddLists).orElse(new Pair<>(Collections.emptyList(), Collections.emptyList()));
        Supplier<Pair<List<Website>, List<Website>>> supplier = Utils.composeWithDriver(function);
        Pair<List<Website>, List<Website>> results = supplier.get();
        allowed.setItems(results.getFirst());
        blocked.setItems(results.getSecond());
    }
}
