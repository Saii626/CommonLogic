package app.saikat.ThreadManagement.StatsLoggerInstanceTest;

import java.io.File;

import app.saikat.Annotations.ConfigurationManagement.ConfigFile;
import app.saikat.Annotations.DIManagement.Provides;

public class ConfigFileProvider {

	@Provides
	@ConfigFile
	static File getConfigFile() {
		File testconfigFile = new File(System.getProperty("user.home") + "/test/statslogger_test.conf");

		if (testconfigFile.exists()) {
			testconfigFile.delete();
		}

		return testconfigFile;
	}
}