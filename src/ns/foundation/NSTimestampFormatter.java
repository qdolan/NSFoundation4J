package ns.foundation;

import java.text.SimpleDateFormat;

public class NSTimestampFormatter extends SimpleDateFormat {
  private static final long serialVersionUID = -464683868393604099L;

  public NSTimestampFormatter() {
    super("z' 'yyyy'-'MM'-'dd' 'HH':'mm':'ss");
  }

  public NSTimestampFormatter(String aPattern) {
    super(aPattern);
  }
}
