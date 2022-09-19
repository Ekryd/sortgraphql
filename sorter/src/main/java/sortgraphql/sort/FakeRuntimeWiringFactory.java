package sortgraphql.sort;

import static java.util.Optional.ofNullable;

import graphql.language.Description;
import graphql.language.ScalarTypeDefinition;
import graphql.schema.Coercing;
import graphql.schema.GraphQLScalarType;
import graphql.schema.idl.EchoingWiringFactory;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.ScalarInfo;
import graphql.schema.idl.TypeDefinitionRegistry;

/** */
public class FakeRuntimeWiringFactory {
  static final Coercing<Object, Object> emptyCoercing =
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

  public RuntimeWiring createFakeRuntime(TypeDefinitionRegistry registry) {
    return EchoingWiringFactory.newEchoingWiring(
        wiringBuilder -> {
          var scalars = registry.scalars();
          scalars.forEach(
              (name, v) -> {
                if (!ScalarInfo.isGraphqlSpecifiedScalar(name)) {
                  addScalarToWiring(wiringBuilder, name, v);
                }
              });
        });
  }

  private void addScalarToWiring(
      RuntimeWiring.Builder wiringBuilder, String scalarName, ScalarTypeDefinition definition) {
    var scalarDescription =
        ofNullable(definition)
            .map(ScalarTypeDefinition::getDescription)
            .map(Description::getContent)
            .orElse(null);

    wiringBuilder.scalar(
        GraphQLScalarType.newScalar()
            .name(scalarName)
            .description(scalarDescription)
            .coercing(emptyCoercing)
            .build());
  }
}
