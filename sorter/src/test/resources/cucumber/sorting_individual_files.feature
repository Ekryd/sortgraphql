Feature: Sorting GraphQL Schema files

  Scenario: sorting multiple files
    Given sort individual schemas is true
    Given schema files
      | schema1_account.graphqls |
      | schema1_inventory.graphqls |
      | schema1_product.graphqls |
      | schema1_review.graphqls |
    When sorting
    Then schema file "schema1_account.graphqls" will be "schema1_account_expected.graphqls"
    Then schema file "schema1_inventory.graphqls" will be "schema1_inventory_expected.graphqls"
    Then schema file "schema1_product.graphqls" will be "schema1_product_expected.graphqls"
    Then schema file "schema1_review.graphqls" will be "schema1_review_expected.graphqls"
    
