package dev.yxy.reactive.model.entity;

import org.springframework.data.mongodb.core.index.HashIndexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.io.Serializable;
import java.util.HashSet;

@Document
public class Person implements Serializable {
    private static final long serialVersionUID = 6338224039163409607L;

    @MongoId
    private String id;

    @HashIndexed
    @Field(value = "name")
    private String name;

    @Field(value = "age")
    private Integer age;

    @Field(value = "roles")
    private HashSet<String> roles;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public HashSet<String> getRoles() {
        return roles;
    }

    public void setRoles(HashSet<String> roles) {
        this.roles = roles;
    }

    @Override
    public String toString() {
        return "{"
                + "\"id\":\""
                + id + '\"'
                + ",\"name\":\""
                + name + '\"'
                + ",\"age\":"
                + age
                + ",\"roles\":"
                + roles
                + "}";
    }
}
