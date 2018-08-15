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

public class Contact {

    private static int mCount;
    private int ID;
    private String FullName;
    private String PhoneNumber;
    private int ColorDark;

    public Contact() {};

    public Contact (String mFullName, String mPhoneNumber, int mColorDark) {
        this.ID = Contact.mCount++;
        this.FullName = mFullName;
        this.PhoneNumber = mPhoneNumber;
        this.ColorDark = mColorDark;
    }

    public String getFullName() {
        return FullName;
    }

    public void setFullName(String fullName) {
        FullName = fullName;
    }

    public String getPhoneNumber() {
        return PhoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        PhoneNumber = phoneNumber;
    }

    public int getColorDark() {
        return ColorDark;
    }

    public void setColorDark(int colorDark) {
        ColorDark = colorDark;
    }

    @Override
    public String toString() {
        return "Contact [ID : " + ID + ", FullName : " + FullName + ", PhoneNumber : " + PhoneNumber + "]";
    }
}
