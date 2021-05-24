package sortgraphql;

import graphql.language.Comment;
import graphql.language.ScalarTypeDefinition;
import graphql.schema.Coercing;
import graphql.schema.GraphQLScalarType;
import graphql.schema.idl.*;
import sortgraphql.exception.FailureException;
import sortgraphql.logger.SortingLogger;
import sortgraphql.parameter.PluginParameters;
import sortgraphql.sort.OptionsBuilder;
import sortgraphql.sort.SchemaPrinter;
import sortgraphql.util.FileUtil;

import java.io.File;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/** Contain the concrete methods to sort the schema */
public class SorterService {
  private final FileUtil fileUtil = new FileUtil();

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
    RuntimeWiring runtimeWiring = createFakeRuntime(registry);

    var graphQLSchema = new SchemaGenerator().makeExecutableSchema(registry, runtimeWiring);

    var options =
        OptionsBuilder.defaultOptions()
            .setDescriptionsAsHashComments(true)
            .setIncludeDirectiveDefinitions(false)
            .setIncludeDefinedDirectiveDefinitions(true)
            .build();

    return new SchemaPrinter(options).print(graphQLSchema);
  }

  private RuntimeWiring createFakeRuntime(TypeDefinitionRegistry registry) {
    var coercing =
        new Coercing<>() {
          @Override
          public Object serialize(Object dataFetcherResult) {
            return dataFetcherResult;
          }

          @Override
          public Object parseValue(Object input) {
            return input;
          }

          @Override
          public Object parseLiteral(Object input) {
            return input;
          }
        };

    return EchoingWiringFactory.newEchoingWiring(
        new Consumer<>() {
          @Override
          public void accept(RuntimeWiring.Builder wiring) {
            Map<String, ScalarTypeDefinition> scalars = registry.scalars();
            scalars.forEach(
                (name, v) -> {
                  if (!ScalarInfo.isGraphqlSpecifiedScalar(name)) {
                    wiring.scalar(
                        GraphQLScalarType.newScalar()
                            .name(name)
                            .description(getDescription(v))
                            .coercing(coercing)
                            .build());
                  }
                });
          }

          private String getDescription(ScalarTypeDefinition definition) {
            if (definition.getDescription() != null
                && definition.getDescription().getContent() != null) {
              return definition.getDescription().getContent();
            }
            if (!definition.getComments().isEmpty()) {
              return definition.getComments().stream()
                  .map(Comment::getContent)
                  .collect(Collectors.joining(" "));
            }
            return null;
          }
        });
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
