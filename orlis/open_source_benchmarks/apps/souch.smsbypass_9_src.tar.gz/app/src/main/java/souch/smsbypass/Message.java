/*
 * SMS-bypass - SMS bypass for Android
 * Copyright (C) 2015  Mathieu Souchaud
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Forked from smsfilter (author: Jelle Geerts).
 */

package souch.smsbypass;

public class Message
{
    static final long MSG_TYPE_RECEIVED = 0;
    static final long MSG_TYPE_SENT = 1;
    static final long MSG_TYPE_DRAFT = 2;

    long id;
    String filter;
    String address;
    long receivedAt;
    long type;
    String message;

    Message(long id, String filter, String address, long receivedAt, long type, String message)
    {
        this.id = id;
        this.filter = filter;
        this.address = address;
        this.receivedAt = receivedAt;
        this.type = type;
        this.message = message;
    }
}
