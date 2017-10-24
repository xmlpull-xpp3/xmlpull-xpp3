/* -*-             c-basic-offset: 4; indent-tabs-mode: nil; -*-  //------100-columns-wide------>|*/
// for license please see accompanying LICENSE.txt file (available also at http://www.xmlpull.org/)


import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

/**
 * Simple Cloneable File Reader to allow cloning pull parsers efficiently.
 *
 * @author <a href="http://www.extreme.indiana.edu/~aslom/">Aleksander Slominski</a>
 */
public class CloneableFileReader extends Reader implements Cloneable
{
    FileReader freader;
    String filename;
    long position;
    
    public CloneableFileReader(String filename) throws FileNotFoundException {
        this.freader = new FileReader(filename);
        this.filename = filename;
    }
    
    //JDK15 covariant public CloneableFileReader clone() throws CloneNotSupportedException {
    public Object clone() throws CloneNotSupportedException {
        CloneableFileReader clone;
        try {
            clone = new CloneableFileReader(filename);
            clone.skip(position);
        } catch (IOException e) {
            throw new CloneNotSupportedException("could not clone file reader "+e);
        }
        return clone;
    }
    
    public int read(char[] cbuf,
                    int offset,
                    int length)
        throws IOException
    {
        
        int actual = freader.read(cbuf, offset, length);
        if(actual > 0) {
            position += actual;
        }
        return actual;
    }
    
    // re-implement remaining functions similar to read above...
    public void close() throws IOException {
        freader.close();
    }
    
}

