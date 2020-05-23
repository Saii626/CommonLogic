package app.saikat.ThreadManagement.impl;

import java.lang.ref.WeakReference;
import java.util.function.Consumer;

import javax.inject.Provider;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import app.saikat.DIManagement.Impl.BeanManagers.InjectBeanManager;
import app.saikat.DIManagement.Impl.BeanManagers.PostConstructBeanManager;
import app.saikat.DIManagement.Impl.DIBeans.ConstantProviderBean;
import app.saikat.DIManagement.Impl.DIBeans.DIBeanImpl;
import app.saikat.DIManagement.Impl.ExternalImpl.ProviderImpl;
import app.saikat.DIManagement.Interfaces.DIBean;

/**
 * Helper class to perform generic actions for scheduling
 * @param <PARENT> type of parent
 * @param <TYPE> type of the bean
 */
public class TaskProvider<PARENT, TYPE> implements Provider<TYPE> {

	protected final DIBean<TYPE> weakInstanceCopy;
	protected final WeakReference<PARENT> weakParent;
	protected final ConstantProviderBean<PARENT> strongEmptyProvider;

	protected final InjectBeanManager injectBeanManager;
	protected final PostConstructBeanManager postConstructBeanManager;

	protected Consumer<TaskProvider<PARENT, TYPE>> onParentDied;

	private Logger logger = LogManager.getLogger(this.getClass());

	@SuppressWarnings("unchecked")
	public TaskProvider(DIBean<TYPE> instanceTaskCopy, ConstantProviderBean<PARENT> strongProvider,
			InjectBeanManager injectBeanManager, PostConstructBeanManager postConstructBeanManager) {
		this.weakInstanceCopy = instanceTaskCopy;
		this.strongEmptyProvider = strongProvider;

		this.weakParent = (WeakReference<PARENT>) this.weakInstanceCopy.getDependencies()
				.get(0)
				.getProvider()
				.get();

		this.injectBeanManager = injectBeanManager;
		this.postConstructBeanManager = postConstructBeanManager;
	}

	public void setOnParentDied(Consumer<TaskProvider<PARENT, TYPE>> onParentDied) {
		this.onParentDied = onParentDied;
	}

	@Override
	public TYPE get() {
		ConstantProviderBean<PARENT> parent = extractStrongReference();
		logger.trace("Executing task {}", this);

		if (parent != null) {
			logger.trace("Parent not null. Invoking method");
			DIBean<TYPE> invocationTaskCopy = this.weakInstanceCopy.copy();
			invocationTaskCopy.getDependencies()
					.set(0, parent);

			return executeBean(invocationTaskCopy);

		} else {
			logger.debug("Parent null. Triggering onParentDied");
			onParentDied.accept(this);
			return null;
		}
	}

	protected ConstantProviderBean<PARENT> extractStrongReference() {
		PARENT parent = weakParent.get();

		if (parent != null) {
			ConstantProviderBean<PARENT> providerBean = strongEmptyProvider.copy();
			providerBean.setProvider(() -> parent);
			return providerBean;
		} else {
			return null;
		}
	}

	protected TYPE executeBean(DIBean<TYPE> bean) {
		ProviderImpl<TYPE> provider = new ProviderImpl<>((DIBeanImpl<TYPE>) bean, injectBeanManager,
				postConstructBeanManager);
		return provider.get();
	}

	WeakReference<PARENT> getWeakParent() {
		return weakParent;
	}

	@Override
	public String toString() {
		return String.format("ScheduledTask#%d[%s, %s]", this.hashCode(), strongEmptyProvider.getProviderType()
				.toString(), weakInstanceCopy.toString());
	}
}