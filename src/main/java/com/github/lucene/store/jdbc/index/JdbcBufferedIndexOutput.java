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

import com.github.lucene.store.jdbc.JdbcDirectory;
import com.github.lucene.store.jdbc.JdbcFileEntrySettings;

/**
 * A simple base class that performs index output memory based buffering. The
 * buffer size can be configured under the {@link #BUFFER_SIZE_SETTING} name.
 *
 * @author kimchy
 */
public abstract class JdbcBufferedIndexOutput extends ConfigurableBufferedIndexOutput implements JdbcIndexConfigurable {

    /**
     * The buffer size setting name. See
     * {@link JdbcFileEntrySettings#setIntSetting(String,int)}. Should be set in
     * bytes.
     */
    public static final String BUFFER_SIZE_SETTING = "indexOutput.bufferSize";

    protected JdbcBufferedIndexOutput(final String resourceDescription, final String name) {
        super(resourceDescription, name);
    }

    @Override
    public void configure(final String name, final JdbcDirectory jdbcDirectory, final JdbcFileEntrySettings settings)
            throws IOException {
        initBuffer(settings.getSettingAsInt(BUFFER_SIZE_SETTING, DEFAULT_BUFFER_SIZE));
    }
}
