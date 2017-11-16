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

import java.util.List;

import cc.suitalk.arbitrarygen.base.Expression;
import cc.suitalk.arbitrarygen.base.PlainCodeBlock;
import cc.suitalk.arbitrarygen.block.MethodCodeBlock;
import cc.suitalk.arbitrarygen.core.Word;
import cc.suitalk.arbitrarygen.model.KeyValuePair;
import cc.suitalk.arbitrarygen.model.TypeName;
import cc.suitalk.arbitrarygen.statement.IfElseStatement;
import cc.suitalk.arbitrarygen.statement.NormalStatement;

/**
 * Created by albieliang on 2017/11/8.
 */

public class SyncMethodCodeBuilder {

    public static PlainCodeBlock createMethodPlainCodeBlock(MethodCodeBlock method) {
        PlainCodeBlock plainCodeBlock = new PlainCodeBlock();
        plainCodeBlock.addStatement(new NormalStatement(buildMethodLog(method)));
        addParameterStatement(plainCodeBlock, method);
        addCallbackStatement(plainCodeBlock, method);
        plainCodeBlock.addStatement(new NormalStatement("IPCInvoker.invokeAsync(PROCESS, bundle, IPCAsyncInvokeTaskImpl.class, __callback);"));
        return plainCodeBlock;
    }

    private static String buildMethodLog(MethodCodeBlock method) {
        List<KeyValuePair<Word, TypeName>> list = method.getArgs();
        StringBuilder builder = new StringBuilder();
        builder.append("Log.d(TAG, \"");
        builder.append(method.getName().getName());
        builder.append("(");
        if (!list.isEmpty()) {
            KeyValuePair<Word, TypeName> keyValuePair = list.get(0);
            builder.append(keyValuePair.getKey());
            builder.append(" : %s");
            for (int i = 1; i < list.size(); i++) {
                keyValuePair = list.get(i);
                builder.append(", ");
                builder.append(keyValuePair.getKey().value);
                builder.append(" : %s");
            }
        }
        builder.append(")");
        builder.append("\"");
        if (!list.isEmpty()) {
            for (KeyValuePair<Word, TypeName> keyValuePair : list) {
                builder.append(", ");
                builder.append(keyValuePair.getKey().value);
            }
        }
        builder.append(");");
        return builder.toString();
    }

    private static void addParameterStatement(PlainCodeBlock plainCodeBlock, MethodCodeBlock method) {
        List<KeyValuePair<Word, TypeName>> list = method.getArgs();

        plainCodeBlock.addStatement(new NormalStatement("Bundle bundle = new Bundle();"));
        plainCodeBlock.addStatement(new NormalStatement(
                String.format("bundle.putInt(KEY_INVOKE_METHOD, ASYNC_INVOKE_METHOD_%s);", method.getName().getName())));

        for (KeyValuePair<Word, TypeName> keyValuePair : list) {
            String argName = keyValuePair.getKey().value;
            plainCodeBlock.addStatement(new NormalStatement(
                    String.format("ParameterHelper.put(bundle, \"%s\", %s);", argName, argName)));
        }
    }

    private static void addCallbackStatement(PlainCodeBlock plainCodeBlock, MethodCodeBlock method) {
        List<KeyValuePair<Word, TypeName>> list = method.getArgs();
        if (list.isEmpty()) {
            return;
        }
        // TODO: 2017/11/8 albieliang,
        String callbackArgName = list.get(list.size() - 1).getKey().value;

        plainCodeBlock.addStatement(new NormalStatement("IPCInvokeCallback __callback = null;"));
        if (callbackArgName != null && callbackArgName.length() > 0) {
            IfElseStatement ifElseStatement = new IfElseStatement();
            Expression conditionExp = new Expression();
            conditionExp.setVariable(String.format("%s != null", callbackArgName));
            ifElseStatement.setConditionExpression(conditionExp);
            String callbackStatement = String.format("__callback = new IPCInvokeCallback() {\n" +
                    "\n" +
                    "                @Override\n" +
                    "                public void onCallback(Bundle data) {\n" +
                    "                    if (callback != null) {\n" +
                    "                        ResultData result = ParameterHelper.get(data, \"__result\");\n" +
                    "                        %s.onCallback(result);\n" +
                    "                    }\n" +
                    "                }\n" +
                    "            };", callbackArgName);
            plainCodeBlock.addStatement(new NormalStatement(callbackStatement));
        }
    }
}
