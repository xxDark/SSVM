package sun.reflect;

import java.lang.reflect.*;

public class Reflection {
    public static Class<?> getCallerClass() { return null; }
    public static Class<?> getCallerClass(int depth) { return null; }
    public static int getClassAccessFlags(Class<?> c) { return 0; }
    public static boolean quickCheckMemberAccess(Class<?> memberClass, int modifiers) { return false; }
    public static void ensureMemberAccess(Class<?> currentClass, Class<?> memberClass, Object target,int modifiers) {}
    public static boolean verifyMemberAccess(Class<?> currentClass, Class<?> memberClass, Object target, int modifiers) { return true; }
    public static synchronized void registerFieldsToFilter(Class<?> containingClass, String ... fieldNames) {}
    public static synchronized void registerMethodsToFilter(Class<?> containingClass, String ... methodNames) {}
    public static Field[] filterFields(Class<?> containingClass, Field[] fields) { return null; }
    public static Method[] filterMethods(Class<?> containingClass, Method[] methods) { return null; }
    public static boolean isCallerSensitive(Method m) { return false; }
}
