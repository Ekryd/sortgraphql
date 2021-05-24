Feature: Sorting GraphQL Schemas
  
  Scenario: basic query schema files
    Given schema content file "basic_products.graphqls"
    Then sorted schema file "basic_products_expected.graphqls"
