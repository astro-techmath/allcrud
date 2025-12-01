package com.techmath.allcrud.config;

import org.junit.jupiter.api.DisplayNameGenerator;

import java.lang.reflect.Method;
import java.util.List;

public class AllcrudDisplayNameGenerator extends DisplayNameGenerator.Standard {

    @Override
    public String generateDisplayNameForClass(Class<?> testClass) {
        return getReadableName(testClass.getSimpleName());
    }

    @Override
    public String generateDisplayNameForNestedClass(List<Class<?>> enclosingInstanceTypes, Class<?> nestedClass) {
        return getReadableName(nestedClass.getSimpleName());
    }

    @Override
    public String generateDisplayNameForMethod(List<Class<?>> enclosingInstanceTypes, Class<?> testClass, Method testMethod) {
        return getReadableName(testMethod.getName()).toLowerCase();
    }

    private String getReadableName(String name) {
        String withSpaces = name.replace("_", " > ");
        return withSpaces.replaceAll("(?<=[a-z])(?=[A-Z])|(?<=[a-zA-Z])(?=[0-9])|(?<=[0-9])(?=[a-zA-Z])", " ");
    }

}
