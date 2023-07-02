package app.ui;

import javafx.beans.property.SimpleStringProperty;

public class TableRow {

    SimpleStringProperty occurrences;
    SimpleStringProperty index;
    SimpleStringProperty oper;

    public void setOper(String oper) {
        this.oper.set(oper);
    }

    public String getOper() {
        return oper.get();
    }

    SimpleStringProperty operProperty() {
        return oper;
    }

    public void setIndex(String index) {
        this.index.set(index);
    }

    public String getIndex() {
        return index.get();
    }

    SimpleStringProperty indexProperty() {
        return index;
    }

    public void setOccurrences(String occurrences) {
        this.occurrences.set(occurrences);
    }

    public String getOccurrences() {
        return occurrences.get();
    }

    SimpleStringProperty occurrencesProperty() {
        return occurrences;
    }

    TableRow(int index, String oper, int occurrences) {
        this.index = new SimpleStringProperty(index + "");
        this.oper = new SimpleStringProperty(oper);
        this.occurrences = new SimpleStringProperty(occurrences + "");
    }

    TableRow(String index, String oper, String occurrences) {
        this.index = new SimpleStringProperty(index + "");
        this.oper = new SimpleStringProperty(oper);
        this.occurrences = new SimpleStringProperty(occurrences + "");
    }
}