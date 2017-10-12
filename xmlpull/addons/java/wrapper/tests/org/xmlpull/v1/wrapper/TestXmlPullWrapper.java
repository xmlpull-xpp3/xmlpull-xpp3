/* -*-             c-basic-offset: 4; indent-tabs-mode: nil; -*-  //------100-columns-wide------>|*/
// for license see accompanying LICENSE_TESTS.txt file (available also at http://www.xmlpull.org)

package org.xmlpull.v1.wrapper;

//import junit.framework.Test;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.wrapper.XmlPullParserWrapper;
import org.xmlpull.v1.wrapper.XmlPullWrapperFactory;
import org.xmlpull.v1.wrapper.XmlSerializerWrapper;

/**
 * Test some wrapper utility operations.
 *
 * @author <a href="http://www.extreme.indiana.edu/~aslom/">Aleksander Slominski</a>
 */
public class TestXmlPullWrapper extends TestCase {
    private XmlPullWrapperFactory wrappedFactory;

    public static void main (String[] args) {
        junit.textui.TestRunner.run (new TestSuite(TestXmlPullWrapper.class));
    }


    public TestXmlPullWrapper(String name) {
        super(name);
    }

    protected void setUp() throws XmlPullParserException {
        wrappedFactory = wrappedFactory.newInstance();
        wrappedFactory.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true);
        assertEquals(true, wrappedFactory.getFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES));
        assertEquals(false, wrappedFactory.getFeature(XmlPullParser.FEATURE_VALIDATION));
    }

    public void testSimple() throws IOException, XmlPullParserException {
        XmlPullWrapperFactory wrapperFactory = XmlPullWrapperFactory.newInstance(
            System.getProperty(XmlPullParserFactory.PROPERTY_NAME),
            XmlPullParserFactory.class);
        wrapperFactory.setNamespaceAware(true);
        XmlSerializerWrapper xs = wrapperFactory.newSerializerWrapper();
        StringWriter sw = new StringWriter();
        xs.setOutput(sw);
        xs.startDocument("UTF-8", Boolean.TRUE);
        xs.startTag("test");
        xs.attribute("fooAttr", "fooValue");
        final String NS = "http://tempuri.org/xmlpull-test";
        xs.setCurrentNamespaceForElements(NS);
        final String MSG = "World & Universe!";
        xs.element("hello", MSG);
        xs.setCurrentNamespaceForElements(null);
        xs.endTag("test");
        xs.endDocument();

        String s = sw.toString();
        //System.out.println(getClass()+" s="+s);
        XmlPullParserWrapper pw = wrapperFactory.newPullParserWrapper();
        pw.setInput(new StringReader(s));
        pw.nextStartTag("test");
        assertEquals(pw.getAttributeValue("fooAttr"), "fooValue");
        pw.nextTag();
        assertTrue(pw.matches(XmlPullParser.START_TAG, NS, "hello"));
        assertEquals(pw.nextText(NS, "hello"), MSG);
        assertTrue(pw.matches(XmlPullParser.END_TAG, null, "hello"));
        pw.nextEndTag("test");
    }



    public void testSerializeEvent() throws IOException, XmlPullParserException
    {

        //final String XML ="<html xmlns=\"http://www.w3.org/1999/xhtml\"><body /></html>";
        final String NS = "http://exmaple.com/foofoo";
        final String XHTML_NS = "http://www.w3.org/1999/xhtml";
        final String XML = "<m:blog  xmlns:m='"+NS+"' xmlns='"+XHTML_NS+"'><html /></m:blog>";

        XmlPullWrapperFactory wf = XmlPullWrapperFactory.newInstance();
        wf.setNamespaceAware(true);

        XmlPullParserWrapper pp = wf.newPullParserWrapper();
        pp.setInput( new StringReader(XML) );

        XmlSerializerWrapper ser = wf.newSerializerWrapper();
        StringWriter sw = new StringWriter();
        ser.setOutput(sw);
        //ser.setCurrentNamespaceForElements("http://www.w3.org/1999/xhtml");
        //ser.startTag("p").attribute("class", "abstract").text("Hello");
        //ser.endTag("p");

        while (pp.nextToken() != XmlPullParser.END_DOCUMENT) {
            ser.event(pp);
        }
        //System.out.println(getClass()+" sw="+sw);
        String s = sw.toString();
        pp.setInput(new StringReader( s ));
        //assertEquals(

        pp.nextStartTag(NS, "blog");
        int start = pp.getNamespaceCount(pp.getDepth() - 1);
        int end = pp.getNamespaceCount(pp.getDepth());
        assertEquals(2, end - start);
        assertEquals("m", pp.getNamespacePrefix(start));
        assertEquals(NS, pp.getNamespaceUri(start));
        assertEquals(null, pp.getNamespacePrefix(start+1));
        assertEquals(XHTML_NS, pp.getNamespaceUri(start+1));
        pp.nextStartTag(XHTML_NS, "html");

    }


    public void testPI() throws IOException, XmlPullParserException {
        final String PI_TARGET = "xml-stylesheet";
        final String PI_DATA = "href='test.css' type='text/css'";
        testPI(PI_TARGET, " ", PI_DATA);
        testPI(PI_TARGET, "\n", PI_DATA);
        testPI(PI_TARGET, "\r", PI_DATA);
        testPI(PI_TARGET, "\t", PI_DATA);
        testPI(PI_TARGET, "  ", PI_DATA);
        testPI(PI_TARGET, "  \n \n\r\n", PI_DATA);
        testPI(PI_TARGET, " ", null);
        testPI(PI_TARGET, "  \n \n\r", null);
        testPI(PI_TARGET, null, null); // TODO FIXME add when next release of XPP3 is fixed
    }

    public void testPI(final String piTarget,
                       final String s,
                       final String piData)
        throws IOException, XmlPullParserException
    {
        final String PI = piTarget + (s != null ? s : "") + (piData != null ? piData : "");
        final String XML_TEST_PI =
            "<tag><?"+PI+"?></tag>";
        final String PI_NORMALIZED = normalized(PI);

        XmlPullParserWrapper pw = wrappedFactory.newPullParserWrapper();
        pw.setInput(new StringReader(XML_TEST_PI));

        assertEquals(XmlPullParser.START_TAG, pw.next());
        assertEquals("tag", pw.getName());
        assertEquals(XmlPullParser.PROCESSING_INSTRUCTION, pw.nextToken());
        assertEquals(printable(PI_NORMALIZED), printable(pw.getText()));
        assertEquals(PI_NORMALIZED, pw.getText());
        assertEquals(printable(piTarget), printable(pw.getPITarget()));
        assertEquals(piTarget, pw.getPITarget());
        if(piData != null) {
            assertEquals(printable(piData), printable(pw.getPIData()));
            assertEquals(piData, pw.getPIData());
        }
        assertEquals(pw.next(), XmlPullParser.END_TAG);
        assertEquals("tag", pw.getName());

        assertEquals(pw.next(), XmlPullParser.END_DOCUMENT);
    }

