package tools.pantheum.gaia.desktop.ui;

import javafx.application.Platform;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import java.util.List;

/**
 * A text cell that commits what was typed instead of throwing it away.
 *
 * <p>JavaFX's stock {@code TextFieldTableCell} commits on <kbd>Enter</kbd> and <em>cancels</em>
 * on everything else — including the two things people do most often: pressing <kbd>Tab</kbd>
 * to move on, and clicking somewhere else. Both silently discard the edit, which in a builder
 * means a value you typed is simply not in the string you then build.
 *
 * <p>This cell commits on <kbd>Enter</kbd>, on <kbd>Tab</kbd> (moving to the next editable
 * cell, or back with <kbd>Shift</kbd>+<kbd>Tab</kbd>), and on losing focus. <kbd>Escape</kbd>
 * still cancels, because that is the one case where discarding is what was asked for.
 */
public final class EditCell<S> extends TableCell<S, String> {

    /** The columns Tab walks between, in order; read-only columns are skipped. */
    private final List<TableColumn<S, ?>> editable;

    private TextField field;

    public EditCell(List<TableColumn<S, ?>> editable) {
        this.editable = editable;
    }

    @Override
    public void startEdit() {
        if (isEmpty()) return;
        super.startEdit();

        if (field == null) createField();
        field.setText(getItem() == null ? "" : getItem());
        setText(null);
        setGraphic(field);

        // Focus on the next pulse: the field is not in the scene graph yet at this point, so
        // requesting focus here silently does nothing — and a field that never held focus
        // never fires the focus-lost that commits an edit when the user clicks away.
        Platform.runLater(() -> {
            if (!isEditing()) return;
            field.requestFocus();
            field.selectAll();
        });
    }

    @Override
    public void cancelEdit() {
        super.cancelEdit();
        setText(getItem());
        setGraphic(null);
    }

    @Override
    protected void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);
        if (empty) {
            setText(null);
            setGraphic(null);
        } else if (isEditing()) {
            if (field != null) field.setText(item == null ? "" : item);
            setText(null);
            setGraphic(field);
        } else {
            setText(item);
            setGraphic(null);
        }
    }

    private void createField() {
        field = new TextField();
        field.getStyleClass().add("edit-cell-field");

        // Enter.
        field.setOnAction(e -> {
            commitEdit(field.getText());
            e.consume();
        });

        // Clicking away, or focus moving anywhere else: commit rather than discard.
        field.focusedProperty().addListener((obs, was, isFocused) -> {
            if (!isFocused && isEditing()) commitEdit(field.getText());
        });

        // A KEY_PRESSED filter, not a handler: the TableView's own Tab handling would
        // otherwise move the focus — cancelling the edit — before this ever ran.
        field.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() == KeyCode.TAB) {
                commitEdit(field.getText());
                moveTo(e.isShiftDown() ? -1 : +1);
                e.consume();
            } else if (e.getCode() == KeyCode.ESCAPE) {
                cancelEdit();
                e.consume();
            }
        });
    }

    /**
     * Opens the next editable cell, wrapping across rows: forwards from the last column
     * carries on at the first column of the row below, and backwards the other way.
     */
    private void moveTo(int direction) {
        TableView<S> table = getTableView();
        if (table == null || editable.isEmpty()) return;

        int column = editable.indexOf(getTableColumn());
        if (column < 0) return;
        int row = getIndex();

        int nextColumn = column + direction;
        int nextRow    = row;
        if (nextColumn >= editable.size()) {
            nextColumn = 0;
            nextRow = row + 1;
        } else if (nextColumn < 0) {
            nextColumn = editable.size() - 1;
            nextRow = row - 1;
        }

        final int targetRow = nextRow;
        final TableColumn<S, ?> targetColumn = editable.get(nextColumn);

        // Later, for two reasons: the commit above is still settling, and moving past the
        // last row is only possible once the editor has appended its trailing blank row —
        // which itself happens on a later pulse.
        Platform.runLater(() -> {
            if (targetRow < 0 || targetRow >= table.getItems().size()) return;
            table.getSelectionModel().select(targetRow, targetColumn);
            table.edit(targetRow, targetColumn);
        });
    }
}
