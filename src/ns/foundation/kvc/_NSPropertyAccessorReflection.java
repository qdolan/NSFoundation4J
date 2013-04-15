package ns.foundation.kvc;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.concurrent.ConcurrentHashMap;

import ns.foundation.NSForwardException;
import ns.foundation.collections.NSMutableDictionary;
import ns.foundation.kvc.NSKeyValueCoding._KeyBinding;
import ns.foundation.kvc._KeyBindingCreation._KeyBindingFactory;
import ns.foundation.kvc._KeyBindingCreation._KeyBindingFactory._BindingStorage;
import ns.foundation.utilities._NSStringUtilities;
import ns.foundation.utilities._NSUtilities;

public class _NSPropertyAccessorReflection {
  private static final _KeyBinding  _NotAvailableIndicator = new NSKeyValueCoding._KeyBinding(null, null);
  private static final ConcurrentHashMap<_KeyBinding, _BindingStorage>  _bindingStorageMapTable     = new ConcurrentHashMap<_KeyBinding, _BindingStorage>(256);

  private Object targetObject;
  private Class<?> targetClass;
  

  public _NSPropertyAccessorReflection(Object object) {
    targetObject = object;
    targetClass = object.getClass();
  }
  
  public static void _flushCaches() {
    _bindingStorageMapTable.clear();
  }
  
  public static boolean _canAccessFieldsDirectlyForClass(Class<?> objectClass) {
    //return _NSReflectionUtilities._staticBooleanMethodValue("canAccessFieldsDirectly", null, null, objectClass, NSKeyValueCoding.class, true);
    return true;
  }

  protected static class _LegacyCompatibleKeyBinding extends _KeyBinding {
    private final _KeyBinding _delegate;
    private Class<?> _valueType;
    
    _LegacyCompatibleKeyBinding(_KeyBinding keyBinding) {
      super(keyBinding.targetClass(), keyBinding.key());
      _delegate = keyBinding;
    }

    @Override
    public boolean isScalarProperty() {
      return _delegate.isScalarProperty();
    }
    
    @Override
    public void setValueInObject(Object value, Object object) {
      _delegate.setValueInObject(value, object);
    }
    
    @Override
    public Class<?> valueType() {
      if (_valueType == null) {
        Class<?> valueType = _delegate.valueType();
        if (valueType.isPrimitive() && _NSUtilities._isClassANumberOrABoolean(valueType)) {
          valueType = _NSUtilities.classObjectForClass(valueType);
        }
        _valueType = valueType;
      }
      return _valueType;
    }
    
    @Override
    public Object valueInObject(Object object) {
      return _delegate.valueInObject(object);
    }
    
    @Override
    public String toString() {
      return _delegate.toString();
    }
  }
  
  public static _KeyBinding _createKeyBindingForKey(Object object, String key, int lookupOrder[], boolean trueForSetAndFalseForGet) {
    _KeyBinding keyBinding = new _NSPropertyAccessorReflection(object)._createKeyBindingForKey(key, lookupOrder, trueForSetAndFalseForGet);
    return keyBinding == null ? null : new _LegacyCompatibleKeyBinding(keyBinding);
  }

