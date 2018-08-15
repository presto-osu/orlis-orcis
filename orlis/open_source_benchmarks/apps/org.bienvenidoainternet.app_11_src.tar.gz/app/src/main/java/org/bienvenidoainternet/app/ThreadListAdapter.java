package org.bienvenidoainternet.app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.Layout;
import android.text.Spannable;
import android.text.style.ClickableSpan;
import android.text.style.URLSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.bienvenidoainternet.app.structure.BoardItem;
import org.bienvenidoainternet.app.structure.BoardItemFile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

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

public class ThreadListAdapter extends ArrayAdapter<BoardItem>{
    private Context context;
    private ThemeManager tm;
    Typeface monaFont;
    public boolean listThreads = false;
    private ArrayList<BoardItem> boardItems = new ArrayList<BoardItem>();
    private static final String EXTRA_FILELIST = "fileList";

    public ThreadListAdapter(Context context, List<BoardItem> objects, ThemeManager tm) {
        super(context, 0, objects);
        this.context = context;
        this.tm = tm;
        monaFont = Typeface.createFromAsset(context.getAssets(), "fonts/mona.ttf");
    }

    public void updateBoardItems(ArrayList<BoardItem> boardItems){
        this.boardItems = boardItems;
    }

    private String intToHexString(int i){
        return String.format("#%06X", (0xFFFFFF & i));
    }
    public static Map<TimeUnit,Long> computeDiff(Date date1, Date date2) {
        long diffInMillies = date2.getTime() - date1.getTime();
        List<TimeUnit> units = new ArrayList<TimeUnit>(EnumSet.allOf(TimeUnit.class));
        Collections.reverse(units);
        Map<TimeUnit,Long> result = new LinkedHashMap<TimeUnit,Long>();
        long milliesRest = diffInMillies;
        for ( TimeUnit unit : units ) {
            long diff = unit.convert(milliesRest,TimeUnit.MILLISECONDS);
            long diffInMilliesForUnit = unit.toMillis(diff);
            milliesRest = milliesRest - diffInMilliesForUnit;
            result.put(unit,diff);
        }
        return result;
    }
    @Override
    public View getView(final int position, final View convertView, final ViewGroup parent){
        LayoutInflater inflater = (LayoutInflater)getContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View listItemView = convertView;
        if (null == convertView) {
            listItemView = inflater.inflate(
                    R.layout.thread_item,
                    parent,
                    false);
        }

        final BoardItem boardItem = getItem(position);
        if (boardItem == null){
            return listItemView;
        }
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this.getContext());
        boolean useMonaFont = settings.getBoolean("setting_monafont", true);
        boolean monaBbsOnly = settings.getBoolean("setting_mona_bbsonly", true);
        boolean resizeThumb = settings.getBoolean("setting_resize_thumbs", true);
        int marginColor = tm.getMarginColor();
        int sageColor = tm.getSageColor();
        int nameColor = tm.getNameColor();
        int tripcodeColor = tm.getTripcodeColor();
        int quoteColor = tm.getQuoteColor();
        String hexColor =intToHexString(boardItem.getIdColor());
        String sageHexColor = intToHexString(sageColor);
        String nameHexColor = intToHexString(nameColor);
        String tripcodeHexColor = intToHexString(tripcodeColor);
        String quoteHexColor = intToHexString(quoteColor);
        String strId = "";

        TextView txtTitle = (TextView)listItemView.findViewById(R.id.lv_txtTitle);
        TextView txtPoster = (TextView)listItemView.findViewById(R.id.lv_txtPoster);
        TextView txtBody = (TextView) listItemView.findViewById(R.id.lv_txtBody);
        TextView txtReplies = (TextView) listItemView.findViewById(R.id.lv_txtReplyCounter);
        TextView txtFileInfo = (TextView) listItemView.findViewById(R.id.lv_txtFileInfo);
        ImageView ivMargin = (ImageView)listItemView.findViewById(R.id.ivMargin);
        ImageView ivThumb = (ImageView)listItemView.findViewById(R.id.ivThumb);

//        Log.v("resize", resizeThumb + "");
        if (resizeThumb){
            ivThumb.setScaleType(ImageView.ScaleType.FIT_XY);
        }else{
            ivThumb.setScaleType(ImageView.ScaleType.FIT_CENTER);
        }

