package de.lmu.ifi.dbs.medmon.sensor.core.util;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SafeRunner;

import de.lmu.ifi.dbs.medmon.sensor.core.sensor.ISensor;

/**
 * 
 * @author Nepomuk Seiler
 * @version 0.5
 * @since 30.03.2011
 *
 */
public class SensorUtil {

	/**
	 * Provides all registered ISensorDataAlgorithm Extensions. No TypeCast
	 * check is made!
	 * 
	 * @return ISensorDataAlgorihtm[] containing all registered Extensions
	 */
	public static ISensor[] evaluateSensors() {
		List<ISensor> extensions = evaluateExtensionsAsList();
		return (ISensor[]) extensions.toArray(new ISensor[extensions.size()]);
	}
	
	/**
	 * Provides all registered ISensorDataAlgorithm Extensions. No TypeCast
	 * check is made!
	 * 
	 * @return ISensorDataAlgorihtm[] containing all registered Extensions
	 */
	public static List<ISensor> evaluateExtensionsAsList() {
		//   public <T> T find(Class<T> entityClass, Object primaryKey); for instanceof check
		IConfigurationElement[] config = Platform.getExtensionRegistry().getConfigurationElementsFor(ISensor.SENSOR_ID);
		final LinkedList<ISensor> extensions = new LinkedList<ISensor>();
		try {
			for (IConfigurationElement e : config) {
				final Object o = e.createExecutableExtension("class");
				// E castCheck = (E)o;

				ISafeRunnable runnable = new ISafeRunnable() {
					@Override
					public void handleException(Throwable exception) {
//						logger.severe("Exception in client");
						exception.printStackTrace();
					}

					@Override
					public void run() throws Exception {
						extensions.add((ISensor) o);
					}
				};
				SafeRunner.run(runnable);

			}
		} catch (CoreException ex) {
			ex.printStackTrace();
//			logger.severe(ex.getMessage());
		}
		return extensions;
	}
}
