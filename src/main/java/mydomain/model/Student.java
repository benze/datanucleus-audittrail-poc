package mydomain.model;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.Discriminator;
import javax.jdo.annotations.DiscriminatorStrategy;
import javax.jdo.annotations.Join;
import javax.jdo.annotations.Key;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Value;
import javax.jdo.annotations.Version;
import javax.jdo.annotations.VersionStrategy;
import java.util.HashMap;
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

    Map<String, String> marks = new HashMap<>();


    @Join(column = "STUDENT_ID")
    @Key(column = "TELEPHONE_TYPE")
    @Value(column = "TELEPHONE_ID")
    Map<TelephoneType, Telephone> telephoneNbs = new HashMap<>();

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

    public Map<TelephoneType, Telephone> getTelephoneNbs() {
        return telephoneNbs;
    }

    public void addTelephoneNb(TelephoneType type,  Telephone telephone) {
        telephoneNbs.put(type,telephone);
    }

    public Map<String, String> getMarks() {
        return marks;
    }

    public void addMark(String subject, String grade) {
        this.marks.put(subject, grade);
    }
}
