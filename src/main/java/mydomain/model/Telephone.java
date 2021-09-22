package mydomain.model;

import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.PrimaryKey;

@PersistenceCapable(detachable="true")
public class Telephone
{

    @PrimaryKey
    Long id;

    String number;

    public Telephone(long id, String number)
    {
        this.id = id;
        this.number = number;
    }

    public Long getId()
    {
        return id;
    }

    public String getNumber()
    {
        return number;
    }
    public void setNumber(String number)
    {
        this.number = number;
    }
}
