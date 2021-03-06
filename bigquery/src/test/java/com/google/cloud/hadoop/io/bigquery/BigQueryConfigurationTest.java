package com.google.cloud.hadoop.io.bigquery;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import com.google.cloud.hadoop.fs.gcs.InMemoryGoogleHadoopFileSystem;
import java.io.IOException;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapreduce.JobID;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Unit tests for BigQueryConfigurationTest.
 */
@RunWith(JUnit4.class)
public class BigQueryConfigurationTest {
  /** Sample jobProjectId. */
  private static final String JOB_PROJECT_ID = "google.com:foo-project";

  /** Sample projectId for input. */
  private static final String INPUT_PROJECT_ID = "google.com:input-project";

  /** Sample datasetId for input. */
  private static final String INPUT_DATASET_ID = "test_input_dataset";

  /** Sample tableId for input. */
  private static final String INPUT_TABLE_ID = "test_input_table";

  /** Sample projectId for output. */
  private static final String OUTPUT_PROJECT_ID = "google.com:output-project";

  /** Sample datasetId for output. */
  private static final String OUTPUT_DATASET_ID = "test_output_dataset";

  /** Sample tableId for output. */
  private static final String OUTPUT_TABLE_ID = "test_output_table";

  /** Sample query for testing for output. */
  private static final String OUTPUT_TABLE_SCHEMA = "test_schema";

  /** Sample gcs bucket for io. */
  private static final String GCS_BUCKET = "test";

  /** Sample gcs temporary path for io. */
  private static final String GCS_TEMP_PATH = "gs://test";

  /** The Job Configuration for testing. */
  private static JobConf conf;

  @Mock private JobID mockJobID;

  /**
   * Set up before all classes.
   *
   * @throws IOException on IOError.
   */
  @Before
  public void setUp() throws IOException {
    // Generate Mocks.
    MockitoAnnotations.initMocks(this);

    // Generate a sample configuration to properly handle gs:// paths.
    Configuration config = InMemoryGoogleHadoopFileSystem.getSampleConfiguration();
    conf = new JobConf(config);
    new BigQueryConfiguration();
  }

  /**
   * Tests the BigQueryConfiguration getTemporaryPathRoot method's response for a custom path.
   *
   * @throws IOException on IOError.
   */
  @Test
  public void testGetTemporaryPathRootSpecific() throws IOException {
    // Set an explicit path.
    conf.set(BigQueryConfiguration.TEMP_GCS_PATH_KEY, GCS_TEMP_PATH);

    assertEquals(GCS_TEMP_PATH, BigQueryConfiguration.getTemporaryPathRoot(conf, mockJobID));
  }

  /**
   * Tests the BigQueryConfiguration getTemporaryPathRoot method's default response.
   *
   * @throws IOException on IOError.
   */
  @Test
  public void testGetTemporaryPathRootDefault() throws IOException {
    // Set the bucket for the default path.
    conf.set(BigQueryConfiguration.GCS_BUCKET_KEY, GCS_BUCKET);

    // Mock the JobID's toString which is used to generate the temporary path.
    when(mockJobID.toString()).thenReturn("test_job_id");

    checkNotNull(BigQueryConfiguration.getTemporaryPathRoot(conf, mockJobID));
  }

  /**
   * Tests the BigQueryConfiguration configureBigQueryInput method.
   */
  @Test
  public void testConfigureBigQueryInput() throws IOException {
    BigQueryConfiguration.configureBigQueryInput(
        conf,
        INPUT_PROJECT_ID,
        INPUT_DATASET_ID,
        INPUT_TABLE_ID);
    assertEquals(INPUT_PROJECT_ID, conf.get(BigQueryConfiguration.INPUT_PROJECT_ID_KEY));
    assertEquals(INPUT_DATASET_ID, conf.get(BigQueryConfiguration.INPUT_DATASET_ID_KEY));
    assertEquals(INPUT_TABLE_ID, conf.get(BigQueryConfiguration.INPUT_TABLE_ID_KEY));

    // By default, the job-level projectId inherits the input projectId if it's not already set.
    assertEquals(INPUT_PROJECT_ID, conf.get(BigQueryConfiguration.PROJECT_ID_KEY));
  }

