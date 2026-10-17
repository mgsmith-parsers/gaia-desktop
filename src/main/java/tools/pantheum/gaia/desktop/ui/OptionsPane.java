package tools.pantheum.gaia.desktop.ui;

import tools.pantheum.gaia.GaiaConstants;
import tools.pantheum.gaia.desktop.i18n.UiStrings;
import tools.pantheum.gaia.desktop.parse.Languages;
import tools.pantheum.gaia.desktop.parse.ParseOptions;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import java.util.function.Function;

/**
 * The parser-options side panel: one control per {@link ParseOptions} property, bound
 * bidirectionally so edits flow straight into the next parse.
 *
 * <p>Mirrors the option set of {@link tools.pantheum.gaia.config.ParseConfig} — pipeline
 * depth, error-message language, date rendering, and the two structural-check escapes.
 *
 * <p>Rebuilt rather than re-labelled when the language changes, which is safe <em>here</em>
 * precisely because the panel owns nothing: every control is a two-way view of a
 * {@link ParseOptions} property, so the values live in the model and a new panel picks them
 * straight back up. {@link BuilderPane} is the counter-example — it holds the user's
 * half-finished rows, so it is re-labelled in place instead.
 */
public final class OptionsPane extends ScrollPane {

    public OptionsPane(ParseOptions options, UiStrings s, Runnable onReset) {
        VBox box = new VBox();
        box.getStyleClass().add("options-box");
        box.setPadding(new Insets(16));
        box.setSpacing(6);

        box.getChildren().addAll(
                header(s.get("lbl.pipeline")),
                labelled(s.get("requestedParseMode"),
                        combo(GaiaConstants.ParseMode.values(), options.parseModeProperty(), Enum::name)),

                spacer(),
                header(s.get("lbl.messages")),
                labelled(s.get("language"),
                        combo(GaiaConstants.Language.values(), options.languageProperty(), Languages::label)),

                spacer(),
                header(s.get("lbl.dates")),
                labelled(s.get("lbl.order"),
                        combo(GaiaConstants.DateEndian.values(), options.dateEndianProperty(),
                                e -> e + endianHint(e))),
                labelled(s.get("lbl.separator"),
                        combo(GaiaConstants.DateSeparator.values(), options.dateSeparatorProperty(),
                                sep -> sep + "  " + sep.symbol())),
                labelled(s.get("lbl.month"),
                        combo(GaiaConstants.MonthFormat.values(), options.monthFormatProperty(),
                                m -> m == GaiaConstants.MonthFormat.TWO_DIGIT ? "TWO_DIGIT  12" : "THREE_LETTER  DEC")),
                labelled(s.get("lbl.year"),
                        combo(GaiaConstants.YearFormat.values(), options.yearFormatProperty(),
                                y -> y == GaiaConstants.YearFormat.FOUR_DIGIT ? "FOUR_DIGIT  2026" : "TWO_DIGIT  26")),

                spacer(),
                header(s.get("lbl.structural")),
                check(s.get("lbl.skipRequires"), options.skipRequiresCheckProperty()),
                check(s.get("lbl.skipExcludes"), options.skipExcludesCheckProperty())
        );

        Button reset = new Button(s.get("act.reset"));
        reset.setMaxWidth(Double.MAX_VALUE);
        reset.setOnAction(e -> {
            options.reset();
            if (onReset != null) onReset.run();
        });
        box.getChildren().addAll(spacer(), reset);

        setContent(box);
        setFitToWidth(true);
        setHbarPolicy(ScrollBarPolicy.NEVER);
        setPrefWidth(280);
        setMinWidth(240);
        getStyleClass().add("options-pane");
    }

    // ------------------------------------------------------------------ helpers

    private static Label header(String text) {
        Label l = new Label(text);
        l.getStyleClass().add("section-header");
        return l;
    }

    private static VBox spacer() {
        VBox v = new VBox(new Separator());
        v.setPadding(new Insets(10, 0, 4, 0));
        return v;
    }

    private static VBox labelled(String caption, Node control) {
        Label l = new Label(caption);
        l.getStyleClass().add("field-label");
        VBox v = new VBox(2, l, control);
        v.setPadding(new Insets(4, 0, 4, 0));
        VBox.setVgrow(control, Priority.NEVER);
        return v;
    }

    private static CheckBox check(String caption, BooleanProperty prop) {
        CheckBox cb = new CheckBox(caption);
        cb.setWrapText(true);
        cb.selectedProperty().bindBidirectional(prop);
        return cb;
    }

    /**
     * A combo over an enum's constants, bound bidirectionally to {@code property} and
     * labelled by {@code labeller} in both the button cell and the drop-down.
     */
    private static <T> ComboBox<T> combo(T[] values, ObjectProperty<T> property,
                                         Function<T, String> labeller) {
        ComboBox<T> cb = new ComboBox<>(FXCollections.observableArrayList(values));
        cb.setMaxWidth(Double.MAX_VALUE);
        cb.setConverter(new StringConverter<T>() {
            @Override public String toString(T value) { return value == null ? "" : labeller.apply(value); }
            @Override public T fromString(String s)   { return cb.getValue(); }  // read-only combo
        });
        cb.setCellFactory(list -> new ListCell<T>() {
            @Override protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : labeller.apply(item));
            }
        });
        cb.valueProperty().bindBidirectional(property);
        return cb;
    }

    private static String endianHint(GaiaConstants.DateEndian e) {
        switch (e) {
            case BIG:    return "  yyyy/mm/dd";
            case MIDDLE: return "  mm/dd/yyyy";
            case LITTLE:
            default:     return "  dd/mm/yyyy";
        }
    }
}
