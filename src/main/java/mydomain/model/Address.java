package mydomain.model;

import javax.jdo.annotations.Element;
import javax.jdo.annotations.Join;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.PrimaryKey;

@PersistenceCapable(detachable="true")
public class Address
{
    @PrimaryKey
    Long id;


    // n-n mapping table
    @Join (column = "ID")
    @Element(column = "ELEMENT")
    Street[] street;

    public Address(long id, Street[] street)
    {
        this.id = id;
        this.street = street;
    }

    public Long getId()
    {
        return id;
    }

    public Street[] getStreet()
    {
        return street;
    }
    public void setStreet(Street[] street)
    {
        this.street = street;
    }
}
