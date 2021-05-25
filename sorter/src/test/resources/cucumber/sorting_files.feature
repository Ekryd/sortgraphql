Feature: Sorting GraphQL Schemas
  
  Scenario: basic query schema files
    Given schema content file "basic_products.graphqls"
    Then sorted schema file "basic_products_expected.graphqls"

  Scenario: base federation specification
    Given schema content file "federation.graphqls"
    Then sorted schema file "federation_expected.graphqls"
