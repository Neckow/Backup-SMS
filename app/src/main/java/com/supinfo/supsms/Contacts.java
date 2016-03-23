package com.supinfo.supsms;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by loicbillaud on 06/02/15.
 */
public class Contacts {
    private String name;
    private List<String> numbers;

    public Contacts(){
        this.numbers = new ArrayList<>();
    }

    public List<String> getNumber() {
        return numbers;
    }

//    public void setNumber(List<String> number) {
//        this.numbers = numbers;
//    }

    public void addNumber(String number) {
        this.numbers.add(number);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
