package mydomain.model;

import javax.jdo.annotations.Element;
import javax.jdo.annotations.Join;
import javax.jdo.annotations.Order;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.PrimaryKey;
import java.util.List;
import java.util.Set;

@PersistenceCapable(detachable="true")
public class School
{
    @PrimaryKey
    Long id;

    String name;

    // 1-n
    @Element( column = "SCHOOL_ID")
    @Order(column = "SCHOOL_ADDRESS_ORDR")
    List<Address> addresses;

    // n-n mapping table
    @Join(column = "ID")
    @Element(column = "ELEMENT")
    Set<Student> students;

    public School(long id, String name)
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

    public List<Address> getAddresses() {
        return addresses;
    }

    public void setAddresses(List<Address> addresses) {
        this.addresses = addresses;
    }

    public Set<Student> getStudents() {
        return students;
    }

    public void setStudents(Set<Student> students) {
        this.students = students;
    }
}
