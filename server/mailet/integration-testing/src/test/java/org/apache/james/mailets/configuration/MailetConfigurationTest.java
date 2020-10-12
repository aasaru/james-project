/****************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one   *
 * or more contributor license agreements.  See the NOTICE file *
 * distributed with this work for additional information        *
 * regarding copyright ownership.  The ASF licenses this file   *
 * to you under the Apache License, Version 2.0 (the            *
 * "License"); you may not use this file except in compliance   *
 * with the License.  You may obtain a copy of the License at   *
 *                                                              *
 *   http://www.apache.org/licenses/LICENSE-2.0                 *
 *                                                              *
 * Unless required by applicable law or agreed to in writing,   *
 * software distributed under the License is distributed on an  *
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY       *
 * KIND, either express or implied.  See the License for the    *
 * specific language governing permissions and limitations      *
 * under the License.                                           *
 ****************************************************************/
package org.apache.james.mailets.configuration;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.james.transport.mailets.ToProcessor;
import org.apache.james.transport.matchers.All;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class MailetConfigurationTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    //@Test
    public void builderShouldThrowWhenMatcherIsNull() {
        expectedException.expect(IllegalStateException.class);
        MailetConfiguration.builder()
            .mailet(ToProcessor.class)
            .build();
    }

    //@Test
    public void builderShouldThrowWhenMailetIsNull() {
        expectedException.expect(IllegalStateException.class);
        MailetConfiguration.builder()
            .matcher(All.class)
            .build();
    }

    //@Test
    public void matcherWithConditionShouldReturnMatcherWhenNoCondition() {
        MailetConfiguration mailetConfiguration = MailetConfiguration.builder()
            .matcher(All.class)
            .mailet(ToProcessor.class)
            .build();

        assertThat(mailetConfiguration.matcherWithCondition()).isEqualTo("org.apache.james.transport.matchers.All");
    }

    //@Test
    public void matcherWithConditionShouldReturnMatcherWithConditionWhenSomeCondition() {
        MailetConfiguration mailetConfiguration = MailetConfiguration.builder()
            .matcher(All.class)
            .matcherCondition("condition")
            .mailet(ToProcessor.class)
            .build();

        assertThat(mailetConfiguration.matcherWithCondition()).isEqualTo("org.apache.james.transport.matchers.All=condition");
    }
}
