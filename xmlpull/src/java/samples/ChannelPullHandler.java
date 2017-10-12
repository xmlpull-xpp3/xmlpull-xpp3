/* -*-             c-basic-offset: 4; indent-tabs-mode: nil; -*-  //------100-columns-wide------>|*/
// for license please see accompanying LICENSE.txt file (available also at http://www.xmlpull.org/)

import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.Reader;
import java.io.FileReader;
import java.util.*;

/**
 * This class will parse a rss or rdf document and build a Map consisting of
 * - attributes of a channel
 * - and a one attribute "items" which is a list with items, where each item consists
 *   of a Map with attributes.
 * <br />NOTE: this class does not use namespaces and instead is based on heuristics
 * @author Ronald Haring
 */
public class ChannelPullHandler {

   private List items = new ArrayList();
   private XmlPullParser xmlPullParser = null;
   private Map channel = null;

   private String[] channelIdentifiers = { "channel", "rdf:description", "rss:channel" };
   private String[] itemIdentifiers = { "item", "rss:item" };

   /**
    * The channelSynonymes consists of an array of identifiers. The first element of the array is the name that you want
    * filled. The others names are synonymes that might be filled. These are all synonymes for channel attributes, for
    * instance, you need the title attribute. If this isnt found initially, this class will look for an rss:title
    * attribute. If found, it will copy the value of rss:title to title. These values could be moved out into a
    * properties kind of file, e.g. url=link,dc:indentifier,rss:link
    */
   private String[][] channelSynonymes = {
      {"title", "rss:title"},
      {"url", "link", "dc:identifier", "rss:link"},
      {"date", "dc:date", "pubDate"},
      {"description", "content:encoded", "rss:description"},
      {"date", "dc:date", "lastBuildDate" }
   };

   /**
    * The channelSynonymes consists of an array of identifiers. The first element of the array is the name that you want
    * filled. The others names are synonymes that might be filled. These are all synonymes for item attributes
    */
   private String[][] itemSynonymes = {
      {"title", "rss:title"},
      {"url", "link", "dc:identifier", "rss:link"},
      {"date", "dc:date", "pubDate"},
      {"description", "content:encoded", "rss:description"}
   };

   public ChannelPullHandler() throws XmlPullParserException {
      XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
      xmlPullParser = factory.newPullParser();
   }

   /**
    * This method will recursively parse the document, returning a List build of Key-Value pairs. A value can be a List
    * again, indicating a nested element inside an element.
    * @param type
    * @return
    * @throws IOException
    * @throws XmlPullParserException
    */
   private List parseFirst(int type) throws IOException, XmlPullParserException {
      int level = 0;
           String lname = null;
           String value = null;
      List values = new ArrayList();
      Map attributes = null;

           while (true) {

         if (type == XmlPullParser.END_DOCUMENT) {
            return values;
         }
         else if (type == XmlPullParser.TEXT) {
                 value = xmlPullParser.getText();
         }
         else if (type == XmlPullParser.END_TAG)  {
            level--;
            if (level < 0) {
               // if level is smaller then 0, then we are done with this nested element. Return the List of values.
               return values;
            }
            if (lname.equals(xmlPullParser.getName())) {
               // name is the same, so we are done with this element. Add it to the List
               values.add(new KeyValueAttributes(lname, value, attributes));
               attributes = null;
            }
         }
         else if (type == XmlPullParser.START_TAG) {
            if (level == 0) {
               // if level is 0, then we have encountered the start of a new element, e.g. <title>
               level++;
               lname = xmlPullParser.getName();
               if (xmlPullParser.getAttributeCount() > 0) {
                  attributes = new HashMap();
                  for (int i=0; i<xmlPullParser.getAttributeCount();i++) {
                     attributes.put(xmlPullParser.getAttributeName(i), xmlPullParser.getAttributeValue(i));
                  }
               }
            }
            else {
               // if level != 0 then we have found the start of a tag within another tag. Call this method recurrent
               // with the xmlPullParser pointer on this element. When parseFirst is done, we have handled this nested
               // element and it can then be added as an element to the List.
               values.add(new KeyValueAttributes(lname, parseFirst(type), null));
               level--;
               lname = xmlPullParser.getName();
            }
                 value = null;
         }
         if ( (type = xmlPullParser.next()) == XmlPullParser.END_DOCUMENT) {
            break;
         }
      }
      return values;
   }

   /**
    * This method will loop over the found values and set the values for a channel, and set the values for each item.
    * If other elements need to be supported, this is where you should check for these (e.g. images).
    * @param values
    */
   private void parseSecond(List values) {
      for (int i=0; i<values.size(); i++) {
         KeyValueAttributes pair = (KeyValueAttributes) values.get(i);
         if (pair.value instanceof List) {
            boolean foundSomething = false;
            for (int j=0; j<channelIdentifiers.length; j++) {
               if (pair.key.equals(channelIdentifiers[j])) {
                  workOnChannel( (List) pair.value);
                  foundSomething = true;
                  break;
               }
            }
            if (foundSomething) {
               continue;
            }

            for (int j=0; j<itemIdentifiers.length; j++) {
               if (pair.key.equals(itemIdentifiers[j])) {
                  workOnItem( (List) pair.value);
                  foundSomething = true;
                  break;
               }
            }
            if (foundSomething) {
               continue;
            }

            parseSecond((List)pair.value);
         }
      }
   }

