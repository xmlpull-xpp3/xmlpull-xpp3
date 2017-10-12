package org.xmlpull.v1.xsd;

//import junit.framework.Test;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.builder.XmlElement;
import org.xmlpull.v1.builder.XmlPullBuilder;
import org.xmlpull.v1.dom2_builder.DOM2XmlPullBuilder;
import org.xmlpull.v1.wrapper.XmlPullWrapperFactory;

/**
 * Test XSD read and write functionality to do very simplified SOAP processing.
 *
 * @author <a href="http://www.extreme.indiana.edu/~aslom/">Aleksander Slominski</a>
 */
public class TestSoapProcessing extends TestCase {
    public final static String SOAP_NS = "http://schemas.xmlsoap.org/soap/envelope/";
    public final static String XSI_NS = "http://www.w3.org/2001/XMLSchema-instance";
    public final static String XSD_NS = "http://www.w3.org/2001/XMLSchema";
    public final static String SAMPLE =
        "<SOAP-ENV:Envelope\n"+
        "     xmlns:SOAP-ENV=\""+SOAP_NS+"\"\n"+
        "     xmlns:xsi=\""+XSI_NS+"\"\n"+
        "     xmlns:xsd=\""+XSD_NS+"\">\n"+
        "     <SOAP-ENV:Header>\n"+
        "       <t:Transaction\n"+
        "           xmlns:t=\"some-URI\"\n"+
        "           xsi:type=\"xsd:int\" mustUnderstand=\"1\">\n"+
        "            5\n"+
        "       </t:Transaction>\n"+
        "     </SOAP-ENV:Header>\n"+
        "     <SOAP-ENV:Body>\n"+
        "        <ns1:getRate xmlns:ns1=\"urn:demo1:exchange\"\n"+
        "           SOAP-ENV:encodingStyle='http://schemas.xmlsoap.org/soap/encoding/'>\n"+
        "           <country1 xsi:type='xsd:string'>USA</country1>\n"+
        "           <country2 xsi:type=\"xsd:string\">japan</country2>\n"+
        "        </ns1:getRate>\n"+
        "     </SOAP-ENV:Body>\n"+
        "</SOAP-ENV:Envelope>\n"
        ;

    private XmlPullBuilder builder;
    private XsdPullFactory xsdFactory;
    private XsdPullParser pp;
    private XsdSerializer xs;
    //private XmlPullBuilder builder;

    private DOM2XmlPullBuilder dom2Builder;
    //private XmlPullBuilder builder;
    //private XmlPullWrapperFactory wrapperFactory;

    private List headers = new ArrayList();
    private String methodNamespace;
    private String methodName;
    private List parameterNames = new ArrayList();
    private List parameterValues = new ArrayList();


    public static void main (String[] args) {
        junit.textui.TestRunner.run (new TestSuite(TestSoapProcessing.class));
    }


    public TestSoapProcessing(String name) {
        super(name);
    }


