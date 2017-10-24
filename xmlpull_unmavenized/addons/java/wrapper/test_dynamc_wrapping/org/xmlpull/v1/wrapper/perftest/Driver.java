/* -*-             c-basic-offset: 4; indent-tabs-mode: nil; -*-  //------100-columns-wide------>|*/
// for license see accompanying LICENSE_TESTS.txt file (available also at http://www.xmlpull.org)

package org.xmlpull.v1.wrapper.perftest;

//import junit.framework.Test;
import java.io.StringReader;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.util.XmlPullUtil;
import org.xmlpull.v1.wrapper.XmlPullParserWrapper;
import org.xmlpull.v1.wrapper.XmlPullWrapperFactory;

/**
 * Test some wrapper utility operations.
 *
 * @author <a href="http://www.extreme.indiana.edu/~aslom/">Aleksander Slominski</a>
 */
public class Driver
{

    public static void main (String[] args) throws Exception
    {
        final int PASSES = 20;
        final int REPEAT = 5 * 10000;

        System.err.println("starting tests with PASSES="+PASSES+" REPEAT="+REPEAT);
        long startDirect=-1,endDirect=-2,startDynamicWrap=-1,endDynamicWrap=-2;
        long startStaticWrap=-1,endStaticWrap=-2;
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        factory.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true);
        XmlPullWrapperFactory staticWrapperFactory = XmlPullWrapperFactory.newInstance(factory);
        XmlPullWrapperFactory dynamicWrapperFactory = XmlPullWrapperFactory.newInstance(factory);
        dynamicWrapperFactory.setUseDynamic(true);

        // multiple passes necessary to do some warmup to remove HotSpot influences ...
        for (int count = 0; count < PASSES; count++)
        {
            System.err.println("pass "+(count+1)+" of "+PASSES);
            startDirect = System.currentTimeMillis();

            XmlPullParser pp = factory.newPullParser();
            for (int i = 0; i < REPEAT; i++)
            {
                StringReader sr = new StringReader("<hello>world!</hello>");
                pp.setInput(sr);
                pp.nextTag();
                pp.require(XmlPullParser.START_TAG, null, "hello");
                pp.next();
                XmlPullUtil.nextEndTag(pp);
                pp.require(XmlPullParser.END_TAG, null, "hello");
            }
            endDirect = System.currentTimeMillis();
            System.err.println("direct test took "+(endDirect-startDirect)/1000.0+" seconds");

            startStaticWrap = System.currentTimeMillis();
            XmlPullParserWrapper spw = staticWrapperFactory.newPullWrapper();
            for (int i = 0; i < REPEAT; i++)
            {
                StringReader sr = new StringReader("<hello>world!</hello>");
                spw.setInput(sr);
                spw.nextTag();
                spw.require(XmlPullParser.START_TAG, null, "hello");
                spw.next();
                spw.nextEndTag();
                spw.require(XmlPullParser.END_TAG, null, "hello");
            }
            endStaticWrap = System.currentTimeMillis();
            System.err.println("static wrap test took "+(endStaticWrap-startStaticWrap)/1000.0+" seconds");


            startDynamicWrap = System.currentTimeMillis();
            XmlPullParserWrapper dpw = dynamicWrapperFactory.newPullWrapper();
            for (int i = 0; i < REPEAT; i++)
            {
                StringReader sr = new StringReader("<hello>world!</hello>");
                dpw.setInput(sr);
                dpw.nextTag();
                dpw.require(XmlPullParser.START_TAG, null, "hello");
                dpw.next();
                dpw.nextEndTag();
                dpw.require(XmlPullParser.END_TAG, null, "hello");
            }
            endDynamicWrap = System.currentTimeMillis();
            System.err.println("dyanmic wrap test took "+(endDynamicWrap-startDynamicWrap)/1000.0+" seconds");
        }

        double directSecs = (endDirect - startDirect) /1000.0;
        //System.err.println("direct test took "+directSecs+" seconds");

        {
            double staticWrapSecs = (endStaticWrap - startStaticWrap) /1000.0;
            double staticSpeedup = staticWrapSecs / directSecs;
            double percent = ((long)Math.round((staticSpeedup - 1)* 100.0 * 100.0))/100.0;
            System.err.println("speedup when using direct over static wrap "+staticSpeedup+" ("+percent+"%)");
        }

        {
            double dynamicWrapSecs = (endDynamicWrap - startDynamicWrap) /1000.0;
            //System.err.println("dyanmic wrap test took "+dynamicWrapSecs+" seconds");
            double dynamicSpeedup = dynamicWrapSecs / directSecs;
            double percent = ((long)Math.round((dynamicSpeedup - 1)* 100.0 * 100.0))/100.0;
            System.err.println("speedup when using direct over dynamic wrap "+dynamicSpeedup+" ("+percent+"%)");
        }

        System.err.println("finished");

    }


}

