package mydomain.model;

import javax.jdo.annotations.Element;
import javax.jdo.annotations.ForeignKey;
import javax.jdo.annotations.ForeignKeyAction;
import javax.jdo.annotations.Join;
import javax.jdo.annotations.Order;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.Version;
import javax.jdo.annotations.VersionStrategy;
import java.util.List;
import java.util.Set;

@PersistenceCapable(detachable="true")
@Version(strategy= VersionStrategy.VERSION_NUMBER, column="VERSN")
public class School
{
    String name;

    // 1-n
    @Element( column = "SCHOOL_ID")
    @Order(column = "SCHOOL_ADDRESS_ORDR")
    @Persistent(dependentElement = "true")
    List<Address> addresses;

    // n-n mapping table
    @Join(column = "ID")
    @Element(column = "ELEMENT")
    Set<Student> students;

    public School(String name)
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
