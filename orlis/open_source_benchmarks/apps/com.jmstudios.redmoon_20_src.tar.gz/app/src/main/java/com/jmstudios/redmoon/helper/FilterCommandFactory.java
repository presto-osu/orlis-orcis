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

import android.content.Context;
import android.content.Intent;

import com.jmstudios.redmoon.service.ScreenFilterService;

/**
 * Factory class to construct a valid {@link Intent} commands that can be sent to
 * {@link com.jmstudios.redmoon.service.ScreenFilterService}.
 *
 * <p>Use {@link FilterCommandSender} to execute the constructed commands.
 */
public class FilterCommandFactory {

    private Context mContext;

    public FilterCommandFactory(Context context) {
        mContext = context;
    }

    /**
     *
     * @param screenFilterServiceCommand one of {@link ScreenFilterService#COMMAND_OFF},
     *        {@link ScreenFilterService#COMMAND_ON}, or {@link ScreenFilterService#COMMAND_PAUSE}.
     * @return an Intent containing a command that can be sent to {@link ScreenFilterService} via
     *         {@link FilterCommandSender#send(Intent)}; null if
     *         {@code screenFilterServiceCommand} is invalid.
     */
    public Intent createCommand(int screenFilterServiceCommand) {
        Intent command;

        if (screenFilterServiceCommand < ScreenFilterService.VALID_COMMAND_START ||
            screenFilterServiceCommand > ScreenFilterService.VALID_COMMAND_END) {
            command = null;
        } else {
            command = new Intent(mContext, ScreenFilterService.class);
            command.putExtra(ScreenFilterService.BUNDLE_KEY_COMMAND, screenFilterServiceCommand);
        }

        return command;
    }
}
