/*
 * Copyright 2010-2013 JetBrains s.r.o.
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

package org.jetbrains.jet.plugin.quickfix;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jet.lang.descriptors.ClassDescriptor;
import org.jetbrains.jet.lang.diagnostics.Diagnostic;
import org.jetbrains.jet.lang.psi.JetClass;
import org.jetbrains.jet.lang.resolve.BindingContext;
import org.jetbrains.jet.lang.resolve.BindingContextUtils;
import org.jetbrains.jet.lang.types.JetType;
import org.jetbrains.jet.lexer.JetKeywordToken;
import org.jetbrains.jet.lexer.JetToken;
import org.jetbrains.jet.plugin.caches.resolve.KotlinCacheManager;

import static org.jetbrains.jet.lexer.JetTokens.FINAL_KEYWORD;
import static org.jetbrains.jet.lexer.JetTokens.OPEN_KEYWORD;

public class FinalSupertypeFix extends JetIntentionAction<JetClass> {
    private final JetClass childClass;

    public FinalSupertypeFix(@NotNull JetClass childClass) {
        super(childClass);
        this.childClass = childClass;
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile file) {
        return super.isAvailable(project, editor, file) && findSuperClass(childClass, getBindingContext(project)) != null;
    }

    @NotNull
    @Override
    public String getText() {
        return "Add 'open' modifier to superclass";
    }

    @NotNull
    @Override
    public String getFamilyName() {
        return "Add modifier";
    }

    @Override
    public void invoke(@NotNull Project project, Editor editor, PsiFile file) throws IncorrectOperationException {
        JetToken[] modifiersThanCanBeReplaced = new JetKeywordToken[] { FINAL_KEYWORD };
        JetClass superClass = findSuperClass(childClass, getBindingContext(project));
        if (superClass != null) {
            superClass.replace(AddModifierFix.addModifier(superClass, OPEN_KEYWORD, modifiersThanCanBeReplaced, project, false));
        }
    }

    @NotNull
    private static BindingContext getBindingContext(Project project) {
        return KotlinCacheManager.getInstance(project).getDeclarationsFromProject(project).getBindingContext();
    }

    @Nullable
    private static JetClass findSuperClass(JetClass childClass, BindingContext context) {
        ClassDescriptor childClassDescriptor = context.get(BindingContext.CLASS, childClass);
        if (childClassDescriptor == null) {
            return null;
        }
        for (JetType supertype: childClassDescriptor.getTypeConstructor().getSupertypes()) {
            ClassDescriptor superClassDescriptor = (ClassDescriptor) supertype.getConstructor().getDeclarationDescriptor();
            if (superClassDescriptor == null) {
                continue;
            }
            JetClass superClass = (JetClass) BindingContextUtils.descriptorToDeclaration(context, superClassDescriptor);
            if (superClass != null) {
                return superClass;
            }
        }
        return null;
    }

    @Nullable
    public static JetIntentionActionFactory createFactory() {
        return new JetIntentionActionFactory() {
            @Nullable
            @Override
            public IntentionAction createAction(Diagnostic diagnostic) {
                JetClass childClass = QuickFixUtil.getParentElementOfType(diagnostic, JetClass.class);
                return childClass == null ? null : new FinalSupertypeFix(childClass);
            }
        };
    }
}
