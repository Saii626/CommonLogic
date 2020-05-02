package app.saikat.ThreadManagement.StatsLoggerStaticTest;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import app.saikat.DIManagement.Exceptions.BeanNotFoundException;
import app.saikat.DIManagement.Interfaces.DIManager;
import app.saikat.ThreadManagement.interfaces.Scheduler;

public class TestStats {

	// Test for static case
	@SuppressWarnings("unused")
	@Test
	public void testStatic() throws BeanNotFoundException, InterruptedException {
		DIManager manager = DIManager.newInstance();
		manager.scan("app.saikat.Annotations", "app.saikat.ConfigurationManagement", "app.saikat.DIManagement",
				"app.saikat.GsonManagement", "app.saikat.ThreadManagement.AnnotationSupport",
				"app.saikat.ThreadManagement.impl", "app.saikat.ThreadManagement.StatsLoggerStaticTest");

		Scheduler scheduler = manager.getBeanOfType(Scheduler.class)
				.getProvider()
				.get();
		Thread.sleep(5000);

		assertEquals("logger invoked 5 time", 5, A.getCounter());
	}
}