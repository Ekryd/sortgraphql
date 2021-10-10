package sortgraphql;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.util.List;

/** Common parent for SortMojo */
@SuppressWarnings("unused")
abstract class AbstractParentMojo extends AbstractMojo {

  final SorterImpl sorter = new SorterImpl();

  /** Name of the file extension for the backup file. */
  @Parameter(property = "sortgraphql.backupFileExtension", defaultValue = ".bak")
  protected String backupFileExtension;

  /** Should a backup copy be created for the sorted schema. */
  @Parameter(property = "sortgraphql.createBackupFile", defaultValue = "true")
  protected boolean createBackupFile;

  /** Encoding for the files. */
  @Parameter(property = "sortgraphql.encoding", defaultValue = "UTF-8")
  protected String encoding;

  /**
   * Use hash sign for descriptions/comments, instead of string literals (with quote character),
   * when generating the sorted schema file.
   */
  @Parameter(property = "sortgraphql.generateHashDescriptions", defaultValue = "true")
  protected boolean generateHashDescriptions;

  /**
   * Generate the 'schema' definition element at the top of the schema, when generating the sorted
   * schema file.
   */
  @Parameter(property = "sortgraphql.generateSchemaDefinition", defaultValue = "false")
  protected boolean generateSchemaDefinition;

  /**
   * By default, if multiple schema files are specified in schema files, then those schemas will be
   * merged together during validation. This flag specifies that each schema should be validated
   * individually.
   */
  @Parameter(property = "sortgraphql.individualSchemas", defaultValue = "false")
  protected boolean individualSchemas;

  /** Location of the graphql schema file that should be sorted. If multiple, use schemaFiles. */
  @Parameter(
      property = "sortgraphql.schemaFile",
      defaultValue = "src/main/resources/schema.graphqls")
  protected File schemaFile;

  /**
   * Location of multiple graphql schema file that should be sorted. Overrides parameter schemaFile.
   * The schema files can reference each other, but shared definitions are not allowed.
   */
  @Parameter(property = "sortgraphql.schemaFiles")
  protected List<File> schemaFiles;

  /** Set this to 'true' to bypass SortGraphQL plugin */
  @Parameter(property = "sortgraphql.skip", defaultValue = "false")
  private boolean skip;

  /** Skip sorting the types in a union. */
  @Parameter(property = "sortgraphql.skipUnionTypeSorting", defaultValue = "false")
  protected boolean skipUnionTypeSorting;

  /** Skip sorting the arguments for a field in a type. */
  @Parameter(property = "sortgraphql.skipFieldArgumentSorting", defaultValue = "false")
  protected boolean skipFieldArgumentSorting;

  /**
   * Execute plugin.
   *
   * @throws org.apache.maven.plugin.MojoFailureException exception that will be handled by plugin
   *     framework
   * @see org.apache.maven.plugin.Mojo#execute()
   */
  @Override
  public void execute() throws MojoFailureException {
    if (skip) {
      getLog().info("Skipping SortGraphQL");
    } else {
      setup();
      sortGraphQLSchema();
    }
  }

  protected abstract void setup() throws MojoFailureException;

  protected abstract void sortGraphQLSchema() throws MojoFailureException;
}
