package tools.pantheum.gaia.desktop.build;

import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.beans.value.ObservableStringValue;

/**
 * One editable row of the builder: an AI code and its complete data value.
 *
 * <p>The title, format and "is this a real AI" flag are derived from the code rather than
 * stored, so a row re-labels itself as the code is typed and can never disagree with what
 * {@link ElementBuilder} will accept.
 *
 * <p>Uses JavaFX properties but no controls, so it stays testable without a toolkit.
 */
public final class AiPair {

    private final StringProperty ai    = new SimpleStringProperty("");
    private final StringProperty value = new SimpleStringProperty("");

    private final ObservableStringValue  title   = Bindings.createStringBinding(
            () -> ElementBuilder.titleOf(ai.get()), ai);
    private final ObservableStringValue  format  = Bindings.createStringBinding(
            () -> ElementBuilder.formatOf(ai.get()), ai);
    private final ObservableBooleanValue known   = Bindings.createBooleanBinding(
            () -> ElementBuilder.isKnownAi(ai.get()), ai);

    public AiPair() {}

    public AiPair(String ai, String value) {
        this.ai.set(ai == null ? "" : ai);
        this.value.set(value == null ? "" : value);
    }

    public StringProperty aiProperty()    { return ai; }
    public StringProperty valueProperty() { return value; }

    /** The AI's data title, e.g. {@code "GTIN"}; empty while the code is unrecognised. */
    public ObservableStringValue titleProperty()  { return title; }

    /** The AI's format string, e.g. {@code "N2+N14"}; empty while the code is unrecognised. */
    public ObservableStringValue formatProperty() { return format; }

    public String  ai()      { return ai.get(); }
    public String  value()   { return value.get(); }
    public boolean isKnown() { return known.get(); }

    /** Whether the row carries anything at all — blank rows are ignored when building. */
    public boolean isEmpty() {
        return ai.get().trim().isEmpty() && value.get().trim().isEmpty();
    }

    /** The pair as {@link ElementBuilder} takes it. */
    public String[] toPair() {
        return new String[] {ai.get().trim(), value.get()};
    }

    @Override
    public String toString() {
        return "(" + ai.get() + ")" + value.get();
    }
}
