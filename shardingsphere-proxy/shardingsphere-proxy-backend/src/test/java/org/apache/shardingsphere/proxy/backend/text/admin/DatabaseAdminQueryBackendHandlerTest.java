/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.proxy.backend.text.admin;

import lombok.SneakyThrows;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.infra.executor.kernel.ExecutorEngine;
import org.apache.shardingsphere.infra.federation.optimizer.context.OptimizerContext;
import org.apache.shardingsphere.infra.instance.InstanceContext;
import org.apache.shardingsphere.infra.merge.result.MergedResult;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistService;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.response.header.query.QueryResponseHeader;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.backend.text.admin.postgresql.executor.SelectTableExecutor;
import org.apache.shardingsphere.sharding.merge.dal.common.SingleLocalDataMergedResult;
import org.apache.shardingsphere.transaction.context.TransactionContexts;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class DatabaseAdminQueryBackendHandlerTest {
    
    @Mock
    private ConnectionSession connectionSession;
    
    private DatabaseAdminQueryBackendHandler handler;
    
    @SneakyThrows
    @Before
    public void before() {
        MetaDataContexts metaDataContexts = new MetaDataContexts(mock(MetaDataPersistService.class), getMetaDataMap(),
                mock(ShardingSphereRuleMetaData.class), mock(ExecutorEngine.class), mock(OptimizerContext.class), new ConfigurationProperties(new Properties()));
        ContextManager contextManager = new ContextManager(metaDataContexts, mock(TransactionContexts.class), mock(InstanceContext.class));
        ProxyContext.init(contextManager);
        when(connectionSession.getDatabaseName()).thenReturn("db");
        SelectTableExecutor executor = mock(SelectTableExecutor.class, RETURNS_DEEP_STUBS);
        MergedResult mergedResult = new SingleLocalDataMergedResult(Arrays.asList("demo_ds_0", "demo_ds_1"));
        when(executor.getMergedResult()).thenReturn(mergedResult);
        when(executor.getQueryResultMetaData().getColumnCount()).thenReturn(1);
        handler = new DatabaseAdminQueryBackendHandler(connectionSession, executor);
    }
    
    private Map<String, ShardingSphereMetaData> getMetaDataMap() {
        ShardingSphereMetaData metaData = mock(ShardingSphereMetaData.class, RETURNS_DEEP_STUBS);
        when(metaData.getDatabase().getName()).thenReturn("db");
        when(metaData.getResource().getDatabaseType()).thenReturn(new MySQLDatabaseType());
        when(metaData.getRuleMetaData().getRules()).thenReturn(Collections.emptyList());
        return Collections.singletonMap("db", metaData);
    }
    
    @SneakyThrows
    @Test
    public void assertExecute() {
        assertThat(((QueryResponseHeader) handler.execute()).getQueryHeaders().size(), is(1));
    }
    
    @SneakyThrows
    @Test
    public void assertNext() {
        handler.execute();
        assertTrue(handler.next());
    }
    
    @SneakyThrows
    @Test
    public void assertGetRowData() {
        handler.execute();
        assertThat(handler.getRowData().size(), is(1));
    }
}
