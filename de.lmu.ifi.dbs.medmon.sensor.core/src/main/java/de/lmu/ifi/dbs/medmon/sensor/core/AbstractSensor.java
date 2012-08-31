package de.lmu.ifi.dbs.medmon.sensor.core;

public abstract class AbstractSensor implements ISensor {

	private final String id;

	public AbstractSensor() {
		id = getClass().getName() + getVersion();
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public String toString() {
		return getName() + " - " + getVersion() + " [" + getId() + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AbstractSensor other = (AbstractSensor) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

}
