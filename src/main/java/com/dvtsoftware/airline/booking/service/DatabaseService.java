package com.dvtsoftware.airline.booking.service;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.jdbcclient.JDBCConnectOptions;
import io.vertx.jdbcclient.JDBCPool;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.PoolOptions;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

public class DatabaseService {

  private final Vertx vertx;
  private io.vertx.sqlclient.Pool pool;

  public DatabaseService(Vertx vertx) {
    this.vertx = vertx;
  }

  public Pool getPool() {
    return pool;
  }

  public Future<Void> initialize() {
    // JDBC connection options
    JDBCConnectOptions connectOptions = new JDBCConnectOptions()
            .setJdbcUrl("jdbc:h2:mem:airline;DB_CLOSE_DELAY=-1")
            .setUser("sa")
            .setPassword("");

    PoolOptions poolOptions = new PoolOptions().setMaxSize(16);

    // Create a JDBC pool
    pool = io.vertx.jdbcclient.JDBCPool.pool(vertx, connectOptions, poolOptions);


    try {
      // Read schema and initial data
      String schema = Files.readString(Paths.get("src/main/resources/schema.sql"));
      String data = Files.readString(Paths.get("src/main/resources/data.sql"));

      String[] stmts = Arrays.stream((schema + "\n" + data).split(";"))
              .map(String::trim)
              .filter(s -> !s.isEmpty())
              .toArray(String[]::new);

      Future<Void> future = Future.succeededFuture();
      for (String sql : stmts) {
        String statement = sql;
        future = future.compose(v -> pool.query(statement).execute().mapEmpty());
      }

      return future;
    } catch (Exception e) {
      return Future.failedFuture(e);
    }
  }
}
