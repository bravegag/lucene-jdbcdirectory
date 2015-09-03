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

import org.apache.lucene.store.IndexInput;
import org.apache.lucene.store.IndexOutput;

import com.github.lucene.store.jdbc.index.FetchPerTransactionJdbcIndexInput;
import com.github.lucene.store.jdbc.index.RAMJdbcIndexOutput;

/**
 * @author kimchy
 */
public class FetchPerTransactionInputRAMOutputITest extends AbstractIndexInputOutputITest {

    @Override
    public void setUp() throws Exception {
        super.setUp();
        if (!jdbcDirectory.getDialect().supportTransactionalScopedBlobs()) {
            disable = true;
            System.out.println("Dialect does not support transacitonal blobs, not testing");
        }
    }

    @Override
    protected Class<? extends IndexInput> indexInputClass() {
        return FetchPerTransactionJdbcIndexInput.class;
    }

    @Override
    protected Class<? extends IndexOutput> indexOutputClass() {
        return RAMJdbcIndexOutput.class;
    }
}
