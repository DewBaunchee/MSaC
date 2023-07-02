package app.ui;

import javafx.beans.property.SimpleStringProperty;

public class TableRow {

    public enum types {P, M, C, T};

    SimpleStringProperty index;
    SimpleStringProperty identifier;
    SimpleStringProperty spen;
    SimpleStringProperty type;

    public TableRow(int index, String identifier, int spen, types type) {
        this.index = new SimpleStringProperty();
        setIndex(index + "");
        this.identifier = new SimpleStringProperty();
        setIdentifier(identifier);
        this.spen = new SimpleStringProperty();
        setSpen(spen + "");
        this.type = new SimpleStringProperty();
        setType(type + "");
    }

    public String getIndex() {
        return index.get();
    }

    public SimpleStringProperty indexProperty() {
        return index;
    }

    public void setIndex(String index) {
        this.index.set(index);
    }

    public String getIdentifier() {
        return identifier.get();
    }

    public SimpleStringProperty identifierProperty() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier.set(identifier);
    }

    public String getSpen() {
        return spen.get();
    }

    public SimpleStringProperty spenProperty() {
        return spen;
    }

    public void setSpen(String spen) {
        this.spen.set(spen);
    }

    public String getType() {
        return type.get();
    }

    public SimpleStringProperty typeProperty() {
        return type;
    }

    public void setType(String type) {
        this.type.set(type);
    }
}