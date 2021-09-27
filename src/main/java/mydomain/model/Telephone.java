package mydomain.model;

import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Version;
import javax.jdo.annotations.VersionStrategy;

@PersistenceCapable(detachable="true")
@Version(strategy= VersionStrategy.VERSION_NUMBER, column="VERSN")
public class Telephone
{
    String number;
    CountryCode countryCode;

    public Telephone(String number, CountryCode countryCode)
    {
        this.number = number;
        this.countryCode = countryCode;
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
