package mydomain.model;

import javax.jdo.annotations.*;
import java.util.Map;

@PersistenceCapable(detachable="true")
@Discriminator(strategy=DiscriminatorStrategy.CLASS_NAME, column="TYP", indexed="true")
@Version(strategy=VersionStrategy.VERSION_NUMBER, column="VERSN")
public class Student
{
    String name;

    // 0-1
    @Column(name = "ADDRESS_ID")
    Address address;

    Map<String, String> marks;

    @Join(column = "STUDENT_ID")
    @Key(column = "TELEPHONE_TYPE")
    @Value(column = "TELEPHONE_ID")
    Map<TelephoneType, Telephone> telephoneNbs;

    public Student(String name)
    {
        this.name = name;
    }

    public String getName()
    {
        return name;
    }
    public void setName(String name)
    {
        this.name = name;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }
}
