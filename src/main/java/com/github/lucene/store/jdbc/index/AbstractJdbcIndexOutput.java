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

package com.github.lucene.store.jdbc.index;

import java.io.IOException;
import java.io.InputStream;
import java.sql.PreparedStatement;

import com.github.lucene.store.jdbc.JdbcDirectory;
import com.github.lucene.store.jdbc.JdbcFileEntrySettings;
import com.github.lucene.store.jdbc.support.InputStreamBlob;
import com.github.lucene.store.jdbc.support.JdbcTemplate;

/**
 * @author kimchy
 */
public abstract class AbstractJdbcIndexOutput extends JdbcBufferedIndexOutput {

    protected String name;

    protected JdbcDirectory jdbcDirectory;

    protected AbstractJdbcIndexOutput(final String resourceDescription, final String name) {
        super(resourceDescription, name);
    }

    @Override
    public void configure(final String name, final JdbcDirectory jdbcDirectory, final JdbcFileEntrySettings settings)
            throws IOException {
        super.configure(name, jdbcDirectory, settings);
        this.name = name;
        this.jdbcDirectory = jdbcDirectory;
    }

    @Override
    public void close() throws IOException {
        super.close();
        final long length = length();
        doBeforeClose();
        jdbcDirectory.getJdbcTemplate().executeUpdate(jdbcDirectory.getTable().sqlInsert(),
                new JdbcTemplate.PrepateStatementAwareCallback() {
                    @Override
                    public void fillPrepareStatement(final PreparedStatement ps) throws Exception {
                        ps.setFetchSize(1);
                        ps.setString(1, name);
                        final InputStream is = openInputStream();
                        if (jdbcDirectory.getDialect().useInputStreamToInsertBlob()) {
                            ps.setBinaryStream(2, is, (int) length());
                        } else {
                            ps.setBlob(2, new InputStreamBlob(is, length));
                        }
                        ps.setLong(3, length);
                        ps.setBoolean(4, false);
                    }
                });
        doAfterClose();
    }

    protected abstract InputStream openInputStream() throws IOException;

    protected void doAfterClose() throws IOException {

    }

    protected void doBeforeClose() throws IOException {

    }
}
