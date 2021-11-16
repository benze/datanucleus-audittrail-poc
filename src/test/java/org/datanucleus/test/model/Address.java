package org.datanucleus.test.model;

import javax.jdo.annotations.Element;
import javax.jdo.annotations.Join;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Version;
import javax.jdo.annotations.VersionStrategy;

@PersistenceCapable(detachable="true")
@Version(strategy= VersionStrategy.VERSION_NUMBER, column="VERSN")
public class Address
{
    // n-n mapping table
    @Join (column = "ID")
    @Element(column = "ELEMENT")
    Street[] street;

    public Address(Street[] street)
    {
        this.street = street;
    }

    public Street[] getStreet()
    {
        return street;
    }
    public void setStreet(Street[] street)
    {
        this.street = street;
    }

    public void replaceStreet(int index, Street street){
        if( this.street == null || this.street.length < index ) {
            throw new IllegalStateException("Index does not exist: " + index );
        }

        this.street[index] = street;
    }
    
}
