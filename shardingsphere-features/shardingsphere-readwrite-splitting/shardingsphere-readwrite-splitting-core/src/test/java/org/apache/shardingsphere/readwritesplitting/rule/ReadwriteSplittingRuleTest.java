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

package org.apache.shardingsphere.readwritesplitting.rule;

import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.infra.metadata.schema.QualifiedDatabase;
import org.apache.shardingsphere.mode.metadata.storage.event.DataSourceNameDisabledEvent;
import org.apache.shardingsphere.readwritesplitting.api.ReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.rule.ReadwriteSplittingDataSourceRuleConfiguration;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class ReadwriteSplittingRuleTest {
    
    @Test(expected = IllegalArgumentException.class)
    public void assertNewWithEmptyDataSourceRule() {
        new ReadwriteSplittingRule(new ReadwriteSplittingRuleConfiguration(Collections.emptyList(), Collections.emptyMap()));
    }
    
    @Test
    public void assertFindDataSourceRule() {
        Optional<ReadwriteSplittingDataSourceRule> actual = createReadwriteSplittingRule().findDataSourceRule("test_pr");
        assertTrue(actual.isPresent());
        assertDataSourceRule(actual.get());
    }
    
    @Test
    public void assertGetSingleDataSourceRule() {
        assertDataSourceRule(createReadwriteSplittingRule().getSingleDataSourceRule());
    }
    
    private ReadwriteSplittingRule createReadwriteSplittingRule() {
        ReadwriteSplittingDataSourceRuleConfiguration config =
                new ReadwriteSplittingDataSourceRuleConfiguration("test_pr", "Static", createProperties(), "random");
        return new ReadwriteSplittingRule(new ReadwriteSplittingRuleConfiguration(
                Collections.singleton(config), Collections.singletonMap("random", new ShardingSphereAlgorithmConfiguration("RANDOM", new Properties()))));
    }
    
    private Properties createProperties() {
        Properties result = new Properties();
        result.setProperty("write-data-source-name", "write_ds");
        result.setProperty("read-data-source-names", "read_ds_0,read_ds_1");
        return result;
    }
    
    private void assertDataSourceRule(final ReadwriteSplittingDataSourceRule actual) {
        assertThat(actual.getName(), is("test_pr"));
        assertThat(actual.getReadwriteSplittingStrategy().getWriteDataSource(), is("write_ds"));
        assertThat(actual.getReadwriteSplittingStrategy().getReadDataSources(), is(Arrays.asList("read_ds_0", "read_ds_1")));
        assertThat(actual.getLoadBalancer().getType(), is("RANDOM"));
    }
    
    @Test
    public void assertUpdateRuleStatusWithNotExistDataSource() {
        ReadwriteSplittingRule readwriteSplittingRule = createReadwriteSplittingRule();
        readwriteSplittingRule.updateStatus(new DataSourceNameDisabledEvent(new QualifiedDatabase("readwrite_splitting_db.readwrite.read_ds"), true));
        assertThat(readwriteSplittingRule.getSingleDataSourceRule().getReadDataSourceNames(), is(Arrays.asList("read_ds_0", "read_ds_1")));
    }
    
    @Test
    public void assertUpdateRuleStatus() {
        ReadwriteSplittingRule readwriteSplittingRule = createReadwriteSplittingRule();
        readwriteSplittingRule.updateStatus(new DataSourceNameDisabledEvent(new QualifiedDatabase("readwrite_splitting_db.readwrite.read_ds_0"), true));
        assertThat(readwriteSplittingRule.getSingleDataSourceRule().getReadDataSourceNames(), is(Collections.singletonList("read_ds_1")));
    }
    
    @Test
    public void assertUpdateRuleStatusWithEnable() {
        ReadwriteSplittingRule readwriteSplittingRule = createReadwriteSplittingRule();
        readwriteSplittingRule.updateStatus(new DataSourceNameDisabledEvent(new QualifiedDatabase("readwrite_splitting_db.readwrite.read_ds_0"), true));
        assertThat(readwriteSplittingRule.getSingleDataSourceRule().getReadDataSourceNames(), is(Collections.singletonList("read_ds_1")));
        readwriteSplittingRule.updateStatus(new DataSourceNameDisabledEvent(new QualifiedDatabase("readwrite_splitting_db.readwrite.read_ds_0"), false));
        assertThat(readwriteSplittingRule.getSingleDataSourceRule().getReadDataSourceNames(), is(Arrays.asList("read_ds_0", "read_ds_1")));
    }
    
    @Test
    public void assertGetDataSourceMapper() {
        ReadwriteSplittingRule readwriteSplittingRule = createReadwriteSplittingRule();
        Map<String, Collection<String>> actual = readwriteSplittingRule.getDataSourceMapper();
        Map<String, Collection<String>> expected = Collections.singletonMap("test_pr", Arrays.asList("write_ds", "read_ds_0", "read_ds_1"));
        assertThat(actual, is(expected));
    }
}
