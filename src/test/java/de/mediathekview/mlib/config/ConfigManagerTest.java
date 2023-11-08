package de.mediathekview.mlib.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

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
@TestMethodOrder(org.junit.jupiter.api.MethodOrderer.OrderAnnotation.class)
class ConfigManagerTest {
  private final String TEST_CONFIG_FILE_NAME = "TestConfig.yaml";

  class TestConfigManager extends ConfigManager<TestConfigDTO> {

    private TestConfigManager() {
      super();
      readConfig();
    }

    @Override
    protected String getConfigFileName() {
      return (TEST_CONFIG_FILE_NAME);
    }

    @Override
    protected Class<TestConfigDTO> getConfigClass() {
      return TestConfigDTO.class;
    }
  }

  @Test
  @Order(1)
  void testGetConfigFileName() {
    assertThat(new TestConfigManager().getConfigFileName()).isEqualTo(TEST_CONFIG_FILE_NAME);
  }

  @Test
  @Order(2)
  void testGetConfigClass() {
    assertThat(new TestConfigManager().getConfigClass()).isEqualTo(TestConfigDTO.class);
  }

  @Test
  @Order(3)
  void testReadClasspathConfig() {
    final TestConfigDTO classpathConfig = new TestConfigManager().getConfig();
    assertThat(classpathConfig.getValueWithDefault()).isEqualTo("TestValue");
    assertThat(classpathConfig.getValueWithoutDefault()).isEqualTo("Some other test value");
  }
    
}
