package de.mediathekview.mlib.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

/**
 * Testin the {@link ConfigManager} and if a config file on the path can override the one in the
 * classpath.
 *
 * @author nicklas
 */
class ConfigManagerTest {
  private static final String TEST_CONFIG_FILE_NAME = "TestConfig.yaml";

  class TestConfigManager extends ConfigManager<TestConfigDTO> {

    private TestConfigManager() {
      super();
      readConfig();
    }

    @Override
    protected String getConfigFileName() {
      return TEST_CONFIG_FILE_NAME;
    }

    @Override
    protected Class<TestConfigDTO> getConfigClass() {
      return TestConfigDTO.class;
    }
  }

  @Test
  void testGetConfigFileName() {
    assertThat(new TestConfigManager().getConfigFileName()).isEqualTo(TEST_CONFIG_FILE_NAME);
  }

  @Test
  void testGetConfigClass() {
    assertThat(new TestConfigManager().getConfigClass()).isEqualTo(TestConfigDTO.class);
  }

  @Test
  void testReadClasspathConfig() {
    final TestConfigDTO classpathConfig = new TestConfigManager().getConfig();
    assertThat(classpathConfig.getValueWithDefault()).isEqualTo("Hello World!");
    assertThat(classpathConfig.getValueWithoutDefault()).isEqualTo("Not the default, sorry!");
  }

  @Test
  void testReadFileConfig() throws IOException {

    writeTempTestFileConfig();

    final TestConfigDTO fileConfig = new TestConfigManager().getConfig();
    assertThat(fileConfig.getValueWithDefault()).isEqualTo("TestValue");
    assertThat(fileConfig.getValueWithoutDefault()).isEqualTo("Some other test value");
  }

  @BeforeEach
  void deleteExistingFiles() throws IOException {
    Files.deleteIfExists(Paths.get(TEST_CONFIG_FILE_NAME));
  }

  private void writeTempTestFileConfig() throws IOException {
    final Path tempConfigPath = Paths.get("./" + TEST_CONFIG_FILE_NAME);

    Files.write(
        tempConfigPath,
        Arrays.asList("valueWithDefault: TestValue", "valueWithoutDefault: Some other test value"),
        StandardOpenOption.CREATE,
        StandardOpenOption.TRUNCATE_EXISTING);
  }
}
