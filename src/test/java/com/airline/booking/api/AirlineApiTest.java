package com.airline.booking.api;

import java.util.List;
import com.airline.booking.handler.AirlineHandler;
import com.airline.booking.service.DatabaseService;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RequestBody;
import io.vertx.ext.web.RoutingContext;
import io.vertx.jdbcclient.JDBCPool;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import io.vertx.sqlclient.PreparedQuery;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.Tuple;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(VertxExtension.class)
class AirlineApiTest {

    private DatabaseService dbService;
    private JDBCPool pool;
    private PreparedQuery<RowSet<Row>> preparedQuery;
    private RowSet<Row> rowSet;
    private RoutingContext rc;
    private HttpServerResponse response;
    private RequestBody requestBody; // Needed for Vert.x 5.x
    private AirlineHandler handler;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setup() {
        dbService = mock(DatabaseService.class);
        pool = mock(JDBCPool.class);
        preparedQuery = mock(PreparedQuery.class);
        rowSet = mock(RowSet.class);
        rc = mock(RoutingContext.class);
        response = mock(HttpServerResponse.class);
        requestBody = mock(RequestBody.class); // Mock the body container

        handler = new AirlineHandler(dbService);

        when(dbService.getPool()).thenReturn(pool);
        when(rc.response()).thenReturn(response);
        when(response.putHeader(anyString(), anyString())).thenReturn(response);
        when(response.setStatusCode(anyInt())).thenReturn(response);

        // Link rc.body() to our mockRequestBody
        when(rc.body()).thenReturn(requestBody);
    }
    @AfterEach
    void resetMocks() {
        Mockito.reset(pool, preparedQuery, rowSet, rc, response);
    }

    @Test
    void testAddAirlineSuccess() {
        JsonObject body = new JsonObject().put("name", "SkyHigh").put("code", "SHA");
        when(requestBody.asJsonObject()).thenReturn(body);

        when(pool.preparedQuery(anyString())).thenReturn(preparedQuery);
        when(preparedQuery.execute(any(Tuple.class))).thenReturn(Future.succeededFuture(rowSet));

        Row mockGeneratedKeys = mock(Row.class);
        when(mockGeneratedKeys.getLong(0)).thenReturn(101L);
        doReturn(mockGeneratedKeys).when(rowSet).property(any());

        handler.addAirline(rc);

        verify(response).setStatusCode(201);
    }

    @Test
    void testListAllAirlines(VertxTestContext ctx) {
        Row row = mock(Row.class);
        when(row.getLong("ID")).thenReturn(1L);
        when(row.getString("NAME")).thenReturn("SkyHigh");
        when(row.getString("CODE")).thenReturn("SHA");
        when(row.getString("COUNTRY")).thenReturn("USA");
        when(pool.query(anyString())).thenReturn(preparedQuery);
        when(preparedQuery.execute()).thenReturn(Future.succeededFuture(rowSet));
        doReturn(List.of(row).spliterator()).when(rowSet).spliterator();
        doAnswer(invocation -> {
            ctx.completeNow();
            return null;
        }).when(response).end(anyString());
        doAnswer(invocation -> {
            ctx.failNow(new RuntimeException("Handler failed: " + invocation.getArgument(0)));
            return null;
        }).when(rc).fail(any());

        handler.listAllAirlines(rc);
    }

    @Test
    void testSearchAirlinesSuccess(VertxTestContext ctx) {
        // 1. Mock the query parameters
        when(rc.request()).thenReturn(mock(io.vertx.core.http.HttpServerRequest.class));
        when(rc.request().getParam("name")).thenReturn("Sky");
        when(rc.request().getParam("code")).thenReturn("SHA");

        // 2. Setup Mock Row with UPPERCASE keys (to match Airline.fromRow)
        Row row = mock(Row.class);
        when(row.getLong("ID")).thenReturn(1L);
        when(row.getString("NAME")).thenReturn("SkyHigh");
        when(row.getString("CODE")).thenReturn("SHA");
        when(row.getString("COUNTRY")).thenReturn("USA");

        // 3. Setup Database Mocks
        when(pool.preparedQuery(anyString())).thenReturn(preparedQuery);
        when(preparedQuery.execute(any(Tuple.class))).thenReturn(Future.succeededFuture(rowSet));
        doReturn(List.of(row).spliterator()).when(rowSet).spliterator();

        // 4. Handle success and verify the body
        doAnswer(invocation -> {
            String body = invocation.getArgument(0);
            ctx.verify(() -> {
                assert(body.contains("SkyHigh"));
                assert(body.contains("SHA"));
            });
            ctx.completeNow();
            return null;
        }).when(response).end(anyString());

        // 5. Handle potential failure
        doAnswer(invocation -> {
            ctx.failNow(new RuntimeException("Search failed with: " + invocation.getArgument(0)));
            return null;
        }).when(rc).fail(any());

        // 6. Execute
        handler.searchAirlines(rc);
    }


}