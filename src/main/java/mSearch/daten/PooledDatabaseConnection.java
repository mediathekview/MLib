package mSearch.daten;

import com.jidesoft.utils.SystemInfo;
import org.apache.commons.dbcp2.*;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPool;

import javax.sql.DataSource;
import java.io.Closeable;
import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;

public class PooledDatabaseConnection implements Closeable {
    private static PooledDatabaseConnection INSTANCE;
    private final DataSource dataSource;

    private PooledDatabaseConnection() {
        dataSource = setupDataSource();
    }

    public static PooledDatabaseConnection getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new PooledDatabaseConnection();
        }
        return INSTANCE;
    }

    public void close() {
        connectionPool.close();
    }

    public Connection getConnection() {
        Connection con = null;
        try {
            con = dataSource.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return con;
    }

    private ObjectPool<PoolableConnection> connectionPool;

    private DataSource setupDataSource() {
        final String CACHE_PATH;
        if (SystemInfo.isMacOSX()) {
            CACHE_PATH = System.getProperty("user.home") + "/Library/Caches/MediathekView/";
        } else
            CACHE_PATH = System.getProperty("user.home") + File.separatorChar + ".mediathek3" + File.separatorChar;

        try {
            Class.forName("org.hsqldb.jdbc.JDBCDriver");
        } catch (ClassNotFoundException ignored) {
        }
        ConnectionFactory connectionFactory = new DriverManagerConnectionFactory("jdbc:hsqldb:file:" + CACHE_PATH + "cache.db;close_result=true;shutdown=true", null);

        PoolableConnectionFactory poolableConnectionFactory =
                new PoolableConnectionFactory(connectionFactory, null);

        connectionPool = new GenericObjectPool<>(poolableConnectionFactory);

        poolableConnectionFactory.setPool(connectionPool);

        return new PoolingDataSource<>(connectionPool);
    }
}
