package com.yador.meeting_manager_client.model;

public class AuthModel {
    private String token;
    private Person person;

    public AuthModel(String token, Person person) {
        this.token = token;
        this.person = person;
    }

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