  /**
   * Tests the BigQueryConfiguration configureBigQueryOutput method.
   */
  @Test
  public void testConfigureBigQueryOutput() throws IOException {
    BigQueryConfiguration.configureBigQueryOutput(
        conf,
        OUTPUT_PROJECT_ID,
        OUTPUT_DATASET_ID,
        OUTPUT_TABLE_ID,
        OUTPUT_TABLE_SCHEMA);
    assertEquals(OUTPUT_PROJECT_ID, conf.get(BigQueryConfiguration.OUTPUT_PROJECT_ID_KEY));
    assertEquals(OUTPUT_DATASET_ID, conf.get(BigQueryConfiguration.OUTPUT_DATASET_ID_KEY));
    assertEquals(OUTPUT_TABLE_ID, conf.get(BigQueryConfiguration.OUTPUT_TABLE_ID_KEY));
    assertEquals(OUTPUT_TABLE_SCHEMA, conf.get(BigQueryConfiguration.OUTPUT_TABLE_SCHEMA_KEY));

    // By default, the job-level projectId inherits the output projectId if it's not already set.
    assertEquals(OUTPUT_PROJECT_ID, conf.get(BigQueryConfiguration.PROJECT_ID_KEY));
  }

  @Test
  public void testConfigureBigQueryInputThenOutput() throws IOException {
    BigQueryConfiguration.configureBigQueryInput(
        conf,
        INPUT_PROJECT_ID,
        INPUT_DATASET_ID,
        INPUT_TABLE_ID);
    BigQueryConfiguration.configureBigQueryOutput(
        conf,
        OUTPUT_PROJECT_ID,
        OUTPUT_DATASET_ID,
        OUTPUT_TABLE_ID,
        OUTPUT_TABLE_SCHEMA);

    assertEquals(INPUT_PROJECT_ID, conf.get(BigQueryConfiguration.INPUT_PROJECT_ID_KEY));
    assertEquals(INPUT_DATASET_ID, conf.get(BigQueryConfiguration.INPUT_DATASET_ID_KEY));
    assertEquals(INPUT_TABLE_ID, conf.get(BigQueryConfiguration.INPUT_TABLE_ID_KEY));
    assertEquals(OUTPUT_PROJECT_ID, conf.get(BigQueryConfiguration.OUTPUT_PROJECT_ID_KEY));
    assertEquals(OUTPUT_DATASET_ID, conf.get(BigQueryConfiguration.OUTPUT_DATASET_ID_KEY));
    assertEquals(OUTPUT_TABLE_ID, conf.get(BigQueryConfiguration.OUTPUT_TABLE_ID_KEY));
    assertEquals(OUTPUT_TABLE_SCHEMA, conf.get(BigQueryConfiguration.OUTPUT_TABLE_SCHEMA_KEY));

    // Job level projectId got the inputProjectId just because we called it first.
    assertEquals(INPUT_PROJECT_ID, conf.get(BigQueryConfiguration.PROJECT_ID_KEY));
  }

  @Test
  public void testConfigureBigQueryInputThenOutputWithPresetJobProject() throws IOException {
    conf.set(BigQueryConfiguration.PROJECT_ID_KEY, JOB_PROJECT_ID);
    BigQueryConfiguration.configureBigQueryInput(
        conf,
        INPUT_PROJECT_ID,
        INPUT_DATASET_ID,
        INPUT_TABLE_ID);
    BigQueryConfiguration.configureBigQueryOutput(
        conf,
        OUTPUT_PROJECT_ID,
        OUTPUT_DATASET_ID,
        OUTPUT_TABLE_ID,
        OUTPUT_TABLE_SCHEMA);

    assertEquals(INPUT_PROJECT_ID, conf.get(BigQueryConfiguration.INPUT_PROJECT_ID_KEY));
    assertEquals(INPUT_DATASET_ID, conf.get(BigQueryConfiguration.INPUT_DATASET_ID_KEY));
    assertEquals(INPUT_TABLE_ID, conf.get(BigQueryConfiguration.INPUT_TABLE_ID_KEY));
    assertEquals(OUTPUT_PROJECT_ID, conf.get(BigQueryConfiguration.OUTPUT_PROJECT_ID_KEY));
    assertEquals(OUTPUT_DATASET_ID, conf.get(BigQueryConfiguration.OUTPUT_DATASET_ID_KEY));
    assertEquals(OUTPUT_TABLE_ID, conf.get(BigQueryConfiguration.OUTPUT_TABLE_ID_KEY));
    assertEquals(OUTPUT_TABLE_SCHEMA, conf.get(BigQueryConfiguration.OUTPUT_TABLE_SCHEMA_KEY));

    // Job level projectId remains unaltered by setting input/output projects.
    assertEquals(JOB_PROJECT_ID, conf.get(BigQueryConfiguration.PROJECT_ID_KEY));
  }