        ivThumb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!boardItem.getThumb().isEmpty() && convertView != null){
                    if (boardItem.getFile().endsWith(".webm") || boardItem.getFile().endsWith(".ogg") || boardItem.getFile().endsWith(".opus") || boardItem.getFile().endsWith(".swf") || boardItem.youtubeLink){
                        Intent in;
                        if (boardItem.youtubeLink){
                            in = new Intent(Intent.ACTION_VIEW, Uri.parse(boardItem.youtubeURL));
                        }else{
                            in = new Intent(Intent.ACTION_VIEW, Uri.parse("http://bienvenidoainternet.org/" + boardItem.getParentBoard().getBoardDir() + "/src/" + boardItem.getFile()));
                        }
                        in.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        v.getContext().startActivity(in);
                    }else {
                        Intent in = new Intent(convertView.getContext(), ViewerActivity.class);
                        Bundle b = new Bundle();
                        ArrayList<BoardItemFile> fileList =  new ArrayList<BoardItemFile>();
                        int relativePosition = 0;
                        for (int i = 0; i < boardItems.size(); i++){
                            if (!boardItems.get(i).getFile().isEmpty()){
                                if (boardItems.get(i).getFile().equals(boardItem.getFile())){
                                    relativePosition = fileList.size();
                                }
                                fileList.add(new BoardItemFile("http://bienvenidoainternet.org/" + boardItems.get(i).getParentBoard().getBoardDir() + "/src/" + boardItems.get(i).getFile(), boardItems.get(i).getFile(), boardItems.get(i).getParentBoard().getBoardDir()));
                            }
                        }
                        b.putParcelableArrayList(EXTRA_FILELIST, fileList);
                        b.putInt("position", relativePosition);
                        in.putExtras(b);
                        convertView.getContext().startActivity(in);
                    }
                }
            }
        });


        ivMargin.setImageDrawable(new ColorDrawable(marginColor));

        if (useMonaFont){
            if (monaBbsOnly && boardItem.getParentBoard() != null){
                if (boardItem.getParentBoard().getBoardType() == 1){
                    txtBody.setTypeface(monaFont);
                }
            }else{
                txtBody.setTypeface(monaFont);
            }
        }

        // Si es una respuesta ocultamos el margen
        if (boardItem.isReply){
            ivMargin.setVisibility(View.VISIBLE);
            txtTitle.setVisibility(View.GONE);
        }else{
            txtTitle.setVisibility(View.VISIBLE);
            ivMargin.setVisibility(View.GONE);
            txtTitle.setText(boardItem.getSubject());
        }

        // Si el fragmento esta viendo un hilo ocultamos los margenes
        if (listThreads){
            ivMargin.setVisibility(View.GONE);
        }

        // Si el item está eliminado activamos el soporte de <strike>
        if (boardItem.getDeletedCode() != 0){
            txtBody.setPaintFlags(txtBody.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        }else{
            txtBody.setPaintFlags(txtBody.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
        }


        if (boardItem.getThumb().isEmpty()){
            ivThumb.setVisibility(View.GONE);
        }else{
            if (boardItem.getThumbBitmap() != null){
                ivThumb.setVisibility(View.VISIBLE);
                ivThumb.setImageBitmap(boardItem.getThumbBitmap());
            }else{
                ivThumb.setVisibility(View.VISIBLE);
                ivThumb.setImageResource(R.drawable.blank);
            }
        }

        Map<TimeUnit,Long> timeDiff = computeDiff(new Date(boardItem.getTimeStamp() * 1000L), new Date(System.currentTimeMillis()));
        String strTimeDiff = "";
        if (timeDiff.get(TimeUnit.SECONDS) != 0){
            strTimeDiff = "Hace " + timeDiff.get(TimeUnit.SECONDS) + (timeDiff.get(TimeUnit.SECONDS) == 1 ? " segundo" : " segundos");
        }
        if (timeDiff.get(TimeUnit.MINUTES) != 0){
            strTimeDiff = "Hace " + timeDiff.get(TimeUnit.MINUTES) + (timeDiff.get(TimeUnit.MINUTES) == 1 ? " minuto" : " minutos");
        }
        if (timeDiff.get(TimeUnit.HOURS) != 0){
            strTimeDiff = "Hace " + timeDiff.get(TimeUnit.HOURS) + (timeDiff.get(TimeUnit.HOURS) == 1 ? " hora" : " horas");
        }
        if (timeDiff.get(TimeUnit.DAYS) != 0){
            strTimeDiff = "Hace " + timeDiff.get(TimeUnit.DAYS) + (timeDiff.get(TimeUnit.DAYS) == 1 ? " día" : " días");
        }


        if (!boardItem.getPosterId().isEmpty() && !boardItem.getPosterId().equals("???")){
           strId = "<font color=" + hexColor + ">[" + boardItem.getPosterId() + "]</font> ";
        }

        // Si estamos mostrando un item de BBS, mostrar el ID_BBS en ves del ID del post
        int idToDisplay = 0;
        if (boardItem.getParentBoard() != null){
            if (boardItem.getParentBoard().getBoardType() == 1){
                idToDisplay = boardItem.getBbsId();
            }else{
                idToDisplay = boardItem.getId();
            }
        }else{
            idToDisplay = boardItem.getId();
        }

        txtPoster.setText(Html.fromHtml("<b>No. " + idToDisplay + "</b> por <font color=" + (boardItem.isSage() ? sageHexColor : nameHexColor) + ">" + boardItem.getName() + "</font> "
                + (boardItem.getTripcode() == "" ? "" : "<font color=" + tripcodeHexColor + ">" + boardItem.getTripcode() + "</font>") + strId + " " + strTimeDiff));
        String fixedMessage = boardItem.getMessage().replace("$_QUOTECOLOR_$", quoteHexColor);
        txtBody.setText(Html.fromHtml(fixedMessage));

        txtReplies.setVisibility(boardItem.isReply ? View.GONE : View.VISIBLE);
        txtReplies.setText(boardItem.getTotalReplies() + " respuestas " + (boardItem.getTotalFiles() == 0 ? "" : ", " + boardItem.getTotalFiles() + " archivos"));

        String fileExt = "";
        txtFileInfo.setVisibility(boardItem.getThumb().isEmpty() ? View.GONE : View.VISIBLE);

        if (!boardItem.getThumb().isEmpty() && boardItem.getThumb().startsWith("http")){
            txtFileInfo.setText("YOUTUBE");
        }else{
            if (!boardItem.getFile().isEmpty()){
                String[] pathSplit = boardItem.getFile().split("\\.");
                if (pathSplit.length != 0){
                    fileExt = pathSplit[1].toUpperCase();
                }
            }
            txtFileInfo.setText(fileExt + " " + (boardItem.getFileSize() / 1024) + " KB " + boardItem.getThumbHeight() + "x" + boardItem.getThumbWidth());
        }


        // Trasnparentar items con sage
        if (convertView != null){
            if (settings.getBoolean("pref_transparent_sage", true)){
                convertView.setAlpha(boardItem.isSage() ? 0.75F : 1.0F);
            }
        }

        /*
            http://stackoverflow.com/questions/8558732/listview-textview-with-linkmovementmethod-makes-list-item-unclickable
         */
        txtBody.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                boolean ret = false;
                CharSequence text = ((TextView) v).getText();
                Spannable stext = Spannable.Factory.getInstance().newSpannable(text);
                TextView widget = (TextView) v;
                int action = event.getAction();

                if (action == MotionEvent.ACTION_UP ||
                        action == MotionEvent.ACTION_DOWN) {
                    int x = (int) event.getX();
                    int y = (int) event.getY();

                    x -= widget.getTotalPaddingLeft();
                    y -= widget.getTotalPaddingTop();

                    x += widget.getScrollX();
                    y += widget.getScrollY();

                    Layout layout = widget.getLayout();
                    int line = layout.getLineForVertical(y);
                    int off = layout.getOffsetForHorizontal(line, x);

                    ClickableSpan[] link = stext.getSpans(off, off, ClickableSpan.class);
/*04-03 17:46:54.646 13693-13693/org.bienvenidoainternet.baiparser V/URLParts: zonavip
04-03 17:46:54.646 13693-13693/org.bienvenidoainternet.baiparser V/URLParts: read
04-03 17:46:54.646 13693-13693/org.bienvenidoainternet.baiparser V/URLParts: 43872
04-03 17:46:54.650 13693-13693/org.bienvenidoainternet.baiparser V/URLParts: 25*/
                    if (link.length != 0) {
                        if (link[0] instanceof URLSpan){
                            URLSpan uspan = (URLSpan) link[0];
                            if ((uspan.getURL().contains("/read/") || uspan.getURL().contains("/res/")) && !uspan.getURL().contains("http")){
                                String url = uspan.getURL();
                                String[] parts = url.split("/");
                                if (parts.length == 4 && listThreads){
                                    Log.v("ConvertView", convertView.getParent().toString());
                                }
                                return true;
                            }
                        }
                        if (action == MotionEvent.ACTION_UP) {
                            link[0].onClick(widget);
                        }
                        ret = true;
                    }
                }
                return ret;
            }
        });
        return listItemView;
    }
}
