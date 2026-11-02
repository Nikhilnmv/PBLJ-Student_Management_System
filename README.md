# Student Registration System

A web-based student registration and management system built with **Java Servlets, JSP, JDBC and MySQL**.
Academic minor project for *Project Based Learning Using Java (PBLJ)*.

> Work in progress. Full setup instructions will be added as the project is built.

## Technology stack

| Component | Technology |
|---|---|
| Language | Java 17 |
| Web layer | Java Servlets 4.0 (`javax.servlet`) + JSP + JSTL |
| Frontend | HTML, CSS, JavaScript, Bootstrap 5 |
| Database | MySQL 8 via JDBC (MySQL Connector/J) |
| Server | Apache Tomcat 9 |
| Build | Maven (WAR packaging) |
| Architecture | MVC + DAO |

## Quick build

```bash
mvn clean package
```

This produces `target/student-registration.war`, which can be deployed to Apache Tomcat 9.
