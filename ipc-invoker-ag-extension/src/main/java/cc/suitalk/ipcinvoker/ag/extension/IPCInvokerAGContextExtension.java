/*
 *  Copyright (C) 2017-present Albie Liang. All rights reserved.
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

package cc.suitalk.ipcinvoker.ag.extension;

import net.sf.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;

import cc.suitalk.arbitrarygen.engine.JavaCodeAGEngine;
import cc.suitalk.arbitrarygen.extension.AGContext;
import cc.suitalk.arbitrarygen.extension.AGContextExtension;
import cc.suitalk.arbitrarygen.extension.ArbitraryGenProcessor;
import cc.suitalk.arbitrarygen.impl.AGAnnotationWrapper;
import cc.suitalk.arbitrarygen.template.TemplateManager;
import cc.suitalk.arbitrarygen.utils.FileOperation;
import cc.suitalk.arbitrarygen.utils.Log;

/**
 * Created by albieliang on 2017/12/18.
 */

public class IPCInvokerAGContextExtension implements AGContextExtension {

    private static final String TAG = "IPCInvoker.IPCInvokerAGContextExtension";

    static {
        loadTemplate("ipc-invoker-gentask", "/res/ag-template/IPCInvokeTask.ag-template");
//        loadTemplate("ipc-invoker-gentask-mgr", "/res/ag-template/IPCInvokeTaskMgr.ag-template");
    }

    private static void loadTemplate(String tag, String templatePath) {
        String template = "";
        InputStream is = IPCInvokerAGContextExtension.class.getResourceAsStream(templatePath);
//        Log.i(TAG, "doGet(jarPath : %s)", IPCInvokerAGContextExtension.class.getProtectionDomain().getCodeSource().getLocation().getPath());
        if (is != null) {
            template = FileOperation.read(is);
            try {
                is.close();
            } catch (IOException e) {
            }
        }
        if (template != null && template.length() > 0) {
            TemplateManager.getImpl().put(tag, template);
        }
    }

    @Override
    public void onPreInitialize(AGContext context, JSONObject initArgs) {
        Log.d(TAG, "onPreInitialize(%s)", context.hashCode());
        ArbitraryGenProcessor processor = context.getProcessor("javaCodeEngine");
        if (processor instanceof JavaCodeAGEngine) {
            JavaCodeAGEngine engine = (JavaCodeAGEngine) processor;
            AGAnnotationWrapper annWrapper = new AGAnnotationWrapper();
            annWrapper.addAnnotationProcessor(new IPCInvokeTaskProcessor());
            engine.addTypeDefWrapper(annWrapper);

            JSONObject javaCodeEngine = initArgs.getJSONObject("javaCodeEngine");
            if (javaCodeEngine != null) {
                if (!javaCodeEngine.containsKey("ruleFile") && !javaCodeEngine.containsKey("rule")) {
                    javaCodeEngine.put("rule", "${project.projectDir}/src/main/java/*.java");
                }
            }
        }
    }
}
