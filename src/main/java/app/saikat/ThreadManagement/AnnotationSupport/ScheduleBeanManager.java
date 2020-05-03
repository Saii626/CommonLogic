package app.saikat.ThreadManagement.AnnotationSupport;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Provider;

import com.google.common.base.Preconditions;
import com.google.common.reflect.TypeToken;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import app.saikat.Annotations.DIManagement.NoQualifier;
import app.saikat.Annotations.DIManagement.Scan;
import app.saikat.Annotations.ThreadManagement.Schedule;
import app.saikat.Annotations.ThreadManagement.Stats;
import app.saikat.DIManagement.Impl.BeanManagers.BeanManagerImpl;
import app.saikat.DIManagement.Impl.BeanManagers.InjectBeanManager;
import app.saikat.DIManagement.Impl.BeanManagers.PostConstructBeanManager;
import app.saikat.DIManagement.Impl.DIBeans.ConstantProviderBean;
import app.saikat.DIManagement.Impl.DIBeans.DIBeanImpl;
import app.saikat.DIManagement.Impl.Helpers.DependencyHelper;
import app.saikat.DIManagement.Interfaces.DIBean;
import app.saikat.ThreadManagement.interfaces.GenericSchedulerTask;

public class ScheduleBeanManager extends BeanManagerImpl {

	private Set<GenericSchedulerTask<?, ?>> toBeScheduledTasks = new HashSet<>();

	@Override
	public Map<Class<?>, Scan> addToScan() {
		Map<Class<?>, Scan> annotationsMap = new HashMap<>();
		Scan scan = createScanObject();

		annotationsMap.put(Schedule.class, scan);
		annotationsMap.put(Stats.class, scan);

		return annotationsMap;
	}

	@Override
	public <T> void beanCreated(DIBean<T> bean) {

		Preconditions.checkArgument(bean instanceof DIBeanImpl<?>,
				"Wrong bean type for Schedule/Stat bean. Expected type DIBeanImpl.class found %s", bean.getClass()
						.getSimpleName());

		DIBeanImpl<?> b = (DIBeanImpl<?>) bean;
		Preconditions.checkArgument(b.isMethod(), "Must be a method bean");
		Preconditions.checkArgument(b.getProviderType()
				.equals(TypeToken.of(Void.TYPE)), "Method should not return value");

		super.beanCreated(bean);
	}

	@Override
	public <T> List<DIBean<?>> resolveDependencies(DIBean<T> target, Collection<DIBean<?>> alreadyResolved,
			Collection<DIBean<?>> toBeResolved, Collection<Class<? extends Annotation>> allQualifiers) {

		if (target.getNonQualifierAnnotation()
				.equals(Schedule.class)) {
			return super.resolveDependencies(target, alreadyResolved, toBeResolved, allQualifiers);
		} else {
			DIBeanImpl<?> t = (DIBeanImpl<?>) target;

			logger.debug("Scanning dependencies of {}", target);

			List<DIBean<?>> unresolvedDependencies = DependencyHelper.scanAndSetDependencies(t, allQualifiers);
			logger.debug("Unresolved dependencies of {}: {}", target, unresolvedDependencies);

			Preconditions.checkArgument(unresolvedDependencies.size() == 2, "Has only 2 dependencies");

			DIBean<?> unresolved = t.getDependencies()
					.remove(1);
			Preconditions.checkArgument(unresolved.getProviderType()
					.equals(TypeToken.of(Logger.class)), "Only logger should be specified as dependency");

			List<DIBean<?>> resolvedDependencies = DependencyHelper.resolveAndSetDependencies(t, alreadyResolved,
					toBeResolved);
			logger.debug("Resolved dependencies of {}: {}", target, resolvedDependencies);


			Logger logger = LogManager.getLogger("stats_logger");
			ConstantProviderBean<Logger> loggerProvider = new ConstantProviderBean<>(TypeToken.of(Logger.class), NoQualifier.class);
			loggerProvider.setProvider(() -> logger);

			t.getDependencies().add(loggerProvider);
			return resolvedDependencies;
		}
	}

	@Override
	public <T> ConstantProviderBean<Provider<T>> createProviderBean(DIBean<T> target,
			InjectBeanManager injectBeanManager, PostConstructBeanManager postConstructBeanManager) {

		if (target.getNonQualifierAnnotation()
				.equals(Schedule.class)) {
			toBeScheduledTasks.add(new ScheduledTask<>(target, injectBeanManager, postConstructBeanManager));
		} else if (target.getNonQualifierAnnotation()
				.equals(Stats.class)) {
			toBeScheduledTasks.add(new StatLogTask<>(target, injectBeanManager, postConstructBeanManager));
		}

		return null;
	}

	public Set<GenericSchedulerTask<?, ?>> getToBeScheduledTasks() {
		return this.toBeScheduledTasks;
	}

}