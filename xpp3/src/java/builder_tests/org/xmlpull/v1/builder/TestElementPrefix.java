/* -*-             c-basic-offset: 4; indent-tabs-mode: nil; -*-  //------100-columns-wide------>|*/
// for license see accompanying LICENSE_TESTS.txt file (available also at http://www.xmlpull.org)

package org.xmlpull.v1.builder;


//import junit.framework.Test;
import java.io.IOException;
import java.io.StringReader;
import java.util.Iterator;
import junit.framework.TestSuite;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.builder.XmlElement;
import org.xmlpull.v1.builder.XmlInfosetBuilder;

/**
 * Test preferred hint prefix problem
 * http://www.extreme.indiana.edu/bugzilla/show_bug.cgi?id=169
 *
 * @author <a href="http://www.extreme.indiana.edu/~aslom/">Aleksander Slominski</a>
 */
public class TestElementPrefix extends BuilderUtilTestCase {
    private XmlInfosetBuilder builder;
    
    public static void main (String[] args) {
        junit.textui.TestRunner.run (new TestSuite(TestElementPrefix.class));
    }
    
    public TestElementPrefix(String name) {
        super(name);
    }
    
    protected void setUp() throws XmlPullParserException {
        builder = XmlInfosetBuilder.newInstance();
    }
    
    protected void tearDown() {
    }
    
    private static String getXml(String prefix) {
        if(prefix != null && prefix.length() > 0) {
            prefix += ':';
        } else {
            prefix="";
        }
        return //final String INPUT_XML =
            "<"+prefix+"Assertion"+
            " xmlns=\"urn:oasis:names:tc:SAML:1.0:assertion\""+
            " xmlns:saml=\"urn:oasis:names:tc:SAML:1.0:assertion\""+
            " xmlns:samlp=\"urn:oasis:names:tc:SAML:1.0:protocol\""+
            " AssertionID=\"c08274b5e85877cbda6cc293ca0deba6\""+
            " IssueInstant=\"2004-01-03T05:49:16.840Z\">"+
            " </"+prefix+"Assertion>";
    }
    
    public void testElementPrefix() throws IOException, XmlPullParserException
    {
        testElementPrefix("");
        testElementPrefix(null);
        testElementPrefix("saml");
    }
    
    private void testElementPrefix(String prefix) throws IOException, XmlPullParserException
    {
        String xml = getXml(prefix);
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance(
            System.getProperty(XmlPullParserFactory.PROPERTY_NAME), null);
        factory.setNamespaceAware(true);
        XmlPullParser pp = factory.newPullParser();
        pp.setInput(new StringReader(xml));
        //System.out.println("\nfactory="+factory.getClass()+" pp="+pp.getClass());
        //XmlElement root = builder.parseFragmentFromReader(new StringReader(xml));
        pp.next();
        XmlElement root = builder.parseFragment(pp);
        //System.out.println("\ndeclared namespaces in xml='"+xml+"'");
        //        for(Iterator i = root.namespaces(); i.hasNext(); ) {
        //            XmlNamespace n = (XmlNamespace) i.next();
        //            System.out.println(getClass()+" "+n);
        //        }
        String expectedPrefix = prefix;
        if("".equals(prefix)) {
            expectedPrefix = null;
        }
        assertEquals(expectedPrefix, root.getNamespace().getPrefix());
        
        String serialized = builder.serializeToString(root);
        //System.out.println("\n"+getClass()+" "+serialized);
        
        root = builder.parseFragmentFromReader(new StringReader(serialized));
        assertEquals(expectedPrefix, root.getNamespace().getPrefix());
        
        
    }
    
    
    private final String NESTED_PREFIX_XML =
        "<b:a xmlns:f=\"foo\" xmlns:b=\"foo\">"+
        "<f:child/>"+
        "</b:a>";
    
    /**
     * As described in http://www.extreme.indiana.edu/bugzilla/show_bug.cgi?id=169
     * tesitng case when nested element needs to override prefix
     */
    public void testNestedPrefix() throws IOException, XmlPullParserException
    {
        XmlElement root = builder.parseFragmentFromReader(new StringReader(NESTED_PREFIX_XML));
        testNestedPrefix(root);
        
        String serialized = builder.serializeToString(root);
        //System.out.println("\n"+getClass()+" "+serialized);
        
        root = builder.parseFragmentFromReader(new StringReader(serialized));
        testNestedPrefix(root);
        
    }
    
    public void testNestedPrefix(XmlElement root) throws IOException, XmlPullParserException
    {
        assertEquals("b", root.getNamespace().getPrefix());
        XmlElement child = root.element(1);
        assertEquals("f", child.getNamespace().getPrefix());
    }
    
    
    private final String ATTR_DEF_PREFIX_XML =
        "<a f:attr='value' xmlns:f=\"foo\" xmlns=\"foo\">"+
        "<child/>"+
        "</a>";
    
    private final String ATTR_DEF_PREFIX_XML_2 =
        "<f:a f:attr='value' xmlns:f=\"foo\" xmlns=\"foo\">"+
        "<f:child/>"+
        "</f:a>";
    
    private final String ATTR_DEF_PREFIX_XML_3 =
        "<f:a f:attr='value' xmlns=\"foo\" xmlns:f=\"foo\">"+
        "<f:child/>"+
        "</f:a>";
    
    /**
     * As described in http://www.extreme.indiana.edu/bugzilla/show_bug.cgi?id=241
     * tesitng case when attribtue gets a new prefix that affected endTag prefix
     */
    public void testDefAttrib() throws IOException, XmlPullParserException
    {
        testDefAttrib(ATTR_DEF_PREFIX_XML, null);
        testDefAttrib(ATTR_DEF_PREFIX_XML_2, "f");
        testDefAttrib(ATTR_DEF_PREFIX_XML_3, "f");
    }
    
    public void testDefAttrib(String XML, String prefix) throws IOException, XmlPullParserException
    {
        XmlElement root = builder.parseFragmentFromReader(new StringReader(XML));
        testDefAttrib(root, prefix);
        
        String serialized = builder.serializeToString(root);
        //System.out.println("\n"+getClass()+" "+serialized);
        
        root = builder.parseFragmentFromReader(new StringReader(serialized));
        testDefAttrib(root, prefix);
        
    }
    
    public void testDefAttrib(XmlElement root, String prefix) throws IOException, XmlPullParserException
    {
        assertEquals(prefix, root.getNamespace().getPrefix());
        XmlElement child = root.element(1);
        assertEquals(prefix, child.getNamespace().getPrefix());
    }
    
}

