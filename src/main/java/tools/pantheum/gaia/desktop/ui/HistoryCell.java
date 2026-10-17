package tools.pantheum.gaia.desktop.ui;

import tools.pantheum.gaia.desktop.model.ScanRecord;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.OverrunStyle;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * One row of the capture history: a valid/invalid dot, the HRI (or raw input), and a
 * footer line with capture time, source, and AI count.
 */
public final class HistoryCell extends ListCell<ScanRecord> {

    private final Label  dot     = new Label("●");
    private final Label  summary = new Label();
    private final Label  detail  = new Label();
    private final HBox   root;

    public HistoryCell() {
        summary.getStyleClass().add("history-summary");
        summary.setWrapText(false);
        detail.getStyleClass().add("history-detail");

        VBox text = new VBox(1, summary, detail);
        text.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(text, Priority.ALWAYS);
        // Let the text column shrink below its preferred width so long HRIs ellipsize
        // instead of forcing a horizontal scrollbar onto the list.
        text.setMinWidth(0);
        summary.setMaxWidth(Double.MAX_VALUE);

        Region gap = new Region();
        gap.setMinWidth(8);

        summary.setTextOverrun(OverrunStyle.CENTER_ELLIPSIS);
        detail.setTextOverrun(OverrunStyle.ELLIPSIS);

        root = new HBox(dot, gap, text);
        root.setAlignment(Pos.CENTER_LEFT);
        root.setPadding(new Insets(6, 4, 6, 2));
        setPadding(Insets.EMPTY);
        // Without this the cell's preferred width follows the longest HRI and the list
        // grows a horizontal scrollbar; at 0 the flow sizes every cell to the viewport
        // and the labels ellipsize instead.
        setPrefWidth(0);
    }

    @Override
    protected void updateItem(ScanRecord item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
            setText(null);
            setGraphic(null);
            return;
        }

        summary.setText(item.summary());
        int n = item.elementCount();
        // timestamp(), not time(): a row from an earlier session has to say which day it is
        // from, while today's rows — nearly all of them — stay at just the time.
        detail.setText(item.timestamp() + " · " + item.source().label()
                + (item.symbology() != null ? " · " + item.symbology() : "")
                + " · " + n + (n == 1 ? " AI" : " AIs"));

        dot.getStyleClass().removeAll("dot-ok", "dot-bad");
        dot.getStyleClass().add(item.isValid() ? "dot-ok" : "dot-bad");

        setText(null);
        setGraphic(root);
    }
}
