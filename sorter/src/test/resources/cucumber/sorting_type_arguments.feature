Feature: Sorting type arguments in different ways
  
  Scenario: Type arguments are sorted by default
    When schema content
"""
type Query {
  thing(b: String, c: String, a: String): String
}
"""
    Then sorted schema
"""
type Query {
  thing(a: String, b: String, c: String): String
}

"""
    
  Scenario: Type arguments should not be sorted
    Given skip field argument sorting is true
    When schema content
"""
type Query {
  thing(b: String, c: String, a: String): String
}
"""
    Then sorted schema
"""
type Query {
  thing(b: String, c: String, a: String): String
}

"""
