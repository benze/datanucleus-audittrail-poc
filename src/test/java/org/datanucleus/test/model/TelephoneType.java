package org.datanucleus.test.model;

public enum TelephoneType {
    HOME("home"),
    MOBILE("mobile"),
    WORK("work"),
    OTHER("other");

    String type;

    TelephoneType(String type) {
        this.type = type;
    }
}
