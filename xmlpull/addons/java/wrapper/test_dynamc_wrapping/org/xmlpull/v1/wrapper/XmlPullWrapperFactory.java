/* -*-             c-basic-offset: 4; indent-tabs-mode: nil; -*-  //------100-columns-wide------>|*/
// for license please see accompanying LICENSE.txt file (available also at http://www.xmlpull.org/)

package org.xmlpull.v1.wrapper;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.util.XmlPullUtil;
import org.xmlpull.v1.wrapper.dynamic_proxy.DynamicXmlPullParserWrapper;
import org.xmlpull.v1.wrapper.classic.StaticXmlPullParserWrapper;

/**
 * Handy functions that combines XmlPull API into higher level functionality.
 * <p>NOTE: returned wrapper object is <strong>not</strong> multi-thread safe
 *
 * @author <a href="http://www.extreme.indiana.edu/~aslom/">Aleksander Slominski</a>
 */

public class XmlPullWrapperFactory {
    private final static boolean DEBUG = false;
    protected ClassLoader classLoader;
    protected XmlPullParserFactory factory;
    protected boolean useDynamic;

    public static XmlPullWrapperFactory newInstance() throws XmlPullParserException
    {
        //TODO: make into real pluggable factory service (later ...)?
        return new XmlPullWrapperFactory(null);
    }

    public static XmlPullWrapperFactory newInstance(XmlPullParserFactory factory)
        throws XmlPullParserException
    {
        return new XmlPullWrapperFactory(factory);
    }

    // ------------ IMPLEMENTATION

    protected XmlPullWrapperFactory(XmlPullParserFactory factory) throws XmlPullParserException {
        if(factory != null) {
            this.factory = factory;
        } else {
            this.factory = XmlPullParserFactory.newInstance();
        }
    }
    public void setUseDynamic(boolean enable) { useDynamic = enable; };
    public boolean getUseDynamic() { return useDynamic; };

    public XmlPullParserWrapper newPullWrapper() throws XmlPullParserException {
        XmlPullParser pp = factory.newPullParser();
        if(useDynamic) {
            return (XmlPullParserWrapper) DynamicXmlPullParserWrapper.newProxy(pp, classLoader);
        } else {
            return new StaticXmlPullParserWrapper(pp);
        }
    }
}