  private NSKeyValueCoding._KeyBinding _createKeyBindingForKey(String key, int[] lookupOrder, boolean trueForSetAndFalseForGet) {
    if ((key == null) || (key.length() == 0)) {
      return null;
    }

    Class<?> objectClass = targetObject.getClass();

    boolean canAccessFieldsDirectlyTestPerformed = false;
    boolean canAccessFieldsDirectly = false;

    // we use a KeyBinding as key for the _BindingStorage object map table since it gives us exactly what we need: a class and a key - but we have to create a new lookup key binding to avoid synchronizing the read lookup (and we need a new instance for the write access)
    NSKeyValueCoding._KeyBinding lookupBinding = new NSKeyValueCoding._KeyBinding(objectClass, key);
    _BindingStorage bindingStorage = _bindingStorageMapTable.get(lookupBinding);
    if (bindingStorage == null) {
      bindingStorage = new _KeyBindingFactory._BindingStorage();
      _bindingStorageMapTable.put(lookupBinding, bindingStorage);
    }

    _KeyBindingFactory.Callback keyBindingCreationCallbackObject = (targetObject instanceof _KeyBindingFactory.Callback) ? (_KeyBindingFactory.Callback) targetObject
        : null;
    NSKeyValueCoding._KeyBinding keyBindings[] = (trueForSetAndFalseForGet) ? bindingStorage._keySetBindings : bindingStorage._keyGetBindings;
    for (int i = 0; i < lookupOrder.length; i++) {
      int lookup = lookupOrder[i];
      NSKeyValueCoding._KeyBinding keyBinding = ((lookup >= _KeyBindingFactory.MethodLookup) && (lookup <= _KeyBindingFactory.UnderbarFieldLookup)) ? keyBindings[lookup] : null;
      if (keyBinding == null) {
        Class<?> valueType = null;
        if (trueForSetAndFalseForGet) {
          _KeyBinding getKeyBinding = _createKeyBindingForKey(key, lookupOrder, false);
          valueType = getKeyBinding != null ? getKeyBinding.valueType() : null;
        }

        switch (lookup) {
          case _KeyBindingFactory.MethodLookup:
            String methodName = prefixedKey((trueForSetAndFalseForGet) ? "set" : "get", key);

            if (trueForSetAndFalseForGet) {
              // look up 'setKey'
              keyBinding = (keyBindingCreationCallbackObject != null) ? keyBindingCreationCallbackObject._methodKeySetBinding(key, methodName) 
                  : _methodKeySetBinding(key, methodName, valueType);
            } else {
              // look up 'getKey'
              keyBinding = (keyBindingCreationCallbackObject != null) ? keyBindingCreationCallbackObject._methodKeyGetBinding(key, methodName)
                  : _methodKeyGetBinding(key, methodName);

              if (keyBinding == null) {
                // look up 'key'
                keyBinding = (keyBindingCreationCallbackObject != null) ? keyBindingCreationCallbackObject._methodKeyGetBinding(key, key)
                    : _methodKeyGetBinding(key, key);
              }

              if (keyBinding == null) {
                // look up 'isKey'
                methodName = new String(prefixedKey("is", key));
                keyBinding = (keyBindingCreationCallbackObject != null) ? keyBindingCreationCallbackObject._methodKeyGetBinding(key, methodName)
                    : _methodKeyGetBinding(key, methodName);
              }
            }
            break;
          case _KeyBindingFactory.UnderbarMethodLookup:
            String underbarMethodName = prefixedKey((trueForSetAndFalseForGet) ? "_set" : "_get", key);

            if (trueForSetAndFalseForGet) {
              // look up '_setKey'
              keyBinding = (keyBindingCreationCallbackObject != null) ? keyBindingCreationCallbackObject._methodKeySetBinding(key, underbarMethodName)
                  : _methodKeySetBinding(key, underbarMethodName, valueType);
            } else {
              // look up '_getKey'
              keyBinding = (keyBindingCreationCallbackObject != null) ? keyBindingCreationCallbackObject._methodKeyGetBinding(key, underbarMethodName)
                  : _methodKeyGetBinding(key, underbarMethodName);

              if (keyBinding == null) {
                // look up '_key'
                underbarMethodName = prefixedKey("_", key);
                keyBinding = (keyBindingCreationCallbackObject != null) ? keyBindingCreationCallbackObject._methodKeyGetBinding(key, underbarMethodName)
                    : _methodKeyGetBinding(key, underbarMethodName);
              }

              if (keyBinding == null) {
                // look up '_isKey'
                underbarMethodName = prefixedKey("_is", key);
                keyBinding = (keyBindingCreationCallbackObject != null) ? keyBindingCreationCallbackObject._methodKeyGetBinding(key, underbarMethodName)
                    : _methodKeyGetBinding(key, underbarMethodName);
              }
            }
            break;
          case _KeyBindingFactory.FieldLookup:
            if (!canAccessFieldsDirectlyTestPerformed) {
              canAccessFieldsDirectlyTestPerformed = true;
              canAccessFieldsDirectly = _canAccessFieldsDirectlyForClass(objectClass);
            }
            if (canAccessFieldsDirectly) {
              // look up 'key'
              keyBinding = (keyBindingCreationCallbackObject != null) ? keyBindingCreationCallbackObject._fieldKeyBinding(key, key)
                  : _fieldKeyBinding(key, key);

              if (keyBinding == null) {
                // look up 'isKey'
                String fieldName = prefixedKey("is", key);
                keyBinding = (keyBindingCreationCallbackObject != null) ? keyBindingCreationCallbackObject._fieldKeyBinding(key, fieldName)
                    : _fieldKeyBinding(key, fieldName);
              }
            }
            break;
          case _KeyBindingFactory.UnderbarFieldLookup:
            if (!canAccessFieldsDirectlyTestPerformed) {
              canAccessFieldsDirectlyTestPerformed = true;
              canAccessFieldsDirectly = _canAccessFieldsDirectlyForClass(objectClass);
            }
            if (canAccessFieldsDirectly) {
              // look up '_key'
              String underbarFieldName = prefixedKey("_", key);
              keyBinding = (keyBindingCreationCallbackObject != null) ? keyBindingCreationCallbackObject._fieldKeyBinding(key, underbarFieldName)
                  : _fieldKeyBinding(key, underbarFieldName);

              if (keyBinding == null) {
                // look up '_isKey'
                underbarFieldName = prefixedKey("_is", key);
                keyBinding = (keyBindingCreationCallbackObject != null) ? keyBindingCreationCallbackObject._fieldKeyBinding(key, underbarFieldName)
                    : _fieldKeyBinding(key, underbarFieldName);
              }
            }
            break;
          case _KeyBindingFactory.OtherStorageLookup:
            keyBinding = (keyBindingCreationCallbackObject != null) ? keyBindingCreationCallbackObject._otherStorageBinding(key) : null;
            break;
        }

        if (keyBinding == null) {
          keyBinding = _NotAvailableIndicator;
        }
        if ((lookup == _KeyBindingFactory.FieldLookup) || (lookup == _KeyBindingFactory.UnderbarFieldLookup)) {
          // set and get bindings are the same for fields (but not for methods since the name of set and get methods are actually different)
          bindingStorage._keySetBindings[lookup] = bindingStorage._keyGetBindings[lookup] = keyBinding;
        } else if ((lookup == _KeyBindingFactory.MethodLookup) || (lookup == _KeyBindingFactory.UnderbarMethodLookup)) {
          keyBindings[lookup] = keyBinding;
        }
      }

      if ((keyBinding != null) && (keyBinding != _NotAvailableIndicator)) {
        return keyBinding;
      }
    }
    return null;
  }

