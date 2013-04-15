package testsuite;

import junit.framework.Test;
import junit.framework.TestSuite;
import ns.foundation.tests.TestNSArray;
import ns.foundation.tests.TestNSDictionary;
import ns.foundation.tests.TestNSMutableArray;
import ns.foundation.tests.TestNSMutableDictionary;
import ns.foundation.tests.TestNSMutableRange;
import ns.foundation.tests.TestNSMutableSet;
import ns.foundation.tests.TestNSNotificationCenter;
import ns.foundation.tests.TestNSRange;
import ns.foundation.tests.TestNSSelector;
import ns.foundation.tests.TestNSSet;
import ns.foundation.tests.TestNSTimestamp;

public class AllTests extends TestSuite {

  public static Test suite() {
    TestSuite suite = new TestSuite("Test for com.webobjects.foundation");
    //$JUnit-BEGIN$
    suite.addTestSuite(TestNSSelector.class);
    suite.addTestSuite(TestNSMutableSet.class);
    suite.addTestSuite(TestNSArray.class);
    suite.addTestSuite(TestNSTimestamp.class);
    suite.addTestSuite(TestNSMutableRange.class);
    suite.addTestSuite(TestNSMutableArray.class);
    suite.addTestSuite(TestNSDictionary.class);
    suite.addTestSuite(TestNSSet.class);
    suite.addTestSuite(TestNSRange.class);
    suite.addTestSuite(TestNSMutableDictionary.class);
    suite.addTestSuite(TestNSNotificationCenter.class);
    //$JUnit-END$
    return suite;
  }

}
