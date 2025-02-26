package dev.ak.springbootresilience4j.model;


public record Person(String id, String firstName, String lastName, String email, String phoneNumber, Address address) {

}