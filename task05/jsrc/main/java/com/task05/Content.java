package com.task05;

public class Content {

    private String name;

    private String surname;

    public Content(String name, String surname) {
        this.name = name;
        this.surname = surname;
    }

    public Content() {
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getName() {
        return name;
    }

    public String getSurname() {
        return surname;
    }

    @Override
    public String toString() {
        return "Content{" +
                "name='" + name + '\'' +
                ", surname='" + surname + '\'' +
                '}';
    }
}
