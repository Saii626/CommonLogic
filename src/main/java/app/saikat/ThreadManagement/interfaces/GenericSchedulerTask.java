package app.saikat.ThreadManagement.interfaces;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.concurrent.ScheduledFuture;

import javax.inject.Provider;

import com.google.common.reflect.TypeParameter;
import com.google.common.reflect.TypeToken;

import app.saikat.DIManagement.Impl.BeanManagers.InjectBeanManager;
import app.saikat.DIManagement.Impl.BeanManagers.PostConstructBeanManager;
import app.saikat.DIManagement.Impl.DIBeans.ConstantProviderBean;
import app.saikat.DIManagement.Impl.DIBeans.DIBeanImpl;
import app.saikat.DIManagement.Impl.ExternalImpl.ProviderImpl;
import app.saikat.DIManagement.Interfaces.DIBean;
import app.saikat.PojoCollections.CommonObjects.Tuple;
import app.saikat.PojoCollections.Utils.CommonFunc;

/**
 * A generic schedulable task
 * @param <PARENT> Type of parent bean. i.e. type of 1st dependency in dependency list
 * @param <TYPE> Type if this bean, i.e. bean's providerType
 */
public abstract class GenericSchedulerTask<PARENT, TYPE> {

	protected Scheduler scheduler;
	protected final DIBean<TYPE> task;

	protected final InjectBeanManager injectBeanManager;
	protected final PostConstructBeanManager postConstructBeanManager;

	public GenericSchedulerTask(DIBean<TYPE> bean, InjectBeanManager injectBeanManager,
			PostConstructBeanManager postConstructBeanManager) {

		this.task = bean;
		this.injectBeanManager = injectBeanManager;
		this.postConstructBeanManager = postConstructBeanManager;
	}

	protected abstract ScheduledFuture<TYPE> scheduleExecutingProvider(Provider<TYPE> provider);

	protected abstract TaskProvider<PARENT, TYPE> createTask(DIBean<TYPE> weakTask, ConstantProviderBean<PARENT> dummyParentProvider);

	public void setSchedulerAndExecute(Scheduler scheduler) {
		this.scheduler = scheduler;

		List<DIBean<?>> dependencies = task.getDependencies();

		if (dependencies.get(0) == null) {
			// static method
			ProviderImpl<TYPE> provider = new ProviderImpl<>((DIBeanImpl<TYPE>) task, injectBeanManager,
					postConstructBeanManager);

			ScheduledFuture<TYPE> scheduledTask = scheduleExecutingProvider(provider);

			Tuple<TaskProvider<?, ?>, ScheduledFuture<?>> tuple = Tuple.of(null, scheduledTask);
			CommonFunc.safeAddToMapSet(this.scheduler.getTasksMap(), this.task, tuple);
		} else {
			setupListenerAndSchedule();
		}
	}

	@SuppressWarnings({ "unchecked", "serial" })
	private void setupListenerAndSchedule() {
		DIBean<PARENT> parentBean = (DIBean<PARENT>) this.task.getDependencies()
				.get(0);

		TypeToken<WeakReference<PARENT>> weakReference = new TypeToken<WeakReference<PARENT>>() {}
				.where(new TypeParameter<PARENT>() {}, parentBean.getProviderType());

		ConstantProviderBean<WeakReference<PARENT>> weakProvider = new ConstantProviderBean<>(weakReference,
				parentBean.getQualifier());
		ConstantProviderBean<PARENT> strongProvider = new ConstantProviderBean<>(parentBean.getProviderType(),
				parentBean.getQualifier());

		parentBean.getBeanManager()
				.addListenerForBean(parentBean, (pBean, instance) -> {
					DIBean<TYPE> instanceTaskCopy = this.task.copy();

					ConstantProviderBean<WeakReference<PARENT>> weakProviderCopy = weakProvider.copy();
					weakProviderCopy.setProvider(() -> new WeakReference<>(instance));

					instanceTaskCopy.getDependencies()
							.set(0, weakProviderCopy);

					TaskProvider<PARENT, TYPE> taskRunner = createTask(instanceTaskCopy, strongProvider);
					ScheduledFuture<TYPE> scheduledTask = scheduleExecutingProvider(taskRunner);

					Tuple<TaskProvider<?, ?>, ScheduledFuture<?>> tuple = Tuple.of(taskRunner, scheduledTask);
					taskRunner.setOnParentDied(t -> {
						scheduledTask.cancel(true);
						CommonFunc.safeRemoveFromMapSet(this.scheduler.getTasksMap(), this.task, tuple);
					});

					CommonFunc.safeAddToMapSet(this.scheduler.getTasksMap(), this.task, tuple);
				});
	}

	// private class TaskProvider<U> implements Provider<U> {

	// 	private final DIBean<U> weakInstanceCopy;
	// 	private final ConstantProviderBean<T> strongEmptyProvider;

	// 	private Consumer<TaskProvider<U>> onParentDied;

	// 	public TaskProvider(DIBean<U> instanceTaskCopy, ConstantProviderBean<T> strongProvider) {
	// 		this.weakInstanceCopy = instanceTaskCopy;
	// 		this.strongEmptyProvider = strongProvider;
	// 	}

	// 	public void setOnParentDied(Consumer<TaskProvider<U>> onParentDied) {
	// 		this.onParentDied = onParentDied;
	// 	}

	// 	@Override
	// 	@SuppressWarnings("unchecked")
	// 	public U get() {
	// 		T parent = ((WeakReference<T>) weakInstanceCopy.getDependencies()
	// 				.get(0)
	// 				.getProvider()
	// 				.get()).get();

	// 		if (parent != null) {
	// 			ConstantProviderBean<T> providerBean = strongEmptyProvider.copy();
	// 			providerBean.setProvider(() -> (T) parent);

	// 			DIBean<?> invocationTaskCopy = this.weakInstanceCopy.copy();
	// 			invocationTaskCopy.getDependencies()
	// 					.set(0, providerBean);

	// 			ProviderImpl<U> provider = new ProviderImpl<>((DIBeanImpl<U>) invocationTaskCopy, injectBeanManager,
	// 					postConstructBeanManager);

	// 			return provider.get();

	// 		} else {
	// 			onParentDied.accept(this);
	// 			return null;
	// 		}
	// 	}

	// }
}