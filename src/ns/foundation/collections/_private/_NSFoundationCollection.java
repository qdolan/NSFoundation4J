package ns.foundation.collections._private;


public abstract interface _NSFoundationCollection {
  public abstract int _shallowHashCode();
  
  public enum NullHandling {
    CheckAndFail, CheckAndSkip, NoCheck
  }
}