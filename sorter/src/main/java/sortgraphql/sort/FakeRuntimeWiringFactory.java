package sortgraphql.sort;

import graphql.language.Comment;
import graphql.language.ScalarTypeDefinition;
import graphql.schema.Coercing;
import graphql.schema.GraphQLScalarType;
import graphql.schema.idl.EchoingWiringFactory;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.ScalarInfo;
import graphql.schema.idl.TypeDefinitionRegistry;

import java.util.Map;
import java.util.stream.Collectors;

/** */
public class FakeRuntimeWiringFactory {
  private static final Coercing<Object, Object> emptyCoercing =
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
          Map<String, ScalarTypeDefinition> scalars = registry.scalars();
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
    wiringBuilder.scalar(
        GraphQLScalarType.newScalar()
            .name(scalarName)
            .description(getScalarDescription(definition))
            .coercing(emptyCoercing)
            .build());
  }

  private String getScalarDescription(ScalarTypeDefinition definition) {
    if (definition.getDescription() != null && definition.getDescription().getContent() != null) {
      return definition.getDescription().getContent();
    }
    if (!definition.getComments().isEmpty()) {
      return definition.getComments().stream()
          .map(Comment::getContent)
          .collect(Collectors.joining(" "));
    }
    return null;
  }
}
