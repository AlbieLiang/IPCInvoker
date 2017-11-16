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

import java.util.Set;

import cc.suitalk.arbitrarygen.base.BaseStatement;
import cc.suitalk.arbitrarygen.block.FieldCodeBlock;
import cc.suitalk.arbitrarygen.block.MethodCodeBlock;
import cc.suitalk.arbitrarygen.block.TypeDefineCodeBlock;
import cc.suitalk.arbitrarygen.core.KeyWords;
import cc.suitalk.arbitrarygen.statement.AnnotationStatement;
import cc.suitalk.arbitrarygen.utils.Util;
import cc.suitalk.ipcinvoker.extension.annotation.IPCAsyncInvokeMethod;
import cc.suitalk.ipcinvoker.extension.annotation.IPCInvokeTaskManager;
import cc.suitalk.ipcinvoker.extension.annotation.IPCSyncInvokeMethod;

/**
 * Created by albieliang on 2017/11/9.
 */

public class FieldCodeBuilder {

    public static void addCommonFields(TypeDefineCodeBlock srcTypeDefine, TypeDefineCodeBlock targetTypeDefine) {
        // Add Fields
        targetTypeDefine.addField(FieldCodeBuilder.createField_TAG(srcTypeDefine));
        targetTypeDefine.addField(FieldCodeBuilder.createField_PROCESS(srcTypeDefine));
        targetTypeDefine.addField(FieldCodeBuilder.createField_TARGET_CLASS(srcTypeDefine));
        targetTypeDefine.addField(FieldCodeBuilder.createField_KEY_INVOKE_METHOD(srcTypeDefine));
    }

    public static FieldCodeBlock createField_TAG(TypeDefineCodeBlock typeDefineCodeBlock) {
        FieldCodeBlock field = new FieldCodeBlock();
        field.setIsFinal(true);
        field.setModifier(KeyWords.V_JAVA_KEYWORDS_PUBLIC);
        field.setIsStatic(true);
        field.setType(Util.createSimpleTypeName(KeyWords.V_JAVA_KEYWORDS_DATA_BASE_TYPE_STRING));
        field.setName(Util.createSimpleTypeName("TAG"));
        field.setDefault("\"AG.Proxy$" + typeDefineCodeBlock.getName().getName() + "\"");
        return field;
    }

    public static FieldCodeBlock createField_PROCESS(TypeDefineCodeBlock typeDefineCodeBlock) {
        FieldCodeBlock field = new FieldCodeBlock();
        field.setIsFinal(true);
        field.setModifier(KeyWords.V_JAVA_KEYWORDS_PUBLIC);
        field.setIsStatic(true);
        field.setType(Util.createSimpleTypeName(KeyWords.V_JAVA_KEYWORDS_DATA_BASE_TYPE_STRING));
        field.setName(Util.createSimpleTypeName("PROCESS"));
        AnnotationStatement as = typeDefineCodeBlock.getAnnotation(IPCInvokeTaskManager.class.getSimpleName());
        field.setDefault("\"" + as.getArg("process").getValue() + "\"");
        return field;
    }

    public static FieldCodeBlock createField_TARGET_CLASS(TypeDefineCodeBlock typeDefineCodeBlock) {
        FieldCodeBlock field = new FieldCodeBlock();
        field.setIsFinal(true);
        field.setModifier(KeyWords.V_JAVA_KEYWORDS_PUBLIC);
        field.setIsStatic(true);
        field.setType(Util.createSimpleTypeName("Class<?>"));
        field.setName(Util.createSimpleTypeName("TARGET_CLASS"));
        field.setDefault(typeDefineCodeBlock.getName().getName() + ".class");
        return field;
    }

    public static FieldCodeBlock createField_KEY_INVOKE_METHOD(TypeDefineCodeBlock typeDefineCodeBlock) {
        FieldCodeBlock field = new FieldCodeBlock();
        field.setIsFinal(true);
        field.setModifier(KeyWords.V_JAVA_KEYWORDS_PRIVATE);
        field.setIsStatic(true);
        field.setType(Util.createSimpleTypeName(KeyWords.V_JAVA_KEYWORDS_DATA_BASE_TYPE_STRING));
        field.setName(Util.createSimpleTypeName("KEY_INVOKE_METHOD"));
        field.setDefault("\"__invoke_method\"");
        return field;
    }


    private static final int ASYNC_INVOKE_METHOD_showLoading = 0;
    private static final int ASYNC_INVOKE_METHOD_hideLoading = 1;
    private static final int SYNC_INVOKE_METHOD_getName = 2;

    public static void addMethodFields(TypeDefineCodeBlock typeDefineCodeBlock, Set<? extends BaseStatement> methodSet) {
        int index = 0;
        for (BaseStatement statement : methodSet) {
            if (!(statement instanceof MethodCodeBlock)) {
                continue;
            }
            MethodCodeBlock method = (MethodCodeBlock) statement;
            if (method.getAnnotation(IPCAsyncInvokeMethod.class.getSimpleName()) != null) {
                typeDefineCodeBlock.addField(createField(
                        KeyWords.V_JAVA_KEYWORDS_DATA_BASE_TYPE_INT,
                        "ASYNC_INVOKE_METHOD_" + method.getName().getName(), "" + (index++)));
            } else if (method.getAnnotation(IPCSyncInvokeMethod.class.getSimpleName()) != null) {
                typeDefineCodeBlock.addField(createField(
                        KeyWords.V_JAVA_KEYWORDS_DATA_BASE_TYPE_INT,
                        "SYNC_INVOKE_METHOD_" + method.getName().getName(), "" + (index++)));
            }
        }
    }

    public static FieldCodeBlock createField(String fieldType, String fieldName, String fieldValue) {
        FieldCodeBlock field = new FieldCodeBlock();
        field.setIsFinal(true);
        field.setModifier(KeyWords.V_JAVA_KEYWORDS_PRIVATE);
        field.setIsStatic(true);
        field.setType(Util.createSimpleTypeName(fieldType));
        field.setName(Util.createSimpleTypeName(fieldName));
        field.setDefault(fieldValue);
        return field;
    }
}
