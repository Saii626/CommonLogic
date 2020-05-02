package app.saikat.ThreadManagement.AnnotationSupport;

import java.util.concurrent.ScheduledFuture;

import javax.inject.Provider;

import com.google.common.base.Preconditions;
import com.google.gson.Gson;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import app.saikat.Annotations.ThreadManagement.Stats;
import app.saikat.DIManagement.Impl.BeanManagers.InjectBeanManager;
import app.saikat.DIManagement.Impl.BeanManagers.PostConstructBeanManager;
import app.saikat.DIManagement.Impl.DIBeans.ConstantProviderBean;
import app.saikat.DIManagement.Impl.DIBeans.DIBeanImpl;
import app.saikat.DIManagement.Impl.ExternalImpl.ProviderImpl;
import app.saikat.DIManagement.Interfaces.DIBean;
import app.saikat.ThreadManagement.interfaces.GenericSchedulerTask;
import app.saikat.ThreadManagement.interfaces.TaskProvider;

public class StatLogTask<PARENT, TYPE> extends GenericSchedulerTask<PARENT, TYPE> {

	private final Stats stats;
	private Gson gson;
	private final Logger logger = LogManager.getLogger("stats_logger");

	public StatLogTask(DIBean<TYPE> bean, InjectBeanManager injectBeanManager,
			PostConstructBeanManager postConstructBeanManager) {
		super(bean, injectBeanManager, postConstructBeanManager);

		logger.debug("all annotations: {}", this.task.getProviderType());
		this.stats = this.task.getInvokable()
				.getAnnotation(Stats.class);

		Preconditions.checkNotNull(this.stats, "Stats null");
		Preconditions.checkState((stats.rate() > 0), "Stat for " + task.getInvokable()
				.getName() + " must have rate set");
	}

	public void setGson(Gson gson) {
		this.gson = gson;
	}

	@Override
	protected TaskProvider<PARENT, TYPE> createTask(DIBean<TYPE> weakTask,
			ConstantProviderBean<PARENT> dummyParentProvider) {
		return this.new StatLogTaskProvider(weakTask, dummyParentProvider);
	}

	@Override
	@SuppressWarnings("unchecked")
	protected ScheduledFuture<TYPE> scheduleExecutingProvider(Provider<TYPE> provider) {
		return (ScheduledFuture<TYPE>) scheduler.scheduleAtFixedRate(provider::get, stats.rate());
	}

	private class StatLogTaskProvider extends TaskProvider<PARENT, TYPE> {

		public StatLogTaskProvider(DIBean<TYPE> instanceTaskCopy, ConstantProviderBean<PARENT> strongProvider) {
			super(instanceTaskCopy, strongProvider, StatLogTask.this.injectBeanManager, StatLogTask.this.postConstructBeanManager);
		}

		@Override
		protected TYPE executeBean(DIBean<TYPE> bean) {
			ProviderImpl<TYPE> provider = new ProviderImpl<>((DIBeanImpl<TYPE>) bean, injectBeanManager,
					postConstructBeanManager);
			TYPE obj = provider.get();

			if (obj != null) {
				String msg = gson.toJson(obj);
				logger.info(msg);
			}

			return obj;
		}

	}
}