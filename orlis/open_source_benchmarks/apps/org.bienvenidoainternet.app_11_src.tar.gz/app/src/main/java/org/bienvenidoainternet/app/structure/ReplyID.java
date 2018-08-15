package org.bienvenidoainternet.app.structure;

import android.graphics.Color;
import android.os.Parcel;
import android.os.Parcelable;

import org.bienvenidoainternet.app.ThemeManager;

import java.util.Random;

/**
 *   BaiApp - Bienvenido a internet Android Application
 *   Copyright (C) 2016 Renard1911(https://github.com/Renard1911)
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
public class ReplyID implements Parcelable{
    public String id;
    public int color;
    public ReplyID(String id, ThemeManager tm){
        this.id = id;
        Random r = new Random();
        if (tm.isDarkTheme()){
            this.color = Color.rgb(r.nextInt(125) + 127, r.nextInt(127) + 127, r.nextInt(127) + 127);
        }else{
            this.color = Color.rgb(r.nextInt(125), r.nextInt(127), r.nextInt(127));
        }
    }

    protected ReplyID(Parcel in) {
        id = in.readString();
        color = in.readInt();
    }

    public static final Creator<ReplyID> CREATOR = new Creator<ReplyID>() {
        @Override
        public ReplyID createFromParcel(Parcel in) {
            return new ReplyID(in);
        }

        @Override
        public ReplyID[] newArray(int size) {
            return new ReplyID[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeInt(color);
    }
}
