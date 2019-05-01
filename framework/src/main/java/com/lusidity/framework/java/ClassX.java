/*
 * Copyright (c) 2008-2012, Venio, Inc.
 * All Rights Reserved Worldwide.
 *
 * This computer software is protected by copyright law and international treaties.
 * It may not be duplicated, reproduced, distributed, compiled, executed,
 * reverse-engineered, or used in any other way, in whole or in part, without the
 * express written consent of Venio, Inc.
 *
 * Portions of this computer software also embody trade secrets, patents, and other
 * protected intellectual property of Venio, Inc. and third parties and are subject to
 * applicable laws, regulations, treaties, agreements, and other legal mechanisms.
 */

package com.lusidity.framework.java;

import com.lusidity.framework.exceptions.ApplicationException;
import com.lusidity.framework.text.StringX;
import org.apache.commons.lang3.reflect.TypeUtils;
import org.reflections.Reflections;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

@SuppressWarnings({
    "NonFinalUtilityClass",
    "OverloadedMethodsWithSameNumberOfParameters"
})
public class ClassX {

    private ClassX()
    {
    }

    /**
     * Does the specified class or one of its superclasses implement the specified interface or a derived interface?
     *
     * @param cls   Class to check.
     * @param which Interface for which to check.
     * @return true if the class implements the specified interface or a derived interface.
     */
    public static boolean implementsInterface(Class cls, Class which)
            throws ApplicationException {
        if (!which.isInterface()) {
            throw new ApplicationException("'%s' is not an interface.", which.getName());
        }

        Class fCls = cls;
        while (null != fCls) {
            Class[] classInterfaces = fCls.getInterfaces();
            for (Class classInterface : classInterfaces) {
                //noinspection unchecked
                if (which.isAssignableFrom(classInterface)) {
                    return true;
                }
            }
            fCls = fCls.equals(Object.class) ? null : fCls.getSuperclass();
        }

        return false;
    }

    /**
     * Is the specified type the same as or a sub-class of another type?
     *
     * @param type   Type to check.
     * @param kindOf Type against which to check.
     * @return true if @param{type} is a kind of @param{kindOf}.
     */
    public static boolean isKindOf(Class type, Class kindOf) {
        return (null!=type) && TypeUtils.isAssignable(type, kindOf);
    }

    /**
     * Get field with specified name, regardless of protection level, in the specified class or one of its superclasses.
     *
     * @param name Field name.
     * @return Field, or null if not found.
     */
    public static Field getField(Class cls, String name)
            throws NoSuchFieldException {
        Class searchCls = cls;

        while (null != searchCls) {
            Field[] fields = searchCls.getDeclaredFields();
            for (Field field : fields) {
                String fieldName = field.getName();
                if (fieldName.equals(name)) {
                    return field;
                }
            }
            searchCls = (searchCls.equals(Object.class)) ? null : searchCls.getSuperclass();
        }

        assert (cls != null);
        throw new NoSuchFieldException(
                String.format("Field '%s' not found in '%s' or superclasses.", name, cls.getName())
        );
    }

    /**
     * Can an object of the specified class be instantiated?
     *
     * @param cls Class to check.
     * @return Can the specified class be instantiated?
     */
    public static boolean canInstantiate(Class cls) {
        int modifiers = cls.getModifiers();
        return !(Modifier.isAbstract(modifiers) || Modifier.isInterface(modifiers));
    }

    /**
     * Is the specified class of a numeric type?
     *
     * @param cls Class to check.
     * @return true if the specified class is numeric, otherwise false.
     */
    @SuppressWarnings("OverlyComplexMethod")
    public static boolean isNumeric(Class cls) {
        return cls.equals(short.class) || cls.equals(int.class) || cls.equals(long.class) || cls.equals(float.class) ||
                cls.equals(double.class) || cls.equals(Byte.class) || cls.equals(Double.class) ||
                cls.equals(Float.class) || cls.equals(Long.class) || cls.equals(Short.class) ||
                cls.equals(Integer.class);
    }

    /**
     * Is the specified object of a numeric type?
     *
     * @param o Object to check.
     * @return true if the specified object is numeric, otherwise false.
     */
    @SuppressWarnings("unused")
    public static boolean isNumeric(Object o) {
        return ClassX.isNumeric(o.getClass());
    }

    /**
     * Is the specified class of a Boolean type?
     *
     * @param cls Class to check.
     * @return true if the specified class is Boolean, otherwise false.
     */
    public static boolean isBoolean(Class cls) {
        return cls.equals(boolean.class) || cls.equals(Boolean.class);
    }

    /**
     * Is the specified object of a Boolean type?
     *
     * @param o Object to check.
     * @return true if the specified object is Boolean, otherwise false.
     */
    @SuppressWarnings("unused")
    public static boolean isBoolean(Object o) {
        return ClassX.isBoolean(o.getClass());
    }

    public static Class getClassFromNameSafely(String clsName) {
        Class cls;
        try {
            String name = StringX.replace(clsName, "class ", "");
            cls = Class.forName(name);
        } catch (ClassNotFoundException ignored) {
            cls = null;
        }
        return cls;
    }

    /**
     * Is the specified object an instance of the specified class?
     *
     * @param object Object.
     * @param cls    Class.
     * @return true if the object is an instance of the class.
     */
    public static boolean isKindOf(Object object, Class cls) {
        boolean result = false;
        if((null!=object) && (null!=cls))
        {
            Class objectCls=object.getClass();
            result = ClassX.isKindOf(objectCls, cls);
        }
        return result;
    }

    @SuppressWarnings("unused")
    public static Collection<Class<? extends Annotation>> getClassesAtPathWithAnnotation(String classPath, Class<? extends Annotation> annotationClass) {
        Collection<Class<? extends Annotation>> results = new ArrayList<>();
        Reflections reflections = new Reflections(classPath);
        Set<Class<?>> filtered = reflections.getTypesAnnotatedWith(annotationClass);
        if ((null != filtered) && !filtered.isEmpty()) {
            for (Class<?> annotation : filtered) {
                //noinspection unchecked
                results.add((Class<? extends Annotation>) annotation);
            }
        }
        return results;
    }

    public static <T> Collection<Class<? extends T>> getSubTypesOf(String classPath, Class<T> extendsType) {
        Collection<Class<? extends T>> results = new ArrayList<>();
        Reflections reflections = new Reflections(classPath);
        Set<Class<? extends T>> filtered = reflections.getSubTypesOf(extendsType);
        if ((null!=filtered) && !filtered.isEmpty())
        {
            for (Class<? extends T> sub : filtered)
            {
                results.add(sub);
            }
        }
        return results;
    }

    public static Collection<Field> getAllFields(Class<?> cls) {
        Collection<Field> results = new ArrayList<>();
        for (Class<?> fCls = cls; fCls != null; fCls = fCls.getSuperclass()) {
            Field[] fields = fCls.getDeclaredFields();
            Collections.addAll(results, fields);
        }

        return results;
    }

    public static boolean isAbstract(Class cls) {
        return Modifier.isAbstract(cls.getModifiers());
    }

    public static boolean isInterface(Class cls) {
        return Modifier.isInterface(cls.getModifiers());
    }
}
