package org.itishka.pointim.utils;

import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;

import org.itishka.pointim.widgets.spans.LinkClickableSpan;
import org.itishka.pointim.widgets.spans.PostClickableSpan;
import org.itishka.pointim.widgets.spans.UserClickableSpan;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Tishka17 on 17.01.2015.
 */
public class TextLinkify {
    private static final Pattern nickPattern = Pattern.compile("(?<=^|[:(>\\s])@([\\w-]+)");// @nick
    private static final Pattern postNumberPattern = Pattern.compile("(?<=^|[:(>\\s])#(\\w+)(?>/(\\d+))?");// #post/comment

    private static final Pattern markdownLinkPattern = Pattern.compile("(?<=^|[:(>\\s])\\[([^\\]]+)\\]\\(([^)\"]+)(\"([^\"]+)\")?\\)");// [title](link "hint")
    private static final Pattern markdownSimpleLinkPattern = Pattern.compile("<([^>]*)>");

    public static Spannable addLinks(Spannable text) {
        Spannable spannable = new SpannableString(text);
        android.text.util.Linkify.addLinks(spannable, android.text.util.Linkify.ALL);
        return spannable;
    }

    public static Spannable markNicks(Spannable text) {
        Matcher m = nickPattern.matcher(text);
        while (m.find()) {
            StyleSpan b = new StyleSpan(android.graphics.Typeface.BOLD);
            text.setSpan(b, m.start(), m.end(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            UserClickableSpan span = new UserClickableSpan(m.group(1));
            text.setSpan(span, m.start(), m.end(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        }
        return text;
    }

    public static Spannable markPostNumbers(Spannable text) {
        Matcher m = postNumberPattern.matcher(text);
        while (m.find()) {
            StyleSpan b = new StyleSpan(android.graphics.Typeface.BOLD);
            text.setSpan(b, m.start(), m.end(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            PostClickableSpan span = new PostClickableSpan(m.group(1), m.group(2));
            text.setSpan(span, m.start(), m.end(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        }
        return text;
    }

    public static Spannable markMarkdownLinks(Spannable text) {
        text = markSimpleMarkdownLinks(text);//parse <> links before main
        Matcher m = markdownLinkPattern.matcher(text);
        Editable newText = new Editable.Factory().newEditable(text);
        int delta = 0;
        while (m.find()) {
            Spannable link = new SpannableStringBuilder(m.group(1));
            LinkClickableSpan span = new LinkClickableSpan(m.group(2));
            link.setSpan(span, 0, link.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            link.setSpan(new UnderlineSpan(), 0, link.length(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
            link.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, link.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            newText.replace(m.start() - delta, m.end() - delta, link);
            delta += (m.end() - m.start() - link.length());
        }
        return newText;
    }

    private static Spannable markSimpleMarkdownLinks(Spannable text) {
        Matcher m = markdownSimpleLinkPattern.matcher(text);
        Editable newText = new Editable.Factory().newEditable(text);
        int delta = 0;
        while (m.find()) {
            Spannable link = new SpannableStringBuilder(m.group(1));
            LinkClickableSpan span = new LinkClickableSpan(m.group(1));
            link.setSpan(span, 0, link.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            link.setSpan(new UnderlineSpan(), 0, link.length(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
            link.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, link.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            newText.replace(m.start() - delta, m.end() - delta, link);
            delta += (m.end() - m.start() - link.length());
        }
        return newText;
    }
}
