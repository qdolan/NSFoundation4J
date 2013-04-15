package ns.foundation;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.logging.Level;

public final class NSLog {
  public static final int DebugLevelOff = 0;
  public static final int DebugLevelCritical = 1;
  public static final int DebugLevelInformational = 2;
  public static final int DebugLevelDetailed = 3;
  public static final int DebugLevelWarning = 4;
  public static volatile Logger debug = new JDKLogger(NSLog.class, DebugLevelDetailed);
  public static volatile Logger err = new JDKLogger(NSLog.class, DebugLevelCritical);
  public static volatile Logger out = new JDKLogger(NSLog.class, DebugLevelInformational);
  public static volatile Logger warn = new JDKLogger(NSLog.class, DebugLevelWarning);

  public static void _conditionallyLogPrivateException(Throwable t) {
    if (_debugLoggingAllowedForLevel(DebugLevelDetailed)) {
      debug.appendln(t);
    }
  }

  public static boolean _debugLoggingAllowedForLevel(int aDebugLevel) {
    return debugLoggingAllowedForLevel(aDebugLevel);
  }

  public static boolean debugLoggingAllowedForLevel(int aDebugLevel) {
    return ((aDebugLevel <= 1) || ((aDebugLevel > 0) && (aDebugLevel <= debug.allowedDebugLevel())));
  }

  public static void setDebug(Logger instance) {
    if (instance != null) {
      debug = instance;
    }
  }

  public static void setDebug(Logger instance, int aDebugLevel) {
    if (instance != null) {
      instance.setAllowedDebugLevel(aDebugLevel);
      debug = instance;
    }
  }

  public static void setErr(Logger instance) {
    if (instance != null) {
      err = instance;
    }
  }

  public static void setOut(Logger instance) {
    if (instance != null) {
      out = instance;
    }
  }

  public static String throwableAsString(Throwable t) {
    final StringBuffer sb = new StringBuffer();
    OutputStream os = new OutputStream() {
      @Override
      public void write(int b) throws IOException {
        sb.append((char) b);
      }
    };
    t.printStackTrace(new PrintStream(os));
    return sb.toString();
  }

  static {
    out.setIsVerbose(false);
    debug.setIsVerbose(true);
    err.setIsVerbose(true);
  }

  public static class JDKLogger extends NSLog.Logger {
    final java.util.logging.Logger log;
    final Class<?> clazz;
    
    public JDKLogger() {
    	this(NSLog.class);
    }

    public JDKLogger(Class<?> clazz) {
    	this(clazz, 0);
    }
    
    public JDKLogger(Class<?> clazz, int level) {
      this.clazz = clazz;
      log = java.util.logging.Logger.getLogger(clazz.getName());
      setAllowedDebugLevel(level);
    }
    
    @Override
    public void appendln() {
      appendln("");
    }

    @Override
    public void appendln(int level, String msg, Object... objects) {
      Level aLevel = null;
      switch (level) {
      case DebugLevelCritical:
        aLevel = Level.SEVERE;
        break;
      case DebugLevelInformational:
        aLevel = Level.INFO;
        break;
      case DebugLevelDetailed:
        aLevel = Level.FINE;
        break;
      case DebugLevelWarning:
        aLevel = Level.WARNING;
        break;
      default:
    	aLevel = Level.OFF;
      }
      log.logp(aLevel, clazz.getName(), null, msg, objects); 
    }

    @Override
    public void flush() {
    }
  }
  
  public static abstract class Logger {
    protected int debugLevel = 0;
    protected boolean isEnabled = true;
    protected boolean isVerbose = true;

    public int allowedDebugLevel() {
      return this.debugLevel;
    }

    public void appendln(boolean aValue) {
      appendln((aValue) ? Boolean.TRUE : Boolean.FALSE);
    }

    public void appendln(byte aValue) {
      appendln(Byte.valueOf(aValue));
    }

    public void appendln(byte[] aValue) {
      appendln(Arrays.toString(aValue));
    }

    public void appendln(char aValue) {
      appendln(Character.valueOf(aValue));
    }

    public void appendln(char[] aValue) {
      appendln(new String(aValue));
    }

    public void appendln(double aValue) {
      appendln(new Double(aValue));
    }

    public void appendln(float aValue) {
      appendln(new Float(aValue));
    }

    public void appendln(int aValue) {
      appendln(Integer.valueOf(aValue));
    }

    public void appendln(long aValue) {
      appendln(Long.valueOf(aValue));
    }

    public void appendln(short aValue) {
      appendln(Short.valueOf(aValue));
    }

    public void appendln(Throwable aValue) {
      appendln(NSLog.throwableAsString(aValue));
    }
    
    public void appendln(Object o) {
      appendln(o != null ? o.toString() : null, (Object[])null);
    }
    
    public void appendln(String msg, Object... o) {
      appendln(debugLevel, msg, o);
    }

    public abstract void appendln(int level, String msg, Object... objects);

    public abstract void appendln();

    public abstract void flush();

    public boolean isEnabled() {
      return this.isEnabled;
    }

    public boolean isVerbose() {
      return this.isVerbose;
    }

    public void setAllowedDebugLevel(int aDebugLevel) {
      if ((aDebugLevel >= 0) && (aDebugLevel <= 4)) {
        this.debugLevel = aDebugLevel;
      } else {
        throw new IllegalArgumentException("<" + super.getClass().getName() + "> Invalid debug level: " + aDebugLevel);
      }
    }

    public void setIsEnabled(boolean aBool) {
      this.isEnabled = aBool;
    }

    public void setIsVerbose(boolean aBool) {
      this.isVerbose = aBool;
    }

	public static Logger getLogger(Class<?> clazz) {
		return new JDKLogger(clazz);
	}

	public void info(String msg, Object... o) {
		appendln(DebugLevelInformational, msg, o);
	}
	
	public void warn(String msg, Object... o) {
		appendln(DebugLevelWarning, msg, o);
	}

	public void error(String msg, Object... o) {
		appendln(DebugLevelCritical, msg, o);
	}

	public void debug(String msg, Object... o) {
		appendln(DebugLevelDetailed, msg, o);
	}


  }
}