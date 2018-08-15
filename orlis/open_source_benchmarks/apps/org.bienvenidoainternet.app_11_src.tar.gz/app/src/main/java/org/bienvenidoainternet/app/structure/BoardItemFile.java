package org.bienvenidoainternet.app.structure;

import android.os.Parcel;
import android.os.Parcelable;

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
public class BoardItemFile implements Parcelable{
    public String file;
    public String fileURL;
    public String boardDir;

    public BoardItemFile(String fileURL, String file, String boardDir){
        this.fileURL = fileURL;
        this.file = file;
        this.boardDir = boardDir;
    }

    protected BoardItemFile(Parcel in) {
        file = in.readString();
        fileURL = in.readString();
        boardDir = in.readString();
    }

    public static final Creator<BoardItemFile> CREATOR = new Creator<BoardItemFile>() {
        @Override
        public BoardItemFile createFromParcel(Parcel in) {
            return new BoardItemFile(in);
        }

        @Override
        public BoardItemFile[] newArray(int size) {
            return new BoardItemFile[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(file);
        dest.writeString(fileURL);
        dest.writeString(boardDir);
    }
}
