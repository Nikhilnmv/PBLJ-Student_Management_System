package com.student.listener;

import com.mysql.cj.jdbc.AbandonedConnectionCleanupThread;
import com.student.util.DBConnection;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;

/**
 * Runs code when Tomcat starts and stops this web application.
 *
 * On start-up  : tries to open a database connection and writes the result to
 *                the Tomcat log, so a wrong password or a stopped MySQL server
 *                is noticed immediately.
 * On shut-down : releases the MySQL driver so Tomcat can unload the
 *                application cleanly (avoids memory-leak warnings).
 *
 * It implements the ServletContextListener INTERFACE: Tomcat calls the two
 * methods below at the right moments.
 */
@WebListener
public class AppContextListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent event) {
        ServletContext context = event.getServletContext();
        try (Connection connection = DBConnection.getConnection()) {
            context.log("Student Registration System: connected to MySQL ("
                    + connection.getMetaData().getDatabaseProductVersion() + ") at "
                    + DBConnection.getConfiguredUrl());
        } catch (SQLException e) {
            // The application still starts; pages that need the database will
            // show a friendly error until MySQL is available.
            context.log("Student Registration System: WARNING - cannot connect to MySQL: "
                    + e.getMessage());
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent event) {
        // Deregister JDBC drivers that were loaded by this web application.
        ClassLoader appClassLoader = Thread.currentThread().getContextClassLoader();
        Enumeration<Driver> drivers = DriverManager.getDrivers();
        while (drivers.hasMoreElements()) {
            Driver driver = drivers.nextElement();
            if (driver.getClass().getClassLoader() == appClassLoader) {
                try {
                    DriverManager.deregisterDriver(driver);
                } catch (SQLException e) {
                    event.getServletContext().log("Could not deregister JDBC driver: " + e.getMessage());
                }
            }
        }
        // Stop the background thread that MySQL Connector/J starts.
        AbandonedConnectionCleanupThread.checkedShutdown();
    }
}
