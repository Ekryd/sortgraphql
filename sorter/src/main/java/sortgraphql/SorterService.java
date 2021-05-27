package sortgraphql;

import graphql.language.InputValueDefinition;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import sortgraphql.exception.FailureException;
import sortgraphql.logger.SortingLogger;
import sortgraphql.parameter.PluginParameters;
import sortgraphql.sort.FakeRuntimeWiringFactory;
import sortgraphql.sort.OptionsBuilder;
import sortgraphql.sort.SchemaPrinter;
import sortgraphql.util.FileUtil;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

/** Contain the concrete methods to sort the schema */
public class SorterService {
  private final FileUtil fileUtil = new FileUtil();
  private final FakeRuntimeWiringFactory wiringFactory = new FakeRuntimeWiringFactory();

  private SortingLogger log;

  private File schemaFile;
  private boolean createBackupFile;
  private String backupFileExtension;

  public void setup(SortingLogger log, PluginParameters pluginParameters) {
    this.log = log;
    this.schemaFile = pluginParameters.schemaFile;
    this.createBackupFile = pluginParameters.createBackupFile;
    this.backupFileExtension = pluginParameters.backupFileExtension;

    fileUtil.setup(pluginParameters);
  }

  public String getSchemaContent() {
    return fileUtil.getSchemaContent();
  }

  public String sortSchema(String schema) {
    var registry = new SchemaParser().parse(schema);
    var something = registry.getDirectiveDefinition("something").orElseThrow();
    registry.remove(something);
    var trans = something.transform(builder -> {
      List<InputValueDefinition> inputValueDefinitions = something.getInputValueDefinitions();
      List<InputValueDefinition> withoutDefaultValues = inputValueDefinitions.stream()
              .map(inputValueDefinition -> {
                return inputValueDefinition.transform(builder1 -> {
                  builder1.defaultValue(null);
                });
              })
              .collect(Collectors.toList());

      builder.inputValueDefinitions(withoutDefaultValues);
    });
    registry.add(trans);
    RuntimeWiring runtimeWiring = wiringFactory.createFakeRuntime(registry);

    var graphQLSchema = new SchemaGenerator().makeExecutableSchema(registry, runtimeWiring);

    var options =
        OptionsBuilder.defaultOptions()
            .setDescriptionsAsHashComments(true)
            .setIncludeDirectiveDefinitions(false)
            .setIncludeDefinedDirectiveDefinitions(true)
            .build();

    return new SchemaPrinter(options).print(graphQLSchema);
  }
  
  public boolean isSchemaSorted(String schemaContent, String sortedContent) {
    return schemaContent.equals(sortedContent);
  }

  public void createBackupFile() {
    if (!createBackupFile) {
      return;
    }
    if (backupFileExtension.trim().length() == 0) {
      throw new FailureException("Could not create backup file, extension name was empty");
    }
    fileUtil.backupFile();
    log.info(
        String.format(
            "Saved backup of %s to %s%s",
            schemaFile.getAbsolutePath(), schemaFile.getAbsolutePath(), backupFileExtension));
  }

  public void saveSortedContent(String sortedContent) {
    fileUtil.saveSchema(sortedContent);
  }
}
