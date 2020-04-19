package com.hyperion.selfcontrol.views.blockadd;

import com.hyperion.selfcontrol.backend.CredentialService;
import com.hyperion.selfcontrol.backend.Website;
import com.hyperion.selfcontrol.views.main.MainView;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.List;
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
        addAllowed.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        allowed = new Grid<>();
        allowed.getStyle().set("width", "75%");
        allowed.addColumn(Website::getName).setHeader("Name");
        allowed.addColumn(new ComponentRenderer<>(item -> {
            Button remove = new Button("Remove");
            remove.addClickListener(buttonClickEvent -> {
                List<Website> collect = allowed.getDataProvider().fetch(new Query<>()).collect(Collectors.toList());
                collect.remove(item);
                allowed.setItems(collect);
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
        addBlocked.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        blocked = new Grid<>();
        blocked.getStyle().set("width", "75%");
        blocked.addColumn(Website::getName).setHeader("Name");
        blocked.addColumn(new ComponentRenderer<>(item -> {
            Button remove = new Button("Remove");
            remove.addClickListener(buttonClickEvent -> {
                List<Website> collect = blocked.getDataProvider().fetch(new Query<>()).collect(Collectors.toList());
                collect.remove(item);
                blocked.setItems(collect);
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

    public void addItem() {
        /*statuses.getDataProvider().fetch(new Query<>()).collect(Collectors.toList());
        ListDataProvider<Person> dataProvider = (ListDataProvider<Person>) grid
                .getDataProvider();
        dataProvider.getItems().remove(person);
        dataProvider.refreshAll();*/
    }


    @Override
    public void afterNavigation(AfterNavigationEvent event) {
        allowed.setItems(Arrays.asList(new Website("cryptowat.ch"),
                new Website("discord.media"),
                new Website("bittrex.com"),
                new Website("kraken.com"),
                new Website("gateway.discord.gg"),
                new Website("highwebmedia.com"),
                new Website("dlive.tv"),
                new Website("discordapp.com"),
                new Website("onenote.com"),
                new Website("live.com"),
                new Website("store.google.com"),
                new Website("sunbasket.com")));

        blocked.setItems(Arrays.asList(new Website("zooville.org"),
                new Website("ts4rent.eu"),
                new Website("tsmasseur.com"),
                new Website("ts4rent.it"),
                new Website("ts4rent.jp"),
                new Website("ts4rent.br"),
                new Website("ts4rent.com"),
                new Website("ts4rent.fr"),
                new Website("duckduckgo.com"),
                new Website("instagram.com")));
    }
}
