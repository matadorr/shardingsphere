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

package org.apache.shardingsphere.scaling.distsql.handler.update;

import org.apache.shardingsphere.infra.distsql.exception.DistSQLException;
import org.apache.shardingsphere.infra.distsql.exception.rule.RequiredRuleMissedException;
import org.apache.shardingsphere.infra.distsql.exception.rule.RuleDisabledException;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.scaling.distsql.statement.DisableShardingScalingRuleStatement;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class DisabledShardingScalingRuleStatementUpdaterTest {
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ShardingSphereMetaData shardingSphereMetaData;
    
    private final DisableShardingScalingRuleStatementUpdater updater = new DisableShardingScalingRuleStatementUpdater();
    
    @Before
    public void before() {
        when(shardingSphereMetaData.getDatabase().getName()).thenReturn("test");
    }
    
    @Test(expected = RequiredRuleMissedException.class)
    public void assertCheckWithoutShardingRule() throws DistSQLException {
        updater.checkSQLStatement(shardingSphereMetaData, createSQLStatement("default_scaling"), null);
    }
    
    @Test(expected = RequiredRuleMissedException.class)
    public void assertCheckNotExist() throws DistSQLException {
        ShardingRuleConfiguration currentRuleConfig = new ShardingRuleConfiguration();
        currentRuleConfig.getScaling().put("default_scaling", null);
        updater.checkSQLStatement(shardingSphereMetaData, createSQLStatement("new_scaling"), currentRuleConfig);
    }
    
    @Test(expected = RuleDisabledException.class)
    public void assertCheckDisabled() throws DistSQLException {
        ShardingRuleConfiguration currentRuleConfig = new ShardingRuleConfiguration();
        String scalingName = "default_scaling";
        currentRuleConfig.getScaling().put(scalingName, null);
        updater.checkSQLStatement(shardingSphereMetaData, createSQLStatement(scalingName), currentRuleConfig);
    }
    
    @Test
    public void assertBuildToBeAlteredRuleConfiguration() {
        ShardingRuleConfiguration result = updater.buildToBeAlteredRuleConfiguration(createSQLStatement("default_scaling"));
        assertNull(result);
    }
    
    @Test
    public void assertUpdateSuccess() {
        ShardingRuleConfiguration currentRuleConfig = new ShardingRuleConfiguration();
        String scalingName = "default_scaling";
        currentRuleConfig.getScaling().put(scalingName, null);
        updater.updateCurrentRuleConfiguration(currentRuleConfig, null);
        assertNull(currentRuleConfig.getScalingName());
    }
    
    private DisableShardingScalingRuleStatement createSQLStatement(final String scalingName) {
        return new DisableShardingScalingRuleStatement(scalingName);
    }
}
