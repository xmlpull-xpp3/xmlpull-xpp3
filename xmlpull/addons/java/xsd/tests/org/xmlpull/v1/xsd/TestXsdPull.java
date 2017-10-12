/* -*-             c-basic-offset: 4; indent-tabs-mode: nil; -*-  //------100-columns-wide------>|*/
// for license see accompanying LICENSE_TESTS.txt file (available also at http://www.xmlpull.org)

package org.xmlpull.v1.xsd;

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
 * Test XSD read and write functionality.
 *
 * @author <a href="http://www.extreme.indiana.edu/~aslom/">Aleksander Slominski</a>
 */
public class TestXsdPull extends TestCase {
    private XsdPullFactory xsdFactory;

    public static void main (String[] args) {
        junit.textui.TestRunner.run (new TestSuite(TestXsdPull.class));
    }


    public TestXsdPull(String name) {
        super(name);
    }

    protected void setUp() throws XmlPullParserException {
        xsdFactory = xsdFactory.newInstance();
        xsdFactory.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true);
        assertEquals(true, xsdFactory.getFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES));
        assertEquals(false, xsdFactory.getFeature(XmlPullParser.FEATURE_VALIDATION));
    }

    public void testXsdTypes() throws IOException, XmlPullParserException {
        XsdSerializer xs = xsdFactory.newXsdSerializer();
        Writer out = new StringWriter();
        xs.setOutput(out);
        xs.startTag("test");
        xs.writeXsdStringElement(null, "s1", "foo");
        xs.writeXsdStringElement(null, "s2", "");
        xs.writeXsdStringElement(null, "s1", null);
        xs.writeXsdIntElement(null, "i1", 0);
        xs.writeXsdIntElement(null, "i2", 100);
        xs.writeXsdIntElement(null, "imax", Integer.MAX_VALUE);
        xs.writeXsdIntElement(null, "imin", Integer.MIN_VALUE);
        xs.writeXsdDoubleElement(null, "d1", 0.0);
        xs.writeXsdDoubleElement(null, "d2", -100.0);
        xs.writeXsdFloatElement(null, "f1", 7.54f);
        xs.endDocument();
        //System.err.println(getClass()+" s="+out.toString());

        XsdPullParser pw = xsdFactory.newXsdPullParser();
        pw.setInput(new StringReader(out.toString()));
        pw.nextStartTag("test");
        //pw.nextTag();
        String s1 = pw.nextXsdStringElement(null, "s1");
        assertEquals("foo", s1);
        String s2 = pw.nextXsdStringElement(null, "s2");
        assertEquals("", s2);
        s1 = pw.nextXsdStringElement(null, "s1");
        assertEquals(null, s1);
        assertEquals(0, pw.nextXsdIntElement(null, "i1"));
        assertEquals(100, pw.nextXsdIntElement(null, "i2"));
        assertEquals(Integer.MAX_VALUE, pw.nextXsdIntElement(null, "imax"));
        assertEquals(Integer.MIN_VALUE, pw.nextXsdIntElement(null, "imin"));
        assertEquals(0.0, pw.nextXsdDoubleElement(null, "d1"), 0.00001);
        assertEquals(-100.0, pw.nextXsdDoubleElement(null, "d2"), 0.00001);
        assertEquals(7.54f, pw.nextXsdFloatElement(null, "f1"), 0.0000000001f);
        pw.nextEndTag("test");
    }


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

