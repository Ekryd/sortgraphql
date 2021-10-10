Feature: Sorting type arguments in different ways
  
  Scenario: Type arguments are sorted by default
    Given schema content
"""
type Query {
  thing(b: String, c: String, a: String): String
}
"""
    When sorting
    Then schema content will be
"""
type Query {
  thing(a: String, b: String, c: String): String
}

"""
    
  Scenario: Type arguments should not be sorted
    Given skip field argument sorting is true
    Given schema content
"""
type Query {
  thing(b: String, c: String, a: String): String
}
"""
    When sorting
    Then schema content will be
"""
type Query {
  thing(b: String, c: String, a: String): String
}

"""
