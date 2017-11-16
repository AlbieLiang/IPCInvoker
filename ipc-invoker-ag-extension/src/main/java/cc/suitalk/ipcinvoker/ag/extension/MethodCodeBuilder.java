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

import cc.suitalk.arbitrarygen.base.PlainCodeBlock;
import cc.suitalk.arbitrarygen.block.MethodCodeBlock;
import cc.suitalk.arbitrarygen.block.TypeDefineCodeBlock;
import cc.suitalk.arbitrarygen.core.KeyWords;
import cc.suitalk.arbitrarygen.statement.NormalStatement;
import cc.suitalk.arbitrarygen.utils.Util;

/**
 * Created by albieliang on 2017/11/9.
 */

public class MethodCodeBuilder {

    public static void addCommonMethods(TypeDefineCodeBlock typeDefineCodeBlock) {
        addMethod_getTarget(typeDefineCodeBlock);
    }

    private static void addMethod_getTarget(TypeDefineCodeBlock typeDefineCodeBlock) {
        final String className = typeDefineCodeBlock.getName().getName();
        MethodCodeBlock method = new MethodCodeBlock();
        method.setCommendBlock("// Get singleton target object");
        method.setModifier(KeyWords.V_JAVA_KEYWORDS_PRIVATE);
        method.setIsStatic(true);
        method.setType(Util.createSimpleTypeName(className));
        method.setName(Util.createSimpleTypeName("getTarget"));
        PlainCodeBlock plainCodeBlock = new PlainCodeBlock();
        plainCodeBlock.addStatement(new NormalStatement(
                String.format("%s task = ObjectStore.get(TARGET_CLASS);\n" +
                "        if (task == null) {\n" +
                "            synchronized (%s$AG.class) {\n" +
                "                task = ObjectStore.get(TARGET_CLASS);\n" +
                "                if (task == null) {\n" +
                "                    task = new %s();\n" +
                "                    ObjectStore.put(task);\n" +
                "                }\n" +
                "            }\n" +
                "        }\n" +
                "        return task;", className, className, className)));
        method.setCodeBlock(plainCodeBlock);
    }
}
