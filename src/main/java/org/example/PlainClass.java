package org.example;

public class PlainClass {

    public String message = "I'm just a simple plain class";

    void method() {
        IO.println(message);
    }

    String anotherMethod() {
        return "This is the unused method from PlainClass";
    }
}
