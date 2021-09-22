package mydomain.model;

import javax.jdo.annotations.*;
import java.util.Map;

@PersistenceCapable(detachable="true")
public class Student
{
    @PrimaryKey
    Long id;
    String name;

    // 0-1
    @Column(name = "ADDRESS_ID")
    Address address;

    Map<String, String> marks;

    @Join(column = "STUDENT_ID")
    @Key(column = "TELEPHONE_TYPE")
    @Value(column = "TELEPHONE_ID")
    Map<TelephoneType, Telephone> telephoneNbs;

    public Student(long id, String name)
    {
        this.id = id;
        this.name = name;
    }

    public Long getId()
    {
        return id;
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
