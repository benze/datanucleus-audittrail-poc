package mydomain.model;

import javax.jdo.annotations.Element;
import javax.jdo.annotations.Join;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.PrimaryKey;

@PersistenceCapable(detachable="true")
public class Address
{
    // n-n mapping table
    @Join (column = "ID")
    @Element(column = "ELEMENT")
    Street[] street;

    public Address(long id, Street[] street)
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
}
