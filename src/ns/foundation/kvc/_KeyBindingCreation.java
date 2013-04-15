package ns.foundation.kvc;

import ns.foundation.kvc.NSKeyValueCoding._KeyBinding;

public interface _KeyBindingCreation {
    public static _KeyBindingFactory defaultFactory = new _ReflectionKeyBindingCreation();

    public _KeyBinding _createKeyGetBindingForKey(String key);

    public _KeyBinding _createKeySetBindingForKey(String key);

    public _KeyBinding _keyGetBindingForKey(String s);

    public _KeyBinding _keySetBindingForKey(String s);

    public interface _KeyBindingFactory {
        public static final int MethodLookup = 0;
        public static final int UnderbarMethodLookup = 1;
        public static final int FieldLookup = 2;
        public static final int UnderbarFieldLookup = 3;
        public static final int OtherStorageLookup = 4;
        public static final int _ValueForKeyLookupOrder[] = { 0, 1, 3, 2, 4 };
        public static final int _StoredValueForKeyLookupOrder[] = { 1, 3, 2, 4, 0 };

        public _KeyBinding _createKeyGetBindingForKey(Object object, String key, int lookupOrder[]);

        public _KeyBinding _createKeySetBindingForKey(Object object, String key, int lookupOrder[]);

        public static class _BindingStorage {
            NSKeyValueCoding._KeyBinding _keyGetBindings[];

            NSKeyValueCoding._KeyBinding _keySetBindings[];

            public _BindingStorage() {
                _keyGetBindings = new NSKeyValueCoding._KeyBinding[UnderbarFieldLookup + 1];
                _keySetBindings = new NSKeyValueCoding._KeyBinding[UnderbarFieldLookup + 1];
            }
        }

        public static interface Callback {

            public abstract _KeyBinding _fieldKeyBinding(String s, String s1);

            public abstract _KeyBinding _methodKeyGetBinding(String s, String s1);

            public abstract _KeyBinding _methodKeySetBinding(String s, String s1);

            public abstract _KeyBinding _otherStorageBinding(String s);
        }
    }
    
    public static class _BytecodeKeyBindingCreation implements _KeyBindingCreation._KeyBindingFactory {

        @Override
        public _KeyBinding _createKeyGetBindingForKey(Object object, String key, int lookupOrder[]) {
            return _NSPropertyAccessorBytecode._createKeyBindingForKey(object, key, lookupOrder, false);
        }

        @Override
        public _KeyBinding _createKeySetBindingForKey(Object object, String key, int lookupOrder[]) {
            return _NSPropertyAccessorBytecode._createKeyBindingForKey(object, key, lookupOrder, true);
        }
    }

    public static class _ReflectionKeyBindingCreation implements _KeyBindingCreation._KeyBindingFactory {

        @Override
        public _KeyBinding _createKeyGetBindingForKey(Object object, String key, int lookupOrder[]) {
            return _NSPropertyAccessorReflection._createKeyBindingForKey(object, key, lookupOrder, false);
        }

        @Override
        public _KeyBinding _createKeySetBindingForKey(Object object, String key, int lookupOrder[]) {
            return _NSPropertyAccessorReflection._createKeyBindingForKey(object, key, lookupOrder, true);
        }
    }

}

