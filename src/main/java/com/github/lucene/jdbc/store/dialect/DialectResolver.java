/*
 * Copyright 2004-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.lucene.jdbc.store.dialect;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.LinkedList;

import javax.sql.DataSource;

import com.github.lucene.jdbc.store.JdbcStoreException;
import com.github.lucene.jdbc.store.datasource.DataSourceUtils;

/**
 * @author kimchy
 */
public class DialectResolver {

    public static interface DatabaseMetaDataToDialectMapper {

        Class<? extends Dialect> getDialect(DatabaseMetaData metaData) throws SQLException;
    }

    public static class DatabaseNameToDialectMapper implements DatabaseMetaDataToDialectMapper {

        private final String databaseName;

        private final Class<? extends Dialect> dialect;

        public DatabaseNameToDialectMapper(final String databaseName, final Class<? extends Dialect> dialect) {
            this.databaseName = databaseName;
            this.dialect = dialect;
        }

        @Override
        public Class<? extends Dialect> getDialect(final DatabaseMetaData metaData) throws SQLException {
            if (metaData.getDatabaseProductName().equals(databaseName)) {
                return dialect;
            }
            return null;
        }
    }

    public static class DatabaseNameStartsWithToDialectMapper implements DatabaseMetaDataToDialectMapper {

        private final String databaseName;

        private final Class<? extends Dialect> dialect;

        public DatabaseNameStartsWithToDialectMapper(final String databaseName, final Class<? extends Dialect> dialect) {
            this.databaseName = databaseName;
            this.dialect = dialect;
        }

        @Override
        public Class<? extends Dialect> getDialect(final DatabaseMetaData metaData) throws SQLException {
            if (metaData.getDatabaseProductName().startsWith(databaseName)) {
                return dialect;
            }
            return null;
        }
    }

    public static class DatabaseNameAndVersionToDialectMapper implements DatabaseMetaDataToDialectMapper {

        private final String databaseName;

        private final Class<? extends Dialect> dialect;

        private final int version;

        public DatabaseNameAndVersionToDialectMapper(final String databaseName, final int version,
                final Class<? extends Dialect> dialect) {
            this.databaseName = databaseName;
            this.dialect = dialect;
            this.version = version;
        }

        @Override
        public Class<? extends Dialect> getDialect(final DatabaseMetaData metaData) throws SQLException {
            if (metaData.getDatabaseProductName().equals(databaseName) && metaData.getDatabaseMajorVersion() == version) {
                return dialect;
            }
            return null;
        }
    }

    private final LinkedList<DatabaseMetaDataToDialectMapper> mappers = new LinkedList<DatabaseMetaDataToDialectMapper>();

    public DialectResolver() {
        this(true);
    }

    public DialectResolver(final boolean useDefaultMappers) {
        if (!useDefaultMappers) {
            return;
        }
        mappers.add(new DatabaseNameToDialectMapper("HSQL Database Engine", HSQLDialect.class));
        mappers.add(new DatabaseNameToDialectMapper("DB2/NT", DB2Dialect.class));
        mappers.add(new DatabaseNameToDialectMapper("DB2/LINUX", DB2Dialect.class));
        mappers.add(new DatabaseNameToDialectMapper("DB2/LINUXX8664", DB2Dialect.class));
        mappers.add(new DatabaseNameToDialectMapper("MySQL", MySQLDialect.class));
        mappers.add(new DatabaseNameToDialectMapper("PostgreSQL", PostgreSQLDialect.class));
        mappers.add(new DatabaseNameStartsWithToDialectMapper("Microsoft SQL Server", SQLServerDialect.class));
        mappers.add(new DatabaseNameToDialectMapper("Sybase SQL Server", SybaseDialect.class));
        mappers.add(new DatabaseNameAndVersionToDialectMapper("Oracle", 8, Oracle8Dialect.class));
        mappers.add(new DatabaseNameAndVersionToDialectMapper("Oracle", 9, Oracle9Dialect.class));
        mappers.add(new DatabaseNameToDialectMapper("Oracle", OracleDialect.class));
    }

    public void addFirstMapper(final DatabaseMetaDataToDialectMapper mapper) {
        mappers.addFirst(mapper);
    }

    public void addLastMapper(final DatabaseMetaDataToDialectMapper mapper) {
        mappers.addLast(mapper);
    }

    public Dialect getDialect(final DataSource dataSource) throws JdbcStoreException {
        final Connection conn = DataSourceUtils.getConnection(dataSource);
        String databaseName;
        int databaseMajorVersion;
        int databaseMinorVersion;
        String driverName;
        try {
            final DatabaseMetaData metaData = conn.getMetaData();
            databaseName = metaData.getDatabaseProductName();
            databaseMajorVersion = metaData.getDatabaseMajorVersion();
            databaseMinorVersion = metaData.getDatabaseMinorVersion();
            driverName = metaData.getDriverName();
            for (final DatabaseMetaDataToDialectMapper mapper : mappers) {
                final Class<? extends Dialect> dialectClass = mapper.getDialect(metaData);
                if (dialectClass == null) {
                    continue;
                }
                return dialectClass.newInstance();
            }
        } catch (final Exception e) {
            throw new JdbcStoreException("Failed to auto detect dialect", e);
        } finally {
            DataSourceUtils.releaseConnection(conn);
        }
        throw new JdbcStoreException("Failed to auto detect dialect, no match found for database [" + databaseName
                + "] version [" + databaseMajorVersion + "/" + databaseMinorVersion + "] driver [" + driverName + "]");
    }
}
