package sortgraphql;

import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.UnExecutableSchemaGenerator;

/** Contain the concrete methods to sort the schema */
public class SorterService {
  public String sortSchema(String schema) {
    var typeDefinitionRegistry = new SchemaParser().parse(schema);
    var graphQLSchema = UnExecutableSchemaGenerator.makeUnExecutableSchema(typeDefinitionRegistry);

    var options =
        SchemaPrinter.Options.defaultOptions()
            .descriptionsAsHashComments(true)
            .includeDirectiveDefinitions(false)
            .includeDefinedDirectiveDefinitions(true);

    return new SchemaPrinter(options).print(graphQLSchema);
  }
}
