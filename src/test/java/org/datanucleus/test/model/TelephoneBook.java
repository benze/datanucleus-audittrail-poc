package org.datanucleus.test.model;

import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Version;
import javax.jdo.annotations.VersionStrategy;
import java.util.ArrayList;
import java.util.List;

@PersistenceCapable
@Version(strategy= VersionStrategy.VERSION_NUMBER, column="VERSN")
public class TelephoneBook {

    List<Telephone> telephoneNumbers;

    String name;

    public TelephoneBook(String name) {
        this.name = name;
    }

    public List<Telephone> getTelephoneNumbers() {
        return telephoneNumbers;
    }

    public void setTelephoneNumbers(List<Telephone> telephoneNumbers) {
        this.telephoneNumbers = telephoneNumbers;
    }

    public void addTelephoneNumber(Telephone telephoneNumber) {
        if( telephoneNumbers == null)
            telephoneNumbers = new ArrayList<>();
        telephoneNumbers.add(telephoneNumber);
    }

    public String getName() {
        return name;
    }

}
