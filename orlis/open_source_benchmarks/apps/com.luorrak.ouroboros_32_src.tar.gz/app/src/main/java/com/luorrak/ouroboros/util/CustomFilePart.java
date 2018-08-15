package com.luorrak.ouroboros.util;

import com.koushikdutta.async.http.BasicNameValuePair;
import com.koushikdutta.async.http.NameValuePair;
import com.koushikdutta.async.http.body.StreamPart;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Ouroboros - An 8chan browser
 * Copyright (C) 2015  Luorrak
 * <p/>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p/>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

public class CustomFilePart extends StreamPart {
    File file;
    public CustomFilePart(String name, final File file) {
        super(name, (int)file.length(), new ArrayList<NameValuePair>() {
            {
                add(new BasicNameValuePair("filename", file.getName()));
            }
        });

        List<String> validExt = Arrays.asList(".png", ".jpg", ".jpeg", ".gif");
        String filePath = file.getAbsolutePath();
        String extension = filePath.substring(filePath.lastIndexOf("."));
        String contentType = (validExt.contains(extension)) ? "image/*" : "video/*";
        getRawHeaders().set("Content-Type", contentType);

        this.file = file;
    }

    @Override
    protected InputStream getInputStream() throws IOException {
        return new FileInputStream(file);
    }
}