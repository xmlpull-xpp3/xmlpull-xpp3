/* -*-             c-basic-offset: 4; indent-tabs-mode: nil; -*-  //------100-columns-wide------>|*/
// for license please see accompanying LICENSE.txt file (available also at http://www.xmlpull.org/)

package org.xmlpull.v1.wrapper.classic;

import java.io.IOException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.wrapper.XmlPullParserWrapper;
import org.xmlpull.v1.util.XmlPullUtil;

/**
 * This class seemlesly extends exisiting parser implementation by adding new methods
 * (provided by XmlPullUtil) and delegating exisiting methods to parser implementation.
 *
 * @author <a href="http://www.extreme.indiana.edu/~aslom/">Aleksander Slominski</a>
 */
public class StaticXmlPullParserWrapper extends XmlPullParserDelegate
    implements XmlPullParserWrapper
{


    public StaticXmlPullParserWrapper(XmlPullParser pp) {
        super(pp);
    }


    public void nextEndTag() throws XmlPullParserException, IOException {
        XmlPullUtil.nextEndTag(pp);
    }

}

