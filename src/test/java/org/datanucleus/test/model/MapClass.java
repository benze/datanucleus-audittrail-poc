package org.datanucleus.test.model;

import javax.jdo.annotations.Join;
import javax.jdo.annotations.Key;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Value;
import javax.jdo.annotations.Version;
import javax.jdo.annotations.VersionStrategy;
import java.util.HashMap;
import java.util.Map;

@PersistenceCapable
@Version(strategy= VersionStrategy.VERSION_NUMBER, column="VERSN")
public class MapClass {
    @Join
    @Key(column = "STREET_ID")
    @Value(column = "COUNTRYCODE_ID")
    Map<Street, CountryCode> streetMap = new HashMap<>();

    public Map<Street, CountryCode> getStreetMap() {
        return streetMap;
    }
}
