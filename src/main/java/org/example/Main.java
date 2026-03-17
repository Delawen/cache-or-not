package org.example;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.agent.ByteBuddyAgent;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.dynamic.loading.ClassReloadingStrategy;
import net.bytebuddy.implementation.FixedValue;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Random;

import static net.bytebuddy.matcher.ElementMatchers.*;

public class Main {
    static void main() throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {

        //First call the plain normal basic class
        IO.println("PlainClass:");
        PlainClass plainClass = new PlainClass();
        plainClass.method();

        Random r = new Random();

        IO.println("NonDeterministicClass, subclassed as org.example.NonDeterministicClassOverrided and method overrided:");
        var nonDeterministicClass = new ByteBuddy()
                .subclass(NonDeterministicClass.class)
                .name("org.example.NonDeterministicClassOverrided")
                .method(named("overrideMe")
                        .and(isDeclaredBy(NonDeterministicClass.class)
                                .and(returns(String.class))))
                .intercept(FixedValue.value(">> Executing intercepted and overrided method with random " + r.nextInt(100)))
                .make()
                .load(Main.class.getClassLoader())
                .getLoaded();
        IO.println(nonDeterministicClass.newInstance().overrideMe());

        IO.println(String.format("RedefinedClass, method redefined and new dynamicMethod:"));
        ByteBuddyAgent.install();
        new ByteBuddy()
                .redefine(RedefinedClass.class)
                .method(named("redefineMe")
                        .and(isDeclaredBy(RedefinedClass.class)
                                .and(returns(String.class))))
                .intercept(FixedValue.value(">> Executing  intercepted and redefined method with random " + r.nextInt(100)))
                .make()
                .load(RedefinedClass.class.getClassLoader(),
                        ClassReloadingStrategy.fromInstalledAgent());
        IO.println((new RedefinedClass()).redefineMe());

        //Create a new dynamic class during runtime, with a custom method
        IO.println(String.format("Dynamic Class:"));
        Class<?> type = new ByteBuddy()
                .subclass(Object.class)
                .name("org.example.DynamicClass")
                .defineMethod("dynamicMethod", String.class, Modifier.PUBLIC)
                .intercept(FixedValue.value(">> Executing the dynamic method of DynamicClass with random " + r.nextInt(100)))
                .make()
                .load(Main.class.getClassLoader(), ClassLoadingStrategy.Default.WRAPPER)
                .getLoaded();
        IO.println(type.getDeclaredMethod("dynamicMethod", null).invoke(type.newInstance()));
    }
}
