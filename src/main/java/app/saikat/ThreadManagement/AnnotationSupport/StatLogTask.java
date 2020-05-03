package app.saikat.ThreadManagement.AnnotationSupport;

import java.util.concurrent.ScheduledFuture;

import javax.inject.Provider;

import com.google.common.base.Preconditions;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import app.saikat.Annotations.ThreadManagement.Stats;
import app.saikat.DIManagement.Impl.BeanManagers.InjectBeanManager;
import app.saikat.DIManagement.Impl.BeanManagers.PostConstructBeanManager;
import app.saikat.DIManagement.Interfaces.DIBean;
import app.saikat.ThreadManagement.interfaces.GenericSchedulerTask;

public class StatLogTask<PARENT, TYPE> extends GenericSchedulerTask<PARENT, TYPE> {

	private final Stats stats;
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

	@Override
	@SuppressWarnings("unchecked")
	protected ScheduledFuture<TYPE> scheduleExecutingProvider(Provider<TYPE> provider) {
		return (ScheduledFuture<TYPE>) scheduler.scheduleAtFixedRate(provider::get, stats.rate());
	}
}