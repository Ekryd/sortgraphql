# sortgraphql

[![Build Status](https://circleci.com/gh/Ekryd/sortgraphql.svg?style=svg)](https://app.circleci.com/pipelines/github/Ekryd/sortgraphql)
[![Coverage Status](https://coveralls.io/repos/github/Ekryd/sortgraphql/badge.svg?branch=master)](https://coveralls.io/github/Ekryd/sortgraphql?branch=master)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.ekryd.sortgraphql/sortgraphql-maven-plugin/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.ekryd.sortgraphql/sortgraphql-maven-plugin)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=Ekryd_sortgraphql&metric=alert_status)](https://sonarcloud.io/dashboard?id=Ekryd_sortgraphql)
[![Licence](https://img.shields.io/github/license/Ekryd/sortgraphql?color=success)](https://github.com/Ekryd/sortgraphql/blob/master/LICENSE.md)

Maven plugin to sort GraphQL Schemas.

## Description ##

The plugin will format and sort GraphQL schemas in a predefined way. The default sort order sections are:

* All Directives first
* The Query type
* The Mutation type
* The Subscription type
* All Scalars
* All Interfaces
* All Unions
* All Input types
* All Types (beside Query, Mutation and Subscription)
* All Enums

Within each section, the entities are sorted alphabetically. There are some parameters to suppress the alphabetical
sorting, please submit a pull request or issue if you need more options.

The plugin should be able to handle custom directives and custom scalars. The plugin should also be able to sort
multiple dependent graphql schema files as long as all entities are only defined in one schema.

## Goals Overview ##

The SortGraphQL Plugin has one goal.

* **mvn sortgraphql:sort** sorts the grapql file/files

## Usage ##

Add the plugin to your pom-file to sort the schema each time you build the project. The plugin will execute by default
in the Maven validate phase. Remember to set the `src/main/resources/` path

```xml

<build>
  <plugins>
    ...
    <plugin>
      <groupId>com.github.ekryd.sortgraphql</groupId>
      <artifactId>sortgraphql-maven-plugin</artifactId>
      <version>@pom.version@</version>
      <configuration>
        <schemaFile>src/main/resources/mySchema.graphqls</schemaFile>
      </configuration>
      <executions>
        <execution>
          <goals>
            <goal>sort</goal>
          </goals>
        </execution>
      </executions>
    </plugin>
    ...
  </plugins>
</build>

```

## Parameters ##

| **Parameter** | **Default value** | **Description** | **Example**
|:------------------------|:-----------------------------|:------------------|:----------------|
|`<createBackupFile>`| `true` | Should a backup copy be created for the sorted schema. | `<createBackupFile>false</createBackupFile>` |
|`<backupFileExtension>`| `.bak` | Name of the file extension for the backup file. | `<backupFileExtension>.temp</backupFileExtension>` |
|`<encoding>`| `UTF-8` | Encoding for the files. | `<encoding>ASCII</encoding>` |
|`<schemaFile>`| `src/main/resources/schema.graphqls` | Location of the schema file. Remember to set the `src/main/resources/` path. |`-Dsortgraphql.schemaFile="src/main/resources/main.graphqls"` <br><br> `<schemaFile>src/main/resources/main.graphqls</schemaFile>` |
|`<schemaFiles>`| `<empty>` | Location of multiple graphql schema file that should be sorted. Overrides parameter schemaFile. The schema files can reference each other, but shared definitions are not allowed. | <pre lang="xml">&lt;schemaFiles&gt;<br>  &lt;schemaFile&gt;src/main/resources/queries.graphqls&lt;/schemaFile&gt; <br>  &lt;schemaFile&gt;src/main/resources/mutations.graphqls&lt;/schemaFile&gt;<br>&lt;/schemaFiles&gt;</pre> | 
|`<skip>`| `false` | Set this to 'true' to bypass SortGraphQL plugin. | `-Dsortgraphql.skip=true` <br><br> `<skip>true</skip>` |
|`<skipFieldArgumentSorting>`| `false` | Set this to 'true' to skip sorting the arguments for a field in a type. | `<skipFieldArgumentSorting>true</skipFieldArgumentSorting>` |
|`<skipUnionTypeSorting>`| `false` | Set this to 'true' to skip sorting the types in a union. | `<skipUnionTypeSorting>true</skipUnionTypeSorting>` |

## Download ##
The plugin is hosted i [Maven Central](https://mvnrepository.com/artifact/com.github.ekryd.sortgraphql/sortgraphql-maven-plugin) and will be downloaded automatically if you include it as a plugin in your pom file.

## Version history ##

* 2021-06-01: Released 0.0.2 Fully functional but with limited configuration for sorting. Try it out and tell me what you think
