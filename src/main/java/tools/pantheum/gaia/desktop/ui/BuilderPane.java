package tools.pantheum.gaia.desktop.ui;

import tools.pantheum.gaia.desktop.build.AiPair;
import tools.pantheum.gaia.desktop.build.ElementBuilder;
import tools.pantheum.gaia.desktop.i18n.UiStrings;
import tools.pantheum.gaia.desktop.parse.ParseOptions;
import tools.pantheum.gaia.result.BuildResult;
import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * The build editor: a table of AI/value pairs, an optional Digital Link target, and the two
 * build buttons.
 *
 * <p>Unlike the rest of the chrome, this pane is <b>re-labelled</b> on a language change
 * rather than rebuilt ({@link #retext}): it holds the user's half-finished work, and
 * rebuilding it would throw the AI rows away mid-edit.
 *
 * <p>It does not render anything itself. A successful build is handed to {@code onBuilt},
 * which the app funnels through {@code capture(...)} — the same path a scan or a paste takes
 * — so the built string gets the ordinary parse report, history entry, export and
 * persistence with no second rendering path. A failed build stays here, next to the rows
 * that caused it.
 */
public final class BuilderPane extends VBox {

    /**
     * The rows, with an extractor so the list also reports a row's <em>own</em> edits, not
     * just additions and removals. Two things depend on that: the trailing blank row below,
     * and the red marking of an unrecognised AI — a row editing its code in place is not a
     * list change, so without the extractor the marking only appeared when something else
     * happened to the list.
     */
    private final ObservableList<AiPair> rows = FXCollections.observableArrayList(
            pair -> new Observable[] {pair.aiProperty(), pair.valueProperty()});
    private final TableView<AiPair> table = new TableView<>(rows);
    private final Label message = new Label();

    private final TextField domain     = new TextField();
    private final TextField pathPrefix = new TextField();

    // Every control whose text comes from the catalogue, kept so a language change can
    // relabel it in place. Adding a labelled control here cannot lose user data; recreating
    // the pane could, which is why that is not what happens.
    private final TableColumn<AiPair, String> aiCol     = new TableColumn<>();
    private final TableColumn<AiPair, String> valueCol  = new TableColumn<>();
    private final TableColumn<AiPair, String> titleCol  = new TableColumn<>();
    private final TableColumn<AiPair, String> formatCol = new TableColumn<>();
    private final TableColumn<AiPair, String> descCol   = new TableColumn<>();
    private final ComboBox<String> picker = new ComboBox<>();
    private final Button addButton    = new Button();
    private final Button removeButton = new Button();
    private final Button clearButton  = new Button();
    private final Button elementButton = new Button();
    private final Button dlButton      = new Button();
    private final Label  caption     = new Label();
    private final Label  domainLabel = new Label();
    private final Label  prefixLabel = new Label();
    private final Label  placeholder = new Label();
    private final TitledPane dlPane  = new TitledPane();

    /** Marks a row whose AI is unrecognised or duplicated; styled by {@code .table-row-cell:bad}. */
    private static final PseudoClass BAD_ROW = PseudoClass.getPseudoClass("bad");

    private final ParseOptions options;
    private UiStrings s;
    private final Consumer<String> onBuilt;

    public BuilderPane(ParseOptions options, UiStrings s, Consumer<String> onBuilt) {
        this.options = options;
        this.s       = s;
        this.onBuilt = onBuilt;

        getStyleClass().add("builder-pane");
        setPadding(new Insets(10, 12, 10, 12));
        setSpacing(8);

        buildTable();
        message.getStyleClass().add("builder-message");
        message.setWrapText(true);
        message.setMaxWidth(Double.MAX_VALUE);

        getChildren().addAll(buildRowBar(), table, buildDigitalLinkConfig(), buildActionBar(), message);
        retext(s);
        rows.add(new AiPair());
    }

    // ------------------------------------------------------------------ layout

    private void buildTable() {
        aiCol.setCellValueFactory(c -> c.getValue().aiProperty());
        // EditCell, not TextFieldTableCell: the stock cell cancels the edit on Tab and on
        // losing focus, which quietly drops whatever was typed.
        aiCol.setCellFactory(c -> new EditCell<>(editableColumns()));
        aiCol.setOnEditCommit(e -> e.getRowValue().aiProperty().set(e.getNewValue().trim()));

        valueCol.setCellValueFactory(c -> c.getValue().valueProperty());
        valueCol.setCellFactory(c -> new EditCell<>(editableColumns()));
        valueCol.setOnEditCommit(e -> e.getRowValue().valueProperty().set(e.getNewValue()));

        // Read-only, and derived from the AI code: the moment a recognised code is typed the
        // row says what it is, which is most of the guidance a builder needs.
        titleCol.setCellValueFactory(c -> c.getValue().titleProperty());
        titleCol.setEditable(false);

        formatCol.setCellValueFactory(c -> c.getValue().formatProperty());
        formatCol.setEditable(false);

        // The description is the one derived column that also depends on the language, which
        // the row itself knows nothing about. A plain wrapper rather than a binding: a
        // binding created per cell would register a listener on the language property every
        // time the table re-rendered and never release it. Recomputed by table.refresh(),
        // which already runs when a code is edited and now also on a language change.
        descCol.setCellValueFactory(c -> new ReadOnlyStringWrapper(
                ElementBuilder.descriptionOf(c.getValue().ai(), options.language())));
        descCol.setEditable(false);

        // Shares set together so they can be read against each other; the constrained
        // resize policy scales them to the available width.
        aiCol.setPrefWidth(70);
        valueCol.setPrefWidth(240);
        titleCol.setPrefWidth(170);
        formatCol.setPrefWidth(95);
        descCol.setPrefWidth(300);

        table.getColumns().addAll(List.of(aiCol, valueCol, titleCol, formatCol, descCol));
        table.setEditable(true);
        table.setPrefHeight(150);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.setPlaceholder(placeholder);

        // An unrecognised AI marks its row rather than waiting for the build to fail.
        table.setRowFactory(t -> new TableRow<AiPair>() {
            @Override protected void updateItem(AiPair item, boolean empty) {
                super.updateItem(item, empty);
                boolean bad = !empty && item != null && !item.isEmpty()
                        && (!item.isKnown() || isDuplicate(item));
                pseudoClassStateChanged(BAD_ROW, bad);
            }
        });

        rows.addListener((ListChangeListener<AiPair>) c -> {
            table.refresh();                                  // restyle rows whose code changed
            // Not inline: the list is mid-notification here, and adding to it now would
            // mutate it while the change is still being delivered.
            Platform.runLater(this::ensureTrailingBlankRow);
        });
    }

    /** The columns Tab walks between — the two the user types into, in visual order. */
    private List<TableColumn<AiPair, ?>> editableColumns() {
        return List.of(aiCol, valueCol);
    }

    private HBox buildRowBar() {
        picker.setItems(FXCollections.observableArrayList(ElementBuilder.catalogue()));
        picker.setPrefWidth(280);
        picker.setVisibleRowCount(14);
        picker.setOnAction(e -> {
            String label = picker.getValue();
            if (label == null) return;

            // Reset on the next pulse, never here. This handler runs from inside the popup
            // list's own clearAndSelect: that call has already chosen the row and is about to
            // announce it, so emptying the selection now makes the announcement describe an
            // addition to a list that is suddenly empty —
            // IndexOutOfBoundsException [fromIndex: 0, toIndex: 1, size: 0] out of
            // ListViewBehavior. Letting the popup finish first costs nothing.
            Platform.runLater(() -> {
                picker.getSelectionModel().clearSelection();
                picker.setValue(null);
            });

            String ai = ElementBuilder.aiFromLabel(label);
            AiPair existing = rowWithAi(ai);
            if (existing != null) {
                // An AI may appear once in an element string (the core rejects a repeat with
                // GE-S004), so adding a second row for it could only ever fail. Point at the
                // row that already has it instead — that is the one they meant to edit.
                showFailure(s.format("build.duplicateAi", "(" + ai + ")"));
                table.getSelectionModel().select(existing);
                table.scrollTo(existing);
                return;
            }
            addRow(new AiPair(ai, ""));
        });

        addButton.setOnAction(e -> addRow(new AiPair()));
        removeButton.setOnAction(e -> {
            AiPair selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) rows.remove(selected);
        });
        clearButton.setOnAction(e -> {
            rows.setAll(new AiPair());
            message.setText("");
        });

        caption.getStyleClass().add("section-header");

        HBox bar = new HBox(8, caption, picker, addButton, removeButton, clearButton);
        bar.setAlignment(Pos.CENTER_LEFT);
        return bar;
    }

    private TitledPane buildDigitalLinkConfig() {
        domain.setPromptText("id.gs1.org");
        domain.setPrefWidth(220);
        pathPrefix.setPromptText("/");
        pathPrefix.setPrefWidth(160);

        HBox fields = new HBox(8, domainLabel, domain, prefixLabel, pathPrefix);
        fields.setAlignment(Pos.CENTER_LEFT);
        fields.setPadding(new Insets(6, 0, 2, 0));

        dlPane.setContent(fields);
        dlPane.setExpanded(false);   // only matters for the Digital Link button
        dlPane.getStyleClass().add("builder-dl");
        return dlPane;
    }

    private HBox buildActionBar() {
        elementButton.getStyleClass().add("accent-button");
        elementButton.setOnAction(e -> build(false));
        dlButton.setOnAction(e -> build(true));

        Region push = new Region();
        HBox.setHgrow(push, Priority.ALWAYS);

        HBox bar = new HBox(8, elementButton, dlButton, push);
        bar.setAlignment(Pos.CENTER_LEFT);
        return bar;
    }

    /** The first row carrying {@code ai}, or {@code null} when none does. */
    private AiPair rowWithAi(String ai) {
        for (AiPair row : rows) {
            if (row.ai().trim().equalsIgnoreCase(ai.trim())) return row;
        }
        return null;
    }

    /**
     * Whether this row's AI appears on another row too.
     *
     * <p>Both rows are marked rather than just the later one: from the outside there is no
     * "second" one, and the user has to decide which to keep.
     */
    private boolean isDuplicate(AiPair row) {
        String ai = row.ai().trim();
        if (ai.isEmpty()) return false;
        int seen = 0;
        for (AiPair other : rows) {
            if (ai.equalsIgnoreCase(other.ai().trim()) && ++seen > 1) return true;
        }
        return false;
    }

    /**
     * Keeps exactly one blank row at the end: type in it and the next one appears, so a
     * multi-AI string never needs "add row", and the editor is never a wall of empty rows
     * waiting to be filled.
     */
    private void ensureTrailingBlankRow() {
        if (rows.isEmpty() || !rows.get(rows.size() - 1).isEmpty()) {
            rows.add(new AiPair());
            return;
        }
        // Trim any blank rows above the last one — they can be left behind by clearing a row
        // in the middle, and they are exactly what this method exists to prevent.
        for (int i = rows.size() - 2; i >= 0; i--) {
            if (rows.get(i).isEmpty()) rows.remove(i);
        }
    }

    private void addRow(AiPair pair) {
        rows.add(pair);
        table.getSelectionModel().select(pair);
        table.scrollTo(pair);
    }

    // ------------------------------------------------------------------ building

    /** Builds from the non-blank rows and hands a success to the app; a failure stays here. */
    private void build(boolean digitalLink) {
        List<String[]> pairs = new ArrayList<>();
        for (AiPair row : rows) {
            if (!row.isEmpty()) pairs.add(row.toPair());
        }
        if (pairs.isEmpty()) {
            showFailure(s.get("build.nothingToBuild"));
            return;
        }

        // Caught here, in the language the user is running in. ElementBuilder guards against
        // unknown AIs too, but its message is built without a catalogue — that guard is for
        // callers outside the UI, and this is the one the user should ever see.
        List<String> unknown = new ArrayList<>();
        for (String[] pair : pairs) {
            if (!ElementBuilder.isKnownAi(pair[0])) unknown.add('(' + pair[0] + ')');
        }
        if (!unknown.isEmpty()) {
            showFailure(s.format("build.unknownAi", String.join(", ", unknown)));
            return;
        }

        BuildResult result = digitalLink
                ? ElementBuilder.buildDigitalLink(pairs, options.language(), domain.getText(), pathPrefix.getText())
                : ElementBuilder.buildElementString(pairs, options.language());

        if (result.isSuccess()) {
            message.getStyleClass().remove("bad");
            message.setText("");
            onBuilt.accept(result.getValue());
        } else {
            showFailure(ElementBuilder.describeFailure(result));
        }
    }

    private void showFailure(String text) {
        if (!message.getStyleClass().contains("bad")) message.getStyleClass().add("bad");
        message.setText(text);
    }

    /**
     * Switches every label to {@code strings}, leaving the AI rows, the Digital Link fields
     * and the selection exactly as they are.
     *
     * <p>This is why the pane is not rebuilt on a language change: the rows are the user's
     * work in progress, and changing the language of the window is not a reason to discard
     * them.
     */
    public void retext(UiStrings strings) {
        this.s = strings;

        aiCol.setText(s.get("build.colAi"));
        valueCol.setText(s.get("value"));
        titleCol.setText(s.get("colTitle"));
        formatCol.setText(s.get("build.colFormat"));
        descCol.setText(s.get("description"));
        // The descriptions themselves are language-dependent, and they are not observable —
        // so the column has to be asked to re-read them.
        table.refresh();

        picker.setPromptText(s.get("build.pickAi"));
        addButton.setText(s.get("build.addRow"));
        removeButton.setText(s.get("build.removeRow"));
        clearButton.setText(s.get("build.clearRows"));
        caption.setText(s.get("build.rows"));
        placeholder.setText(s.get("build.noRows"));

        dlPane.setText(s.get("build.dlTarget"));
        domainLabel.setText(s.get("build.domain"));
        prefixLabel.setText(s.get("build.pathPrefix"));

        elementButton.setText(s.get("build.elementString"));
        dlButton.setText(s.get("build.digitalLink"));

        // A message names a specific failure in the language it was produced in; re-labelling
        // the window does not re-run the build, so a stale one is cleared rather than left
        // sitting in the previous language.
        message.setText("");
    }

    /** Replaces the rows with {@code pairs} — used to seed the editor from an existing capture. */
    public void setRows(List<String[]> pairs) {
        rows.clear();
        for (String[] pair : pairs) rows.add(new AiPair(pair[0], pair[1]));
        ensureTrailingBlankRow();
        message.setText("");
    }
}
