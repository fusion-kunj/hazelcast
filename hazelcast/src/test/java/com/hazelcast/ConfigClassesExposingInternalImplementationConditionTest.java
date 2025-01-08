/*
 * Copyright (c) 2008-2025, Hazelcast, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hazelcast;

import com.hazelcast.test.archunit.ArchUnitRules;
import com.hazelcast.test.archunit.ArchUnitTestSupport;
import com.hazelcast.test.archunit.ModuleImportOptions;
import com.tngtech.archunit.core.domain.JavaClasses;
import org.junit.Test;

public class ConfigClassesExposingInternalImplementationConditionTest extends ArchUnitTestSupport {
    @Test
    public void configClassesExposingInternalImplementationCondition() {
        String basePackage = "com.hazelcast";
        JavaClasses classes = ModuleImportOptions.getCurrentModuleClasses(basePackage);

        ArchUnitRules.CONFIG_CLASSES_EXPOSING_INTERNAL_IMPLEMENTATION.check(classes);
    }
}
