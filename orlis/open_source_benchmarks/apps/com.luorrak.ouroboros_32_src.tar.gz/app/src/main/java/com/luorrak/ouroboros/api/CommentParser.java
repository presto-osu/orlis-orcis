package com.luorrak.ouroboros.api;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.BackgroundColorSpan;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StrikethroughSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.view.View;

import com.luorrak.ouroboros.R;
import com.luorrak.ouroboros.thread.CardDialogFragment;
import com.luorrak.ouroboros.thread.ExternalNavigationWarningFragment;
import com.luorrak.ouroboros.thread.InterThreadNavigationWarningFragment;
import com.luorrak.ouroboros.util.InfiniteDbHelper;
import com.luorrak.ouroboros.util.SpoilerSpan;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;

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


public class CommentParser {
    public final static int CATALOG_VIEW = 0;
    public final static int THREAD_VIEW = 1;

    public Spannable parseId(String id){
        SpannableString coloredId = new SpannableString(id);
        coloredId.setSpan(new ForegroundColorSpan(Color.parseColor("#" + id)), 0, coloredId.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return coloredId;
    }

    /* Example formatting
        <p class="body-line ltr ">normal text</p>
        <p class="body-line ltr "><span class="heading">Red Text</span></p>
        <p class="body-line ltr quote">&gt;Green Text</p>
        <p class="body-line ltr "><span class="spoiler">Spoiler Text</span></p>
        <p class="body-line ltr "><em>Italic Text</em></p>
        <p class="body-line ltr "><strong>Bold Text</strong></p>
        <p class="body-line ltr "><u>Under Line</u></p>
        <p class="body-line ltr "><s>Strike-through</s></p>
        <p class="body-line ltr "><span class="aa">Escaped text</span></p>
        <p class="body-line ltr ">
            <code>
                <pre class='prettyprint' style='display:inline-block'>codeblock</pre>
            </code>
        </p>
    */

    /*
    JSoup making it this for some reason
    <html>
     <head></head>
     <body>
      <p class="body-line ltr ">normal text</p>
      <p class="body-line ltr "><span class="heading">Red Text</span></p>
      <p class="body-line ltr quote">&gt;Green Text</p>
      <p class="body-line ltr "><span class="spoiler">Spoiler Text</span></p>
      <p class="body-line ltr "><em>Italic Text</em></p>
      <p class="body-line ltr "><strong>Bold Text</strong></p>
      <p class="body-line ltr "><u>Under Line</u></p>
      <p class="body-line ltr "><s>Strike-through</s></p>
      <p class="body-line ltr "><span class="aa">Escaped text</span></p>
      <p class="body-line ltr "><code></code></p>
      <pre class="prettyprint" style="display:inline-block"><code>codeblock</code></pre>
      <p></p>
     </body>
    </html>

     <p class="body-line empty ">
     <p class=\"body-line ltr \"><a onclick=\"highlightReply('22543', event);\" href=\"\/test\/res\/22543.html#22543\">&gt;&gt;22543<\/a><\/p>
     <p class=\"body-line ltr \"><a href=\"\/irc\/res\/468.html#468\">&gt;&gt;&gt;\/irc\/468<\/a><\/p>
     <p class=\"body-line ltr \"><a href=\"https:\/\/www.ixquick.com\/\" rel=\"nofollow\" target=\"_blank\">https:\/\/www.ixquick.com\/<\/a><\/p>
     */

    public Spannable parseCom(String rawCom, int viewState, String currentBoard, String resto, FragmentManager fragmentManager, InfiniteDbHelper infiniteDbHelper){
        CharSequence processedText = new SpannableString("");
        Document doc = Jsoup.parse(rawCom);
        int parseLimit = 4;
        int limit = 0;

        if (doc.select("p").size() == 0) {
            return new SpannableString("LEGACY COMMENT SYSTEM!\n " + doc.body().text());
        } else {
            for (Element bodyLine : doc.body().children()){
                //This speeds up swiping on catalogview without risking an error
                if (viewState == CATALOG_VIEW && limit == parseLimit){
                    break;
                }
                limit++;

                if (bodyLine.className().equals("body-line ltr quote")){
                    processedText = TextUtils.concat(processedText, parseGreenText(new SpannableString(parseFormatting(bodyLine, currentBoard, resto, fragmentManager, infiniteDbHelper))));
                    processedText = TextUtils.concat(processedText, "\n");
                } else if (bodyLine.className().equals("body-line ltr")){
                    if (bodyLine.children().size() == 0){
                        //Normal Text
                        processedText = TextUtils.concat(processedText, parseNormalText(new SpannableString(bodyLine.text())));
                    } else {
                        processedText = TextUtils.concat(processedText, parseFormatting(bodyLine, currentBoard, resto, fragmentManager, infiniteDbHelper));
                    }
                    processedText = TextUtils.concat(processedText, "\n");
                } else if (bodyLine.className().equals("body-line empty")){
                    processedText = TextUtils.concat(processedText, "\n");
                } else if (bodyLine.tagName().equals("pre")){
                    processedText = TextUtils.concat(processedText, parseCodeText(bodyLine));
                }

            }
        }

        //trim trailing newline.
        if (processedText.length() > 0 ){
            processedText = processedText.subSequence(0, processedText.length() - 1);
        }
        return SpannableStringBuilder.valueOf(processedText);
    }

    //nested switch statement
    private CharSequence parseSpanText(Element child){
        CharSequence spanText = new SpannableString("");
        switch (child.className()){
            case "heading":
                spanText = parseHeadingText(new SpannableString(child.text()));
                break;
            case "spoiler":
                spanText =  parseSpoilerText(new SpannableString(child.text()));
                break;
            case "aa":
                spanText =  parseEscapedText(new SpannableString(child.text()));
                break;
            case "tex":
                spanText = parseTexText(new SpannableString(child.text()));
                break;
        }
        return spanText;
    }


    private CharSequence parseFormatting(Element bodyLine, String currentBoard, String resto, FragmentManager fragmentManager, InfiniteDbHelper infiniteDbHelper){
        CharSequence parsedText = "";
        for (Node childNode : bodyLine.childNodes()){
            if (childNode instanceof TextNode){
                parsedText = TextUtils.concat(parsedText, parseNormalText(new SpannableString(((TextNode) childNode).text())));
            } else if (childNode instanceof Element){
                Element childElement = (Element) childNode;
                switch(childElement.tagName()){
                    default:
                        parsedText = TextUtils.concat(parsedText, parseNormalText(new SpannableString(childElement.text())));
                        break;
                    case "span":
                        CharSequence spanText = parseSpanText(childElement);
                        parsedText = TextUtils.concat(parsedText, spanText);
                        break;
                    case "em":
                        parsedText = TextUtils.concat(parsedText, parseItalicText(new SpannableString(childElement.text())));
                        break;
                    case "strong":
                        parsedText = TextUtils.concat(parsedText, parseBoldText(new SpannableString(childElement.text())));
                        break;
                    case "u":
                        parsedText = TextUtils.concat(parsedText, parseUnderlineText(new SpannableString(childElement.text())));
                        break;
                    case "s":
                        parsedText = TextUtils.concat(parsedText, parseStrikethroughText(new SpannableString(childElement.text())));
                        break;
                    case "a":
                        parsedText = TextUtils.concat(parsedText, parseAnchorText(childElement, currentBoard, resto, fragmentManager, infiniteDbHelper));
                }
            }
        }
        return parsedText;
    }
    private CharSequence parseNormalText(SpannableString normalText){
       return normalText;
    }

    private CharSequence parseGreenText(SpannableString greenText){
        greenText.setSpan(new ForegroundColorSpan(Color.parseColor("#789922")), 0, greenText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return greenText;
    }

    private CharSequence parseSpoilerText(SpannableString spoilerText){
        SpoilerSpan spoilerSpan = new SpoilerSpan();
        spoilerText.setSpan(spoilerSpan, 0, spoilerText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spoilerText;
    }

    private CharSequence parseItalicText(SpannableString italic){
        italic.setSpan(new StyleSpan(Typeface.ITALIC), 0, italic.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return italic;
    }

    private CharSequence parseBoldText(SpannableString bold){
        bold.setSpan(new StyleSpan(Typeface.BOLD), 0, bold.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return bold;
    }

    private CharSequence parseUnderlineText(SpannableString underline) {
        underline.setSpan(new UnderlineSpan(), 0, underline.length(), 0);
        return underline;
    }

    private CharSequence parseHeadingText(SpannableString heading){
        heading.setSpan(new ForegroundColorSpan(Color.RED), 0, heading.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        heading.setSpan(new StyleSpan(Typeface.BOLD), 0, heading.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return heading;
    }

    private CharSequence parseStrikethroughText(SpannableString strikethrough){
        strikethrough.setSpan(new StrikethroughSpan(), 0, strikethrough.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return strikethrough;
    }

    private CharSequence parseEscapedText(SpannableString escapedText){
        return escapedText;
    }

    private CharSequence parseTexText(SpannableString escapedText){
        return escapedText;
    }

    private CharSequence parseCodeText(Element codeElement){
        Element preElement = codeElement.child(0);
        SpannableString codeText = new SpannableString("\n" + preElement.text() + "\n");
        codeText.setSpan(new BackgroundColorSpan(Color.LTGRAY), 0, codeText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return codeText;
    }

    private CharSequence parseAnchorText(final Element anchor, final String currentBoard, final String resto,  final FragmentManager fragmentManager, InfiniteDbHelper infiniteDbHelper) {
        final String linkUrl = anchor.attr("href");
        SpannableString linkText = new SpannableString(anchor.text());
        if (linkUrl.contains("http")) {
            //normal link
            ClickableSpan clickableNormalLink = new ClickableSpan() {
                @Override
                public void onClick(View widget) {
                    ExternalNavigationWarningFragment dialog = ExternalNavigationWarningFragment.newInstance(linkUrl);
                    dialog.show(fragmentManager, "externallink");
                }
            };
            linkText.setSpan(new ForegroundColorSpan(Color.parseColor("#0645AD")), 0, linkText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            linkText.setSpan(clickableNormalLink, 0, linkText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            return linkText;
        }  else if (linkUrl.contains("_g")){
            //normal link
            ClickableSpan clickableNormalLink = new ClickableSpan() {
                @Override
                public void onClick(View widget) {
                    ExternalNavigationWarningFragment dialog = ExternalNavigationWarningFragment.newInstance(anchor.text());
                    dialog.show(fragmentManager, "externallink");
                }
            };
            linkText.setSpan(new ForegroundColorSpan(Color.parseColor("#0645AD")), 0, linkText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            linkText.setSpan(clickableNormalLink, 0, linkText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            return linkText;
        } else if (linkUrl.contains(resto)){
            //same thread
            if (infiniteDbHelper.isNoUserPost(currentBoard, linkUrl.split("#")[1])){
                linkText = SpannableString.valueOf(TextUtils.concat(linkText, " (You)"));
            } else if (linkUrl.split("#")[1].equals(resto)){
                linkText = SpannableString.valueOf(TextUtils.concat(linkText, " (OP)"));
            }
            ClickableSpan clickableSameThreadLink = new ClickableSpan() {
                @Override
                public void onClick(View widget) {
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    CardDialogFragment cardDialogFragment = CardDialogFragment.showPost(linkUrl, currentBoard);
                    fragmentTransaction.add(R.id.placeholder_card, cardDialogFragment)
                            .addToBackStack("threadDialog")
                            .commit();

                }
            };
            linkText.setSpan(clickableSameThreadLink, 0, linkText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            linkText.setSpan(new ForegroundColorSpan(Color.parseColor("#FF6600")), 0, linkText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            return linkText;

        } else {
            //different thread
            ClickableSpan clickableDifferentThreadLink = new ClickableSpan() {
                @Override
                public void onClick(View widget) {
                    InterThreadNavigationWarningFragment dialog = InterThreadNavigationWarningFragment.newInstance(linkUrl);
                    dialog.show(fragmentManager, "internallink");
                }
            };
            linkText.setSpan(clickableDifferentThreadLink, 0, linkText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            linkText.setSpan(new ForegroundColorSpan(Color.parseColor("#FF6600")), 0, linkText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            return linkText;
        }
    }
}
