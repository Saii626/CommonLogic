package app.saikat.ThreadManagement.StatsLoggerInstanceTest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Set;
import java.util.concurrent.ScheduledFuture;

import javax.inject.Provider;

import org.junit.Test;

import app.saikat.Annotations.ThreadManagement.Stats;
import app.saikat.DIManagement.Exceptions.BeanNotFoundException;
import app.saikat.DIManagement.Interfaces.DIBean;
import app.saikat.DIManagement.Interfaces.DIManager;
import app.saikat.PojoCollections.CommonObjects.Tuple;
import app.saikat.ThreadManagement.interfaces.Scheduler;
import app.saikat.ThreadManagement.interfaces.TaskProvider;
import app.saikat.ThreadManagement.interfaces.TaskProviderHelper;

public class TestStats {

	//Test for non static case. Check thats jobs are scheduled
	// only after Scheduler is initialized
	@Test
	@SuppressWarnings("unchecked")
	public void testNonStatic() throws BeanNotFoundException, InterruptedException {
		DIManager manager = DIManager.newInstance();
		manager.scan("app.saikat.Annotations", "app.saikat.ConfigurationManagement", "app.saikat.DIManagement",
				"app.saikat.GsonManagement", "app.saikat.ThreadManagement.AnnotationSupport",
				"app.saikat.ThreadManagement.impl", "app.saikat.ThreadManagement.StatsLoggerInstanceTest");

		assertEquals("No logger running", 0, A.getC());

		Scheduler scheduler = manager.getBeanOfType(Scheduler.class)
				.getProvider()
				.get();
		Thread.sleep(5000);

		assertEquals("Still no logger running as no instance created", 0, A.getC());

		DIBean<A> aBean = manager.getBeanOfType(A.class);
		DIBean<?> aStatBean = manager.getBeansWithType(Stats.class)
				.parallelStream()
				.filter(b -> b.getDependencies().get(0) != null)
				.filter(b -> aBean.getProviderType().equals(b.getDependencies()
						.get(0).getProviderType()))
				.findAny()
				.get();
		Provider<A> aProvider = aBean.getProvider();

		A a = aProvider.get();
		a.setStr("a");
		Thread.sleep(5100);
		assertEquals("A's counter increased", 5, A.getC());
		assertEquals("b's counter increased", 5, a.getCounter());

		A a1 = aProvider.get();
		A a2 = aProvider.get();
		a1.setStr("a1");
		a2.setStr("a2");

		Thread.sleep(5100);
		assertEquals("A's counter increased", 20, A.getC());
		assertEquals("a's counter increased", 10, a.getCounter());
		assertEquals("a1's counter increased", 5, a1.getCounter());
		assertEquals("a2's counter increased", 5, a2.getCounter());

		Set<Tuple<TaskProvider<?, ?>, ScheduledFuture<?>>> scheduledTasks = scheduler.getTasksMap()
				.get(aStatBean);
		assertTrue("Has expected number of scheduled tasks", scheduledTasks.size() == 3);

		TaskProvider<A, Object> taskOfb1 = (TaskProvider<A, Object>) scheduledTasks.parallelStream()
				.filter(t -> TaskProviderHelper.getParent(t.first)
						.equals(a1))
				.findAny()
				.get().first;

		TaskProviderHelper.clearParent(taskOfb1);

		Thread.sleep(5100);
		assertEquals("A's counter increased", 30, A.getC());
		assertEquals("a's counter increased", 15, a.getCounter());
		assertEquals("a1's counter remained same", 5, a1.getCounter());
		assertEquals("a2's counter increased", 10, a2.getCounter());

		scheduledTasks = scheduler.getTasksMap()
				.get(aStatBean);
		assertTrue("Has expected number of scheduled tasks", scheduledTasks.size() == 2);

		scheduledTasks.forEach(t -> TaskProviderHelper.clearParent(t.first));

		Thread.sleep(5100);
		assertEquals("A's counter remained same", 30, A.getC());
		assertEquals("a's counter remained same", 15, a.getCounter());
		assertEquals("a1's counter remained same", 5, a1.getCounter());
		assertEquals("a2's counter remained same", 10, a2.getCounter());
	}

}