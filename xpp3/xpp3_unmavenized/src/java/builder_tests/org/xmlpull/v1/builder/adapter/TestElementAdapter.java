/* -*-             c-basic-offset: 4; indent-tabs-mode: nil; -*-  //------100-columns-wide------>|*/
// for license see accompanying LICENSE_TESTS.txt file (available also at http://www.xmlpull.org)

package org.xmlpull.v1.builder.adapter;

import org.xmlpull.v1.builder.*;

//import junit.framework.Test;
import java.io.IOException;
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
public class TestElementAdapter extends BuilderUtilTestCase {
    private XmlInfosetBuilder builder;

    public static void main (String[] args) {
        junit.textui.TestRunner.run (new TestSuite(TestElementAdapter.class));
    }

    public TestElementAdapter(String name) {
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
        "<foo>\n"+
        "    <bar>\n"+
        "        <baz/>\n"+
        "        <cheese/>\n"+
        "        <baz/>\n"+
        "        <cheese/>\n"+
        "        <baz/>\n"+
        "    </bar>\n"+
        "</foo>";


    public void testElementAdapter() throws IOException, XmlPullParserException
    {
        XmlElement foo = builder.newFragment("foo");
        XmlElement bar = foo.addElement("bar");
        bar = new XmlElementAdapter(bar);
        XmlElement baz1 = bar.addElement("baz");
        XmlElement cheese1 = bar.addElement("cheese");
        XmlElement baz2 = bar.addElement("baz");
        baz2 = new XmlElementAdapter(baz2);
        XmlElement cheese2 = bar.addElement("cheese");
        XmlElement baz3 = bar.addElement("baz");
        //System.out.println(getClass()+" "+builder.serializeToString(foo));
    }

    public void testElementAdapterWrapping() throws IOException, XmlPullParserException
    {
        XmlElement el = builder.newFragment("foo");
        FooAdapter foo = (FooAdapter) XmlElementAdapter.castOrWrap(el, FooAdapter.class);
        assertSame(el, foo.getTarget());
        BarAdapter bar = (BarAdapter) XmlElementAdapter.castOrWrap(foo, BarAdapter.class);
        assertSame(el, foo.getTarget());
        assertSame(foo, bar.getTarget());
        FooAdapter foo2 = (FooAdapter) XmlElementAdapter.castOrWrap(bar, FooAdapter.class);
        // make sure that no wrappings are duplicated
        assertSame(foo, foo2);
        assertSame(el, foo.getTarget());
        assertSame(foo, bar.getTarget());

        FooAdapter foo3 = (FooAdapter) XmlElementAdapter.castOrWrap(foo, FooAdapter.class);
        // make sure that no wrappings are duplicated
        assertSame(foo, foo3);
        assertSame(el, foo.getTarget());
        assertSame(foo, bar.getTarget());

        BarAdapter bar2 = (BarAdapter) XmlElementAdapter.castOrWrap(foo3, BarAdapter.class);
        assertSame(bar, bar2);
        assertSame(el, foo.getTarget());
        assertSame(foo, bar.getTarget());
        
    }
    
    private static class FooAdapter extends XmlElementAdapter {
        public FooAdapter(XmlElement el) {
            super(el);
        }
    }

    private static class BarAdapter extends XmlElementAdapter {
        public BarAdapter(XmlElement el) {
            super(el);
        }
    }
    
}

