package org.jboss.aerogear.android.authorization.test.util;

import org.jboss.aerogear.android.core.reflection.FieldNotFoundException;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by summers on 7/20/17.
 */
public class UnitTestUtils {

    public static void setPrivateField(Object target, String fieldName,
                                       Object value) throws NoSuchFieldException,
            IllegalArgumentException, IllegalAccessException {
        List<Field> fields = getAllFields(new ArrayList<Field>(), target.getClass());

        for (Field field : fields) {
            if (field.getName().equals(fieldName)) {
                field.setAccessible(true);
                field.set(target, value);
                return;
            }
        }

        throw new FieldNotFoundException(target.getClass(), fieldName);

    }

    public static Object getPrivateField(Object target, String fieldName)
            throws NoSuchFieldException, IllegalArgumentException,
            IllegalAccessException {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(target);
    }

    public static Object getSuperPrivateField(Object target, String fieldName)
            throws NoSuchFieldException, IllegalArgumentException,
            IllegalAccessException {
        Field field = target.getClass().getSuperclass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(target);
    }

    public static <T> T getPrivateField(Object target, String fieldName,
                                        Class<T> type) throws NoSuchFieldException,
            IllegalArgumentException, IllegalAccessException {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return (T) field.get(target);
    }

    /**
     * This method extracts a named field, replaces it with a spy, and returns
     * the spy.
     *
     * @param target    The object requiring a hot spy injection
     * @param fieldName the field to spy on
     * @param type      The type of the spy
     * @param <T>       the class of object which is being replaced with a spy.
     * @return a spy which has replaced the field from fieldName
     * @throws NoSuchFieldException     if a field is not found. (Thrown from the java reflection API)
     * @throws IllegalArgumentException if an argument is illegal. (Thrown from the java reflection API)
     * @throws IllegalAccessException   if access is exceptional. (Thrown from the java reflection API)
     */
    public static <T> T replaceWithSpy(Object target, String fieldName,
                                       Class<T> type) throws NoSuchFieldException,
            IllegalArgumentException, IllegalAccessException {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        T object = (T) field.get(target);
        object = Mockito.spy(object);
        setPrivateField(target, fieldName, object);
        return object;
    }

    public static List<Field> getAllFields(List<Field> fields, Class<?> type) {
        Collections.addAll(fields, type.getDeclaredFields());

        if (type.getSuperclass() != null) {
            fields = getAllFields(fields, type.getSuperclass());
        }

        return fields;
    }

    public static void callMethod(Object target, String methodName) {
        try {
            Method method = target.getClass().getDeclaredMethod(methodName);
            method.setAccessible(true);
            method.invoke(target);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(UnitTestUtils.class.getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex);
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(UnitTestUtils.class.getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex);
        } catch (InvocationTargetException ex) {
            Logger.getLogger(UnitTestUtils.class.getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex);
        } catch (NoSuchMethodException ex) {
            Logger.getLogger(UnitTestUtils.class.getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex);
        } catch (SecurityException ex) {
            Logger.getLogger(UnitTestUtils.class.getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex);
        }
    }

    public static Object getPrivateEnum(Class<?> klass, String enumName, String constantName) {
        for (Class declaredClass : klass.getDeclaredClasses()) {
            if (declaredClass.getCanonicalName().equals(enumName)) {
                for (Object t : declaredClass.getEnumConstants()) {
                    if (t.toString().equals(constantName)) {
                        return t;
                    }
                }
            }
        }
        return null;
    }
}
