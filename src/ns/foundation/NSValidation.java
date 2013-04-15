package ns.foundation;

public interface NSValidation {

  public Object validateTakeValueForKeyPath(Object value, String keyPath) throws ValidationException;

  public Object validateValueForKey(Object value, String key) throws ValidationException;

  public static class ValidationException extends RuntimeException {
    private static final long serialVersionUID = -6550198639705626107L;

	public ValidationException(String message) {
      super(message);
    }
  }
}
