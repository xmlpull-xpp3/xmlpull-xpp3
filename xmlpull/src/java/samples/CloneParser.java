/* -*-             c-basic-offset: 4; indent-tabs-mode: nil; -*-  //------100-columns-wide------>|*/
// for license please see accompanying LICENSE.txt file (available also at http://www.xmlpull.org/)

//import CloneableMXParser;
import java.io.CharArrayReader;
import java.io.Reader;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

/**
 * An example to demonstrate how cloneable parser can be used.
 *
 * @author <a href="http://www.extreme.indiana.edu/~aslom/">Aleksander Slominski</a>
 */
class CloneParser {
    
    public static class CloenableCharArrayReader extends CharArrayReader implements Cloneable {
        public CloenableCharArrayReader(char buf[]) {
            super(buf);
        }
        
        public Object clone() throws CloneNotSupportedException
        {
            CloenableCharArrayReader cloned = (CloenableCharArrayReader) super.clone();
            cloned.buf = (char[]) buf.clone();
            //cloned.buf = buf.clone();
            //UGLY UGLY UGLY ...
            cloned.lock = cloned;
            return cloned;
        }
    }
    
    public static void main(String[] args) throws Exception
    {
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance(
            System.getProperty(XmlPullParserFactory.PROPERTY_NAME), null);
        //factory.setNamespaceAware(true);
        factory.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true);
        
        XmlPullParser mpp = factory.newPullParser();
        
        final String XML = "<foo><baz>bar</baz><moo>maz</moo></foo>";
        
        //RecordableXmlPullParser mpp = new RecordableXmlPullParser(xpp);
        //CloneableMXParser mpp = new CloneableMXParser();
        System.out.println("parser implementation class is "+mpp.getClass());
        Reader cpp = new CloenableCharArrayReader(XML.toCharArray());
        mpp.setInput(cpp);
        mpp.next(); print(mpp);
        mpp.next(); print(mpp);
        System.out.println(">>> SPLIT POINT "+mpp);
        //CloneableMXParser m2 = (CloneableMXParser) mpp.clone();
        XmlPullParser m2 = cloneParserIfCloneable(mpp);
        mpp.next(); print(mpp);
        mpp.next(); print(mpp);
        mpp.next(); print(mpp);
        while(mpp.getEventType() != XmlPullParser.END_DOCUMENT) {
            mpp.next(); print(mpp);
        }
        System.out.println(">>> CLONED PARSER "+m2);
        //mpp.rewind();
        while(m2.getEventType() != XmlPullParser.END_DOCUMENT) {
            m2.next(); print(m2);
        }
        System.out.println(">>> END");
    }
    
    private static XmlPullParser cloneParserIfCloneable(XmlPullParser pp)
        throws CloneNotSupportedException
    {
        
        if(pp instanceof java.lang.Cloneable) {
            // due to design decisions Cloneable is emtpy interface without clone() method
            //use reflection to call clone() -- this is getting ugly!!!!
            // more background on this in http://www.artima.com/intv/issues3.html "The clone Dilemma"
            Object o;
            try {
                o = pp.getClass().getMethod("clone", null).invoke(pp, null);
            } catch (Exception e) {
                CloneNotSupportedException ee =
                    new CloneNotSupportedException("failed to call clone() on  "+pp+":"+e);
                ee.initCause(e);
                throw ee;
            }
            return (XmlPullParser) o;
        }
        throw new CloneNotSupportedException(
            "could not clone pull parser as it does not implement Cloneable "+pp.getClass());
    }
    
    private static void print(XmlPullParser pp) throws XmlPullParserException {
        int type = pp.getEventType();
        System.out.print(XmlPullParser.TYPES[type]+" ");
        if(type == XmlPullParser.START_TAG || type == XmlPullParser.END_TAG) {
            System.out.print(pp.getName());
        } else if(type == XmlPullParser.TEXT) {
            System.out.print("'"+pp.getText()+"'");
        }
        System.out.println();
    }
    
}

