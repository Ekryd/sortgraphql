Feature: Sorting annotations in different ways
  
  Scenario: Default arguments should not be printed
    Given schema content
"""
directive @something(name: String = "a default name", title: String) on FIELD_DEFINITION

type Query {
  thing1: String @something
  thing2: String @something(title: "Preacher")
  thing3: String @something(name: "Jessie")
  thing4: String @something(name: "a default name")
}
"""
    When sorting
    Then schema content will be
"""
directive @something(name: String = "a default name", title: String) on FIELD_DEFINITION

type Query {
  thing1: String @something
  thing2: String @something(title: "Preacher")
  thing3: String @something(name: "Jessie")
  thing4: String @something
}

"""
