package app.saikat.ThreadManagement.AnnotationSupport;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.inject.Provider;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import app.saikat.Annotations.DIManagement.Scan;
import app.saikat.Annotations.GsonManagement.NoPretty;
import app.saikat.Annotations.ThreadManagement.Schedule;
import app.saikat.Annotations.ThreadManagement.Stats;
import app.saikat.DIManagement.Exceptions.NotValidBean;
import app.saikat.DIManagement.Impl.BeanManagers.BeanManagerImpl;
import app.saikat.DIManagement.Impl.BeanManagers.InjectBeanManager;
import app.saikat.DIManagement.Impl.BeanManagers.PostConstructBeanManager;
import app.saikat.DIManagement.Impl.DIBeans.ConstantProviderBean;
import app.saikat.DIManagement.Impl.DIBeans.DIBeanImpl;
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

		if (!(bean instanceof DIBeanImpl<?>)) {
			throw new RuntimeException(String.format(
					"Wrong bean type for Schedule/Stat bean. Expected type DIBeanImpl.class found %s", bean.getClass()
							.getSimpleName()));
		}

		if (bean.getNonQualifierAnnotation()
				.equals(Schedule.class) && ((DIBeanImpl<?>) bean).isMethod()
				&& !bean.getProviderType()
						.equals(TypeToken.of(Void.TYPE))) {
			throw new NotValidBean(bean, "Schedule should not return value");
		}

		if (bean.getNonQualifierAnnotation()
				.equals(Stats.class) && ((DIBeanImpl<?>) bean).isMethod()
				&& bean.getProviderType()
						.equals(TypeToken.of(Void.TYPE))) {
			throw new NotValidBean(bean, "Stats should return value");
		}

		super.beanCreated(bean);
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
		TypeToken<Gson> gsonTypeToken = TypeToken.of(Gson.class);
		DIBean<?> gsonBean = repo.getBeans()
				.parallelStream()
				.filter(b -> b.getProviderType()
						.equals(gsonTypeToken)
						&& b.getQualifier()
								.equals(NoPretty.class))
				.findAny()
				.orElseThrow(() -> new NullPointerException());

		Gson gson = (Gson) gsonBean.getProvider()
				.get();

		toBeScheduledTasks.parallelStream()
				.filter(task -> task instanceof StatLogTask)
				.forEach(task -> ((StatLogTask<?, ?>) task).setGson(gson));
		return this.toBeScheduledTasks;
	}

}