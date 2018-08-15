package com.luorrak.ouroboros.util;

import android.net.Uri;

/**
 * Ouroboros - An 8chan browser
 * Copyright (C) 2015  Luorrak
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
public class ChanUrls {

    private static final String SCHEME = "https";
    private static final String DOMAIN_NAME = "8ch.net";
    private static final String POST_SUBDOMAIN_NAME = "sys.8ch.net";
    private static final String CATALOG_ENDPOINT = "catalog.json"; //http(s)://siteurl/board/catalog.json
    private static final String THREAD_FOLDER = "res"; //http(s):///siteurl/board/res/threadnumber.json
    private static final String THREAD_ENDPOINT = ".json";
    private static final String IMAGE_THUMBNAIL_DIRECTORY = "thumb";
    private static final String IMAGE_DIRECTORY = "src"; //http(s)://siteurl/board/src/tim.ext
    private static final String POST_ENDPOINT = "post.php";
    private static final String DNSBL_ENDPOINT = "dnsbls_bypass.php"; //https://8ch.net/dnsbls_bypass.php

    public static String getCatalogUrl(String boardName){
        Uri.Builder builder = new Uri.Builder();
        builder.scheme(SCHEME)
                .authority(DOMAIN_NAME)
                .appendPath(boardName)
                .appendPath(CATALOG_ENDPOINT)
                .build();
        return builder.toString();
    }

    public static String getCatalogUrlExternal(String boardName){
        Uri.Builder builder = new Uri.Builder();
        builder.scheme(SCHEME)
                .authority(DOMAIN_NAME)
                .appendPath(boardName)
                .appendPath("catalog.html")
                .build();
        return builder.toString();
    }
    public static String getThreadUrl(String boardName, String no){
        Uri.Builder builder = new Uri.Builder();
        builder.scheme(SCHEME)
                .authority(DOMAIN_NAME)
                .appendPath(boardName)
                .appendPath(THREAD_FOLDER)
                .appendPath(no + THREAD_ENDPOINT)
                .build();
        return builder.toString();
    }

    public static String getThreadUrlExternal(String boardName, String no){
        Uri.Builder builder = new Uri.Builder();
        builder.scheme(SCHEME)
                .authority(DOMAIN_NAME)
                .appendPath(boardName)
                .appendPath(THREAD_FOLDER)
                .appendPath(no + ".html")
                .build();
        return builder.toString();
    }

    public static String getThumbnailUrl(String boardName, String tim){
        Uri.Builder builder = new Uri.Builder();
        builder.scheme(SCHEME)
                .authority(DOMAIN_NAME)
                .appendPath(boardName)
                .appendPath(IMAGE_THUMBNAIL_DIRECTORY)
                .appendPath(tim + ".jpg")
                .build();
        return builder.toString();
    }

    public static String getImageUrl(String boardName, String tim, String ext){
        Uri.Builder builder = new Uri.Builder();
        builder.scheme(SCHEME)
                .authority(DOMAIN_NAME)
                .appendPath(boardName)
                .appendPath(IMAGE_DIRECTORY)
                .appendPath(tim + ext)
                .build();
        return builder.toString();
    }

    public static String getSpoilerUrl(){
        return SCHEME + "://8ch.net/static/spoiler.png";
    }

    public static String getReplyUrl(){
        Uri.Builder builder = new Uri.Builder();
        builder.scheme(SCHEME) //https://8ch.net/post.php
                .authority(POST_SUBDOMAIN_NAME)
                .appendPath(POST_ENDPOINT)
                .build();
        return builder.toString();
    }

    public static String getDeletePostUrl(){
        Uri.Builder builder = new Uri.Builder();
        builder.scheme(SCHEME) //https://8ch.net/post.php
                .authority(DOMAIN_NAME)
                .appendPath(POST_ENDPOINT)
                .build();
        return builder.toString();
    }

    public static String getThreadHtmlUrl(String boardName, String no){
        Uri.Builder builder = new Uri.Builder();
        builder.scheme(SCHEME) //http://8ch.net/test/res/7102.html
                .authority(DOMAIN_NAME)
                .appendPath(boardName)
                .appendPath(THREAD_FOLDER)
                .appendPath(no + ".html")
                .build();
        return builder.toString();
    }

    public static String getDnsblUrl(){
        Uri.Builder builder = new Uri.Builder();
        builder.scheme(SCHEME) //https://8ch.net/dnsbls_bypass.php
                .authority(DOMAIN_NAME)
                .appendPath(DNSBL_ENDPOINT)
                .build();
        return builder.toString();
    }

    public static String getCaptchaEntrypoint(){
        Uri.Builder builder = new Uri.Builder();
        builder.scheme(SCHEME) //http://8ch.net/8chan-captcha/entrypoint.php?mode=get&extra=abcdefghijklmnopqrstuvwxyz&nojs=true
                .authority(DOMAIN_NAME)
                .appendPath("8chan-captcha")
                .appendPath("entrypoint.php")
                .appendQueryParameter("mode", "get")
                .appendQueryParameter("extra", "abcdefghijklmnopqrstuvwxyz")
                .appendQueryParameter("nojs", "true")
                .build();
        return builder.toString();
    }

}
