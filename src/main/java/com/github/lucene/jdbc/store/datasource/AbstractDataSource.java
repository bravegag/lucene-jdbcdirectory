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

package com.github.lucene.jdbc.store.datasource;

import java.io.PrintWriter;
import java.sql.SQLException;

import javax.sql.DataSource;

/**
 * Abstract base class for Spring's DataSource implementations, taking care of the "uninteresting" glue.
 *
 * @author kimchy
 */
public abstract class AbstractDataSource implements DataSource {

    /**
     * Returns 0: means use default system timeout.
     */
    @Override
    public int getLoginTimeout() throws SQLException {
        return 0;
    }

    @Override
    public void setLoginTimeout(final int timeout) throws SQLException {
        throw new UnsupportedOperationException("setLoginTimeout");
    }

    /**
     * LogWriter methods are unsupported.
     */
    @Override
    public PrintWriter getLogWriter() {
        throw new UnsupportedOperationException("getLogWriter");
    }

    /**
     * LogWriter methods are unsupported.
     */
    @Override
    public void setLogWriter(final PrintWriter pw) throws SQLException {
        throw new UnsupportedOperationException("setLogWriter");
    }

    // ---------------------------------------------------------------------
    // Implementation of JDBC 4.0's Wrapper interface
    // ---------------------------------------------------------------------

    @SuppressWarnings("unchecked")
    @Override
    public <T> T unwrap(final java.lang.Class<T> iface) throws SQLException {
        if (!DataSource.class.equals(iface)) {
            throw new SQLException("DataSource of type [" + getClass().getName()
                    + "] can only be unwrapped as [javax.sql.DataSource], not as [" + iface.getName());
        }
        return (T) this;
    }

    @Override
    public boolean isWrapperFor(final java.lang.Class<?> iface) throws SQLException {
        return DataSource.class.equals(iface);
    }
}
