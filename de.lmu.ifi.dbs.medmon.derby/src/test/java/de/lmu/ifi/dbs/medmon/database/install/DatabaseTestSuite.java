package de.lmu.ifi.dbs.medmon.database.install;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
  DatabaseTest.class,
  PatientTest.class,
  DataTest.class,
  TherapyTest.class,
  TherapyResultTest.class
})
public class DatabaseTestSuite {

}
