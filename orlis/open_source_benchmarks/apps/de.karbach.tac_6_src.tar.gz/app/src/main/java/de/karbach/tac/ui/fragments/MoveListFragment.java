package de.karbach.tac.ui.fragments;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import de.karbach.tac.R;
import de.karbach.tac.core.BoardData;
import de.karbach.tac.core.Move;
import de.karbach.tac.ui.ColorToBallImage;

/**
 * Shows a list of moves in the game. A move is a played card aloing with a possible color.
 *
 */
public class MoveListFragment extends ListFragment{

    /**
     * The data model, which has to be shown
     */
    private List<Move> moves;

    /**
     * List of colors, first color for first player and so forth
     */
    private List<Integer> colors;

    /**
     * Adapter for showing moves
     */
    private class MoveAdapter extends ArrayAdapter<Move>{

        private ColorToBallImage colorToImage;

        private SparseIntArray ballIDToColor;
        private SparseArray<Bitmap> ballIDToBMP;

        private List<Integer> colors;

        public MoveAdapter(List<Move> objects, List<Integer> colors) {
            super(getActivity(), 0, objects);

            colorToImage = new ColorToBallImage(getContext());
            this.colors = colors;
            if(this.colors == null){
                this.colors = new BoardData().getColors();
            }
            initBallColors();
        }

        /**
         * Reassign the colors painted for the players.
         *
         */
        protected void initBallColors(){
            ballIDToColor = new SparseIntArray();
            ballIDToBMP = new SparseArray<Bitmap>();

            int players = 4;

            for(int col=0; col<players; col++ ){
                int color = colors.get(col);
                for(int id = 0+col*players; id<(col+1)*players; id++){
                    ballIDToColor.put(id, color);
                    ballIDToBMP.put(id, colorToImage.colorToBitmap(color));
                }
            }

            Bitmap greyBallBitmap = BitmapFactory.decodeResource(getActivity().getResources(), R.drawable.ic_help);
            ballIDToBMP.put(R.drawable.grey, greyBallBitmap);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if(convertView == null){
                convertView = getActivity().getLayoutInflater().inflate(R.layout.move_item, parent, false);
            }

            Move m = getItem(position);

            TextView idview = (TextView) convertView.findViewById(R.id.id_textview);
            ImageView cardview = (ImageView) convertView.findViewById(R.id.card_imageview);
            ImageView ballview = (ImageView) convertView.findViewById(R.id.ball_imageview);
            ImageView ballview2 = (ImageView) convertView.findViewById(R.id.ball2_imageview);

            ballview2.setImageBitmap(null);

            if(m.getId() != -1){
                idview.setText(String.valueOf(m.getId()));
            }
            else{
                idview.setText(R.string.emptyid);
            }

            if(m.getCard() != null){
                cardview.setImageBitmap(m.getCard().getBitmap(getActivity()));
            }
            else{
                cardview.setImageResource(R.drawable.backside);
            }

            TextView numberText = (TextView) convertView.findViewById(R.id.movenumber);
            if(m.getDistance() > -1){
                numberText.setText(String.valueOf(m.getDistance()));
            }
            else{
                numberText.setText("");
            }

            int[] ballIds = m.getBallIDs();
            Bitmap bm = null;
            if(ballIds == null){
                bm = ballIDToBMP.get(R.drawable.grey);
            }
            else{
                if(ballIds.length >= 1){
                    bm = ballIDToBMP.get(ballIds[0]);
                }
                if(bm == null){
                    bm = ballIDToBMP.get(R.drawable.grey);
                }

                if(ballIds.length >= 2){
                    Bitmap bm2 = ballIDToBMP.get(ballIds[1]);
                    if(bm2 != null){
                        ballview2.setImageBitmap(bm2);
                    }
                }
            }
            ballview.setImageBitmap(bm);

            return convertView;
        }
    }

    public MoveListFragment(){
        setRetainInstance(true);
        moves = new ArrayList<Move>();
    }

    public void addMove(Move m){
        moves.add(m);

        Collections.sort(moves, new Comparator<Move>() {
            @Override
            public int compare(Move lhs, Move rhs) {
                if(lhs.getId() < rhs.getId() ){
                    return 1;
                }
                if(lhs.getId() > rhs.getId() ){
                    return -1;
                }
                return 0;
            }
        });
    }

    public void clearMoves(){
        this.moves.clear();
    }

    /**
     *
     * @return list of colors used for each player
     */
    public List<Integer> getColors() {
        return colors;
    }

    /**
     * Set the colors to show for players
     * @param colors list of colors
     */
    public void setColors(List<Integer> colors) {
        this.colors = colors;
    }

    @Override
    public void onCreate(Bundle savedInstance){
        super.onCreate(savedInstance);

        setListAdapter(new MoveAdapter(moves, colors) );
    }

}
