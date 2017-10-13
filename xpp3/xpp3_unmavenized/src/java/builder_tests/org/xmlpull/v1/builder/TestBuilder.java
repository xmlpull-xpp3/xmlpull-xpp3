/* -*-             c-basic-offset: 4; indent-tabs-mode: nil; -*-  //------100-columns-wide------>|*/
// for license see accompanying LICENSE_TESTS.txt file (available also at http://www.xmlpull.org)

package org.xmlpull.v1.builder;


//import junit.framework.Test;
import java.io.IOException;
import java.io.StringReader;
import junit.framework.TestSuite;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.builder.XmlElement;
import org.xmlpull.v1.builder.XmlInfosetBuilder;
import org.xmlpull.v1.builder.adapter.XmlElementAdapter;

/**
 * Test element adapter.
 *
 * @author <a href="http://www.extreme.indiana.edu/~aslom/">Aleksander Slominski</a>
 */
public class TestBuilder extends BuilderUtilTestCase {
    private XmlInfosetBuilder builder;

    public static void main (String[] args) {
        junit.textui.TestRunner.run (new TestSuite(TestBuilder.class));
    }

    public TestBuilder(String name) {
        super(name);
    }

    protected void setUp() throws XmlPullParserException {
        builder = XmlInfosetBuilder.newInstance();
    }

    protected void tearDown() {
    }

    private static final String BASIC_XML =
        "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n"+
        "\n"+
        "<foo ba='test'>\n"+
        "    <bar>\n"+
        "        <baz/>\n"+
        "        <cheese/>\n"+
        "        <baz/>\n"+
        "        <cheese/>\n"+
        "        <baz/>\n"+
        "    </bar>\n"+
        "</foo>";


    public void testRetrieval() throws IOException, XmlPullParserException
    {
        XmlElement foo = builder.parseFragmentFromReader(new StringReader(BASIC_XML));
        assertEquals("foo", foo.getName());
        XmlElement bar = (XmlElement) foo.requiredElementContent().iterator().next();
        assertEquals("bar", bar.getName());
        //System.out.println(getClass()+" "+builder.serializeToString(foo));
    }

    public void testInserBefore() throws IOException, XmlPullParserException
    {
        XmlElement foo = builder.parseFragmentFromReader(new StringReader("<foo><bar/></foo>"));
        assertEquals("foo", foo.getName());
        XmlElement bar = (XmlElement) foo.requiredElementContent().iterator().next();
        assertFalse(foo.element(null, "Header", false) != null);
        assertEquals("bar", bar.getName());
        foo.addElement(0, builder.newFragment("Header"));
        assertTrue(foo.element(null, "Header", false) != null);
        //System.out.println(getClass()+" "+builder.serializeToString(foo));
        XmlElement header = (XmlElement) foo.requiredElementContent().iterator().next();
        assertEquals("Header", header.getName());
    }

    public void testManipulations() throws IOException, XmlPullParserException
    {
        XmlElement foo = builder.parseFragmentFromReader(new StringReader(BASIC_XML));
        assertEquals("foo", foo.getName());
        XmlAttribute att = foo.attribute(null, "ba");
        assertNotNull(att);
        if(att != null) {
            foo.removeAttribute(att);
        }
        att = foo.attribute(null, "ba");
        assertNull(att);
    }
}