  public NSKeyValueCoding._KeyBinding _fieldKeyBinding(String key, String fieldName) {
    Class<?> objectClass = targetObject.getClass();
    NSKeyValueCoding.ValueAccessor valueAccessor = NSKeyValueCoding.ValueAccessor._valueAccessorForClass(objectClass);
    boolean publicFieldOnly = (valueAccessor == null);

    try {
      Field field = targetClass.getField(fieldName);
      if ((publicFieldOnly && !Modifier.isPublic(field.getModifiers()))
          || Modifier.isPrivate((field.getModifiers()))) {
        return null;
      } 
      
      final Class<?> valueType = field.getType();
      final Field targetField = field;
      if(Modifier.isPublic(targetField.getModifiers())) {
    	  targetField.setAccessible(true);
      }
      NSKeyValueCoding._KeyBinding binding = new NSKeyValueCoding._KeyBinding(targetClass, key) {
    	  public Object valueInObject(Object object) {
    		try {
				return targetField.get(object);
			}
			catch (Exception e) {
				throw NSForwardException._runtimeExceptionForThrowable(e);
			}
    	  }
    	  @Override
    	  public void setValueInObject(Object value, Object object) {
    		  try {
    			  if (value != null || !valueType().isPrimitive()) {
    				  targetField.set(object, _NSUtilities._convertObjectIntoCompatibleValue(value, valueType));
    			  } else {
    				  NSKeyValueCoding.Utility.unableToSetNullForKey(object, _key);
    			  }
    		  }
    		  catch (Exception e) {
    			  throw NSForwardException._runtimeExceptionForThrowable(e);
    		  }
    	  }
    	  
    	  @Override
    	public Class<?> valueType() {
    		return valueType;
    	}
    	  
    	@Override
    	public boolean isScalarProperty() {
    		return valueType.isPrimitive();
    	}
      };
      return binding;
    } catch (NoSuchFieldException e) {
        return null;
    } catch (Exception e) {
      throw new NSForwardException(e);
    }
  }
  
