package com.hm.bitmaploadexample.learn_reflect;

import android.util.Log;

/**
 * Created by Administrator on 2017/1/13.
 */
public class Person {

    private String name;
    private int age;
    private String msg = "good morning 2017";

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    //java.lang.Class<com.hm.bitmaploadexample.learn_reflect.Person> has no zero argument constructor
    public Person() {
    }

    public Person(String name) {
        this.name = name;
        System.out.println(name);
    }

    public void fun() {
        System.out.println("fun");
    }

    private void fun(String name, int age) {
        Log.e("ReflectActivity", "我叫" + name + ",今年" + age + "岁");
    }
}
