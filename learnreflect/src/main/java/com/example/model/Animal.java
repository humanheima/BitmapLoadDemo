package com.example.model;

/**
 * Created by Administrator on 2017/1/14.
 */
public class Animal {

    private int eyes;
    protected String name;
    public float weight;
    float height;

    public Animal() {
    }

    public Animal(int eyes, String name, float weight, float height) {
        this.eyes = eyes;
        this.name = name;
        this.weight = weight;
        this.height = height;
    }

    public int getEyes() {
        return eyes;
    }

    public void setEyes(int eyes) {
        this.eyes = eyes;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public float getWeight() {
        return weight;
    }

    public void setWeight(float weight) {
        this.weight = weight;
    }

    public float getHeight() {
        return height;
    }

    public void setHeight(float height) {
        this.height = height;
    }
}
