module org.xmlpull.mxp1 {
    requires org.xmlpull.v1;

    exports org.xmlpull.mxp1;

    provides org.xmlpull.v1.XmlPullParser with org.xmlpull.mxp1.MXParser;
}
