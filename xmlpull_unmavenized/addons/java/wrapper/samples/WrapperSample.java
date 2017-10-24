/* -*-             c-basic-offset: 4; indent-tabs-mode: nil; -*-  //------100-columns-wide------>|*/
// for license please see accompanying LICENSE.txt file (available also at http://www.xmlpull.org/)

import java.io.StringReader;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.wrapper.XmlPullParserWrapper;
import org.xmlpull.v1.wrapper.XmlPullWrapperFactory;

/**
 * Example how to use wrapper addon to XmlPull API
 *
 * @author <a href="http://www.extreme.indiana.edu/~aslom/">Aleksander Slominski</a>
 */
public class WrapperSample {
    /**
     *
     */
    public static void main(String[] args) throws Exception
    {
        StringReader sr = new StringReader("<hello>world!</hello>");
        XmlPullParserWrapper pw = XmlPullWrapperFactory.newInstance().newPullParserWrapper();
        pw.setInput(sr);
        pw.nextTag();
        pw.require(XmlPullParser.START_TAG, null, "hello");
        pw.next();
        pw.nextEndTag();
        pw.require(XmlPullParser.END_TAG, null, "hello");
    }
}
