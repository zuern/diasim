/*******************************************************************************
 * Copyright (c) 2004, 2006 The Board of Trustees of Stanford University.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU General Public License
 * which is available at http://www.gnu.org/licenses/gpl.txt.
 *******************************************************************************/
package csli.util;

import java.lang.reflect.Constructor;

/**
 * @author alexgru
 * 
 * Has helper methods which descendents can use to make instances of particular
 * objects
 */
public class InstanceFactory {

    /**
     * Create a new instance of the an object with the passed in class name and
     * passing the arguments passed in to the constructor
     * 
     * @param className
     *            the name of the class to construct
     * @param consArgs
     *            an array of the arguments, or null if no arguments; see
     *            convenience constructors if you don't want to have to make the
     *            array yourself Note, uses the class types of the passed in
     *            arguments to find the constructor
     * @return a new instance of hte object
     */
    public static Object newInstance(String className, Object[] consArgs) {
        if (consArgs == null) {
            consArgs = new Object[0];
        }
        int numArgs = consArgs.length;
        Class[] paramTypes = new Class[numArgs];

        for (int i = 0; i < consArgs.length; i++) {
            Object arg = consArgs[i];
            paramTypes[i] = arg.getClass();
        }

        return newInstance(className, consArgs, paramTypes);
    }

    /**
     * Same as newInstance, only returns null if class doesn't exist, without
     * throwing an exception.
     */
    public static Object newInstanceIfExists(String className, Object[] consArgs) {
        if (consArgs == null) {
            consArgs = new Object[0];
        }
        int numArgs = consArgs.length;
        Class[] paramTypes = new Class[numArgs];

        for (int i = 0; i < consArgs.length; i++) {
            Object arg = consArgs[i];
            paramTypes[i] = arg.getClass();
        }

        try {
            Class newClass = Class.forName(className);
            Constructor constructor = newClass.getConstructor(paramTypes);
            return constructor.newInstance(consArgs);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Create a new instance of the an object with the passed in class name and
     * passing the arguments passed in to the constructor
     * 
     * @param className
     *            the name of the class to construct
     * @param consArgs
     *            an array of the arguments, or null if no arguments; see
     *            convenience constructors if you don't want to have to make the
     *            array yourself
     * @param paramTypes
     *            the param types of the constructor to invoke
     * @return a new instance of hte object
     */
    public static Object newInstance(String className, Object[] consArgs,
            Class[] paramTypes) {

        try {
            Class newClass = Class.forName(className);

            Constructor constructor = newClass.getConstructor(paramTypes);
            return constructor.newInstance(consArgs);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static <T> T newInstance(Class<T> myClass) {
        return newInstance(myClass, null, null);
    }

    public static <T> T newInstance(Class<T> myClass, Object[] consArgs,
            Class[] paramTypes) {
        try {
            Constructor<T> constructor = myClass.getConstructor(paramTypes);
            return constructor.newInstance(consArgs);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Object newInstance(String className) {
        return newInstance(className, null);
    }

    public static Object newInstance(String className, Object arg1) {
        Object[] args = { arg1 };
        return newInstance(className, args);
    }

    public static Object newInstance(String className, Object arg1, Object arg2) {
        Object[] args = { arg1, arg2 };
        return newInstance(className, args);
    }

    public static Object newInstance(String className, Object arg1,
            Object arg2, Object arg3) {
        Object[] args = { arg1, arg2, arg3 };
        return newInstance(className, args);
    }

    public static Object newInstance(String className, Object arg1,
            Object arg2, Object arg3, Object arg4) {
        Object[] args = { arg1, arg2, arg3, arg4 };
        return newInstance(className, args);
    }

    public static Object newInstance(String className, Object arg1,
            Object arg2, Object arg3, Object arg4, Object arg5) {
        Object[] args = { arg1, arg2, arg3, arg4, arg5 };
        return newInstance(className, args);
    }

    public static Object newInstance(String className, Object arg1,
            Object arg2, Object arg3, Object arg4, Object arg5, Object arg6) {
        Object[] args = { arg1, arg2, arg3, arg4, arg5, arg6 };
        return newInstance(className, args);
    }
}