package ns.foundation._private;

import java.io.Serializable;

import ns.foundation.NSKeyValueObserving;
import ns.foundation.NSObservable;
import ns.foundation.NSObserver;
import ns.foundation.NSSelector;
import ns.foundation.kvc.NSKeyValueCoding;
import ns.foundation.kvc.NSKeyValueCodingAdditions;

public interface _NSObject extends NSKeyValueCoding, NSKeyValueCodingAdditions, NSKeyValueObserving, NSObservable, NSObserver, Cloneable, Serializable {
  public boolean respondsToSelector(NSSelector<?> selector);
}
