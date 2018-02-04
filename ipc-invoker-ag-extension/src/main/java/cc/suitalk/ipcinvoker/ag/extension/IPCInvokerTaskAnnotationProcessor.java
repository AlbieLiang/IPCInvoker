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

import java.io.File;
import java.util.Set;

import cc.suitalk.arbitrarygen.base.BaseStatement;
import cc.suitalk.arbitrarygen.base.JavaFileObject;
import cc.suitalk.arbitrarygen.block.MethodCodeBlock;
import cc.suitalk.arbitrarygen.block.TypeDefineCodeBlock;
import cc.suitalk.arbitrarygen.core.KeyWords;
import cc.suitalk.arbitrarygen.extension.AGContext;
import cc.suitalk.arbitrarygen.extension.CustomizeGenerator;
import cc.suitalk.arbitrarygen.extension.processoing.AGSupportedAnnotationTypes;
import cc.suitalk.arbitrarygen.extension.processoing.AbstractAGAnnotationProcessor;
import cc.suitalk.arbitrarygen.gencode.CodeGenerator;
import cc.suitalk.arbitrarygen.gencode.GenCodeTaskInfo;
import cc.suitalk.arbitrarygen.protocol.EnvArgsConstants;
import cc.suitalk.arbitrarygen.utils.FileOperation;
import cc.suitalk.arbitrarygen.utils.Log;
import cc.suitalk.arbitrarygen.utils.Util;
import cc.suitalk.ipcinvoker.extension.annotation.IPCAsyncInvokeMethod;
import cc.suitalk.ipcinvoker.extension.annotation.IPCInvokeTask;
import cc.suitalk.ipcinvoker.extension.annotation.IPCSyncInvokeMethod;

/**
 * Created by albieliang on 2017/9/29.
 */

@AGSupportedAnnotationTypes({"IPCAsyncInvokeMethod", "IPCAsyncInvokeMethod"})
public class IPCInvokerTaskAnnotationProcessor extends AbstractAGAnnotationProcessor {

    private static final String TAG = "AG.IPCInvokerTaskAnnotationProcessor";
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
        if (typeDefineCodeBlock.getAnnotation(IPCInvokeTask.class.getSimpleName()) == null) {
            Log.i(TAG, "the TypeDefineCodeBlock do not contains 'IPCInvokeTask' annotation.(%s)", javaFileObject.getFileName());
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

        JavaFileObject fObject = new JavaFileObject();

        fObject.addTypeDefineCodeBlock(createTypeDefineCodeBlock(typeDefineCodeBlock, set));
        fObject.setPackageStatement(javaFileObject.getPackageStatement());
        fObject.copyImports(javaFileObject);
        fObject.attachEnvironmentArgs(javaFileObject.getEnvironmentArgs());

        GenCodeTaskInfo taskInfo = new GenCodeTaskInfo();
        taskInfo.FileName = fObject.getFileName();
        taskInfo.RootDir = Util.joint(File.separator, outputDir, Util.exchangeToPath(pkg));
        taskInfo.javaFileObject = fObject;

        CustomizeGenerator generator = new CodeGenerator(fObject);
        FileOperation.saveToFile(taskInfo, generator.genCode());
        Log.i(TAG, "genCode rootDir : %s, fileName : %s, suffix : %s", taskInfo.RootDir, taskInfo.FileName, taskInfo.Suffix);
        return true;
    }

    private static TypeDefineCodeBlock createTypeDefineCodeBlock(TypeDefineCodeBlock typeDefineCodeBlock, Set<? extends BaseStatement> set) {
        TypeDefineCodeBlock typeDefine = new TypeDefineCodeBlock();
        typeDefine.setIsFinal(true);
        typeDefine.setModifier(KeyWords.V_JAVA_KEYWORDS_PUBLIC);
        typeDefine.setType(Util.createSimpleTypeName(KeyWords.V_JAVA_KEYWORDS_CLASS));
        typeDefine.setName(Util.createSimpleTypeName(typeDefineCodeBlock.getName().getName() + "$AG"));

        FieldCodeBuilder.addCommonFields(typeDefineCodeBlock, typeDefine);
        FieldCodeBuilder.addMethodFields(typeDefineCodeBlock, set);
//        typeDefine.addInterface();
        for (BaseStatement statement : set) {
            if (!(statement instanceof MethodCodeBlock)) {
                continue;
            }
            MethodCodeBlock method = (MethodCodeBlock) statement;
            if (method.getAnnotation(IPCAsyncInvokeMethod.class.getSimpleName()) != null) {
                method.setCodeBlock(AsyncMethodCodeBuilder.createMethodPlainCodeBlock(method));
            } else if (method.getAnnotation(IPCSyncInvokeMethod.class.getSimpleName()) != null) {
                method.setCodeBlock(SyncMethodCodeBuilder.createMethodPlainCodeBlock(method));
            }
        }
        MethodCodeBuilder.addCommonMethods(typeDefineCodeBlock);
        InnerClassCodeBuilder.addInnerAsyncTaskClass(typeDefineCodeBlock, set);
        return typeDefine;
    }

}
