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

import android.content.Context;

public class DimenUtil {

    private DimenUtil() {
    }

    public static float getDensity(Context context) {
        return context.getResources().getDisplayMetrics().density;
    }

    public static int getPixelFromDp(Context context, int dp) {
        // density (比率)を取得する
        float density = context.getResources().getDisplayMetrics().density;
        // 50 dp を pixel に変換する ( dp × density + 0.5f（四捨五入) )
        return (int) (dp * density + 0.5f);
    }
}
