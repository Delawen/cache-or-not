package org.example;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.implementation.FixedValue;
import net.bytebuddy.implementation.MethodDelegation;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import static net.bytebuddy.matcher.ElementMatchers.*;

public class Main {
    static void main() throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        IO.println(String.format("Let's do some tricks!"));

        //First call the plain normal basic class

        IO.println(String.format("PlainClass:"));
        PlainClass plainClass = new PlainClass();
        plainClass.method();

        //Now let's reflect
        //Change the method in NonDeterministicClass by the one on PlainClass
        IO.println(String.format("NonDeterministicClass, method overrided:"));
        var nonDeterministicClass = new ByteBuddy()
                .subclass(NonDeterministicClass.class)
                .method(named("overrideMe")
                        .and(isDeclaredBy(NonDeterministicClass.class)
                                .and(returns(String.class))))
                .intercept(FixedValue.value("Intercepted and overrided method."))
                .make();
        var nonDeterministicType = nonDeterministicClass
                .load(Main.class.getClassLoader())
                .getLoaded();
        IO.println(nonDeterministicType.newInstance().overrideMe());

        //Create a new dynamic class during runtime, with a custom method
        IO.println(String.format("Dynamic Class:"));
        Class<?> type = new ByteBuddy()
                .subclass(NonDeterministicClass.class)
                .name("DynamicClass")
                .defineMethod("dynamicMethod", String.class, Modifier.PUBLIC)
                .intercept(FixedValue.value("Calling the dynamic method"))
                .make()
                .load(Main.class.getClassLoader(), ClassLoadingStrategy.Default.WRAPPER)
                .getLoaded();
        IO.println(type.getDeclaredMethod("dynamicMethod", null).invoke(type.newInstance()));
    }
}
