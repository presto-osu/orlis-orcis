/*
 * Copyright (c) 2014. FarrelltonSolar
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
 */

package ca.farrelltonsolar.classic;

import android.text.InputType;
import android.text.Spanned;
import android.text.method.NumberKeyListener;

public class IPAddressKeyListener extends NumberKeyListener {

    private char[] mAccepted;
    private static IPAddressKeyListener sInstance;

    @Override
    protected char[] getAcceptedChars() {
        return mAccepted;
    }

    /**
     * The characters that are used.
     */
    private static final char[] CHARACTERS = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '.'};

    private IPAddressKeyListener() {
        mAccepted = CHARACTERS;
    }

    /**
     * Returns a IPAddressKeyListener that accepts the digits 0 through 9, plus the dot
     * character, subject to IP socketAddress rules: the first character has to be a digit, and
     * no more than 3 dots are allowed.
     */
    public static IPAddressKeyListener getInstance() {
        if (sInstance != null)
            return sInstance;

        sInstance = new IPAddressKeyListener();
        return sInstance;
    }

    /**
     * Display a number-only soft keyboard.
     */
    public int getInputType() {
        return InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL;
    }

    /**
     * Filter out unacceptable dot characters.
     */
    @Override
    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart,
                               int dend) {
        if (end > start) {
            String destTxt = dest.toString();
            String resultingTxt = destTxt.substring(0, dstart) + source.subSequence(start, end) + destTxt.substring(dend);
            if (!resultingTxt.matches("^\\d{1,3}(\\.(\\d{1,3}(\\.(\\d{1,3}(\\.(\\d{1,3})?)?)?)?)?)?")) {
                return "";
            } else {
                String[] splits = resultingTxt.split("\\.");
                for (int i = 0; i < splits.length; i++) {
                    if (Integer.valueOf(splits[i]) > 255) {
                        return "";
                    }
                }
            }
        }
        return null;

    }
}
