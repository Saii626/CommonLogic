package app.saikat.ThreadManagement.AnnotationSupport;

import java.util.concurrent.ScheduledFuture;

import javax.inject.Provider;

import com.google.common.base.Preconditions;

import app.saikat.Annotations.ThreadManagement.Schedule;
import app.saikat.DIManagement.Impl.BeanManagers.InjectBeanManager;
import app.saikat.DIManagement.Impl.BeanManagers.PostConstructBeanManager;
import app.saikat.DIManagement.Impl.DIBeans.ConstantProviderBean;
import app.saikat.DIManagement.Impl.DIBeans.DIBeanImpl;
import app.saikat.DIManagement.Impl.ExternalImpl.ProviderImpl;
import app.saikat.DIManagement.Interfaces.DIBean;
import app.saikat.ThreadManagement.interfaces.GenericSchedulerTask;
import app.saikat.ThreadManagement.interfaces.TaskProvider;

public class ScheduledTask<PARENT, TYPE> extends GenericSchedulerTask<PARENT, TYPE> {

	private final Schedule schedule;

	public ScheduledTask(DIBean<TYPE> bean, InjectBeanManager injectBeanManager,
			PostConstructBeanManager postConstructBeanManager) {
		super(bean, injectBeanManager, postConstructBeanManager);

		this.schedule = this.task.getInvokable()
				.getAnnotation(Schedule.class);

		Preconditions.checkState((schedule.interval() > 0) ^ (schedule.rate() > 0),
				"Schedule for " + task.getInvokable()
						.getName() + " must have either interval or rate set");
	}

	@Override
	@SuppressWarnings("unchecked")
	protected ScheduledFuture<TYPE> scheduleExecutingProvider(Provider<TYPE> provider) {
		if (schedule.interval() > 0) {
			return (ScheduledFuture<TYPE>)scheduler.scheduleWithFixedDelay(provider::get, schedule.interval());
		} else {
			return (ScheduledFuture<TYPE>)scheduler.scheduleAtFixedRate(provider::get, schedule.rate());
		}
	}

	@Override
	protected TaskProvider<PARENT, TYPE> createTask(DIBean<TYPE> weakTask, ConstantProviderBean<PARENT> dummyParentProvider) {
		return this.new ScheduledTaskProvider(weakTask, dummyParentProvider);
	}

	private class ScheduledTaskProvider extends TaskProvider<PARENT, TYPE> {

		public ScheduledTaskProvider(DIBean<TYPE> instanceTaskCopy, ConstantProviderBean<PARENT> strongProvider) {
			super(instanceTaskCopy, strongProvider, ScheduledTask.this.injectBeanManager, ScheduledTask.this.postConstructBeanManager);
		}

		@Override
		protected TYPE executeBean(DIBean<TYPE> bean) {
			ProviderImpl<TYPE> provider = new ProviderImpl<>((DIBeanImpl<TYPE>)bean, injectBeanManager,
                    postConstructBeanManager);
            return provider.get();
		}
		
	}
}