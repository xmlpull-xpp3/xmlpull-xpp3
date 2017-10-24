/* -*-             c-basic-offset: 4; indent-tabs-mode: nil; -*-  //------100-columns-wide------>|*/
// for license see accompanying LICENSE_TESTS.txt file (available also at http://www.xmlpull.org)

package org.xmlpull.v1.builder;

import org.xmlpull.v1.builder.adapter.*;

import junit.framework.Test;
import junit.framework.TestResult;
import junit.framework.TestSuite;
import junit.textui.ResultPrinter;
import junit.textui.TestRunner;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlSerializer;

/**
 * Test XmlPull Builder API.
 *
 * @author <a href="http://www.extreme.indiana.edu/~aslom/">Aleksander Slominski</a>
 */
public class PackageTests extends TestRunner {
    private static boolean runAll;

    public static boolean runnigAllTests() { return runAll; }

    public static void main (String[] args) {
        junit.textui.TestRunner.run (PackageTests.suite());
    }
    
    public PackageTests() {
        super();
    }

    public static Test suite() {
        TestSuite suite = new TestSuite("XmlPull Builder V1 API TESTS");

        //suite.addTestSuite(TestApi.class);
        suite.addTestSuite(TestBuilder.class);
        suite.addTestSuite(TestElementAdapter.class);
        suite.addTestSuite(TestElementPrefix.class);
        return suite;
    }

}

