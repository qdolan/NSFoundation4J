package ns.foundation;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URI;
import java.util.Collection;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import ns.foundation.collections.NSArray;
import ns.foundation.collections.NSMutableArray;
import ns.foundation.kvc.NSKeyValueCodingAdditions;
import ns.foundation.properties.NSProperties;
import ns.foundation.utilities.NSValueUtilities;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class NSXMLData implements NSKeyValueCodingAdditions {
  private String _contentKey = NSProperties.stringForKeyWithDefault("XMLData.contentTag", "content");
  private boolean _flattenArrays = NSProperties.booleanForKeyWithDefault("XMLData.flattenArrays", true);

  private final Element element;
  private final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
  private final TransformerFactory transfactory = TransformerFactory.newInstance();
  
  public NSXMLData(URI uri) {
    element = parse(uri).getDocumentElement();
  }

  public NSXMLData(File file) {
    element = parse(file).getDocumentElement();
  }

  public NSXMLData(InputStream is) {
    element = parse(new InputSource(is)).getDocumentElement();
  }

  public NSXMLData(InputSource is) {
    element = parse(is).getDocumentElement();
  }
  
  public NSXMLData(NSData data) {
    element = parse(data).getDocumentElement();
  }
  
  public NSXMLData(Element el) {
    element = el;
  }
  
  public NSXMLData(Document document) {
    element = document.getDocumentElement();
  }

  public NSXMLData(Element node, String contentKey, boolean flattenArrays) {
    this(node);
    setContentKey(contentKey);
    setFlattenArrays(flattenArrays);
  }

  private DocumentBuilder builder() {
    try {      
      return factory.newDocumentBuilder();
    } catch (ParserConfigurationException e) {
      throw new NSForwardException("Exception creating new document parser: ", e);
    }
  }
  
  private Transformer transformer() {
    try {
      return transfactory.newTransformer();
    } catch (TransformerConfigurationException e) {
      throw new NSForwardException("Exception creating new document transformer: ", e);
    }
  }
  
  private Document parse(URI uri) {
    try {      
      return builder().parse(uri.toString());
    } catch (IOException e) {
      throw new NSForwardException("Exception Loading XML File: ", e);
    } catch (SAXException e) {
      throw new NSForwardException("Exception parsing XML Document: ", e);
    }
  }

  private Document parse(File file) {
    try {      
      return builder().parse(file);
    } catch (IOException e) {
      throw new NSForwardException("Exception Loading XML File: ", e);
    } catch (SAXException e) {
      throw new NSForwardException("Exception parsing XML Document: ", e);
    }
  }
  
  private Document parse(InputSource is) {
    try {      
      return builder().parse(is);
    } catch (IOException e) {
      throw new NSForwardException("Exception Loading XML File: ", e);
    } catch (SAXException e) {
      throw new NSForwardException("Exception parsing XML Document: ", e);
    }
  }
  
  private Document parse(NSData data) {
    return parse(new InputSource(data.stream()));
  }

  private NSArray<Object> childDataForKey(String key) {
    NSArray<Object> list = new NSMutableArray<Object>();
    if (element.hasChildNodes()) {
      NodeList nodes = element.getChildNodes();
      int length = nodes.getLength();
      for (int i = 0; i < length; i++) {
        Node node = nodes.item(i);
        if (node.getNodeType() == Node.ELEMENT_NODE && key.equals(((Element)node).getTagName())) {
          list.add(new NSXMLData((Element)node, _contentKey, _flattenArrays));
        }
      }
    }
    return list;
  }
  
  public void setContentKey(String key) {
    if (key == null) {
      throw new IllegalArgumentException("Key cannot be null");
    }
    _contentKey = key;
  }
  
  public String contentKey() {
    return _contentKey;
  }
  
  public void setFlattenArrays(boolean flatten) {
    _flattenArrays = flatten;
  }
  
  public boolean flattenArrays() {
    return _flattenArrays;
  }
  
  @Override
  public void takeValueForKey(Object value, String key) {
    throw new UnsupportedOperationException();
  }
  
  @Override
  public Object valueForKey(String key) {
    Object result = null;
    boolean isAttribute = key.startsWith("#");
    boolean isElement = key.startsWith("-");
    String newKey = isAttribute || isElement ? key.substring(1) : key;
    if (!isElement && isAttribute || element.hasAttribute(newKey)) {
      result = element.getAttribute(newKey);
    } 
    else {
      if (newKey.equals(element.getTagName())) {
        result = this;
      }
      else if (_contentKey.equals(newKey)) {
        result = element.getTextContent();
      }
      else {
        NSArray<Object> list = new ElementAccessArray<Object>(childDataForKey(newKey));
        if (list.size() == 1 && _flattenArrays) {
          result = list.get(0);
        } 
        else {
          result = list;
        }
      }
    }
    return result;
  }
  
  @Override
  public void takeValueForKeyPath(Object value, String keyPath) {
    NSKeyValueCodingAdditions.DefaultImplementation.takeValueForKeyPath(this, value, keyPath);
  }

  @Override
  public Object valueForKeyPath(String keyPath) {
    return NSKeyValueCodingAdditions.DefaultImplementation.valueForKeyPath(this, keyPath);
  }
  
  @Override
  public String toString() {
    return element.toString();
  }
  
  public String toXML() {
    Transformer transformer = transformer();
    transformer.setOutputProperty(OutputKeys.ENCODING, "utf-8");
    transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
    transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
    
    StringWriter sw = new StringWriter();
    StreamResult result = new StreamResult(sw);
    DOMSource source = new DOMSource(element);
    try {
      transformer.transform(source, result);
      return sw.toString();
    } catch (TransformerException e) {
      throw new NSForwardException(e);
    }
  }
  
  public class ElementAccessArray<T> extends NSArray<T> {
    private static final long serialVersionUID = 8691857533048290220L;

    public ElementAccessArray(Collection<T> collection) {
      super(collection);
    }

    @Override
    public Object valueForKey(String key) {
      if (key.matches("^[0-9]*$")) {
        Integer i = NSValueUtilities.IntegerValueWithDefault(key, null);
        if (i != null) {
          return objectAtIndex(i);
        }
      }
      Object result = super.valueForKey(key);
      if (result instanceof NSArray && _flattenArrays) {
        return ((NSArray<?>)result).flatten();
      }
      return result;
    }
  }
}