package Server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.sql.*;

public class DatabaseService implements Closeable {
    private static DatabaseService instance;
    private Connection connection;
    private Statement statement;

    private final static String DB_USERNAME = "CLOUD_STORAGE_AUTH";
    private final static String DB_PASSWORD = "CLOUD_STORAGE_AUTH";

    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseService.class);

    private DatabaseService(String jdbc) {
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
            connection = DriverManager.getConnection(jdbc, DB_USERNAME, DB_PASSWORD);
            statement = connection.createStatement();
            LOGGER.info("DB connection created. jdbc = {}", jdbc);
        } catch (ClassNotFoundException | SQLException e) {
            LOGGER.error(null, e);
        }
    }

    public static DatabaseService getInstance() {
        if (instance == null) {
            return null;
        }
        return instance;
    }

    public static boolean initInstance(String jdbc) {
        if (instance == null) {
            instance = new DatabaseService(jdbc); //ВР c username, будет вынесено в БД сервис
            return true;
        }
        return false;
    }

    public void addUser(String login, String pass) {
        String sql = String.format("""
                INSERT INTO USER_DAO (LOGIN,
                PASSWORD,
                CREATE_DATE)
                VALUES ('%s', '%s', sysdate)""", login, pass);
        try {
            statement.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean isLoginExists(String login) throws SQLException {
        String sql = String.format("SELECT 1 FROM USER_DAO WHERE LOGIN = '%s' AND IS_DELETE is null", login);
        ResultSet rs = statement.executeQuery(sql);
        return rs.next();
    }

    public boolean auth(String login, String pass) throws SQLException {
        boolean isAuth = false;
        String sql = String.format("""
                SELECT 'true'
                FROM USER_DAO WHERE LOGIN = '%s'
                AND PASSWORD = '%s'
                AND IS_DELETE IS NULL""", login, pass);
        ResultSet rs = statement.executeQuery(sql);
        while (rs.next()) {
            isAuth = rs.getBoolean("'true'");
        }
        return isAuth;
    }

    public boolean changePassword(String login, String pass) {
        String sql = String.format("""
                UPDATE USER_DAO
                SET PASSWORD = '%s'
                WHERE LOGIN = '%s'
                AND IS_DELETE IS NULL""", pass, login);
        try {
            statement.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean deleteAccount(String login, String pass) {
        String sql = String.format("""
                UPDATE USER_DAO
                SET IS_DELETE = 'Y',
                DEL_DATE = sysdate
                WHERE LOGIN = '%s'
                AND PASSWORD = '%s'
                AND IS_DELETE IS NULL""", login, pass);
        try {
            statement.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public void close() {
        try {
            statement.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
