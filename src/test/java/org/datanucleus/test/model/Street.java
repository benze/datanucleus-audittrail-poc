package org.datanucleus.test.model;

import mydomain.datanucleus.datatrail.ITrailDesc;

import javax.jdo.JDOHelper;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Version;
import javax.jdo.annotations.VersionStrategy;
import java.util.Objects;

@PersistenceCapable(detachable = "true")
@Version(strategy = VersionStrategy.VERSION_NUMBER, column = "VERSN")
public class Street implements ITrailDesc {
    String name;

    public Street(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String minimalTxtDesc() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Street street = (Street) o;
        return Objects.equals(getName(), street.getName()) && Objects.equals(JDOHelper.getObjectId(this), JDOHelper.getObjectId(street));
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), JDOHelper.getObjectId(this));
    }

}
