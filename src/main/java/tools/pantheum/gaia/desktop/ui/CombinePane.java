package tools.pantheum.gaia.desktop.ui;

import java.util.function.Consumer;

import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

import tools.pantheum.gaia.desktop.combine.ScanCombiner;
import tools.pantheum.gaia.desktop.i18n.UiStrings;
import tools.pantheum.gaia.desktop.parse.GsToken;

/**
 * The Combine strip: the bar along the bottom of the window in Combine mode, where the viewer's
 * Multi GS1-128 Validator keeps its toolbar — an entry field for a GS1-128 barcode, and the three
 * actions that manage the scans held.
 *
 * <p>The field exists because the dialog has to work on a machine with no scanner, which is every
 * machine that is not Windows. FNC1 is typed as {@code {GS}}, the same token the input bar uses,
 * and {@link GsToken} converts it on the way out — a text field cannot hold {@code 0x1D} at all.
 *
 * <p>The pane owns no combining logic and holds no scans: it collects an intent and hands it to
 * {@code MainView}, which owns the {@link ScanCombiner}. That keeps every barcode — typed here or
 * arriving from the scanner thread — going through one path.
 */
public final class CombinePane extends HBox {

    /** What the buttons ask for. */
    public enum Action { ADD, REMOVE_LAST, RESET }

    private final Label  caption     = new Label();
    private final TextField entry    = new TextField();
    private final Button addButton   = new Button();
    private final Button removeLast  = new Button();
    private final Button resetButton = new Button();

    /**
     * @param s        the label catalogue for the language the app is running in
     * @param onAction called on the JavaFX thread when a button is pressed, or Enter is hit in the
     *                 entry field (which means {@link Action#ADD})
     */
    public CombinePane(UiStrings s, Consumer<Action> onAction) {
        getStyleClass().add("input-bar");
        setAlignment(Pos.CENTER_LEFT);
        setPadding(new Insets(10, 12, 10, 12));
        setSpacing(8);

        // Enter is the fast path: scan, Enter, scan, Enter, without reaching for the mouse.
        entry.setOnAction(e -> onAction.accept(Action.ADD));
        entry.setMinWidth(160);
        HBox.setHgrow(entry, Priority.ALWAYS);

        addButton.setOnAction(e -> onAction.accept(Action.ADD));
        addButton.setDefaultButton(false);
        removeLast.setOnAction(e -> onAction.accept(Action.REMOVE_LAST));
        resetButton.setOnAction(e -> onAction.accept(Action.RESET));

        for (Region control : new Region[] {addButton, removeLast, resetButton}) {
            control.setMinWidth(Region.USE_PREF_SIZE);
        }

        // The viewer's toolbar, left to right: Reset, Remove Last, a divider, then the entry. Its
        // Close and Save are not repeated here — leaving the mode is the close, and Save report
        // sits in the main toolbar for every mode.
        Separator divider = new Separator(Orientation.VERTICAL);
        entry.setPrefWidth(420);
        getChildren().addAll(resetButton, removeLast, divider, caption, entry, addButton);
        retext(s);
    }

    /** Re-reads every label from {@code s}; called when the language changes. */
    public void retext(UiStrings s) {
        caption.setText(s.get("combine.barcode"));
        entry.setPromptText(s.get("combine.prompt"));
        addButton.setText(s.get("combine.add"));
        removeLast.setText(s.get("combine.removeLast"));
        resetButton.setText(s.get("combine.reset"));
    }

    /**
     * The barcode typed, with {@code {GS}} converted to the real FNC1, or null when the field is
     * empty. Clears the field, so the next barcode can be typed straight away.
     */
    public String takeTyped() {
        String typed = entry.getText() == null ? "" : entry.getText().trim();

        if (typed.isEmpty()) {
            return null;
        }
        entry.clear();
        return GsToken.toParserInput(typed);
    }

    /** Enables Remove Last only while there is a scan to remove. */
    public void setHasScans(boolean hasScans) {
        // As in the viewer: Remove Last needs a scan to remove; Reset is always available, since
        // it also clears a refusal and the entry field.
        removeLast.setDisable(!hasScans);
    }

    /** Empties the entry field, as the viewer's Reset does. */
    public void clearEntry() {
        entry.clear();
    }

    /** Puts the caret back in the entry field, so a typed barcode needs no click first. */
    public void focusEntry() {
        entry.requestFocus();
    }
}
