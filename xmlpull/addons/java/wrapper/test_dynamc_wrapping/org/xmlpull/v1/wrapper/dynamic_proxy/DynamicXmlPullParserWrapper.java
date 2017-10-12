/* -*-             c-basic-offset: 4; indent-tabs-mode: nil; -*-  //------100-columns-wide------>|*/
// for license please see accompanying LICENSE.txt file (available also at http://www.xmlpull.org/)

package org.xmlpull.v1.wrapper.dynamic_proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.util.XmlPullUtil;
import org.xmlpull.v1.wrapper.XmlPullParserWrapper;

/**
 * Handy functions that combines XmlPull API into higher level functionality.
 * <p>NOTE: returned wrapper object is <strong>not</strong> multi-thread safe
 *
 * @author <a href="http://www.extreme.indiana.edu/~aslom/">Aleksander Slominski</a>
 */

public class DynamicXmlPullParserWrapper {
    private final static boolean DEBUG = false;

    //protected ClassLoader classLoader;
    //protected Class interfaces[]; // = new Class[] {clazz}; //List<Class>
    //protected InvocationHandler handlers[];

    public static XmlPullParserWrapper newProxy(XmlPullParser pp,
                                                ClassLoader classLoader)
        throws XmlPullParserException
    {

        if(classLoader == null) {
            classLoader = pp.getClass().getClassLoader();
        }

        Class interfaces[] = new Class[] {
            //XmlPullWrapper.class,
            XmlPullParserWrapper.class
        };

        //InvocationHandler handlers[] = new InvocationHandler[] {
        //new StaticUtilInvoker(XmlPullUtil.class, pp, XmlPullWrapper.class),
        //new DirectDelegationInvoker(XmlPullParser.class, pp),
        //}
        //InvocationHandler handler = new DirectDelegationInvoker(XmlPullParser.class, pp);
        InvocationHandler handler =
            new StaticUtilInvoker(XmlPullUtil.class, pp, XmlPullParserWrapper.class,
                                  new DirectDelegationInvoker(pp, XmlPullParserWrapper.class, null)
                                 );

        // Create a dynamic proxy for clazz and return it
        return (XmlPullParserWrapper) Proxy.newProxyInstance(
            classLoader,
            interfaces,
            handler);
    }


    // ------------ PRE DEFINED INVOKERS

    // TODO: facotr out stateless part to class that cna be shared for parser isnatnces ...

    protected static class StaticUtilInvoker implements InvocationHandler {
        private Class targetInterface;
        private Class util;
        private Object target;
        private InvocationHandler chainedHandler;
        private Method mInterf;
        private Method mUtil;
        private Object[] zeroArgs = new Object[1];

        public StaticUtilInvoker(Class util, Object o, Class i, InvocationHandler handler)
            throws XmlPullParserException
        {
            this.targetInterface = i;
            this.target = o;
            this.util = util;
            //TODO: check that o implemnts all targetInterface methods!

            zeroArgs[0] = target;

            // use this handler when method we can not handle is called ...
            this.chainedHandler = handler;
            try {
                mInterf = targetInterface.getMethod("nextEndTag", new Class[]{});
                mUtil = util.getMethod("nextEndTag", new Class[]{XmlPullParser.class});
            }
            catch (SecurityException ex)  {
                throw new XmlPullParserException("could not find method", null, ex);
            }
            catch (NoSuchMethodException ex)  {
                throw new XmlPullParserException("could not find method", null, ex);
            }
        }

        public Object invoke(Object proxy, Method method, Object[] args)
            throws Throwable {
            //Object result = null;

            // pre-advice
            if(DEBUG) System.err.println(getClass().getName()+" calling method " + method.getName());

            try {
                //if(method.getDeclaringClass().equals(XmlPullWrapper.class))  {
                if(method.getDeclaringClass() == XmlPullParserWrapper.class)  {
                    //if(method.equals(mInterf)) {

                    // Pass on method call to the object we're proxying
                    //result = method.invoke(target, args);
                    if(args == null || args.length == 0) {
                        return mUtil.invoke(null, zeroArgs);
                    }else {
                        //TODO: optimize it!
                        int size = args != null ? args.length : 0;
                        //TODO: keep pre-allocated - but no reentry allowed (is it OK?) ...
                        Object[] newArgs = new Object[size+1];
                        newArgs[0] = target;
                        for (int i = 0; i < size; i++) {
                            newArgs[i + 1] = args[i];
                        }
                        return mUtil.invoke(null, newArgs);
                    }
                }
                else  {
                    return method.invoke(target, args);
                }
            }
            catch (InvocationTargetException e) {
                //TODO: resolve if it is safe to throw exception .....
                throw e.getTargetException();
            }
            //} else {
            //    result =  chainedHandler.invoke(proxy, method, args);
            //}


            // after-advice

            // result
            //return result;
        }
    }

    protected static class DirectDelegationInvoker implements InvocationHandler {
        private Class targetInterface;
        private Object target;
        private InvocationHandler chainedHandler;

        public DirectDelegationInvoker(Object o, Class i, InvocationHandler handler) {
            this.targetInterface = i;
            this.target = o;
            //TODO: check that o implemnts all targetInterface methods!

            // use this handler when method we can not handle is called ...
            this.chainedHandler = handler;
        }

        public Object invoke(Object proxy, Method method, Object[] args)
            throws Throwable {
            Object result = null;

            // pre-advice
            if(DEBUG) System.err.println(getClass().getName()+" calling method " + method.getName());

            try {
                // Pass on method call to the object we're proxying
                result = method.invoke(target, args);
            }
            catch (InvocationTargetException e) {
                //TODO: resolve if it is safe to throw exception .....
                throw e.getTargetException();
            }

            // after-advice

            // result
            return result;
        }
    }
}
