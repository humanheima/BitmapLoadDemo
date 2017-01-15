package com.example.model;

/**
 * Created by Administrator on 2017/1/13.
 */
public class Person extends Animal implements RunInterface, LiveInterface {

    private int age;
    private String msg = "good morning 2017";
    private static String test = "1234";

    //java.lang.Class<com.hm.bitmaploadexample.learn_reflect.Person> has no zero argument constructor
    public Person() {
    }

    public Person(String name) {
        this.name = name;
        System.out.println(name);
    }

    private Person(int age, String name) {
        this.age = age;
        this.name = name;
        System.out.println(name);
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public void fun() {
        System.out.println("fun");
    }

    private void info(String name, int age) {
        System.out.printf("ReflectActivity 我叫" + name + ",今年" + age + "岁");
    }

    @Override
    public void run() {

    }

    @Override
    public void jump() {

    }

    @Override
    public void eat() {

    }

    @Override
    public void sleep() {

    }

   public  class Man {
        int beerCount;//能喝几瓶啤酒

        public Man() {
        }

        public Man(int beerCount) {
            this.beerCount = beerCount;
        }

        public int getBeerCount() {
            return beerCount;
        }

        public void setBeerCount(int beerCount) {
            this.beerCount = beerCount;
            System.out.println("你能喝几瓶啤酒?" + beerCount);
        }
    }
}
