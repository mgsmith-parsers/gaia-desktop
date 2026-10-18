package tools.pantheum.gaia.desktop.ui;

import tools.pantheum.gaia.GaiaConstants;
import tools.pantheum.gaia.GaiaParser;
import tools.pantheum.gaia.GaiaVersion;
import tools.pantheum.gaia.desktop.GaiaDesktopVersion;
import tools.pantheum.gaia.desktop.update.UpdateChecker;
import tools.pantheum.gaia.desktop.Theme;
import tools.pantheum.gaia.desktop.combine.ScanCombiner;
import tools.pantheum.gaia.desktop.html.GaiaCombineReport;
import tools.pantheum.gaia.desktop.html.GaiaDesktopReport;
import tools.pantheum.gaia.desktop.i18n.UiStrings;
import tools.pantheum.gaia.desktop.model.ScanRecord;
import tools.pantheum.gaia.desktop.model.ScanSource;
import tools.pantheum.gaia.desktop.parse.GsToken;
import tools.pantheum.gaia.desktop.parse.Languages;
import tools.pantheum.gaia.desktop.parse.ParseOptions;
import tools.pantheum.gaia.desktop.scanner.ScannerListener;
import tools.pantheum.gaia.desktop.scanner.ScannerService;
import tools.pantheum.gaia.desktop.store.CaptureStore;
import tools.pantheum.gaia.desktop.store.OptionsStore;
import tools.pantheum.gaia.result.ParseResult;
import javafx.application.HostServices;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.NodeOrientation;
import javafx.geometry.Pos;
import javafx.print.PrinterJob;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * The application window: a toolbar, the capture history, the rendered parse report, and
 * the parser-options panel — with the strip under the toolbar switching between the input
 * bar (Parse) and the AI editor (Build).
 *
 * <p>Input reaches the parser from four places — typed or pasted into the input bar, read
 * from a text file (one input per line), pushed by {@link ScannerService}, or assembled by
 * {@link BuilderPane} — and all four land in {@link #capture}, so a scan, a paste and a
 * build of the same string behave identically from there on. Each becomes a
 * {@link ScanRecord}; selecting one renders its report into the {@link WebView} via
 * {@link GaiaDesktopReport}. That single funnel is why a built string needs no separate
 * report, history or export path of its own.
 *
 * <p>Changing a parser option re-parses the selected capture in place, so the panel acts
 * as a live "what does this input look like under these settings" control rather than
 * only affecting the next scan.
 *
 * <p>Options and captures survive a restart — see
 * {@link tools.pantheum.gaia.desktop.store.AppPaths}. Options are written whenever they
 * change; captures whenever the history changes, and restored in the background at start-up
 * so a long history never delays the first frame.
 *
 * <p>Two rules the window is built around, both learned the hard way:
 * <ul>
 *   <li>a control that holds <em>user data</em> is a field, re-labelled on a language change
 *       ({@link BuilderPane#retext}); everything else is rebuilt from the catalogue;</li>
 *   <li>a text field cannot hold an FNC1, so the separator travels through the UI as the
 *       {@code {GS}} token — see {@link GsToken}.</li>
 * </ul>
 */
public final class MainView extends BorderPane {

    private static final Logger log = LoggerFactory.getLogger(MainView.class);

    /** Stateless and thread-safe; reused for every parse. */
    private static final GaiaParser PARSER = new GaiaParser();

    /** Verified sample inputs, offered from the Input menu. */
    private static final String[][] SAMPLES = {
            {"GTIN + expiry + lot",     "01095060001343521726123110LOT-001"},
            {"GTIN only",               "0109506000134352"},
            {"Data Matrix, weight, price",
                                        "]d2" + "0109506000134352" + "3103001500" + "3932036002953"},
            {"Digital Link URI",        "https://example.com/01/09506000134352/10/LOT-ABC?17=271231"},
            {"Bad check digit (error)", "0109506000134350"},
    };

    private final ParseOptions options = new ParseOptions();
    private final OptionsStore.UiState uiState = new OptionsStore.UiState();
    private final ObjectProperty<Theme> theme = new SimpleObjectProperty<>(Theme.LIGHT);

    /** The label catalogue for the current language; replaced whenever the language changes. */
    private UiStrings strings;

    private final ObservableList<ScanRecord> history = FXCollections.observableArrayList();
    private final ListView<ScanRecord> historyList = new ListView<>(history);

    private final TextField        input         = new TextField();
    private final WebView          webView       = new WebView();
    private final Label            status        = new Label();
    private final Label            scannerState  = new Label();
    /** The "new version available" notice; in the status bar, hidden until a check finds one. */
    private final Hyperlink        updateLink    = new Hyperlink();
    private final Hyperlink        skipLink      = new Hyperlink();
    private final HBox             updateBox     = new HBox(10, updateLink, skipLink);
    private final UpdateChecker    updates       = new UpdateChecker();
    /** The newer release on offer, or null when there is none to show. */
    private UpdateChecker.Release  availableRelease;
    private final Label            captionLabel  = new Label();
    private final ToggleButton     scannerToggle = new ToggleButton();
    private final ToggleButton     optionsToggle = new ToggleButton();
    private final ComboBox<String> deviceCombo   = new ComboBox<>();
    /** Lives in the status bar, which is built once — so it is re-labelled, not rebuilt. */
    private final Button           aboutButton   = new Button();

    /** Parse ⇄ Build ⇄ Combine. The modes share the window; only the strip under the toolbar swaps. */
    private final ToggleButton parseMode   = new ToggleButton();
    private final ToggleButton buildMode   = new ToggleButton();
    private final ToggleButton combineMode = new ToggleButton();
    /**
     * Built once and re-labelled thereafter — unlike the rest of the chrome, it holds the
     * user's half-finished AI rows, which a language change must not discard.
     */
    private BuilderPane builderPane;
    /**
     * Combine mode's strip, and the scans it is managing. Like the builder, the pane is built once
     * and re-labelled, because a language change must not throw away the barcodes taken so far.
     *
     * <p>The combiner lives here rather than in the pane so that a barcode from the scanner and a
     * barcode typed into the field travel the same path — see {@link #combineAdd}.
     */
    private CombinePane combinePane;
    /**
     * The window's bottom edge: the status bar, with the Combine strip above it in Combine mode.
     */
    private final VBox bottomBox = new VBox();
    /** The capture history and the split it shares with the report; Combine hides the history. */
    private SplitPane split;
    private VBox historyPane;
    private ScanCombiner combiner;
    /** Why the last barcode offered was refused, shown until the next one is offered. */
    private ScanCombiner.Refusal lastRefusal;
    /** Holds the menu bar, the toolbar, and — in build mode — the builder. */
    private final VBox topBox = new VBox();
    /** Parse-only toolbar controls, hidden in build mode. */
    private Button gsButton, parseButton;

    private final ScannerService scanner;

    /**
     * Whether this machine has scanner support at all — see
     * {@link ScannerService#isSupportedPlatform()}. When it does not, the scanner controls are
     * never built: no toggle or device picker in the toolbar, no Scanner menu, no state label
     * in the status bar. Hiding them would be the wrong shape, because there is nothing here
     * that a setting or a plugged-in device could switch on.
     */
    private final boolean scannerSupported = ScannerService.isSupportedPlatform();

    /** The HTML currently in the WebView — what "Save report" and "Copy report HTML" use. */
    private String currentHtml = "";

    /** Suppresses persistence while options are being loaded into the UI at start-up. */
    private boolean loading = true;

    /**
     * The message language {@code --language=…} asked for, or {@code null} for an ordinary run.
     *
     * <p>An override is for the run, not for the file: {@link #persistedLanguage()} keeps
     * writing {@link #savedLanguage} until the user picks a language themselves, so opening the
     * app once in another language cannot silently change what it opens in tomorrow.
     */
    private final GaiaConstants.Language languageOverride;

    /** The language read from the options file, which an override must not overwrite. */
    private GaiaConstants.Language savedLanguage;

    /** Set when the language is changed from inside the window — a real choice, which persists. */
    private boolean languageChosenInSession;

    /**
     * The caret position the input field last had while it was focused.
     *
     * <p>JavaFX resets a text field's caret to 0 when it loses focus, and opening a menu
     * takes focus away — so by the time an "insert at the caret" action runs from the menu,
     * {@code getCaretPosition()} reports 0 and would drop the text at the start.
     */
    private int lastCaret;

    public MainView() {
        this(null);
    }

    /**
     * @param languageOverride the language to open in regardless of the saved setting, or
     *                         {@code null} to use the saved one
     */
    public MainView(GaiaConstants.Language languageOverride) {
        this.languageOverride = languageOverride;
        this.scanner = new ScannerService(new FxScannerListener());

        // Load before building: the first layout should already be in the saved language,
        // with the options panel in the state the user left it (closed on a first run).
        OptionsStore.load(options, uiState);
        savedLanguage = options.language();
        // Before the listeners below are attached, so the override costs no rebuild.
        if (languageOverride != null) options.languageProperty().set(languageOverride);
        theme.set(uiState.theme());
        strings = UiStrings.of(options.language());

        // Read before building: applyMode() writes the mode back into uiState, and the
        // chrome calls it — so by the time the constructor got here, the saved value had
        // already been overwritten with the default and Build mode was never restored.
        buildMode.setSelected("BUILD".equals(uiState.mode()));
        combineMode.setSelected("COMBINE".equals(uiState.mode()));

        historyList.setCellFactory(list -> new HistoryCell());
        historyList.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        historyList.getSelectionModel().selectedItemProperty()
                .addListener((obs, was, now) -> render(now));

        getStyleClass().add("main-view");
        setCenter(buildSplit());
        bottomBox.getChildren().setAll(buildStatusBar());
        setBottom(bottomBox);
        buildChrome();

        deviceCombo.setValue(uiState.scannerDevice());
        optionsToggle.setSelected(uiState.optionsPaneOpen());
        applyOptionsVisibility();
        applyTheme();

        // A parser option is a property of the *view* of a capture, not of the capture:
        // re-render the selection so the panel shows its effect immediately.
        options.onChange(this::onOptionChanged);
        // Deferred for the same reason the builder's picker reset is: this fires from inside
        // the language ComboBox's own selection cycle, and applyLanguage() rebuilds the panel
        // that owns that ComboBox. Destroying the control currently dispatching an event is
        // how the popup ends up announcing changes about itself after it has been discarded.
        options.languageProperty().addListener((obs, was, now) -> {
            languageChosenInSession = true;   // a choice made here outranks --language
            Platform.runLater(this::applyLanguage);
        });
        theme.addListener((obs, was, now) -> {
            uiState.theme(now);
            applyTheme();
            persistOptions();
        });
        history.addListener((ListChangeListener<ScanRecord>) c -> persistCaptures());

        loading = false;
        status.setText(strings.get("lbl.ready"));

        restoreCaptures();
        scheduleUpdateCheck();
    }

    // ------------------------------------------------------------------ layout

    /**
     * Builds every part of the window whose text comes from the catalogue. Called again
     * after a language change — the controls themselves are fields and keep their state, so
     * only the containers around them are rebuilt.
     */
    private void buildChrome() {
        // Here rather than in applyLanguage(), which only runs on a language *change*: a saved
        // right-to-left language would otherwise open its first window left-to-right, and only
        // right itself if the user switched language and switched back.
        setNodeOrientation(strings.isRightToLeft()
                ? NodeOrientation.RIGHT_TO_LEFT
                : NodeOrientation.LEFT_TO_RIGHT);

        if (builderPane == null) {
            builderPane = new BuilderPane(options, strings, this::captureBuilt);
        } else {
            builderPane.retext(strings);   // keeps the rows; see BuilderPane.retext
        }
        if (combinePane == null) {
            combinePane = new CombinePane(strings, this::onCombineAction);
        } else {
            combinePane.retext(strings);   // keeps the scans, for the same reason
        }
        parseMode.setText(strings.get("mode.parse"));
        buildMode.setText(strings.get("mode.build"));
        combineMode.setText(strings.get("mode.combine"));
        topBox.getChildren().setAll(buildMenuBar(), buildInputBar());
        setTop(topBox);
        applyMode();
        setRight(null);
        historyList.setPlaceholder(new Label(strings.get("lbl.noCaptures")));
        historyList.setContextMenu(buildHistoryMenu());
        captionLabel.setText(strings.get("lbl.captures"));
        aboutButton.setText(strings.get("act.aboutShort"));
        aboutButton.setTooltip(new Tooltip(strings.get("act.about")));
        setScannerState(scanner.isConnected());
        showUpdateNotice();   // re-labels the notice in the new language, if one is showing
        applyOptionsVisibility();
    }

    private MenuBar buildMenuBar() {
        Menu file = new Menu(strings.get("menu.file"), null,
                item("act.openFile", new KeyCodeCombination(KeyCode.O, KeyCombination.SHORTCUT_DOWN), e -> openTextFile()),
                new SeparatorMenuItem(),
                item("act.saveHtml", new KeyCodeCombination(KeyCode.S, KeyCombination.SHORTCUT_DOWN), e -> saveReportAsHtml()),
                item("act.savePdf",  new KeyCodeCombination(KeyCode.P, KeyCombination.SHORTCUT_DOWN), e -> saveReportAsPdf()),
                new SeparatorMenuItem(),
                item("act.quit", new KeyCodeCombination(KeyCode.Q, KeyCombination.SHORTCUT_DOWN), e -> Platform.exit()));

        Menu samples = new Menu(strings.get("act.samples"));
        for (String[] sample : SAMPLES) {
            // Sample names stay in English: they name GS1 constructs (GTIN, Data Matrix,
            // Digital Link), not prose.
            MenuItem mi = new MenuItem(sample[0]);
            mi.setOnAction(e -> {
                input.setText(sample[1]);
                input.requestFocus();
                input.positionCaret(sample[1].length());
            });
            samples.getItems().add(mi);
        }

        Menu inputMenu = new Menu(strings.get("menu.input"), null,
                item("act.parse", new KeyCodeCombination(KeyCode.ENTER, KeyCombination.SHORTCUT_DOWN), e -> parseFromInputBar()),
                item("act.insertGs", new KeyCodeCombination(KeyCode.G, KeyCombination.SHORTCUT_DOWN), e -> insertGs()),
                new SeparatorMenuItem(),
                samples,
                new SeparatorMenuItem(),
                item("act.clearInput", null, e -> input.clear()),
                item("act.clearHistory", null, e -> clearHistory()));

        Menu copyMenu = new Menu(strings.get("menu.copy"), null,
                item("input", null, e -> copySelected(ScanRecord::input)),
                item("act.hri", null, e -> copySelected(r -> r.result().getAiObject() == null
                        ? null : r.result().getAiObject().toHriString())),
                item("elementString", null, e -> copySelected(r -> r.result().getAiObject() == null
                        ? null : r.result().getAiObject().toElementString())),
                item("canonicalUrl", null, e -> copySelected(r ->
                        r.result().getAiObject() != null && r.result().getAiObject().hasDigitalLink()
                                ? r.result().getAiObject().getCanonicalDigitalLink() : null)),
                new SeparatorMenuItem(),
                item("act.reportHtml", null, e -> copyToClipboard(currentHtml)));

        // Explicit state + handler rather than a bidirectional binding: the menu bar is
        // rebuilt on every language change, and a binding from a discarded menu item would
        // outlive the item that owns it.
        CheckMenuItem darkMode = new CheckMenuItem(strings.get("view.dark"));
        darkMode.setSelected(theme.get() == Theme.DARK);
        darkMode.setAccelerator(new KeyCodeCombination(KeyCode.D, KeyCombination.SHORTCUT_DOWN));
        darkMode.setOnAction(e -> theme.set(darkMode.isSelected() ? Theme.DARK : Theme.LIGHT));

        CheckMenuItem showOptions = new CheckMenuItem(strings.get("view.optionsPanel"));
        showOptions.setSelected(optionsToggle.isSelected());
        showOptions.setOnAction(e -> {
            optionsToggle.setSelected(showOptions.isSelected());
            applyOptionsVisibility();
            persistOptions();
        });

        Menu view = new Menu(strings.get("menu.view"), null, darkMode, showOptions);

        CheckMenuItem autoUpdates = new CheckMenuItem(strings.get("act.autoUpdates"));
        autoUpdates.setSelected(uiState.updateCheck());
        autoUpdates.setOnAction(e -> {
            uiState.updateCheck(autoUpdates.isSelected());
            persistOptions();
        });
        Menu help = new Menu(strings.get("menu.help"), null,
                item("act.checkUpdates", null, e -> checkForUpdates(true)),
                autoUpdates,
                new SeparatorMenuItem(),
                item("act.about", null, e -> showAbout()));

        MenuBar bar = new MenuBar(file, inputMenu, copyMenu, view, help);
        if (scannerSupported) {
            // Before Help, which stays last. ⌘K lives here, so it goes with the menu.
            bar.getMenus().add(bar.getMenus().size() - 1,
                    new Menu(strings.get("menu.scanner"), null,
                            item("act.connect", new KeyCodeCombination(KeyCode.K, KeyCombination.SHORTCUT_DOWN),
                                    e -> scannerToggle.fire())));
        }
        // Use the macOS system menu bar when there is one; a no-op elsewhere.
        bar.setUseSystemMenuBar(true);
        return bar;
    }

    private HBox buildInputBar() {
        input.setPromptText(strings.get("lbl.inputPrompt"));
        input.getStyleClass().add("input-field");
        input.setOnAction(e -> parseFromInputBar());
        installInputBehaviour();
        HBox.setHgrow(input, Priority.ALWAYS);

        Button parse = new Button(strings.get("act.parse"));
        parse.setDefaultButton(true);
        parse.setOnAction(e -> parseFromInputBar());
        parse.getStyleClass().add("accent-button");
        parseButton = parse;

        Button gs = new Button("{GS}");
        gs.setTooltip(new Tooltip("FNC1 · 0x1D"));
        gs.setOnAction(e -> insertGs());
        // A focus-traversable button takes focus when clicked, and a text field that loses
        // focus has its caret reset to 0 — which would make this button always insert at the
        // start of the input. Non-traversable buttons leave the caret where the user put it.
        gs.setFocusTraversable(false);
        gsButton = gs;

        MenuButton save = new MenuButton(strings.get("act.saveReport"), null,
                item("act.asHtml", null, e -> saveReportAsHtml()),
                item("act.asPdf",  null, e -> saveReportAsPdf()));

        // Populate once: this method runs again on every language change, and replacing the
        // items makes the combo clear its selection, which fires onAction with a null value as
        // if the user had chosen nothing. The default stands in until the configured names
        // arrive from loadScannerNames().
        if (scannerSupported && deviceCombo.getItems().isEmpty()) {
            deviceCombo.getItems().setAll(ScannerService.DEFAULT_LOGICAL_NAME);
            loadScannerNames();
        }
        if (deviceCombo.getValue() == null) deviceCombo.setValue(ScannerService.DEFAULT_LOGICAL_NAME);
        deviceCombo.setOnAction(e -> {
            // A cleared selection is not a choice — only record a real one.
            if (deviceCombo.getValue() == null) return;
            uiState.scannerDevice(deviceCombo.getValue());
            persistOptions();
        });

        scannerToggle.setOnAction(e -> toggleScanner());
        optionsToggle.setText(strings.get("lbl.options"));
        optionsToggle.setOnAction(e -> {
            applyOptionsVisibility();
            persistOptions();
        });

        if (parseMode.getToggleGroup() == null) {
            ToggleGroup modes = new ToggleGroup();
            parseMode.setToggleGroup(modes);
            buildMode.setToggleGroup(modes);
            combineMode.setToggleGroup(modes);
            // Only default to Parse when nothing has chosen a mode yet — a restored Build
            // selection is made before the group exists and must survive joining it.
            if (modes.getSelectedToggle() == null) parseMode.setSelected(true);
            // A toggle group would otherwise allow "neither selected" by clicking the active
            // one; the window always has to be in one mode or the other.
            modes.selectedToggleProperty().addListener((obs, was, now) -> {
                if (now == null) was.setSelected(true);
                else applyMode();
            });
        }
        HBox modeBar = new HBox(parseMode, buildMode, combineMode);
        modeBar.getStyleClass().add("mode-bar");

        Region gap = new Region();
        gap.setMinWidth(12);

        // Only the input field may shrink when the window is narrow; a button that gives up
        // width just truncates its label to "P…", which is worse than a shorter text field.
        input.setMinWidth(140);
        for (Region control : new Region[] {gs, parse, save, deviceCombo, scannerToggle, optionsToggle}) {
            control.setMinWidth(Region.USE_PREF_SIZE);
        }
        deviceCombo.setPrefWidth(165);

        HBox bar = scannerSupported
                ? new HBox(8, modeBar, input, gs, parse, save, gap, deviceCombo, scannerToggle, optionsToggle)
                : new HBox(8, modeBar, input, gs, parse, save, gap, optionsToggle);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(10, 12, 10, 12));
        bar.getStyleClass().add("input-bar");
        return bar;
    }

    private SplitPane buildSplit() {
        captionLabel.getStyleClass().add("section-header");
        HBox head = new HBox(captionLabel);
        head.setPadding(new Insets(12, 12, 6, 12));

        VBox left = new VBox(head, historyList);
        VBox.setVgrow(historyList, Priority.ALWAYS);
        left.setMinWidth(200);
        left.getStyleClass().add("history-pane");
        historyPane = left;

        split = new SplitPane(left, webView);
        split.setDividerPositions(0.28);
        SplitPane.setResizableWithParent(left, Boolean.FALSE);
        return split;
    }

    private HBox buildStatusBar() {
        status.getStyleClass().add("status-text");
        scannerState.getStyleClass().add("status-text");

        // Also on Help → About, but that menu moves into the macOS system menu bar, where it
        // is easy to miss, and the toolbar is already full at the window's minimum width.
        // The versions are not repeated beside it: About reports both of them, along with the
        // build timestamp and the GS1 releases, which is more than a status line can carry.
        aboutButton.setOnAction(e -> showAbout());
        aboutButton.getStyleClass().add("status-button");

        Region push = new Region();
        HBox.setHgrow(push, Priority.ALWAYS);

        updateLink.getStyleClass().add("update-link");
        skipLink.getStyleClass().add("update-skip");
        updateBox.setAlignment(Pos.CENTER_LEFT);
        updateBox.setVisible(false);
        updateBox.setManaged(false);

        HBox bar = scannerSupported
                ? new HBox(16, status, push, updateBox, scannerState, aboutButton)
                : new HBox(16, status, push, updateBox, aboutButton);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(6, 12, 6, 12));
        bar.getStyleClass().add("status-bar");
        return bar;
    }

    private ContextMenu buildHistoryMenu() {
        return new ContextMenu(
                item("input", null, e -> copySelected(ScanRecord::input)),
                item("act.hri", null, e -> copySelected(r -> r.result().getAiObject() == null
                        ? null : r.result().getAiObject().toHriString())),
                item("mode.build", null, e -> editSelectedInBuilder()),
                item("act.sendBack", null, e -> {
                    ScanRecord r = selected();
                    if (r != null) input.setText(GsToken.toDisplay(r.input()));
                }),
                new SeparatorMenuItem(),
                item("act.removeCapture", null, e -> {
                    ScanRecord r = selected();
                    if (r != null) history.remove(r);
                }),
                item("act.clearHistory", null, e -> clearHistory()));
    }

    // ------------------------------------------------------------------ parsing

    /** Parses whatever is in the input bar, if anything, and keeps the text for editing. */
    private void parseFromInputBar() {
        String typed = input.getText();
        if (typed == null || typed.isEmpty()) {
            status.setText(strings.get("st.nothingToParse"));
            return;
        }
        // What the field holds is the display form; the parser needs real separators.
        capture(GsToken.toParserInput(typed), ScanSource.MANUAL, null);
        input.selectAll();
    }

    /**
     * Parses one input and appends it to the history as the new selection.
     *
     * <p>The single funnel for every source. Parsing is cheap (sub-millisecond once the AI
     * tables are loaded) and never throws on bad input, so it runs on the FX thread —
     * scanner events hop here via {@code Platform.runLater} first.
     */
    public void capture(String raw, ScanSource source, String symbology) {
        // In Combine mode a scan is a barcode of the label being combined, not a capture of its
        // own. The history is left alone: a legacy label's individual barcodes are incomplete by
        // design, and filing three invalid captures per label would bury the real ones.
        if (combineMode.isSelected()) {
            combineAdd(raw);
            return;
        }

        ParseResult result = PARSER.parse(raw, options.toParseConfig());
        ScanRecord record  = new ScanRecord(raw, source, symbology, result);

        history.add(0, record);
        historyList.getSelectionModel().select(record);
        historyList.scrollTo(0);

        status.setText(summarise(record));
    }

    /** Re-parses the selected capture under the current options and re-renders it. */
    private void reparseSelected() {
        ScanRecord r = selected();
        if (r == null) {
            render(null);
            return;
        }
        int index = history.indexOf(r);
        if (index < 0) return;  // removed from under us
        ScanRecord updated = r.withResult(PARSER.parse(r.input(), options.toParseConfig()));
        history.set(index, updated);
        historyList.getSelectionModel().select(updated);
        status.setText(summarise(updated));
    }

    /** An option changed: re-render under it, and remember it for next time. */
    private void onOptionChanged() {
        if (loading) return;
        // The Combine scans are parses too: a new language, date format or mode must reach the
        // barcodes already taken, not only the next one.
        if (combiner != null) combiner.reparse(options.toParseConfig());
        reparseSelected();
        if (combineMode.isSelected()) afterCombineChange();
        persistOptions();
    }

    private String summarise(ScanRecord r) {
        ParseResult res = r.result();
        int errors   = res.getAiObject() == null ? 0 : res.getAiObject().getAllErrors().size();
        int warnings = res.getAiObject() == null ? 0 : res.getAiObject().getAllWarnings().size();
        return (r.isValid() ? strings.get("valid") : strings.get("invalid"))
                + " · " + r.elementCount() + " " + strings.get("elements")
                + " · " + errors + " " + strings.get("errors")
                + " · " + warnings + " " + strings.get("warnings")
                + " · " + describeOptions();
    }

    /**
     * The parser options as one line for the status bar: mode, message language, date
     * rendering, and any structural check that has been switched off.
     *
     * <p>Here rather than on {@link ParseOptions} because the last two parts are prose and so
     * need the catalogue, which the options model has no business knowing about. The enum
     * names in front of them are the parser's own vocabulary and read the same in every
     * language.
     */
    private String describeOptions() {
        return options.parseModeProperty().get()
                + " · " + Languages.label(options.language())
                + " · " + options.dateEndianProperty().get() + " " + options.dateSeparatorProperty().get()
                + (options.skipRequiresCheckProperty().get() ? " · " + strings.get("opt.noRequiresCheck") : "")
                + (options.skipExcludesCheckProperty().get() ? " · " + strings.get("opt.noExcludesCheck") : "");
    }

    /** Renders a record — or the welcome page when there is no selection — into the WebView. */
    private void render(ScanRecord record) {
        // In Combine mode the pane belongs to the Combine report; a history selection or a
        // re-parse of the hidden capture must not draw over it. Leaving Combine renders the
        // selection again (applyMode), so nothing is lost by skipping it here.
        if (combineMode.isSelected()) return;
        currentHtml = record == null
                ? GaiaDesktopReport.welcome(theme.get(), strings)
                : GaiaDesktopReport.build(record.input(), record.result(), theme.get(), record.provenance(), strings);
        webView.getEngine().loadContent(currentHtml);
    }

    // ------------------------------------------------------------------ persistence

    private void persistOptions() {
        if (loading) return;
        uiState.theme(theme.get());
        uiState.optionsPaneOpen(optionsToggle.isSelected());
        if (deviceCombo.getValue() != null) uiState.scannerDevice(deviceCombo.getValue());
        OptionsStore.save(options, uiState, persistedLanguage());
    }

    /**
     * The language to write to the options file: the saved one while a {@code --language}
     * override is merely being displayed, and the live one once the user has chosen for
     * themselves — or on any ordinary run.
     */
    private GaiaConstants.Language persistedLanguage() {
        return languageOverride != null && !languageChosenInSession
                ? savedLanguage
                : options.language();
    }

    private void persistCaptures() {
        if (loading) return;
        CaptureStore.save(new ArrayList<>(history));
    }

    /**
     * Re-parses the saved history off the FX thread and appends it, oldest-last, below
     * anything captured in the meantime. Doing this in the background also serves as the
     * parser warm-up: the AI tables are loaded before the first interactive parse.
     */
    private void restoreCaptures() {
        Thread t = new Thread(() -> {
            List<CaptureStore.StoredCapture> stored = CaptureStore.load();
            List<ScanRecord> restored = new ArrayList<>(stored.size());
            for (CaptureStore.StoredCapture c : stored) {
                try {
                    restored.add(ScanRecord.restored(c.input(), c.source(), c.symbology(), c.captured(),
                            PARSER.parse(c.input(), options.toParseConfig())));
                } catch (RuntimeException e) {
                    log.warn("Could not re-parse a restored capture: {}", e.toString());
                }
            }
            if (restored.isEmpty()) {
                // Nothing to restore, but the tables are warm now.
                return;
            }
            Platform.runLater(() -> {
                boolean hadSelection = selected() != null;
                history.addAll(restored);
                if (!hadSelection) historyList.getSelectionModel().select(0);
                status.setText(restored.size() + " " + strings.get("st.restored"));
            });
        }, "gaia-restore");
        t.setDaemon(true);
        t.start();
    }

    // ------------------------------------------------------------------ scanner

    private void toggleScanner() {
        if (scannerToggle.isSelected()) {
            String device = deviceCombo.getValue();
            scannerToggle.setDisable(true);
            status.setText(strings.get("st.connecting") + " " + device + "…");
            // connect() waits for the claim, up to 5 s; never on the FX thread. The device calls
            // themselves run on the service's own long-lived thread (see ScannerService).
            Thread t = new Thread(() -> {
                try {
                    scanner.connect(device);
                } catch (Exception e) {
                    log.error("Scanner connect failed", e);
                    Platform.runLater(() -> {
                        scannerToggle.setSelected(false);
                        setScannerState(false);
                        error(strings.get("act.connect"),
                                "\"" + device + "\"\n\n" + e.getMessage()
                                        + "\n\n" + strings.get("msg.scannerConnectFailed"));
                    });
                } finally {
                    Platform.runLater(() -> scannerToggle.setDisable(false));
                }
            }, "gaia-scanner-connect");
            t.setDaemon(true);
            t.start();
        } else {
            // The release itself runs on the service's device thread (see ScannerService); this
            // thread only waits for it, so the window does not. The DISCONNECTED callback
            // relabels the controls.
            scannerToggle.setDisable(true);
            Thread t = new Thread(() -> {
                try {
                    scanner.disconnect();
                } finally {
                    Platform.runLater(() -> scannerToggle.setDisable(false));
                }
            }, "gaia-scanner-disconnect");
            t.setDaemon(true);
            t.start();
        }
    }

    /**
     * Fills the device picker with the scanners JavaPOS loaded from its configuration. Off the FX
     * thread: the names are read on the scanner's device thread, which a connect can keep busy for
     * the length of a claim. The choice already in the picker — saved from the last run, or
     * picked meanwhile — is kept, even if the configuration no longer lists it.
     */
    private void loadScannerNames() {
        Thread t = new Thread(() -> {
            List<String> names = scanner.logicalNames();
            Platform.runLater(() -> {
                String chosen = deviceCombo.getValue();
                deviceCombo.getItems().setAll(names);
                deviceCombo.setValue(chosen != null ? chosen : names.get(0));
            });
        }, "gaia-scanner-names");
        t.setDaemon(true);
        t.start();
    }

    private void setScannerState(boolean connected) {
        if (!scannerSupported) return;   // none of these controls are in the scene
        scannerState.setText(connected
                ? strings.format("lbl.scannerOn", scanner.logicalName())
                : strings.get("lbl.scannerOff"));
        scannerToggle.setText(connected ? strings.get("act.disconnect") : strings.get("act.connect"));
        deviceCombo.setDisable(connected);
    }

    /**
     * The status line for a scanner event, in the current language.
     *
     * <p>The wording lives here rather than in {@link ScannerService} because this is the side
     * that has a catalogue: the service reports what happened, the window says it. Each case
     * holds its key as a literal so the catalogue-coverage test can see it.
     */
    private String scannerMessage(ScannerListener.Event event, String... details) {
        Object[] args = details;
        switch (event) {
            case CONNECTED:      return strings.format("st.scannerConnected", args);
            case DISCONNECTED:   return strings.get("st.scannerDisconnected");
            case DEVICE_STATUS:  return strings.format("st.scannerStatus", args);
            case DEVICE_ERROR:   return strings.format("st.scannerError", args);
            case READ_FAILED:    return strings.format("st.scanReadFailed", args);
            case HANDLER_FAILED: return strings.format("st.scanHandlerFailed", args);
            default:             return String.valueOf(event);
        }
    }

    /** Bridges {@link ScannerService} callbacks (JavaPOS thread) onto the FX thread. */
    private final class FxScannerListener implements ScannerListener {

        @Override
        public void onScan(String barcode, String symbology) {
            Platform.runLater(() -> capture(barcode, ScanSource.SCANNER, symbology));
        }

        @Override
        public void onStatus(Event event, boolean connected, String... details) {
            Platform.runLater(() -> {
                status.setText(scannerMessage(event, details));
                scannerToggle.setSelected(connected);
                setScannerState(connected);
            });
        }

        @Override
        public void onError(Event event, String... details) {
            Platform.runLater(() -> status.setText(scannerMessage(event, details)));
        }
    }

    // ------------------------------------------------------------------ file I/O

    /** Reads a text file and parses each non-blank line as its own capture. */
    private void openTextFile() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle(strings.get("act.openFile"));
        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Text", "*.txt", "*.log", "*.csv"),
                new FileChooser.ExtensionFilter("All", "*.*"));
        File file = chooser.showOpenDialog(window());
        if (file == null) return;

        try {
            // ISO-8859-1, not UTF-8: captured scanner output is bytes, and a raw 0x1D
            // separator has to survive the round trip through the file unchanged.
            List<String> lines = Files.readAllLines(file.toPath(), StandardCharsets.ISO_8859_1);
            int parsed = 0;
            for (String line : lines) {
                if (line.trim().isEmpty()) continue;
                capture(line, ScanSource.FILE, null);
                parsed++;
            }
            status.setText(strings.format("st.parsedFile", parsed, file.getName()));
        } catch (IOException e) {
            log.error("Could not read {}", file, e);
            error(strings.get("act.openFile"), e.getMessage());
        }
    }

    /** Writes the report currently on screen to an HTML file the user picks. */
    private void saveReportAsHtml() {
        if (!hasReport()) return;

        FileChooser chooser = new FileChooser();
        chooser.setTitle(strings.get("act.saveHtml"));
        chooser.setInitialFileName(GaiaDesktopReport.suggestedFileName(".html"));
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("HTML", "*.html", "*.htm"));
        File file = chooser.showSaveDialog(window());
        if (file == null) return;

        Path path = withExtension(file.getPath(), ".html");
        try {
            GaiaDesktopReport.writeTo(path, currentHtml);
            status.setText(strings.get("st.savedTo") + " " + path);
        } catch (IOException e) {
            log.error("Could not write {}", path, e);
            error(strings.get("act.saveHtml"), e.getMessage());
        }
    }

    /**
     * Writes the report currently on screen to a PDF.
     *
     * <p>JavaFX has no API that renders a page straight to a PDF file, so this goes through
     * the platform's print pipeline — {@code WebEngine.print} lays the document out with the
     * report's {@code @media print} rules and hands paginated, text-selectable pages to the
     * printer job. The user picks the PDF destination in the system dialog ("PDF ▸ Save as
     * PDF" on macOS, "Microsoft Print to PDF" on Windows, "Print to File" on Linux/CUPS),
     * which is also where the file name is chosen — hence a print dialog rather than a save
     * dialog here.
     */
    private void saveReportAsPdf() {
        if (!hasReport()) return;

        PrinterJob job = PrinterJob.createPrinterJob();
        if (job == null) {
            error(strings.get("act.savePdf"), strings.get("msg.noPrintDestination"));
            return;
        }

        ScanRecord r = selected();
        job.getJobSettings().setJobName("GAIA " + r.time());

        // Choose the destination — and, for a PDF destination, the file name — in the
        // system dialog. Cancelling there must cancel the job, or it stays open.
        if (!job.showPrintDialog(window())) {
            job.cancelJob();
            status.setText(strings.get("st.cancelled"));
            return;
        }

        webView.getEngine().print(job);
        boolean done = job.endJob();
        String destination = job.getPrinter() != null ? job.getPrinter().getName() : "?";
        if (done) {
            status.setText(strings.get("st.sentTo") + " " + destination);
        } else {
            log.warn("Print job did not complete (destination '{}')", destination);
            error(strings.get("act.savePdf"), strings.format("msg.printJobFailed", "\"" + destination + "\""));
        }
    }

    /** True when there is a rendered report to export; sets the status line when there is not. */
    private boolean hasReport() {
        if (currentHtml.isEmpty() || selected() == null) {
            status.setText(strings.get("st.nothingToSave"));
            return false;
        }
        return true;
    }

    /**
     * The chosen path, with {@code extension} appended when the user typed a bare name.
     * The extension filter only pre-selects a type; on most platforms nothing stops the
     * user from confirming a name without one.
     */
    private static Path withExtension(String path, String extension) {
        String name = Path.of(path).getFileName().toString();
        return name.lastIndexOf('.') > 0 ? Path.of(path) : Path.of(path + extension);
    }

    // ------------------------------------------------------------------ misc UI

    /**
     * Shows the strip that belongs to the current mode.
     *
     * <p>Only the strip under the toolbar changes: the history, the report, the options
     * panel and the export actions serve both modes, because a built string becomes an
     * ordinary capture the moment it succeeds.
     */
    private void applyMode() {
        boolean building  = buildMode.isSelected();
        boolean combining = combineMode.isSelected();

        // The parse-only controls: hidden *and* unmanaged, so they give their space back.
        // The scanner pair goes too — it belongs to capturing input, not to composing it.
        // Where the platform has no scanner support those two were never added to the bar, and
        // setting visibility on a node outside the scene is simply a no-op.
        // A connected scanner is not stranded by that: Scanner → Connect/disconnect (⌘K)
        // still toggles it, the status bar still reports it, and a scan arriving while the
        // builder is open is still captured.
        // Combine keeps the scanner pair — a scan is its main way in — but not the parse input
        // bar, which would otherwise look like the place to type a barcode.
        for (Node n : new Node[] {input, gsButton, parseButton}) {
            if (n == null) continue;
            n.setVisible(!building && !combining);
            n.setManaged(!building && !combining);
        }
        for (Node n : new Node[] {deviceCombo, scannerToggle}) {
            if (n == null) continue;
            n.setVisible(!building);
            n.setManaged(!building);
        }

        if (building) {
            if (!topBox.getChildren().contains(builderPane)) topBox.getChildren().add(builderPane);
        } else {
            topBox.getChildren().remove(builderPane);
        }

        if (combining) {
            // The report fills the window and the strip runs along the
            // bottom. The capture history steps aside — Combine scans never enter it.
            if (!bottomBox.getChildren().contains(combinePane)) bottomBox.getChildren().add(0, combinePane);
            split.getItems().remove(historyPane);
            combinePane.setHasScans(combiner != null && !combiner.scans().isEmpty());
            renderCombine();
        } else {
            bottomBox.getChildren().remove(combinePane);
            if (!split.getItems().contains(historyPane)) {
                split.getItems().add(0, historyPane);
                split.setDividerPositions(0.28);
            }
            // Leaving Combine puts the window back on whatever the history has selected, so the
            // report pane never keeps showing a strip that is no longer on screen.
            if (!loading) render(selected());
        }

        if (!loading) {
            uiState.mode(building ? "BUILD" : combining ? "COMBINE" : "PARSE");
            persistOptions();
        }
    }

    // ------------------------------------------------------------------ Combine mode

    /** Lazily built, so Parse and Build users never pay for a combiner they do not open. */
    private ScanCombiner combiner() {
        if (combiner == null) {
            // No input modifiers: the barcodes are combined exactly as they read, which is the
            // whole point of validating a legacy label.
            combiner = new ScanCombiner(PARSER, options.toParseConfig());
        }
        return combiner;
    }

    private void onCombineAction(CombinePane.Action action) {
        switch (action) {
            case ADD:
                String typed = combinePane.takeTyped();
                if (typed != null) combineAdd(typed);
                break;
            case REMOVE_LAST:
                combiner().removeLast();
                lastRefusal = null;
                afterCombineChange();
                break;
            case RESET:
                combiner().clear();
                combinePane.clearEntry();
                lastRefusal = null;
                afterCombineChange();
                break;
            default:
                break;
        }
    }

    /**
     * Offers one barcode to the combiner. The single path for both doors — the entry field and the
     * scanner — so a refusal reads the same either way. FX thread.
     */
    private void combineAdd(String barcode) {
        ScanCombiner.Added added = combiner().add(barcode);
        lastRefusal = added.accepted() ? null : added.refusal();
        afterCombineChange();
    }

    private void afterCombineChange() {
        combinePane.setHasScans(!combiner().scans().isEmpty());
        renderCombine();
        showCombineStatus();
    }

    /** The status line for Combine mode: the last refusal, or how many barcodes are held. */
    private void showCombineStatus() {
        status.setText(lastRefusal != null
                ? GaiaCombineReport.resolve(lastRefusal, strings)
                : strings.format("combine.ofMax", combiner().scans().size(), ScanCombiner.MAX_SCANS));
    }

    private void renderCombine() {
        ScanCombiner c = combiner();
        currentHtml = c.scans().isEmpty() && lastRefusal == null
                ? GaiaCombineReport.idle(theme.get(), strings)
                : GaiaCombineReport.build(c.scans(), c.combine(), lastRefusal, theme.get(), strings);
        webView.getEngine().loadContent(currentHtml);
    }

    /**
     * A successful build, handed back from the editor.
     *
     * <p>It goes through {@link #capture} like anything else: the builder validates by
     * parsing its own output, so the string is known to parse, and routing it here is what
     * gives a build the same report, history entry, export and persistence as a scan.
     */
    private void captureBuilt(String built) {
        capture(built, ScanSource.BUILT, null);
        status.setText(strings.get("st.built") + " · " + summarise(selected()));
    }

    /** Opens the selected capture's AIs in the build editor, ready to change and rebuild. */
    private void editSelectedInBuilder() {
        ScanRecord r = selected();
        if (r == null || r.result().getAiObject() == null) {
            status.setText(strings.get("st.nothingSelected"));
            return;
        }
        List<String[]> pairs = new ArrayList<>();
        r.result().getAiObject().getAis()
                .forEach(e -> pairs.add(new String[] {e.getAi(), e.getValue()}));
        buildMode.setSelected(true);
        applyMode();
        builderPane.setRows(pairs);
    }

    /**
     * Inserts the FNC1 separator at the caret — as the {@code {GS}} token, because a text
     * field cannot hold the raw byte.
     */
    private void insertGs() {
        // Only trust the live caret while the field holds focus; otherwise it has already
        // been reset to 0 by the focus change, and the position the user actually chose is
        // the one recorded while they were still typing.
        int caret = input.isFocused()
                ? input.getCaretPosition()
                : Math.min(lastCaret, input.getLength());

        input.insertText(caret, GsToken.TOKEN);
        input.requestFocus();
        input.positionCaret(caret + GsToken.TOKEN.length());
    }

    /**
     * Rewrites any real {@code 0x1D} to the {@code {GS}} token on its way into the field.
     *
     * <p>A {@code TextFormatter} filter runs before the control's own character filtering, so
     * this is the one place that catches every route text can arrive by — typing, ⌘V, the
     * context menu's Paste, drag and drop, and {@code setText} — instead of the separator
     * being silently dropped by each of them in turn.
     */
    private void installInputBehaviour() {
        if (input.getTextFormatter() != null) return;  // buildInputBar runs again per language

        input.setTextFormatter(new TextFormatter<>(change -> {
            if (GsToken.containsSeparator(change.getText())) {
                change.setText(GsToken.toDisplay(change.getText()));
            }
            return change;
        }));

        // Remember where the caret was while the field still had focus. Guarded on focus so
        // the reset-to-0 that accompanies losing focus is never recorded as a real position.
        input.caretPositionProperty().addListener((obs, was, now) -> {
            if (input.isFocused()) lastCaret = now.intValue();
        });
    }

    private void clearHistory() {
        history.clear();
        historyList.getSelectionModel().clearSelection();
        CaptureStore.clear();
        render(null);
        status.setText(strings.get("st.historyCleared"));
    }

    private void applyOptionsVisibility() {
        setRight(optionsToggle.isSelected()
                ? new OptionsPane(options, strings, this::reparseSelected)
                : null);
    }

    /** Pushes the current theme to both the JavaFX chrome and the report in the WebView. */
    private void applyTheme() {
        for (Theme t : Theme.values()) {
            getStyleClass().remove(t.styleClass());
        }
        getStyleClass().add(theme.get().styleClass());
        if (combineMode.isSelected()) {
            renderCombine();
        } else {
            render(selected());
        }
    }

    /**
     * Switches every label in the window — and the report — to the newly chosen language.
     *
     * <p>The controls that hold state (input field, history, toggles) are fields and are
     * reused; only the containers whose text comes from the catalogue are rebuilt.
     */
    private void applyLanguage() {
        strings = UiStrings.of(options.language());
        buildChrome();   // sets the orientation for the new language
        if (combineMode.isSelected()) {
            // buildChrome() has re-labelled the strip and redrawn the Combine report in the new
            // language (applyMode); the scans themselves were re-parsed in it by onOptionChanged.
            showCombineStatus();
            return;
        }
        ScanRecord sel = selected();
        status.setText(sel != null ? summarise(sel) : strings.get("lbl.ready"));
        render(sel);
    }

    private ScanRecord selected() {
        return historyList.getSelectionModel().getSelectedItem();
    }

    private void copySelected(Function<ScanRecord, String> extractor) {
        ScanRecord r = selected();
        if (r == null) {
            status.setText(strings.get("st.nothingSelected"));
            return;
        }
        copyToClipboard(extractor.apply(r));
    }

    private void copyToClipboard(String value) {
        if (value == null || value.isEmpty()) {
            status.setText(strings.get("st.nothingSelected"));
            return;
        }
        ClipboardContent content = new ClipboardContent();
        content.putString(value);
        Clipboard.getSystemClipboard().setContent(content);
        status.setText(strings.get("st.copied"));
    }

    /** Where each project's source lives, linked from the About box. */
    static final String GAIA_URL         = "https://github.com/mgsmith-parsers/gaia";
    static final String GAIA_DESKTOP_URL = "https://github.com/mgsmith-parsers/gaia-desktop";

    /**
     * Opens links in the user's browser. Only the {@code Application} can hand these out, so
     * {@code GaiaDesktopApp} passes them in after building the view; until it does — and in any
     * code that builds a MainView on its own — a link is copied to the clipboard instead.
     */
    private HostServices hostServices;

    /** Lets the About box open links in the user's browser. */
    public void setHostServices(HostServices hostServices) {
        this.hostServices = hostServices;
    }

    private void showAbout() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        styleDialog(alert);
        alert.setTitle(strings.get("act.about"));
        // The name only: the version is the first line of the list below, beside its build time
        // and the parser's, and the header repeating it read as the same line twice.
        alert.setHeaderText("GAIA Desktop");

        // A content node rather than setContentText: plain text cannot carry a link. The two
        // project names are names, not words, so like the rest of this box they need no
        // catalogue entry.
        Label versions = new Label("GAIA Desktop  " + GaiaDesktopVersion.VERSION
                + "  (built " + GaiaDesktopVersion.TIMESTAMP + ")\n"
                + "GAIA Parser   " + GaiaVersion.VERSION + "\n\n"
                + "GS1 General Specifications Release 26.0\n"
                + "GS1 Digital Link: URI Syntax Release 1.7.0");
        versions.setWrapText(true);

        Region gap = new Region();
        gap.setMinHeight(8);
        VBox content = new VBox(4, versions, gap,
                sourceLink("GAIA", GAIA_URL),
                sourceLink("GAIA Desktop", GAIA_DESKTOP_URL));
        alert.getDialogPane().setContent(content);

        alert.getButtonTypes().setAll(ButtonType.CLOSE);
        alert.showAndWait();
    }

    /** One "name — link" line of the About box. */
    private HBox sourceLink(String project, String url) {
        Hyperlink link = new Hyperlink(url);
        link.setOnAction(e -> openLink(url));
        // Hyperlink pads itself to look clickable; flush with the label beside it reads better.
        link.setPadding(Insets.EMPTY);

        Label name = new Label(project);
        name.setMinWidth(Region.USE_PREF_SIZE);
        HBox line = new HBox(10, name, link);
        line.setAlignment(Pos.BASELINE_LEFT);
        return line;
    }

    // ------------------------------------------------------------------ update check

    /**
     * Checks GitHub for a newer release a few seconds after start-up — once the window is up and
     * the history restored — when the user has not switched the check off and the last one was
     * at least a day ago. A development build, whose version is "unknown", never asks.
     */
    private void scheduleUpdateCheck() {
        if (!uiState.updateCheck()
                || !UpdateChecker.isRelease(GaiaDesktopVersion.VERSION)
                || !UpdateChecker.isDue(uiState.lastUpdateCheck(), Instant.now())) {
            return;
        }
        PauseTransition delay = new PauseTransition(javafx.util.Duration.seconds(5));
        delay.setOnFinished(e -> checkForUpdates(false));
        delay.play();
    }

    /**
     * Asks GitHub for the latest release on a background thread, then reports on the FX thread.
     * {@code manual} is Help → Check for updates: it says what it found either way, and offers
     * a release even if the user skipped it. An automatic check speaks only when there is
     * something newer, and stays silent when it fails — a status line about a firewall is not
     * what someone scanning barcodes needs.
     */
    private void checkForUpdates(boolean manual) {
        if (manual) status.setText(strings.get("st.checkingUpdates"));
        Thread t = new Thread(() -> {
            Optional<UpdateChecker.Release> latest = Optional.empty();
            String failure = null;
            try {
                latest = updates.fetchLatest();
            } catch (IOException | RuntimeException e) {
                failure = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
                log.info("Update check failed: {}", failure);
            }
            Optional<UpdateChecker.Release> found = latest;
            String error = failure;
            Platform.runLater(() -> onUpdateChecked(found, error, manual));
        }, "gaia-update-check");
        t.setDaemon(true);
        t.start();
    }

    private void onUpdateChecked(Optional<UpdateChecker.Release> latest, String error, boolean manual) {
        uiState.lastUpdateCheck(Instant.now());
        persistOptions();

        if (error != null) {
            if (manual) status.setText(strings.format("st.updateFailed", error));
            return;
        }
        UpdateChecker.Release release = latest.orElse(null);
        boolean newer = release != null && UpdateChecker.isNewer(release.version(), GaiaDesktopVersion.VERSION);
        if (newer && (manual || !release.version().equals(uiState.skippedVersion()))) {
            log.info("GAIA Desktop {} is available ({})", release.version(), release.pageUrl());
            availableRelease = release;
            showUpdateNotice();
            if (manual) status.setText(strings.format("st.updateAvailable", release.version()));
        } else if (manual) {
            status.setText(strings.format("st.upToDate", GaiaDesktopVersion.VERSION));
        }
    }

    /** Shows, or re-labels after a language change, the notice for {@link #availableRelease}. */
    private void showUpdateNotice() {
        UpdateChecker.Release release = availableRelease;
        boolean show = release != null;
        updateBox.setVisible(show);
        updateBox.setManaged(show);
        if (!show) return;

        updateLink.setText(strings.format("st.updateAvailable", release.version()));
        updateLink.setTooltip(new Tooltip(release.pageUrl()));
        updateLink.setOnAction(e -> openLink(release.pageUrl()));
        skipLink.setText(strings.get("act.skipVersion"));
        skipLink.setOnAction(e -> {
            uiState.skippedVersion(release.version());
            persistOptions();
            availableRelease = null;
            showUpdateNotice();
        });
    }

    private void openLink(String url) {
        if (hostServices != null) {
            hostServices.showDocument(url);
            return;
        }
        ClipboardContent content = new ClipboardContent();
        content.putString(url);
        Clipboard.getSystemClipboard().setContent(content);
        status.setText(strings.get("st.copied"));
    }

    /**
     * Gives a dialog the application stylesheet and the current theme class — dialogs live
     * in their own scene and inherit neither from the owner window.
     */
    private void styleDialog(Alert alert) {
        alert.initOwner(window());
        if (getScene() != null) {
            alert.getDialogPane().getStylesheets().setAll(getScene().getStylesheets());
        }
        alert.getDialogPane().getStyleClass().removeAll(Theme.LIGHT.styleClass(), Theme.DARK.styleClass());
        alert.getDialogPane().getStyleClass().add(theme.get().styleClass());
    }

    private void error(String header, String detail) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        styleDialog(alert);
        alert.setTitle("GAIA Desktop");
        alert.setHeaderText(header);
        alert.setContentText(detail);
        alert.showAndWait();
        status.setText(header);
    }

    private Window window() {
        return getScene() != null ? getScene().getWindow() : null;
    }

    /** A menu item whose text comes from the catalogue under {@code key}. */
    private MenuItem item(String key, KeyCombination accelerator, EventHandler<ActionEvent> action) {
        MenuItem mi = new MenuItem(strings.get(key));
        if (accelerator != null) mi.setAccelerator(accelerator);
        mi.setOnAction(action);
        return mi;
    }

    /** The theme currently applied — the app uses it to style the window chrome. */
    public ObjectProperty<Theme> themeProperty() { return theme; }

    /** Releases the scanner and writes the final state. Called from {@code Application.stop()}. */
    public void shutdown() {
        scanner.disconnect();
        persistOptions();
        persistCaptures();
    }
}
