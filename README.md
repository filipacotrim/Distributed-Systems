# Turmas

Distributed Systems Project 2021/2022

## Authors

**Group A24**

### Code Identification

In all source files (namely in the *groupId*s of the POMs), replace __GXX__ with your group identifier. The group
identifier consists of a G and the group number - always two digits. This change is important for code dependency
management, to ensure your code runs using the correct components and not someone else's.

### Team Members


| Number | Name              | User                                | Email                                          |
|--------|-------------------|-------------------------------------|------------------------------------------------|
| 93230  | Catarina Bento    | <https://github.com/catarinab>      | <mailto:catarina.c.bento@tecnico.ulisboa.pt>   |
| 94179  | Luís D'Andrarde   | <https://github.com/luisvfandrade>  | <mailto:luis.vilhena@tecnico.ulisboa.pt>       |
| 95572  | Filipa Cotrim     | <https://github.com/filipacotrim>   | <mailto:filipa.cotrim@tecnico.ulisboa.pt>      |

## Getting Started

The overall system is made up of several modules. The main server is the _ClassServer_. The clients are the _Student_,
the _Professor_ and the _Admin_. The definition of messages and services is in the _Contract_. The future naming server
is the _NamingServer_.

See the [Project Statement](https://github.com/tecnico-distsys/Turmas) or a complete domain and system description.

### Prerequisites

The Project is configured with Java 11, but if you want to use Java 17 (which is only compatible with Maven >= 3.8) you
can too, just upgrade the version in the POMs.

To confirm that you have them installed and which versions they are, run in the terminal:

```s
javac -version
mvn -version
```

### Installation

To compile and install all modules:

```s
mvn clean install
```

### Execution
To run the Naming Server from the project root:
```s
cd NamingServer
mvn compile exec:java -Dexec.args="address 5000 -debug"
```
- The debug flag is optional.

To run the Server from the project root:
```s
cd ClassServer
mvn compile exec:java -Dexec.args="address port P|S -debug"
```
- The debug flag is optional.

To run the Admin Client from the project root:
```s
cd Admin
mvn compile exec:java
```

To run the Professor Client from the project root:
```s
cd Professor
mvn compile exec:java
```

To run the Student Client from the project root:
```s
cd Student
mvn compile exec:java -Dexec.args="alunoXXXX StudentName"
```
- Where XXXX is your student number.

## Built With

* [Maven](https://maven.apache.org/) - Build and dependency management tool;
* [gRPC](https://grpc.io/) - RPC framework.