   /**
    * This method will set the item attributes.
    * @param list
    */
   private void workOnItem(List list) {
      Map item = new HashMap();
      for(int i=0; i<list.size(); i++) {
         KeyValueAttributes pair = (KeyValueAttributes) list.get(i);
         if (pair.value instanceof String) {
            // we are only interested in Strings right now. This implies that an item will not have another nested
            // element inside of it. Otherwise we should have checked if pair.value is a List and call this method
            // recursively again. If the item has attributes, these are saved as keys in the Map as well, e.g.
            // <rdf:rdf about="sometext"><title>blah</title></rdf:rdf> will yield in a rdf element, with a key value
            // of about=sometext and title=blah
            item.put(pair.key, (String) pair.value);
            if (pair.attributes != null) {
               Iterator it = pair.attributes.keySet().iterator();
               while(it.hasNext()) {
                  String key = (String) it.next();
                  item.put(key, (String) pair.attributes.get(key));
               }
            }
         }
      }

      // check the synonymes. If the main attribute (element 0 of each array) isnt found in the Map, then try to fill
      // the main attribute using the first found synonyme.
      for(int i=0; i<itemSynonymes.length; i++) {
         if (item.get(itemSynonymes[i][0]) == null || item.get(itemSynonymes[i][0]).equals("")) {
            for (int j=1; j<itemSynonymes[i].length; j++) {
               if (item.get(itemSynonymes[i][j]) != null &&
                     !item.get(itemSynonymes[i][j]).equals("")) {
                  item.put(itemSynonymes[i][0], item.get(itemSynonymes[i][j]));
                  j = itemSynonymes[i].length;
               }
            }
         }
      }
      items.add(item);
   }

   /**
    * This method will set the attributes for a channel.
    * @param list
    */
   private void workOnChannel(List list) {
      channel = new HashMap();
      for(int i=0; i<list.size(); i++) {
         KeyValueAttributes pair = (KeyValueAttributes) list.get(i);
         if (pair.key.equals("item")) {
            // there can be an item inside of the channel, so parse the item when found here.
                 if (pair.value instanceof List) {
               workOnItem((List) pair.value);
                 }
         }
         else {
            if (pair.value instanceof String) {
               // we are only interested in Strings right now. This implies that an channel will not have another nested
               // element inside of it (besides item elements). Otherwise we should have checked if pair.value is a List
               // and call the appropiate method, like workOnItem.
               channel.put(pair.key, (String) pair.value);
               if (pair.attributes != null) {
                  Iterator it = pair.attributes.keySet().iterator();
                  while(it.hasNext()) {
                     String key = (String) it.next();
                     channel.put(key, (String) pair.attributes.get(key));
                  }
               }
            }
         }
      }

      // check the synonymes. If the main attribute (element 0 of each array) isnt found in the Map, then try to fill
      // the main attribute using the first found synonyme.
      for(int i=0; i<channelSynonymes.length; i++) {
         if (channel.get(channelSynonymes[i][0]) == null || channel.get(channelSynonymes[i][0]).equals("")) {
            for (int j=1; j<channelSynonymes[i].length; j++) {
               if (channel.get(channelSynonymes[i][j]) != null &&
                     !channel.get(channelSynonymes[i][j]).equals("")) {
                  channel.put(channelSynonymes[i][0], channel.get(channelSynonymes[i][j]));
                  j = channelSynonymes[i].length;
               }
            }
         }
      }
   }

   /**
    * This method will parse the input reader and return a Map. This map consists of :
    * - the attributes of a channel
    * - and a List with items, where each item is a Map again.
    * @param in
    * @return
    * @throws XmlPullParserException
    * @throws IOException
    */
   public Map parse(Reader in) throws XmlPullParserException, IOException {
      this.xmlPullParser.setInput(in);
      int type = this.xmlPullParser.next();
      if (type != XmlPullParser.END_DOCUMENT) {
         // first parse the document. This will result in a List of lists of lists
         List values = this.parseFirst(type);
              this.items = new ArrayList();
         // the second parse will loop over all the Lists and create one map for the channel and create a list of
         // items. The items are seperately added, because this way the <channel><item></item></channel> and
         // <channel></channel><item></item> format are both supported.
         this.parseSecond(values);
      }
      if (items != null && channel != null) {
              channel.put("items", items);
      }
      else {
         throw new XmlPullParserException("Error, channel or items is null");
      }
      return channel;
   }

   public static void main(String[] args) throws XmlPullParserException, IOException {
      ChannelPullHandler handler = new ChannelPullHandler();
      //Reader in = new FileReader("/temp/error_111.xml");
      Reader in = new FileReader(args[0]);
      Map returnMap = handler.parse(in);
           int x = 3;
   }

   private class KeyValueAttributes {
      private String key = null;
      private Object value = null;
      private Map attributes = null;

      public KeyValueAttributes(String key, Object value, Map attributes) {
         this.key = key;
         this.value = value;
         this.attributes = attributes;
      }
   }
}
