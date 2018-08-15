/* Copyright (c) 2015 Chris Nguyen
**
** Permission to use, copy, modify, and/or distribute this software for
** any purpose with or without fee is hereby granted, provided that the
** above copyright notice and this permission notice appear in all copies.
**
** THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL
** WARRANTIES WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED
** WARRANTIES OF MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR
** BE LIABLE FOR ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES
** OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS,
** WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION,
** ARISING OUT OF OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS
** SOFTWARE.
*/
package com.jmstudios.redmoon.helper;

import android.content.Intent;

import com.jmstudios.redmoon.service.ScreenFilterService;

/**
 * Helper class that encapsulates the logic to parse an {@link Intent} that was created by
 * {@link FilterCommandFactory} and sent to {@link ScreenFilterService}.
 */
public class FilterCommandParser {

    /**
     * Retrieves the command in an intent sent to {@link ScreenFilterService}.
     *
     * @param intent that was constructed by {@link FilterCommandFactory}.
     * @return one of {@link ScreenFilterService#COMMAND_OFF}, {@link ScreenFilterService#COMMAND_ON},
     *         {@link ScreenFilterService#COMMAND_PAUSE}, or -1 if {@code intent} doesn't contain a
     *         valid command.
     */
    public int parseCommandFlag(Intent intent) {
        int errorCode = -1;

        if (intent == null) {
            return errorCode;
        }

        int commandFlag = intent.getIntExtra(ScreenFilterService.BUNDLE_KEY_COMMAND, errorCode);
        return commandFlag;
    }
}
