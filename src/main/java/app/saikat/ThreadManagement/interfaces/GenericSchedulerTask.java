package app.saikat.ThreadManagement.interfaces;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.concurrent.ScheduledFuture;

import javax.inject.Provider;

import com.google.common.reflect.TypeParameter;
import com.google.common.reflect.TypeToken;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import app.saikat.DIManagement.Impl.BeanManagers.InjectBeanManager;
import app.saikat.DIManagement.Impl.BeanManagers.PostConstructBeanManager;
import app.saikat.DIManagement.Impl.DIBeans.ConstantProviderBean;
import app.saikat.DIManagement.Impl.DIBeans.DIBeanImpl;
import app.saikat.DIManagement.Impl.ExternalImpl.ProviderImpl;
import app.saikat.DIManagement.Interfaces.DIBean;
import app.saikat.DIManagement.Interfaces.DIBeanManager;
import app.saikat.PojoCollections.CommonObjects.Tuple;
import app.saikat.PojoCollections.Utils.CommonFunc;
import app.saikat.ThreadManagement.impl.TaskProvider;

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

	private Logger logger = LogManager.getLogger(this.getClass());

	public GenericSchedulerTask(DIBean<TYPE> bean, InjectBeanManager injectBeanManager,
			PostConstructBeanManager postConstructBeanManager) {

		this.task = bean;
		this.injectBeanManager = injectBeanManager;
		this.postConstructBeanManager = postConstructBeanManager;
	}

	protected abstract ScheduledFuture<TYPE> scheduleExecutingProvider(Provider<TYPE> provider);

	public void setSchedulerAndExecute(Scheduler scheduler) {
		this.scheduler = scheduler;

		List<DIBean<?>> dependencies = task.getDependencies();

		if (dependencies.get(0) == null) {
			logger.debug("Scheduler task {} is static. Scheduling now");
			// static method
			ProviderImpl<TYPE> provider = new ProviderImpl<>((DIBeanImpl<TYPE>) task, injectBeanManager,
					postConstructBeanManager);

			ScheduledFuture<TYPE> scheduledTask = scheduleExecutingProvider(provider);

			Tuple<TaskProvider<?, ?>, ScheduledFuture<?>> tuple = Tuple.of(null, scheduledTask);
			CommonFunc.safeAddToMapSet(this.scheduler.getTasksMap(), this.task, tuple);
		} else {
			logger.debug("Scheduler task {} is not static. Setting up listeners", task);
			setupListenerAndSchedule();
		}
	}

	@SuppressWarnings({ "unchecked", "serial" })
	private void setupListenerAndSchedule() {
		DIBean<PARENT> parentBean = (DIBean<PARENT>) this.task.getDependencies()
				.get(0);

		TypeToken<WeakReference<PARENT>> weakReference = new TypeToken<WeakReference<PARENT>>() {
		}.where(new TypeParameter<PARENT>() {
		}, parentBean.getProviderType());

		ConstantProviderBean<WeakReference<PARENT>> weakProvider = new ConstantProviderBean<>(weakReference,
				parentBean.getQualifier());
		ConstantProviderBean<PARENT> strongProvider = new ConstantProviderBean<>(parentBean.getProviderType(),
				parentBean.getQualifier());

		logger.debug("Setting up listener for type {}", parentBean.getProviderType());
		DIBeanManager.addListenerForClass(parentBean.getProviderType()
				.getRawType(), (pBean, instance) -> {
					logger.debug("New instance {} created for {} bean. Scheduling task {} for this instance", instance,
							pBean, task);

					DIBean<TYPE> instanceTaskCopy = this.task.copy();

					ConstantProviderBean<WeakReference<PARENT>> weakProviderCopy = weakProvider.copy();
					weakProviderCopy.setProvider(() -> (WeakReference<PARENT>) new WeakReference<>(instance));

					instanceTaskCopy.getDependencies()
							.set(0, weakProviderCopy);

					TaskProvider<PARENT, TYPE> taskRunner = new TaskProvider<>(instanceTaskCopy, strongProvider,
							injectBeanManager, postConstructBeanManager);

					logger.debug("Scheduling provider {}", taskRunner);
					ScheduledFuture<TYPE> scheduledTask = scheduleExecutingProvider(taskRunner);

					Tuple<TaskProvider<?, ?>, ScheduledFuture<?>> tuple = Tuple.of(taskRunner, scheduledTask);
					taskRunner.setOnParentDied(t -> {
						logger.debug("Parent died. Removing scheduled task {}", t);
						scheduledTask.cancel(true);
						CommonFunc.safeRemoveFromMapSet(this.scheduler.getTasksMap(), this.task, tuple);
					});

					CommonFunc.safeAddToMapSet(this.scheduler.getTasksMap(), this.task, tuple);
				});
	}
}