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

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import java.io.File;
import java.util.Set;

import cc.suitalk.arbitrarygen.base.BaseStatement;
import cc.suitalk.arbitrarygen.base.JavaFileObject;
import cc.suitalk.arbitrarygen.block.TypeDefineCodeBlock;
import cc.suitalk.arbitrarygen.core.ArgsConstants;
import cc.suitalk.arbitrarygen.extension.AGContext;
import cc.suitalk.arbitrarygen.extension.processoing.AGSupportedAnnotationTypes;
import cc.suitalk.arbitrarygen.extension.processoing.AbstractAGAnnotationProcessor;
import cc.suitalk.arbitrarygen.protocol.EnvArgsConstants;
import cc.suitalk.arbitrarygen.utils.Log;
import cc.suitalk.arbitrarygen.utils.Util;
import cc.suitalk.ipcinvoker.extension.annotation.IPCInvokeTaskManager;

/**
 * Created by albieliang on 2017/11/14.
 */

@AGSupportedAnnotationTypes({"IPCAsyncInvokeMethod", "IPCSyncInvokeMethod"})
public class IPCInvokeTaskProcessor extends AbstractAGAnnotationProcessor {

    private static final String TAG = "AG.IPCInvokeTaskProcessor";
//
//    private static Set<String> sSupportedAnnotationTypes = new HashSet<>();
//
//    static {
//        sSupportedAnnotationTypes.add(IPCAsyncInvokeMethod.class.getSimpleName());
//    }
//
//    @Override
//    public Set<String> getSupportedAnnotationTypes() {
//        return sSupportedAnnotationTypes;
//    }

    @Override
    public boolean process(AGContext context, JSONObject env, JavaFileObject javaFileObject, TypeDefineCodeBlock typeDefineCodeBlock, Set<? extends BaseStatement> set) {
        if (typeDefineCodeBlock.getAnnotation(IPCInvokeTaskManager.class.getSimpleName()) == null) {
            Log.i(TAG, "the TypeDefineCodeBlock do not contains 'IPCInvokeTaskManager' annotation.(%s)", javaFileObject.getFileName());
            return false;
        }
        if (set.isEmpty()) {
            Log.i(TAG, "containsSpecialAnnotationStatements is nil");
            return false;
        }
        final String outputDir = env.optString(EnvArgsConstants.KEY_OUTPUT_DIR);
        final String pkg = env.optString(EnvArgsConstants.KEY_PACKAGE);
        final String filePath = env.optString(EnvArgsConstants.KEY_FILE_PATH);

        if (Util.isNullOrNil(outputDir)) {
            Log.i(TAG, "process failed, outputDir is null or nil.(filePath : %s)", filePath);
            return false;
        }
        JSONObject args = new JSONObject();
//        args.put("template", "");
        args.put("templateTag", "ipc-invoker-gentask");
        args.put(ArgsConstants.EXTERNAL_ARGS_KEY_DEST_DIR, outputDir);
        args.put("toFile", Util.joint(File.separator, Util.exchangeToPath(pkg),
                String.format("%s$AG.java", typeDefineCodeBlock.getName().getName())));
        args.put("fileObject", javaFileObject.toJSONObject());
        args.put("methodSet", toJSONArray(set));
        context.execProcess("template-processor", args);
        return true;
    }

    private JSONArray toJSONArray(Set<? extends BaseStatement> set) {
        JSONArray jsonArray = new JSONArray();
        for (BaseStatement s : set) {
            jsonArray.add(s.toJSONObject());
        }
        return jsonArray;
    }
}
