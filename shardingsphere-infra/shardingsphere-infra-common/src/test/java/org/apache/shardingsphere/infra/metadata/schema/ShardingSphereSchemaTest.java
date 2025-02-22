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

package org.apache.shardingsphere.infra.metadata.schema;

import org.apache.shardingsphere.infra.metadata.schema.model.ColumnMetaData;
import org.apache.shardingsphere.infra.metadata.schema.model.TableMetaData;
import org.junit.Test;

import java.util.Collections;
import java.util.HashSet;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public final class ShardingSphereSchemaTest {
    
    @Test
    public void assertGetAllTableNames() {
        assertThat(new ShardingSphereSchema(Collections.singletonMap("tbl", mock(TableMetaData.class))).getAllTableNames(), is(new HashSet<>(Collections.singleton("tbl"))));
    }
    
    @Test
    public void assertGet() {
        TableMetaData tableMetaData = mock(TableMetaData.class);
        assertThat(new ShardingSphereSchema(Collections.singletonMap("tbl", tableMetaData)).get("tbl"), is(tableMetaData));
    }
    
    @Test
    public void assertPut() {
        ShardingSphereSchema actual = new ShardingSphereSchema(Collections.emptyMap());
        TableMetaData tableMetaData = mock(TableMetaData.class);
        actual.put("tbl", tableMetaData);
        assertThat(actual.get("tbl"), is(tableMetaData));
    }
    
    @Test
    public void assertRemove() {
        ShardingSphereSchema actual = new ShardingSphereSchema(Collections.singletonMap("tbl", mock(TableMetaData.class)));
        actual.remove("tbl");
        assertNull(actual.get("tbl"));
    }
    
    @Test
    public void assertContainsTable() {
        assertTrue(new ShardingSphereSchema(Collections.singletonMap("tbl", mock(TableMetaData.class))).containsTable("tbl"));
    }
    
    @Test
    public void assertContainsColumn() {
        TableMetaData tableMetaData = new TableMetaData("tbl", Collections.singletonList(new ColumnMetaData("col", 0, false, false, false)), Collections.emptyList(), Collections.emptyList());
        assertTrue(new ShardingSphereSchema(Collections.singletonMap("tbl", tableMetaData)).containsColumn("tbl", "col"));
    }
    
    @Test
    public void assertGetAllColumnNamesWhenContainsKey() {
        TableMetaData tableMetaData = new TableMetaData("tbl", Collections.singletonList(new ColumnMetaData("col", 0, false, false, false)), Collections.emptyList(), Collections.emptyList());
        assertThat(new ShardingSphereSchema(Collections.singletonMap("tbl", tableMetaData)).getAllColumnNames("tbl"), is(Collections.singletonList("col")));
    }
    
    @Test
    public void assertGetAllColumnNamesWhenNotContainsKey() {
        TableMetaData tableMetaData = new TableMetaData("tbl", Collections.singletonList(new ColumnMetaData("col", 0, false, false, false)), Collections.emptyList(), Collections.emptyList());
        assertThat(new ShardingSphereSchema(Collections.singletonMap("tbl1", tableMetaData)).getAllColumnNames("tbl2"), is(Collections.<String>emptyList()));
    }
}