//    public void testXsdTypes() throws IOException, XmlPullParserException {
//        XmlSerializerWrapper xs = wrappedFactory.newSerializerWrapper();
//        Writer out = new StringWriter();
//        xs.setOutput(out);
//        xs.startTag("test");
//        xs.writeStringElement(null, "s1", "foo");
//        xs.writeStringElement(null, "s2", "");
//        xs.writeStringElement(null, "s1", null);
//        xs.writeIntElement(null, "i1", 0);
//        xs.writeIntElement(null, "i2", 100);
//        xs.writeIntElement(null, "imax", Integer.MAX_VALUE);
//        xs.writeIntElement(null, "imin", Integer.MIN_VALUE);
//        xs.writeDoubleElement(null, "d1", 0.0);
//        xs.writeDoubleElement(null, "d2", -100.0);
//        xs.writeFloatElement(null, "f1", 7.54f);
//        xs.endDocument();
//        //System.err.println(getClass()+" s="+out.toString());
//
//        XmlPullParserWrapper pw = wrappedFactory.newPullParserWrapper();
//        pw.setInput(new StringReader(out.toString()));
//        pw.nextStartTag("test");
//        pw.nextTag();
//        String s1 = pw.readStringElemet(null, "s1");
//        assertEquals("foo", s1);
//        pw.nextTag();
//        String s2 = pw.readStringElemet(null, "s2");
//        assertEquals("", s2);
//        pw.nextTag();
//        s1 = pw.readStringElemet(null, "s1");
//        assertEquals(null, s1);
//        pw.nextTag();
//        assertEquals(0, pw.readIntElement(null, "i1"));
//        pw.nextTag();
//        assertEquals(100, pw.readIntElement(null, "i2"));
//        pw.nextTag();
//        assertEquals(Integer.MAX_VALUE, pw.readIntElement(null, "imax"));
//        pw.nextTag();
//        assertEquals(Integer.MIN_VALUE, pw.readIntElement(null, "imin"));
//        pw.nextTag();
//        assertEquals(0.0, pw.readDoubleElement(null, "d1"), 0.00001);
//        pw.nextTag();
//        assertEquals(-100.0, pw.readDoubleElement(null, "d2"), 0.00001);
//        pw.nextTag();
//        assertEquals(7.54f, pw.readFloatElement(null, "f1"), 0.0000000001f);
//    }


    private static String printable(char ch) {
        if(ch == '\n') {
            return "\\n";
        } else if(ch == '\r') {
            return "\\r";
        } else if(ch == '\t') {
            return "\\t";
        } if(ch > 127 || ch < 32) {
            StringBuffer buf = new StringBuffer("\\u");
            String hex = Integer.toHexString((int)ch);
            for (int i = 0; i < 4-hex.length(); i++)
            {
                buf.append('0');
            }
            buf.append(hex);
            return buf.toString();
        }
        return ""+ch;
    }

    private static String printable(String s) {
        if(s == null) return null;
        StringBuffer buf = new StringBuffer();
        for(int i = 0; i < s.length(); ++i) {
            buf.append(printable(s.charAt(i)));
        }
        s = buf.toString();
        return s;
    }

    private static String normalized(String s) {
        if(s == null) return null;
        StringBuffer buf = new StringBuffer();
        boolean seenCR = false;
        for(int i = 0; i < s.length(); ++i) {
            char ch = s.charAt(i);
            if(ch == '\r') {
                buf.append('\n');
                seenCR = true;
            } else if(ch == '\n') {
                if( !seenCR ) {
                    buf.append(ch);
                }
                seenCR = false;
            } else {
                buf.append(ch);
                seenCR = false;
            }
        }
        s = buf.toString();
        return s;
    }

}