  @Test
  public void testConfigureBigQueryDefaultToJobProject() throws IOException {
    conf.set(BigQueryConfiguration.PROJECT_ID_KEY, JOB_PROJECT_ID);

    BigQueryConfiguration.configureBigQueryInput(
        conf,
        "",
        INPUT_DATASET_ID,
        INPUT_TABLE_ID);

    assertEquals(JOB_PROJECT_ID, conf.get(BigQueryConfiguration.INPUT_PROJECT_ID_KEY));
    assertEquals(INPUT_DATASET_ID, conf.get(BigQueryConfiguration.INPUT_DATASET_ID_KEY));
    assertEquals(INPUT_TABLE_ID, conf.get(BigQueryConfiguration.INPUT_TABLE_ID_KEY));

    BigQueryConfiguration.configureBigQueryOutput(
        conf,
        null,
        OUTPUT_DATASET_ID,
        OUTPUT_TABLE_ID,
        OUTPUT_TABLE_SCHEMA);

    assertEquals(JOB_PROJECT_ID, conf.get(BigQueryConfiguration.OUTPUT_PROJECT_ID_KEY));
    assertEquals(OUTPUT_DATASET_ID, conf.get(BigQueryConfiguration.OUTPUT_DATASET_ID_KEY));
    assertEquals(OUTPUT_TABLE_ID, conf.get(BigQueryConfiguration.OUTPUT_TABLE_ID_KEY));
    assertEquals(OUTPUT_TABLE_SCHEMA, conf.get(BigQueryConfiguration.OUTPUT_TABLE_SCHEMA_KEY));

    // Job level projectId remains unaltered by setting input/output projects.
    assertEquals(JOB_PROJECT_ID, conf.get(BigQueryConfiguration.PROJECT_ID_KEY));
  }

  @Test
  public void testConfigureBigQueryDefaultToJobProjectFullyQualifiedNames() throws IOException {
    conf.set(BigQueryConfiguration.PROJECT_ID_KEY, JOB_PROJECT_ID);

    BigQueryConfiguration.configureBigQueryInput(
        conf, String.format("%s.%s", INPUT_DATASET_ID, INPUT_TABLE_ID));

    assertEquals(JOB_PROJECT_ID, conf.get(BigQueryConfiguration.INPUT_PROJECT_ID_KEY));
    assertEquals(INPUT_DATASET_ID, conf.get(BigQueryConfiguration.INPUT_DATASET_ID_KEY));
    assertEquals(INPUT_TABLE_ID, conf.get(BigQueryConfiguration.INPUT_TABLE_ID_KEY));

    BigQueryConfiguration.configureBigQueryOutput(
        conf, String.format("%s.%s", OUTPUT_DATASET_ID, OUTPUT_TABLE_ID), OUTPUT_TABLE_SCHEMA);

    assertEquals(JOB_PROJECT_ID, conf.get(BigQueryConfiguration.OUTPUT_PROJECT_ID_KEY));
    assertEquals(OUTPUT_DATASET_ID, conf.get(BigQueryConfiguration.OUTPUT_DATASET_ID_KEY));
    assertEquals(OUTPUT_TABLE_ID, conf.get(BigQueryConfiguration.OUTPUT_TABLE_ID_KEY));
    assertEquals(OUTPUT_TABLE_SCHEMA, conf.get(BigQueryConfiguration.OUTPUT_TABLE_SCHEMA_KEY));

    // Job level projectId remains unaltered by setting input/output projects.
    assertEquals(JOB_PROJECT_ID, conf.get(BigQueryConfiguration.PROJECT_ID_KEY));
  }
}