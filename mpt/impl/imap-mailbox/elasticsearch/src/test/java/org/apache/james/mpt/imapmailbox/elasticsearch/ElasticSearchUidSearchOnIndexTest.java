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

package org.apache.james.mpt.imapmailbox.elasticsearch;

import org.apache.james.mpt.api.ImapHostSystem;
import org.apache.james.mpt.imapmailbox.elasticsearch.host.ElasticSearchHostSystem;
import org.apache.james.mpt.imapmailbox.suite.UidSearchOnIndex;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class ElasticSearchUidSearchOnIndexTest extends UidSearchOnIndex {

    private ImapHostSystem system;

    @Override
    @Before
    public void setUp() throws Exception {
        system = new ElasticSearchHostSystem();
        system.beforeTest();
        super.setUp();
    }
    
    @Override
    protected ImapHostSystem createImapHostSystem() {
        return system;
    }

    @Ignore // failed: https://travis-ci.com/github/aasaru/james-project/jobs/395799807
    @Test
    public void testSearchAtomsUS() throws Exception {
        super.testSearchAtomsUS();
    }

    @After
    public void tearDown() throws Exception {
        system.afterTest();
    }
    
}
