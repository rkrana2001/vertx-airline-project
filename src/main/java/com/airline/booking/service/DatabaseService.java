package com.airline.booking.service;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.jdbcclient.JDBCConnectOptions;
import io.vertx.jdbcclient.JDBCPool;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.PoolOptions;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class DatabaseService {

  private final Vertx vertx;
  private final Pool pool;

  // Updated constructor to accept config from MainVerticle
  public DatabaseService(Vertx vertx, JsonObject config) {
    this.vertx = vertx;

    // Use config from application.json with defaults as fallbacks
    String jdbcUrl = config.getString("DB_URL", "jdbc:h2:mem:airline;DB_CLOSE_DELAY=-1");
    String user = config.getString("DB_USER", "sa");
    String password = config.getString("DB_PASSWORD", "");
    int poolSize = config.getInteger("DB_POOL_SIZE", 16);

    JDBCConnectOptions connectOptions = new JDBCConnectOptions()
            .setJdbcUrl(jdbcUrl)
            .setUser(user)
            .setPassword(password);

    PoolOptions poolOptions = new PoolOptions().setMaxSize(poolSize);

    // Create JDBC pool immediately
    this.pool = JDBCPool.pool(vertx, connectOptions, poolOptions);
  }

  public Pool getPool() {
    return pool;
  }

  public Future<Void> initialize() {
    try {
      // Load schema.sql and data.sql from resources
      String schema = readResourceFile("schema.sql");
      String data = readResourceFile("data.sql");

      String[] statements = Arrays.stream((schema + "\n" + data).split(";"))
              .map(String::trim)
              .filter(s -> !s.isEmpty())
              .toArray(String[]::new);

      Future<Void> future = Future.succeededFuture();
      for (String sql : statements) {
        future = future.compose(v -> pool.query(sql).execute().mapEmpty());
      }

      return future;
    } catch (Exception e) {
      return Future.failedFuture(e);
    }
  }

  private String readResourceFile(String resourceName) throws Exception {
    InputStream in = getClass().getClassLoader().getResourceAsStream(resourceName);
    if (in == null) {
      throw new RuntimeException(resourceName + " not found in classpath");
    }
    return new String(in.readAllBytes(), StandardCharsets.UTF_8);
  }

  public void close() {
    if (pool != null) {
      pool.close();
    }
  }
}
