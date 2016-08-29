package org.saiku.database;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import javax.servlet.ServletContext;

import org.saiku.service.datasource.IDatasourceManager;
import org.saiku.service.importer.LegacyImporter;
import org.saiku.service.importer.LegacyImporterImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;



public class Database2 {

    @Autowired
    ServletContext servletContext;

    //由DataSource改为MysqlDataSource
    private MysqlDataSource ds;
    private static final Logger log = LoggerFactory.getLogger(Database2.class);
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private IDatasourceManager dsm;
    
    
    public Database2() {

    }

    public void setDatasourceManager(IDatasourceManager dsm) {
        this.dsm = dsm;
    }

    public ServletContext getServletContext() {
        return servletContext;
    }

    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    public void init() throws SQLException {
        initDB();
        loadUsers();
        //笔者将这些日后没用的都注释了
//        loadFoodmart();
//        loadEarthquakes();
//        loadLegacyDatasources();
    }

    private void initDB() {
    	//获取web.xml中的mysql配置信息
    	String url = servletContext.getInitParameter("db.url");
        String user = servletContext.getInitParameter("db.user");
        String pword = servletContext.getInitParameter("db.password");
    	ds = new MysqlDataSource();
        ds.setUrl(url);
        ds.setUser(user);
        ds.setPassword(pword);	
    	
    }

    private void loadFoodmart() throws SQLException {

   }

    private void loadEarthquakes() throws SQLException {

   }

    private static String readFile(String path, Charset encoding)
            throws IOException
    {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, encoding);
    }
    
    private void loadUsers() throws SQLException {

        Connection c = ds.getConnection();

        Statement statement = c.createStatement();

        statement.execute(" CREATE TABLE IF NOT EXISTS log ( time  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, log  TEXT); ");
        statement.execute(" CREATE TABLE IF NOT EXISTS users(user_id INT(11) NOT NULL AUTO_INCREMENT, " + " username VARCHAR(45) NOT NULL UNIQUE, password VARCHAR(100) NOT NULL, email VARCHAR(100), " + " enabled TINYINT NOT NULL DEFAULT 1, PRIMARY KEY(user_id)); ");
        statement.execute(" CREATE TABLE IF NOT EXISTS user_roles ( " + " user_role_id INT(11) NOT NULL AUTO_INCREMENT,username VARCHAR(45), "  + " user_id INT(11) NOT NULL REFERENCES users(user_id), " + " ROLE VARCHAR(45) NOT NULL, " + " PRIMARY KEY (user_role_id)); ");

        ResultSet result = statement.executeQuery("select count(*) as c from log where log = 'insert users'");

        result.next();

        if (result.getInt("c") == 0) {

            statement.execute("INSERT INTO users (username,password,email, enabled) VALUES ('admin','admin', 'test@admin.com',TRUE);");
            statement.execute("INSERT INTO users (username,password,enabled) VALUES ('smith','smith', TRUE);");
            statement.execute("INSERT INTO user_roles (user_id, username, ROLE) VALUES (1, 'admin', 'ROLE_USER');");
            statement.execute("INSERT INTO user_roles (user_id, username, ROLE) VALUES (1, 'admin', 'ROLE_ADMIN');");
            statement.execute("INSERT INTO user_roles (user_id, username, ROLE) VALUES (2, 'smith', 'ROLE_USER');");
            statement.execute("INSERT INTO log (log) VALUES('insert users');");
        }

        String encrypt = servletContext.getInitParameter("db.encryptpassword");
        if (encrypt.equals("true") && !checkUpdatedEncyption()) {
            updateForEncyption();
        }
    }


    public boolean checkUpdatedEncyption() throws SQLException{
        Connection c = ds.getConnection();
        Statement statement = c.createStatement();
        ResultSet result = statement.executeQuery("select count(*) as c from log where log = 'update passwords'");
        result.next();
        return result.getInt("c") != 0;
    }

    public void updateForEncyption() throws SQLException {

        Connection c = ds.getConnection();
        Statement statement = c.createStatement();
        statement.execute("ALTER TABLE users MODIFY COLUMN PASSWORD VARCHAR(100) DEFAULT NULL");
        ResultSet result = statement.executeQuery("select username, password from users");
        while (result.next()) {
            statement = c.createStatement();
            String pword = result.getString("password");
            String hashedPassword = passwordEncoder.encode(pword);
            String sql = "UPDATE users " + "SET password = '" + hashedPassword
                    + "' WHERE username = '" + result.getString("username")
                    + "'";
            statement.executeUpdate(sql);
        }
        statement = c.createStatement();
        statement.execute("INSERT INTO log (log) VALUES('update passwords');");
    }
    
    
    private void loadLegacyDatasources() throws SQLException {
        Connection c = ds.getConnection();

        Statement statement = c.createStatement();
        ResultSet result = statement.executeQuery("select count(*) as c from LOG where log = 'insert datasources'");

        result.next();
        if (result.getInt("c") == 0) {
            LegacyImporter l = new LegacyImporterImpl(dsm);
            l.importSchema();
            l.importDatasources();
            statement.execute("INSERT INTO LOG(log) VALUES('insert datasources');");

        }
    }


    public List<String> getUsers() throws java.sql.SQLException
    {
        //Stub for EE.
        return null;
    }

    public void addUsers(List<String> l) throws java.sql.SQLException
    {
        //Stub for EE.
    }
}
