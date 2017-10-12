/* -*-             c-basic-offset: 4; indent-tabs-mode: nil; -*-  //------100-columns-wide------>|*/
// for license see accompanying LICENSE_TESTS.txt file (available also at http://www.xmlpull.org)

package org.xmlpull.v1.wrapper.junit;

//import junit.framework.Test;
import java.io.IOException;
import java.io.StringReader;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.util.XmlPullWrapper;

/**
 * Test some wrapper utility operations.
 *
 * @author <a href="http://www.extreme.indiana.edu/~aslom/">Aleksander Slominski</a>
 */
public class TestXmlPullWrapper extends TestCase {
    private XmlPullParserFactory factory;

    public static void main (String[] args) {
        junit.textui.TestRunner.run (new TestSuite(TestXmlPullWrapper.class));
    }


    public TestXmlPullWrapper(String name) {
        super(name);
    }

    protected void setUp() throws XmlPullParserException {
        factory = factory.newInstance();
        factory.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true);
        assertEquals(true, factory.getFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES));
        assertEquals(false, factory.getFeature(XmlPullParser.FEATURE_VALIDATION));
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

        XmlPullParser pp = factory.newPullParser();
        XmlPullWrapper pw = new XmlPullWrapper(pp);
        pp.setInput(new StringReader(XML_TEST_PI));

        assertEquals(XmlPullParser.START_TAG, pp.next());
        assertEquals("tag", pp.getName());
        assertEquals(XmlPullParser.PROCESSING_INSTRUCTION, pp.nextToken());
        assertEquals(printable(PI_NORMALIZED), printable(pp.getText()));
        assertEquals(PI_NORMALIZED, pp.getText());
        assertEquals(printable(piTarget), printable(pw.getPITarget()));
        assertEquals(piTarget, pw.getPITarget());
        if(piData != null) {
            assertEquals(printable(piData), printable(pw.getPIData()));
            assertEquals(piData, pw.getPIData());
        }
        assertEquals(pp.next(), XmlPullParser.END_TAG);
        assertEquals("tag", pp.getName());

        assertEquals(pp.next(), XmlPullParser.END_DOCUMENT);
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

