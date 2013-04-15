package ns.foundation;

import java.text.DecimalFormat;

public class NSNumberFormatter extends DecimalFormat {
  private static final long serialVersionUID = -7602070951589353522L;

  public NSNumberFormatter() {
    super();
  }

  public NSNumberFormatter(String aPattern) {
    super(aPattern);
  }
}
