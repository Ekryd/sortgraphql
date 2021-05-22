package sortgraphql;

import graphql.schema.GraphQLSchema;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import graphql.schema.idl.UnExecutableSchemaGenerator;

import java.io.IOException;
import java.io.InputStream;

import static sortgraphql.SchemaPrinter.Options;

public class Main {
    public static void main(String[] args) {

        InputStream resourceAsStream = Main.class.getClassLoader().getResourceAsStream("basic_products.graphql");
        try (resourceAsStream) {
            TypeDefinitionRegistry typeDefinitionRegistry = new SchemaParser().parse(resourceAsStream);

            GraphQLSchema graphQLSchema = UnExecutableSchemaGenerator.makeUnExecutableSchema(typeDefinitionRegistry);
            Options options = Options.defaultOptions();
            options = options.descriptionsAsHashComments(true)
                    .includeDirectiveDefinitions(false)
                    .includeDefinedDirectiveDefinitions(true);

            String st = new SchemaPrinter(options).print(graphQLSchema);
            System.out.println(st);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
