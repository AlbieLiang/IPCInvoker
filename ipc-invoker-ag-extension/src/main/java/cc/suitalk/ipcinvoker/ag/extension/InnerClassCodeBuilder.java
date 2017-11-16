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

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import cc.suitalk.arbitrarygen.base.BaseStatement;
import cc.suitalk.arbitrarygen.base.Expression;
import cc.suitalk.arbitrarygen.base.PlainCodeBlock;
import cc.suitalk.arbitrarygen.block.MethodCodeBlock;
import cc.suitalk.arbitrarygen.block.TypeDefineCodeBlock;
import cc.suitalk.arbitrarygen.core.KeyWords;
import cc.suitalk.arbitrarygen.core.Value;
import cc.suitalk.arbitrarygen.core.Word;
import cc.suitalk.arbitrarygen.expression.VariableExpression;
import cc.suitalk.arbitrarygen.model.KeyValuePair;
import cc.suitalk.arbitrarygen.model.TypeName;
import cc.suitalk.arbitrarygen.statement.AnnotationStatement;
import cc.suitalk.arbitrarygen.statement.CaseStatement;
import cc.suitalk.arbitrarygen.statement.MethodInvokeStatement;
import cc.suitalk.arbitrarygen.statement.NormalStatement;
import cc.suitalk.arbitrarygen.statement.SwitchStatement;
import cc.suitalk.arbitrarygen.utils.Util;
import cc.suitalk.ipcinvoker.extension.annotation.IPCAsyncInvokeMethod;

/**
 * Created by albieliang on 2017/11/13.
 */

public class InnerClassCodeBuilder {

    public static void addInnerAsyncTaskClass(TypeDefineCodeBlock typeDefineCodeBlock, Set<? extends BaseStatement> methodSet) {
        TypeDefineCodeBlock asyncTaskClass = new TypeDefineCodeBlock();
        asyncTaskClass.setName(Util.createSimpleTypeName("IPCAsyncInvokeTaskImpl"));
        asyncTaskClass.setModifier(KeyWords.V_JAVA_KEYWORDS_PRIVATE);
        asyncTaskClass.setIsStatic(true);
        asyncTaskClass.setIsFinal(true);
        asyncTaskClass.setType(Util.createSimpleTypeName(KeyWords.V_JAVA_KEYWORDS_CLASS));
        asyncTaskClass.addInterface(Util.createSimpleTypeName("IPCAsyncInvokeTask"));
        asyncTaskClass.addAnnotation(new AnnotationStatement("Singleton"));

        MethodCodeBlock method = new MethodCodeBlock();
        method.setModifier(KeyWords.V_JAVA_KEYWORDS_PUBLIC);
        method.setType(Util.createSimpleTypeName(KeyWords.V_JAVA_KEYWORDS_DATA_BASE_TYPE_VOID));
        method.setName(Util.createSimpleTypeName("invoke"));
        method.addAnnotation(new AnnotationStatement("Override"));
        // Bundle __data, final IPCInvokeCallback __callback
        method.addArg(new KeyValuePair<Word, TypeName>(Util.createKeyWord("__data"), Util.createSimpleTypeName("Bundle")));
        TypeName typeName = Util.createSimpleTypeName("IPCInvokeCallback");
        typeName.setFinal(Util.createKeyWord(KeyWords.V_JAVA_KEYWORDS_FINAL));
        method.addArg(new KeyValuePair<Word, TypeName>(Util.createKeyWord("__callback"), typeName));
        method.addStatement(new NormalStatement("int invokeMethod = __data.getInt(KEY_INVOKE_METHOD);"));

        SwitchStatement switchStatement = new SwitchStatement();
        switchStatement.setConditionExpression(new VariableExpression("invokeMethod"));

        for (BaseStatement statement : methodSet) {
            if (method.getAnnotation(IPCAsyncInvokeMethod.class.getSimpleName()) == null) {
                continue;
            }
            switchStatement.addCaseStatement(buildCaseStatement((MethodCodeBlock) statement));
        }

        method.addStatement(switchStatement);

        asyncTaskClass.addMethod(method);
    }

    private static CaseStatement buildCaseStatement(MethodCodeBlock method) {
        CaseStatement caseStatement = new CaseStatement(
                new VariableExpression(String.format("ASYNC_INVOKE_METHOD_%s", method.getName().getName())));
        PlainCodeBlock plainCodeBlock = new PlainCodeBlock();
        List<KeyValuePair<Word, TypeName>> list = method.getArgs();
        // Get parameters
        for (KeyValuePair<Word, TypeName> arg : list) {
            String value = arg.getKey().value;
            plainCodeBlock.addStatement(
                    new NormalStatement(String.format("final int %s = ParameterHelper.get(__data, \"%s\");", value, value)));
        }
        plainCodeBlock.addStatement(new NormalStatement("final CustomIPCTask __task = getTarget();"));
        plainCodeBlock.addStatement(new NormalStatement("IPCRemoteInvokeCallback __callbackProxy = null;"));
        // TODO: 2017/11/13 albieliang
        boolean needCallback = false;
        plainCodeBlock.addStatement(new NormalStatement(String.format("if (__callback != null) {\n" +
                "        __callbackProxy = new IPCRemoteInvokeCallback<ResultData>() {\n" +
                "            @Override\n" +
                "            public void onCallback(ResultData data) {\n" +
                "                Bundle result = new Bundle();\n" +
                "                ParameterHelper.put(result, \"__result\", data);\n" +
                "                __callback.onCallback(result);\n" +
                "            }\n" +
                "        };\n" +
                "    }")));

        List<Value> invokeMethodArgs = getMethodArgs(list);
        if (needCallback) {
            invokeMethodArgs.add(Util.convertTo(Util.createSignWord("__callbackProxy", KeyWords.Sign.Type.NORMAL)));
        }
        MethodInvokeStatement invokeStatement = new MethodInvokeStatement("__task.showLoading", invokeMethodArgs);
        plainCodeBlock.addStatement(invokeStatement);

        caseStatement.setCodeBlock(plainCodeBlock);
        return caseStatement;
    }

    private static List<Value> getMethodArgs(List<KeyValuePair<Word, TypeName>> args) {
        List<Value> list = new LinkedList<>();
        for (KeyValuePair<Word, TypeName> keyValuePair : args) {
            list.add(Util.convertTo(keyValuePair.getKey()));
        }
        return list;
    }
}
