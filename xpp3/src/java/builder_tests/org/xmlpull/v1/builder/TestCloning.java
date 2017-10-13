/* -*-             c-basic-offset: 4; indent-tabs-mode: nil; -*-  //------100-columns-wide------>|*/
// for license see accompanying LICENSE_TESTS.txt file (available also at http://www.xmlpull.org)

package org.xmlpull.v1.builder;


//import junit.framework.Test;
import java.io.IOException;
import java.io.StringReader;
import java.util.Iterator;
import junit.framework.TestSuite;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.builder.XmlAttribute;
import org.xmlpull.v1.builder.XmlDocument;
import org.xmlpull.v1.builder.XmlElement;
import org.xmlpull.v1.builder.XmlNamespace;
import org.xmlpull.v1.builder.XmlInfosetBuilder;

/**
 * Simple XML cloning tests
 *
 * @author <a href="http://www.extreme.indiana.edu/~aslom/">Aleksander Slominski</a>
 */
public class TestCloning extends BuilderUtilTestCase {
    private XmlInfosetBuilder builder;
    
    public static void main (String[] args) {
        junit.textui.TestRunner.run (new TestSuite(TestCloning.class));
    }
    
    public TestCloning(String name) {
        super(name);
    }
    
    protected void setUp() throws XmlPullParserException {
        builder = XmlInfosetBuilder.newInstance();
    }
    
    //based on Appendix C from Infoset spec http://www.w3.org/TR/2004/REC-xml-infoset-20040204/#example
    private final String XML =
        "<?xml version=\"1.0\"?>\n"+
        "\n"+
        "<msg:message doc:date=\"19990421\"\n"+
        "               xmlns:doc=\"http://doc.example.org/namespaces/doc\"\n"+
        "               xmlns:msg=\"http://message.example.org/\"\n"+
        ">Phone home!<test><inline/><bar><baz>A</baz>\n</bar></test></msg:message>";
    
    public void testCloning() throws IOException, XmlPullParserException, CloneNotSupportedException
    {
        //A document information item.
        XmlDocument doc = builder.parseReader(new StringReader(XML));
        XmlDocument docC = (XmlDocument) doc.clone(); //JDK15 covariant
        String stringified = builder.serializeToString(doc);
        
        // modify - shoul dnot affect clone!
        XmlElement root = doc.getDocumentElement();
        root.setName("foo");
        
        String stringifiedC = builder.serializeToString(docC);
        assertEquals(stringified, stringifiedC);
    }
    
    
}