  public NSKeyValueCoding._KeyBinding _methodKeyGetBinding(String key, String methodName) {
    Class<?> objectClass = targetObject.getClass();
    NSKeyValueCoding.ValueAccessor valueAccessor = NSKeyValueCoding.ValueAccessor._valueAccessorForClass(objectClass);
    boolean publicMethodOnly = (valueAccessor == null);
    
    try {
      Method method = null;
      for (Method target : targetClass.getMethods()) {
        if (!target.getName().equals(methodName)
            || target.getParameterTypes().length != 0) {
          continue;
        }
        if (( publicMethodOnly && !Modifier.isPublic(target.getModifiers()))
            || (Modifier.isPrivate(target.getModifiers()))) {
          continue;
        }
        method = target;
        break;
      }
      if (method == null) {
        return null;
      }
      final Class<?> valueType = method.getReturnType();
      final Method targetMethod = method;
      NSKeyValueCoding._KeyBinding binding = new NSKeyValueCoding._KeyBinding(targetClass, key) {
    	  public Object valueInObject(Object object) {
    		  try {
				return targetMethod.invoke(object, new Object[0]);
			}
			catch (Exception e) {
				throw NSForwardException._runtimeExceptionForThrowable(e);
			}
    	  };

    	@Override
    	public Class<?> valueType() {
    		return valueType;
    	}
    	  
    	@Override
    	public boolean isScalarProperty() {
    		return false;
    	}
      };
      return binding;
    } catch (Exception e) {
      throw new NSForwardException(e);
    }
  }

  public NSKeyValueCoding._KeyBinding _methodKeySetBinding(String key, String methodName, Class<?> targetValueType) {
    Class<?> objectClass = targetObject.getClass();
    NSKeyValueCoding.ValueAccessor valueAccessor = NSKeyValueCoding.ValueAccessor._valueAccessorForClass(objectClass);
    boolean publicMethodOnly = (valueAccessor == null);
    if (targetValueType == null) {
      targetValueType = Object.class;
    }
    try {
      Method method = null;
      for (Method target : targetClass.getMethods()) {
        if (!target.getName().equals(methodName) || target.getParameterTypes().length != 1) {
          continue;
        }

        if ((publicMethodOnly && !Modifier.isPublic(target.getModifiers()))
            || Modifier.isPrivate(target.getModifiers())) {
          continue;
        }
        Class<?> clazz = target.getParameterTypes()[0];
        if (clazz.getName().equals(targetValueType.getName())) {
          method = target;
          break;
        }
        else if (boxedTypeName(clazz).equals(_NSUtilities.classObjectForClass(targetValueType).getName())) {
          method = target;
        } 
        else if (method == null) {
          method = target;
        }
      }
      
      if (method == null) {
        return null;
      }
      final Class<?> valueType = targetValueType;
      final Method targetMethod = method;
      if(Modifier.isPublic(targetMethod.getModifiers())) {
        targetMethod.setAccessible(true);
      }
      NSKeyValueCoding._KeyBinding binding = new NSKeyValueCoding._KeyBinding(targetClass, key) {
    	  @Override
    	public void setValueInObject(Object value, Object object) {
    		try {
				targetMethod.invoke(object, new Object[] { _NSUtilities._convertObjectIntoCompatibleValue(value, valueType) });
			}
			catch (Exception e) {
				throw NSForwardException._runtimeExceptionForThrowable(e);
			}
    	}

    	@Override
    	public Class<?> valueType() {
    		return valueType;
    	}
    	
    	@Override
    	public boolean isScalarProperty() {
    		return valueType.isPrimitive();
    	}
      };
      return binding;
    } catch (Exception e) {
      throw new NSForwardException(e);
    }
  }

  private String prefixedKey(String prefix, String key) {
    if (prefix == null) {
      return key;
    }
    StringBuffer sb = new StringBuffer(prefix.length() + key.length());
    sb.append(prefix);
    if ("_".equals(prefix)) {
      return sb.append(key).toString();
    }
    return sb.append(_NSStringUtilities.capitalizedString(key)).toString();
  }

  private static final NSMutableDictionary<Class<?>, Class<?>> PRIMITIVE_WRAPPERS = new NSMutableDictionary<Class<?>, Class<?>>(
		  new Class<?>[] { boolean.class, byte.class, char.class, double.class, float.class, int.class, long.class, short.class, void.class } ,
		  new Class<?>[] { Boolean.class, Byte.class, Character.class, Double.class, Float.class, Integer.class, Long.class, Short.class, Void.class }); 
  private String boxedTypeName(Class<?> type) {
	  if (type.isPrimitive() && PRIMITIVE_WRAPPERS.containsKey(type)) {
		  return PRIMITIVE_WRAPPERS.get(type).getName();
	  }
	  return type.getName();
  }
}
