package org.datanucleus.test.model;

import mydomain.audit.DataTrail;

import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;


@DataTrail
@Inheritance(strategy= InheritanceStrategy.SUPERCLASS_TABLE)
@PersistenceCapable
public class Teacher extends BaseTeacher{

    private BaseTeacher mentor;

    @DataTrail(excludeFromDataTrail = true)
    private String nickname;

    public Teacher(String name, BaseTeacher mentor) {
        super(name);
        this.mentor = mentor;
        this.nickname = "nickname";
    }
}
