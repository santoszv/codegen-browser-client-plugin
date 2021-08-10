/*
 *    Copyright 2021 Santos Zatarain Vera
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

@file:Suppress("DuplicatedCode")

package mx.com.inftel.codegen.browser_client

import io.github.classgraph.MethodInfo
import io.github.classgraph.TypeSignature

class PropertyModel(val getter: MethodInfo) {

    val propertyName: String by lazy {
        getter.propertyName
    }

    val capitalizedName: String by lazy {
        getter.capitalizedName
    }

    val type: TypeSignature by lazy {
        getter.typeSignatureOrTypeDescriptor.resultType
    }

    val isNotNull: Boolean by lazy {
        getter.hasAnnotation("org.jetbrains.annotations.NotNull")
                || getter.hasAnnotation("javax.validation.constraints.NotNull")
                || getter.hasAnnotation("javax.validation.constraints.NotBlank")
    }
}