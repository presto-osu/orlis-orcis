/*
 * Copyright (C) 2015 75py
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.nagopy.android.disablemanager2.support;

import android.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * リフレクションでメソッドを取得する際、try-catchを書かずに済ませるためのラッパークラス.<br>
 * インスタンス生成時にリフレクションを実行し、成功判定を保存する。<br>
 * 呼び出す側は、{@link #isEnabled()}を事前に使用し、{@link java.lang.reflect.Method}取得に成功している場合はメソッドを使用できる。
 */
public class MethodReflectWrapper {

    private final Method method;
    private final boolean enabled;

    public MethodReflectWrapper(Class<?> cls, String methodName, Class<?>... parameterTypes) {
        Method method = null;
        boolean enabled = false;
        try {
            method = cls.getDeclaredMethod(methodName, parameterTypes);
            method.setAccessible(true);
            enabled = true;
        } catch (NoSuchMethodException e) {
            log(e);
        }
        this.method = method;
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public Object invoke(Object receiver, Object... args) {
        try {
            return method.invoke(receiver, args);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    private void log(Exception e) {
        Log.e("MethodReflectWrapper", e.getMessage());
    }

}
