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

public class Board implements Parcelable{
    private String boardName, boardDir;
    private int boardType;

    public boolean isCanAttachFiles() {
        return canAttachFiles;
    }

    public void setCanAttachFiles(boolean canAttachFiles) {
        this.canAttachFiles = canAttachFiles;
    }

    private boolean canAttachFiles;
    public Board(String boardName, String boardDir, int boardType, boolean canAttachFiles){
        this.boardName = boardName;
        this.boardDir = boardDir;
        this.boardType = boardType;
        this.canAttachFiles = canAttachFiles;
    }

    public Board(Parcel in){
        this.boardName = in.readString();
        this.boardDir = in.readString();
        this.boardType = in.readInt();
        this.canAttachFiles = in.readByte() != 0;
    }

    public String getBoardDir() {
        return boardDir;
    }

    public String getBoardName() {
        return boardName;
    }

    public int getBoardType() {
        return boardType;
    }

    public static final Creator<Board> CREATOR = new Creator<Board>() {
        @Override
        public Board createFromParcel(Parcel in) {
            return new Board(in);
        }

        @Override
        public Board[] newArray(int size) {
            return new Board[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(boardName);
        dest.writeString(boardDir);
        dest.writeInt(boardType);
        dest.writeByte((byte)(canAttachFiles ? 1 : 0));
    }
}
