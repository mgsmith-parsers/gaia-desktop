package tools.pantheum.gaia.desktop.parse;

import tools.pantheum.gaia.GaiaConstants;
import tools.pantheum.gaia.config.ParseConfig;
import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;

/**
 * The observable, editable form of {@link ParseConfig} — one JavaFX property per parser
 * option, bound to the controls in the options panel.
 *
 * <p>{@code ParseConfig} is immutable and built per call, so this class holds the UI state
 * and mints a fresh config on every parse via {@link #toParseConfig()}. Anything that has
 * to react to an option change (the app re-parses the selected capture) registers through
 * {@link #onChange}, which fires for any of the properties.
 */
public final class ParseOptions {

    private final ObjectProperty<GaiaConstants.Language>      language      =
            new SimpleObjectProperty<>(GaiaConstants.Language.ENGLISH);
    private final ObjectProperty<GaiaConstants.ParseMode>     parseMode     =
            new SimpleObjectProperty<>(GaiaConstants.ParseMode.INTERPRETATION);
    private final ObjectProperty<GaiaConstants.DateEndian>    dateEndian    =
            new SimpleObjectProperty<>(GaiaConstants.DateEndian.LITTLE);
    private final ObjectProperty<GaiaConstants.DateSeparator> dateSeparator =
            new SimpleObjectProperty<>(GaiaConstants.DateSeparator.SLASH);
    private final ObjectProperty<GaiaConstants.MonthFormat>   monthFormat   =
            new SimpleObjectProperty<>(GaiaConstants.MonthFormat.TWO_DIGIT);
    private final ObjectProperty<GaiaConstants.YearFormat>    yearFormat    =
            new SimpleObjectProperty<>(GaiaConstants.YearFormat.FOUR_DIGIT);
    private final BooleanProperty skipRequiresCheck = new SimpleBooleanProperty(false);
    private final BooleanProperty skipExcludesCheck = new SimpleBooleanProperty(false);

    public ObjectProperty<GaiaConstants.Language>      languageProperty()      { return language; }
    public ObjectProperty<GaiaConstants.ParseMode>     parseModeProperty()     { return parseMode; }
    public ObjectProperty<GaiaConstants.DateEndian>    dateEndianProperty()    { return dateEndian; }
    public ObjectProperty<GaiaConstants.DateSeparator> dateSeparatorProperty() { return dateSeparator; }
    public ObjectProperty<GaiaConstants.MonthFormat>   monthFormatProperty()   { return monthFormat; }
    public ObjectProperty<GaiaConstants.YearFormat>    yearFormatProperty()    { return yearFormat; }
    public BooleanProperty skipRequiresCheckProperty() { return skipRequiresCheck; }
    public BooleanProperty skipExcludesCheckProperty() { return skipExcludesCheck; }

    public GaiaConstants.Language language() { return language.get(); }

    /** Runs {@code action} whenever any option changes. */
    public void onChange(Runnable action) {
        Observable[] all = {
                language, parseMode, dateEndian, dateSeparator,
                monthFormat, yearFormat, skipRequiresCheck, skipExcludesCheck
        };
        for (Observable o : all) {
            o.addListener(obs -> action.run());
        }
    }

    /** Restores every option to the {@link ParseConfig#defaultConfig()} value. */
    public void reset() {
        language.set(GaiaConstants.Language.ENGLISH);
        parseMode.set(GaiaConstants.ParseMode.INTERPRETATION);
        dateEndian.set(GaiaConstants.DateEndian.LITTLE);
        dateSeparator.set(GaiaConstants.DateSeparator.SLASH);
        monthFormat.set(GaiaConstants.MonthFormat.TWO_DIGIT);
        yearFormat.set(GaiaConstants.YearFormat.FOUR_DIGIT);
        skipRequiresCheck.set(false);
        skipExcludesCheck.set(false);
    }

    /** Builds the immutable {@link ParseConfig} for a single {@code GaiaParser.parse} call. */
    public ParseConfig toParseConfig() {
        return ParseConfig.builder()
                .language(language.get())
                .requestedParseMode(parseMode.get())
                .dateEndian(dateEndian.get())
                .dateSeparator(dateSeparator.get())
                .monthFormat(monthFormat.get())
                .yearFormat(yearFormat.get())
                .skipRequiresCheck(skipRequiresCheck.get())
                .skipExcludesCheck(skipExcludesCheck.get())
                .build();
    }
}
