/**
 * Copyright (C) 2013 Damien Chazoule
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
 */

package com.doomy.overflow;

public class Message {

    private static int mCount = 1;
    private int ID;
    private String Recipient;
    private int Color;
    private String Text;
    private String Quantity;

    public Message() {
        super();
    }

    public Message (String mRecipient, int mColor, String mText, String mQuantity) {
        this.ID = Message.mCount++;
        this.Recipient = mRecipient;
        this.Color = mColor;
        this.Text = mText;
        this.Quantity = mQuantity;
    }

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public String getRecipient() {
        return Recipient;
    }

    public void setRecipient(String recipient) {
        Recipient = recipient;
    }

    public int getColor() {
        return Color;
    }

    public void setColor(int color) {
        Color = color;
    }

    public String getText() {
        return Text;
    }

    public void setText(String text) {
        Text = text;
    }

    public String getQuantity() {
        return Quantity;
    }

    public void setQuantity(String quantity) {
        Quantity = quantity;
    }

    @Override
    public String toString() {
        return "Contact [ID : " + ID + ", Recipient : " + Recipient + "]";
    }
}
