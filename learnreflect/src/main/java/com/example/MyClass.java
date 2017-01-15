package com.example;

import com.example.model.Person;

import java.lang.reflect.Constructor;

public class MyClass {

    public static void main(String args[]) {
        Class c = Person.class;
        /*try {
            Object o = c.newInstance();
            Field field = c.getField("FieldInLiveInterface");
            int a = field.getInt(o);
            System.out.println(a + "");
            Field field1 = c.getDeclaredField("msg");
            field1.setAccessible(true);
            String msg = (String) field1.get(o);
            System.out.println(msg);
            field1.set(o,"good by 2016");
            String msgModified=(String) field1.get(o);
            System.out.println(msgModified);
        } catch (Exception e) {
            e.printStackTrace();
        }*/

       /* Field[] fields = c.getFields();
        for (Field field : fields) {
            System.out.println(field.getName());
        }

        System.out.println("----------------------------------------");
        Field[] declaredFields = c.getDeclaredFields();
        for (Field declaredField : declaredFields) {
            System.out.println(declaredField.getName());
        }*/
       /* System.out.println("----------------------------------------");
        Method[] methods = c.getMethods();
        for (Method method : methods) {
            System.out.println(method.getName());
        }
        System.out.println("----------------------------------------");
        Method[] declaredMethods = c.getDeclaredMethods();
        for (Method method : declaredMethods) {
            System.out.println(method.getName());
        }
        System.out.println("----------------------------------------");*/
        /*try {
            Method method = c.getMethod("setEyes", int.class);
            System.out.println("name=" + method.getName() + ",return type= " + method.getReturnType());
            Method method1 = c.getMethod("run");
            System.out.println("name=" + method1.getName() + ",return type= " + method1.getReturnType());
            Method method2 = c.getMethod("setAge",int.class);
            System.out.println("name=" + method2.getName() + ",return type= " + method2.getReturnType());
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }*/
        try {
          /*  Method method = c.getDeclaredMethod("setEyes", int.class);
            System.out.println("name=" + method.getName() + ",return type= " + method.getReturnType());*/
         /*   Method method1 = c.getDeclaredMethod("run");
            System.out.println("name=" + method1.getName() + ",return type= " + method1.getReturnType());
            Method method3 = c.getDeclaredMethod("jump");
            System.out.println("name=" + method3.getName() + ",return type= " + method3.getReturnType());
*/
           /* Method method2 = c.getDeclaredMethod("info", String.class, int.class);
            System.out.println("name=" + method2.getName() + ",return type= " + method2.getReturnType());
            method2.setAccessible(true);//info()方法是私有的。
            method2.invoke(c.newInstance(), "dumingwei", 25);*/
           /* Constructor constructor = c.getConstructor(String.class);
            System.out.println(constructor.getName());
            Constructor constructor0 = c.getConstructor(String.class);
            System.out.println(constructor0.getName());
            Constructor constructor1 = c.getDeclaredConstructor(int.class, String.class);
            System.out.println(constructor1.getName());*/
          /*  Constructor[] constructors = c.getConstructors();
            for (Constructor constructor : constructors) {
                System.out.println(constructor);
            }
            System.out.println("----------------------------------------");
            Constructor[] declaredConstructors = c.getDeclaredConstructors();
            for (Constructor constructor : declaredConstructors) {
                System.out.println(constructor);
            }*/
            Class clz = Person.Man.class;
            Constructor constructor = clz.getConstructor(Person.class,int.class);
            System.out.println(constructor.getName());
            Constructor constructor1 = clz.getDeclaredConstructor(Person.class);
            System.out.println(constructor1.getName());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

