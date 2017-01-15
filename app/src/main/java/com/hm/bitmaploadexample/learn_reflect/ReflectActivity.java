package com.hm.bitmaploadexample.learn_reflect;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;

import com.hm.bitmaploadexample.R;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class ReflectActivity extends AppCompatActivity {

    private ImageView imageView;
    private String tag = getClass().getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reflect);
        imageView = new ImageView(this);
        testReflect1();
        //testReflect2();
        // testReflect3();
    }

    private void testReflect1() {

        Class c1 = Person.class;
        Log.e(tag, "testReflect1" + c1.getName());
        try {
            Object o = c1.newInstance();
            Method method = c1.getDeclaredMethod("fun", String.class, int.class);
            method.setAccessible(true);
            method.invoke(o, "helloworld", 3);

            Field field = c1.getDeclaredField("msg");// 因为msg变量是private的，所以不能用getField方法
            field.setAccessible(true);//设置是否允许访问，因为该变量是private的，所以要手动设置允许访问，如果msg是public的就不需要这行了。
            String msg = (String) field.get(o);
            Log.e(tag, "testReflect1:" + msg);

            Field[] fields = c1.getDeclaredFields();
            for (Field field1 : fields) {
                field1.setAccessible(true);
                Log.e(tag, "testReflect1: Field =" + field1.getName());
            }

            Method[] methods = c1.getDeclaredMethods();
            for (Method method1 : methods) {
                Log.e(tag, "testReflect1: Method =" + method1.getName());
            }

            Constructor[] constructors = c1.getDeclaredConstructors();
            for (Constructor constructor : constructors) {
                Log.e(tag, "testReflect1: constructor =" + constructor);
            }
        } catch (Exception e) {
            Log.e(tag, "testReflect1" + e.getMessage());
        }

    }

    private void testReflect2() {

        Class c1 = imageView.getClass();
        Log.e(tag, "testReflect2" + c1.getName());
        try {
            Object o = c1.newInstance();
            Method[] methods = c1.getMethods();
            for (Method method : methods) {
                Log.e(tag, "testReflect2" + method.getName());
            }
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

    }

    private void testReflect3() {

        try {
            Class c1 = Class.forName("android.widget.ImageView");
            Log.e(tag, "testReflect3" + c1.getName());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