    protected void setUp() throws XmlPullParserException {
        dom2Builder = new DOM2XmlPullBuilder();

        builder = XmlPullBuilder.newInstance();

        xsdFactory = xsdFactory.newInstance();
        xsdFactory.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true);
        assertEquals(true, xsdFactory.getFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES));
        assertEquals(false, xsdFactory.getFeature(XmlPullParser.FEATURE_VALIDATION));
    }

    private void log(String msg) {
        System.err.println(getClass()+" " + msg);
    }

    public void testParseAndSerializeSoap() throws IOException, XmlPullParserException {
        Writer sw = new StringWriter();
        generateRequest(sw);
        String soapEnvelope = sw.toString(); //SAMPLE;
        log(" soapEnvelope="+soapEnvelope);
        parseRequest( new StringReader(soapEnvelope) );
        log("got "+headers.size()+" header(s)");
        for (int i = 0; i < headers.size(); i++)
        {
            Object header = headers.get(i);
            String pfx = "header "+(i+1)+" ("+header.getClass()+") = ";
            if(header instanceof XmlElement) {
                log(pfx+""+builder.serializeToString(header));
            } else {
                log(pfx+""+header);
            }
        }

        log("got method namespace='"+methodNamespace+"' name='"+methodName+"'");
        for (int i = 0; i < parameterValues.size(); i++)
        {
            Object value = parameterValues.get(i);
            String pfx = "parameter "+parameterNames.get(i)+" ("+value.getClass()+") = ";
            if(value instanceof XmlElement) {
                log(pfx+""+builder.serializeToString(value));
            } else {
                log(pfx+""+value);
            }
        }
    }



    // --- generating SOAP output - simpler than using String ...

    private void generateRequest(Writer writer)
        throws XmlPullParserException, IOException
    {
        xs = xsdFactory.newXsdSerializer();
        xs.setOutput(writer);
        // set pretty printing -- not required but gives nicely indented output
        // WARNING: this should be turned off when generating doc/literal content for Body or Header
        try {
            xs.setProperty(
                "http://xmlpull.org/v1/doc/properties.html#serializer-indentation", "  ");
        } catch(Exception ex) {
            // ignore exception as this property is optional and may not be implemented!
        }

        generateSoapEnvelope();
    }

    private void generateSoapEnvelope()
        throws XmlPullParserException, IOException
    {
        xs.setCurrentNamespaceForElements(""); //make sure default is empty namespace
        // declare used namespace prefixes
        xs.setPrefix("SOAP-ENV", SOAP_NS);
        xs.setPrefix("xsi", XSI_NS);
        xs.setPrefix("xsd", XSD_NS);
        xs.startTag(SOAP_NS, "Envelope");
        generateSoapHeader();
        generateSoapBody();
        xs.endTag(SOAP_NS, "Envelope");
        xs.endDocument(); //this implicitly call xs.flush()
    }

    private void generateSoapHeader()
        throws XmlPullParserException, IOException
    {
        xs.startTag(SOAP_NS, "Header");

        // now we write exmaple header
        xs.setPrefix("t", "some-URI"); //NOTE: this is optional, prefix will be declared if needed automatically
        xs.writeXsdIntElement("some-URI", "Transaction", 5); //using typed input

        xs.endTag(SOAP_NS, "Header");
    }

    private void generateSoapBody()
        throws XmlPullParserException, IOException
    {
        xs.startTag(SOAP_NS, "Body");
        serializeMethodCall();
        xs.endTag(SOAP_NS, "Body");

    }

    private void serializeMethodCall()
        throws XmlPullParserException, IOException
    {
        xs.startTag("urn:demo1:exchange", "getRate");
        xs.attribute(SOAP_NS, "encodingStyle", "http://schemas.xmlsoap.org/soap/encoding/");

        // write paramters
        xs.startTag("country1");
        xs.attribute(XSI_NS, "type", "string");
        xs.text("USA");
        xs.endTag("country1");

        //xs.startTag("country2");
        //xs.attribute(XSI_NS, "type", "string");
        //xs.text("japan");
        //xs.endTag("country2");
        xs.writeXsdStringElement("", "country2", "japan"); //use typed serializer

        xs.endTag("urn:demo1:exchange", "getRate");
    }


    // --- pull parsing of SOAP input - notice use of validation ...

    private void parseRequest(Reader reader)
        throws XmlPullParserException, IOException
    {
        pp = xsdFactory.newXsdPullParser();
        log("parser implementated by "+pp.getClass());

        pp.setInput(reader);
        processSoapEnvelope();
    }


    private void processSoapEnvelope()
        throws XmlPullParserException, IOException
    {
        pp.nextStartTag(SOAP_NS, "Envelope");
        //this methonextTag();
        //pp.require(XmlPullParser.START_TAG, );
        pp.nextStartTag(SOAP_NS, null);
        if("Header".equals(pp.getName()) ){
            processSoapHeaders();
            pp.nextStartTag(SOAP_NS, "Body");
        }
        pp.require(XmlPullParser.START_TAG, SOAP_NS, "Body");
        processSoapBody();
        pp.nextEndTag(SOAP_NS, "Envelope");
    }

    private void processSoapHeaders()
        throws XmlPullParserException, IOException
    {
        pp.require(XmlPullParser.START_TAG, SOAP_NS, "Header");
        while(pp.nextTag() == XmlPullParser.START_TAG) { // process all header (if any)
            if(pp.getNamespace().equals("some-URI")
               && pp.getName().equals("Transaction"))
            {
                //  read header as doc/literal into DOM::Element
                Element header = dom2Builder.parseSubTree(pp); // or create header as DOM element
                headers.add( header );
                //} else { // deserialize header content
                //    int headerValue = pp.readXsdIntElement();
                //    headers.add( new Integer( headerValue ) );
                //}
            } else if("1".equals(pp.getAttributeValue(SOAP_NS, "mustUnderstand"))  ) {
                throw new RuntimeException(
                    "no processor for mustUnderstand header"
                        +" in namespace "+pp.getNamespace()
                        +" and with name "+pp.getName()+pp.getPositionDescription());
            }
        }
        pp.require(XmlPullParser.END_TAG, SOAP_NS, "Header");
    }

    private void processSoapBody()
        throws XmlPullParserException, IOException
    {
        pp.require(XmlPullParser.START_TAG, SOAP_NS, "Body");
        // now we have stream with SOAP body elements

        // we could just dump it to XML tree
        if(false) {
            XmlElement bodyAsElement = builder.parseFragment(pp);
        }

        // here is exampel how to convert stream into SAX2 stream
        if(false) {
            try {
                org.xmlpull.v1.sax2.Driver parser = new org.xmlpull.v1.sax2.Driver();
                //parser.setContentHandler( ... );
                parser.parseSubTree(pp); //here is extension to convert pull event stream into SAX2
            } catch(SAXException ex) {
                throw new RuntimeException("could not convert XML input into SAX2 stream", ex);
            }
        }

        // here is exampel how to convert stream into DOM2 element
        if(false) {
            // uncomment when DOM2 builder addon is present: http://www.xmlpull.org/v1/doc/addons.html
            Element el = dom2Builder.parseSubTree(pp);
        }

        if(true) {
            parseMethodCall();
        }
        pp.nextEndTag(SOAP_NS, "Body");
    }


    private void parseMethodCall()
        throws XmlPullParserException, IOException
    {
        pp.nextStartTag();
        methodNamespace = pp.getNamespace();
        methodName = pp.getName();

        //process arguments
        while(pp.nextTag() == XmlPullParser.START_TAG) { // process all parameters (if any)
            String paramName = pp.getName();
            parameterNames.add( paramName );
            if("country1".equals(paramName) ){
                // show how some parameters can be doc/literal
                XmlElement element =  builder.parseFragment(pp);
                parameterValues.add( element );
            } else if("country2".equals(paramName) ){
                // some parameters can be decoded into Java objects
                String value = pp.readXsdStringElement();
                parameterValues.add( value );
            } else {
                throw new RuntimeException(
                    "unknow parameter "+paramName+pp.getPositionDescription());
            }

        }
        pp.require(XmlPullParser.END_TAG, methodNamespace, methodName);
        // if it was SOAP 1.1 Section 5 encoding we would need to read trailers after Body too ...
    }

}

