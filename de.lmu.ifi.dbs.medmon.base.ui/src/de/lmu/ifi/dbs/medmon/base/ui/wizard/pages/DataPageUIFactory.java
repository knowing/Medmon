package de.lmu.ifi.dbs.medmon.base.ui.wizard.pages;

import org.eclipse.swt.widgets.Composite;

import akka.actor.ActorRef;
import akka.actor.TypedActor;

import de.lmu.ifi.dbs.knowing.core.events.Status;
import de.lmu.ifi.dbs.knowing.core.factory.UIFactory;
import de.lmu.ifi.dbs.knowing.core.model.INode;

/**
 * @author Nepomuk Seiler
 * @version 0.1
 * @since 13.06.2011
 * 
 */
public class DataPageUIFactory extends TypedActor implements UIFactory {

	private Composite parent;
	private ActorRef supervisor;


	/**
	 * @param parent
	 */
	public DataPageUIFactory(Composite parent) {
		this.parent = parent;
	}

	/**
	 * @param parent
	 *            the parent to set
	 */
	public void setParent(Composite parent) {
		this.parent = parent;
	}

	@Override
	public Object createContainer(INode node) {
		return parent;
	}

	@Override
	public void update(ActorRef ref, Status status) {
		parent.update();
	}

	@Override
	public void setSupervisor(ActorRef supervisor) {
		this.supervisor = supervisor;
	}
}
