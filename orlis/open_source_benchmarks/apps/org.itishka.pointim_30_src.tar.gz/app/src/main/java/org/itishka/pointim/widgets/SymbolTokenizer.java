package org.itishka.pointim.widgets;

import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.widget.MultiAutoCompleteTextView;

/**
 * Created by Tishka17 on 05.08.2015.
 */
public class SymbolTokenizer implements MultiAutoCompleteTextView.Tokenizer {
    final char symbol;

    public SymbolTokenizer(char symbol) {
        this.symbol = symbol;
    }

    public int findTokenStart(CharSequence text, int cursor) {
        int i = cursor;

        while (i > 0 && text.charAt(i - 1) != symbol) {
            i--;
        }
        while (i < cursor && text.charAt(i) == symbol) {
            i++;
        }

        return i;
    }

    public int findTokenEnd(CharSequence text, int cursor) {
        int i = cursor;
        int len = text.length();

        while (i < len) {
            if (text.charAt(i) == symbol) {
                return i;
            } else {
                i++;
            }
        }

        return len;
    }

    public CharSequence terminateToken(CharSequence text) {
        int i = text.length();

        while (i > 0 && text.charAt(i - 1) == symbol) {
            i--;
        }

        if (i > 0 && text.charAt(i - 1) == symbol) {
            return text;
        } else {
            if (text instanceof Spanned) {
                SpannableString sp = new SpannableString(text + " ");
                TextUtils.copySpansFrom((Spanned) text, 0, text.length(),
                        Object.class, sp, 0);
                return sp;
            } else {
                return text + " ";
            }
        }
    }
}
