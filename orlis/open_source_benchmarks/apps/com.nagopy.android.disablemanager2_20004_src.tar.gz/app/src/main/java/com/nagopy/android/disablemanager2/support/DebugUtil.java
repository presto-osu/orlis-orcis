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

/**
 * デバッグ用クラス.<br>
 * このクラスのメソッドは全て、リリースビルド時にProguardで削除する。
 */
public class DebugUtil {

    private DebugUtil() {
    }


    /**
     * ログ出力.<br>
     * リリースビルドではProguardで削除する。
     *
     * @param msg メッセージ
     */
    public static void verboseLog(String msg) {
        Log.v("Disable Manager", msg);
    }

    /**
     * デバッグログ出力.<br>
     * リリースビルドではProguardで削除する。
     *
     * @param msg メッセージ
     */
    public static void debugLog(String msg) {
        Log.d("Disable Manager", msg);
    }

    /**
     * 情報ログ出力.<br>
     * リリースビルドではProguardで削除する。
     *
     * @param msg メッセージ
     */
    public static void infoLog(String msg) {
        Log.i("Disable Manager", msg);
    }

    /**
     * エラーログ出力.<br>
     * リリースビルドではProguardで削除する。
     *
     * @param msg メッセージ
     */
    public static void errorLog(String msg) {
        Log.e("Disable Manager", msg);
    }
}
