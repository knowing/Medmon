package de.sendsor.accelerationSensor.algorithm.presentation

import java.io.PrintWriter
import java.text.DecimalFormat
import java.util.Properties
import org.joda.time.DateTime
import org.joda.time.Minutes
import de.lmu.ifi.dbs.knowing.core.factory.ProcessorFactory
import de.lmu.ifi.dbs.knowing.core.processing.TSaver
import weka.core.Instances
import org.joda.time.Seconds

/**
 *
 * WSDL excerpt for ACData
 * <pre>
 * {@code
 * <xsd:simpleType name="ACData">
 * 	<xsd:annotation>
 * 		<xsd:documentation>
 * 		    ASCII_DELIMITED format for activity:
 * 		    ##;record_id;year;month;day;hour;minute;second;unit;duration;steps;weight;met;calorie;distance
 * 		    date values must be valid
 * 		    record_id may be empty
 * 		    duration (unit: second) is an integer value
 * 		    steps is an integer value
 * 		    weight, met, calorie and distance (unit: km) are float values with maximum 2 decimal places
 * 		    either weight or met has to be passed
 * 		    calorie and distance are optional
 * 		</xsd:documentation>
 * 	</xsd:annotation>
 * 	<xsd:restriction base="xsd:token">
 * 		<xsd:pattern value="(^##;.*;20\d{2};(0[1-9]|1[0-2]);([0-2]\d|3[0-1]);([0-1]\d|2[0-4]);([0-5]\d|60);([0-5]\d|60);.+;\d+;\d+;\d{1,3}(\.\d{1,2})?;\d{1,3}(\.\d{1,2})?;\d{1,3}(\.\d{1,2})?(;\d{1,3}(\.\d{1,2})?)?(;\d{1,3}(\.\d{1,2})?)?$)" />
 * 	</xsd:restriction>
 * </xsd:simpleType>
 * }
 * </pre>
 *
 * Allowed values
 * <pre>
 * {@code
 * ^##;                     ##;
 * .;                       record_id;
 * 20\d{2};                 year;
 * (0[1-9]|1[0-2]);         month;
 * ([0-2]\d|3[0-1]);        day;
 * ([0-1]\d|2[0-4]);        hour;
 * ([0-5]\d|60);            minute;
 * ([0-5]\d|60);            second;
 * .+;                      unit;           // is used for class label
 * \d+;                     duration;       // measured in seconds
 * \d+;                     steps;
 * \d{1,3}(\.\d{1,2})?;     weight;
 * \d{1,3}(\.\d{1,2})?;     met;
 * \d{1,3}(\.\d{1,2})?(;    calorie;
 * \d{1,3}(\.\d{1,2})?)?(;  distance
 * \d{1,3}(\.\d{1,2})?)?$
 *
 * }
 *
 * </pre>
 *
 * @author Nepomuk Seiler
 *
 */
class ACDataSaver extends TSaver {
  
  val df = new DecimalFormat("00")

  def write(dataset: Instances) {
    outputs foreach {
      case (name, out) =>
        val attrFrom = dataset.attribute("from");
        val attrTo = dataset.attribute("to");
        val attrClass = dataset.attribute("class");

        val writer = new PrintWriter(out)
        for (i <- 0 until dataset.size) {
          val instance = dataset.get(i)
          val from = new DateTime(instance.value(attrFrom).asInstanceOf[Long])
          val to = new DateTime(instance.value(attrFrom).asInstanceOf[Long])
          val clazz = instance.stringValue(attrClass)

          val duration = Seconds.secondsBetween(from, to)

          
          writer.append("##;")

          writer.append(i.toHexString).append(";") //recordId
          writer.append(from.getYear.toString).append(";") //year
          writer.append(df.format(from.getMonthOfYear)).append(";") //month
          writer.append(df.format(from.getDayOfMonth)).append(";")
          writer.append(df.format(from.getHourOfDay)).append(";")
          writer.append(df.format(from.getMinuteOfHour)).append(";")
          writer.append(df.format(from.getSecondOfMinute)).append(";")
          writer.append(clazz).append(";") // unit == classlabel
          writer.append(duration.getSeconds.toString).append(";") // duration in seconds
          writer.append("0;\n") //steps;
        }

        writer.flush
        writer.close
    }
  }

  def reset {}

  def configure(properties: Properties) {}

}

class ACDataSaverFactory extends ProcessorFactory(classOf[ACDataSaver])