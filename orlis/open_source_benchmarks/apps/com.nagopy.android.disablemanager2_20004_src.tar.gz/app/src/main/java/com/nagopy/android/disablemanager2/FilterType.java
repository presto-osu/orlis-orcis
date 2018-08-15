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
package com.nagopy.android.disablemanager2;

public enum FilterType {

    DISABLABLE(R.string.title_disablable) {
        @Override
        public boolean isTarget(AppData appData) {
            return appData.isSystem && appData.isEnabled && appData.isDisableable && appData.process.isEmpty();
        }
    }, DISABLABLE_RUNNING(R.string.title_disablable_running) {
        @Override
        public boolean isTarget(AppData appData) {
            return appData.isSystem && appData.isEnabled && appData.isDisableable && !appData.process.isEmpty();
        }
    }, DISABLED(R.string.title_disabled) {
        @Override
        public boolean isTarget(AppData appData) {
            return appData.isSystem && !appData.isEnabled;
        }
    }, UNDISABLABLE(R.string.title_undisablable) {
        @Override
        public boolean isTarget(AppData appData) {
            return appData.isSystem && appData.isEnabled && !appData.isDisableable;
        }
    }, USER(R.string.title_user) {
        @Override
        public boolean isTarget(AppData appData) {
            return !appData.isSystem;
        }
    };

    public final int titleId;

    private FilterType(int titleId) {
        this.titleId = titleId;
    }

    public static FilterType indexOf(int index){
        return values()[index];
    }

    public abstract boolean isTarget(AppData appData);
}
