/*
 *  Copyright (C) 2016-present Albie Liang. All rights reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package cc.suitalk.gradle.plugin

import cc.suitalk.arbitrarygen.core.AGContextExtensionManager
import cc.suitalk.ipcinvoker.ag.extension.IPCInvokerAGContextExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 *
 * @Author AlbieLiang
 *
 */
class IPCInvokerAGExtensionPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        // Add AGContextExtension
        AGContextExtensionManager.getImpl().addExtension(IPCInvokerAGContextExtension.class)
        println("project ${project.name} apply IPCInvokerAGExtensionPlugin")
    }

}

