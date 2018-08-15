package com.idunnololz.igo;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;

import com.idunnololz.igo.MainActivity.NewGameDialogFragment;
import com.idunnololz.igo.Parser.Node;
import com.idunnololz.igo.Player.OnPlayerChangeListener;
import com.idunnololz.igo.R;
import com.idunnololz.igo.StoneManager.Consequence;
import com.idunnololz.igo.StoneManager.Stone;
import com.idunnololz.utils.AlertDialogFragment;
import com.idunnololz.utils.LogUtils;
import com.idunnololz.utils.Utils;
import com.idunnololz.widgets.GoBoardView;
import com.idunnololz.widgets.GoBoardView.GoBoardAdapter;
import com.idunnololz.widgets.GoBoardView.OnPointClickListener;
import com.idunnololz.widgets.GoBoardView.OnPointLongClickListener;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

public class GameFragment extends Fragment implements GameActivity.OnBackPressListener {
	private static final String TAG = GameFragment.class.getSimpleName();
	
	public static final String ARGS_SGF = "sgf";
	
	private static final String EXTRA_STATE = "state";
	private static final String EXTRA_STONE_MANAGER = "stone_manager";
	private static final String DIALOG_TAG_GAME_INFO = "game_info_d";

	public static final int DEFAULT_BOARD_SIZE = 9;
	public static final int DEFAULT_HANDICAP = 0;
	public static final float DEFAULT_KOMI = 6.5f;

	private static final int STATE_BLACK_TURN = 1, STATE_WHITE_TURN = 2;
	private static final int DEFAULT_ANIMATION_DURATION = 300;

	private View rootView;
	private GoBoardView board;
	private ViewGroup extras;
	private Button expandCollapseExtra;
	private View extra;
	private TextView extraText;
	private TextView blackCaptures;
	private TextView whiteCaptures;
	
	private MenuItem itemUndo;
	private MenuItem itemRedo;

	private int boardSize;
	private int handicap;
	private float komi;

	private CustomGoBoardAdapter adapter;
	private StoneManager stoneMgr;
	
	private int[] starPoints;

	private int state = 0;
	
	private boolean previewMode = false;
	
	private Node gameTree;
	
	private native static
	void initGTP (float pMemory);

	native static
	String playGTP (String pInput);

	native static
	void setRules (int chineseRules);
	
	/**
	 * Our two players. Note that player[0] is always black...
	 */
	private Player[] player = new Player[2];

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setHasOptionsMenu(true);

		// handle arguments
		Bundle args = getArguments();
		boardSize = DEFAULT_BOARD_SIZE;
		if (args != null) {
			boardSize = args.getInt(GameActivity.ARGS_BOARD_SIZE, DEFAULT_BOARD_SIZE);
			handicap = args.getInt(GameActivity.ARGS_HANDICAP, DEFAULT_HANDICAP);
			komi = args.getFloat(GameActivity.ARGS_KOMI, DEFAULT_KOMI);
			
			// check if a file load is in order!
			String sgfFile = args.containsKey(ARGS_SGF) ? args.getString(ARGS_SGF) : null;
			if (sgfFile != null) {
				previewMode = true;
				
				Parser p = new Parser();
				
				// load this file!
				try {
					gameTree = p.load(sgfFile);
					
					boardSize = 19; // this is always 19 when loading from SGF
					handicap = gameTree.getInt("HA", 0);
					komi = gameTree.getFloat("KM", 0);
					
					LogUtils.d(TAG, "Komi: " + komi);
				} catch (IOException e) {
					LogUtils.e(TAG, "", e);
				}
			}
		}
		
		switch (boardSize) {
		case 9:
			starPoints = new int[]{
					2, 2,
					2, 6,
					6, 2,
					6, 6
			};
			break;
		case 13:
			starPoints = new int[]{
					/* Corner points */
					3, 3,
					3, 9,
					9, 3,
					9, 9,
					
					/* Tengen */
					6, 6,
					
					/* Side points */
					3, 6,
					6, 3,
					6, 9,
					9, 6,
			};
			break;
		case 19:
			starPoints = new int[]{
					/* Corner points */
					3, 3,
					3, 15,
					15, 3,
					15, 15,
					
					/* Tengen */
					9, 9,
					
					/* Side points */
					3, 9,
					9, 3,
					9, 15,
					15, 9,
			};
			break;
		default:
			starPoints = new int[0];
			break;
		}

		player[0] = new Player(true);
		player[1] = new Player(false);

		if (savedInstanceState != null) {
			stoneMgr = savedInstanceState.getParcelable(EXTRA_STONE_MANAGER);
			
			player[0].loadState(savedInstanceState);
			player[1].loadState(savedInstanceState);
			
			state = savedInstanceState.getInt(EXTRA_STATE, state);
		} else {
			stoneMgr = new StoneManager();
			stoneMgr.setBoardSize(boardSize);

			// setup handicaps...
			for (int i = 0; i < handicap; i++) {
				stoneMgr.placeStone(starPoints[i << 1], starPoints[(i << 1) + 1], StoneManager.STONE_BLACK);
			}

			if (handicap == 0) {
				state = STATE_BLACK_TURN;
			} else {
				state = STATE_WHITE_TURN;
			}
			
			if (previewMode) {
				if (gameTree != null) {
					stoneMgr.ingestGameTree(gameTree);
					refreshMenuItems();
				}
			}
		}

		adapter = new CustomGoBoardAdapter();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.fragment_game, container, false);

		board = (GoBoardView) rootView.findViewById(R.id.board);
		extras = (ViewGroup) rootView.findViewById(R.id.extras);
		expandCollapseExtra = (Button) rootView.findViewById(R.id.expandCollapse);
		extra = rootView.findViewById(R.id.extra);
		extraText = (TextView) rootView.findViewById(R.id.extraText);
		blackCaptures = (TextView) rootView.findViewById(R.id.black_score);
		whiteCaptures = (TextView) rootView.findViewById(R.id.white_score);
		 
		expandCollapseExtra.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (extra.getVisibility() == View.GONE) {
					expandExtra();
				} else {
					collapseExtra();
				}
			}
			
		});
		
		board.setOnPointClickListener(new OnPointClickListener() {

			@Override
			public void onPointClick(View v, int across, int down) {
				LogUtils.d(TAG, "clicked: " + across + "," + down);
				
				if (stoneMgr.isPointEmpty(across, down)) {
					Consequence con = null;
					
					Stone last = stoneMgr.getLastMove();

					if (state == STATE_BLACK_TURN) {
						con = stoneMgr.placeStone(across, down, StoneManager.STONE_BLACK);
						state = STATE_WHITE_TURN;
					} else if (state == STATE_WHITE_TURN) {
						con = stoneMgr.placeStone(across, down, StoneManager.STONE_WHITE);
						state = STATE_BLACK_TURN;
					}

					if (con != null) {
						if (con.type == Consequence.TYPE_LEGAL_MOVE_CAPTURE) {
							if (state == STATE_BLACK_TURN) {
								player[1].addCaptures(con.extra);
							} else if (state == STATE_WHITE_TURN) {
								player[0].addCaptures(con.extra);
							}
							adapter.notifyDataSetChanged();
						} else if (con.type == Consequence.TYPE_LEGAL_MOVE_NO_CAPTURE) {
							adapter.notifySingleDataChanged(across, down);
							if (last != null) {
								adapter.notifySingleDataChanged(last.across, last.down);
							}
						} else if (!con.isLegal()) {
							if (state == STATE_BLACK_TURN) {
								state = STATE_WHITE_TURN;
							} else if (state == STATE_WHITE_TURN) {
								state = STATE_BLACK_TURN;
							}
						} else {
							throw new RuntimeException("Not handled");
						}
					}
				}
				
				refreshMenuItems();
			}

		});
		
		board.setOnPointLongClickListener(new OnPointLongClickListener() {

			@Override
			public boolean onPointLongClick(View v, int across, int down) {
				stoneMgr.printInfoRegardingGroup(across, down);
				return false;
			}
			
		});

		board.setBoard(boardSize, starPoints);
		board.setAdapter(adapter);
		
		refreshMenuItems();
		
		player[0].setOnPlayerChangeListener(new OnPlayerChangeListener() {

			@Override
			public void onCaptureChanged(int newCaptures) {
				blackCaptures.setText(String.valueOf(newCaptures));
			}
			
		});
		
		player[1].setOnPlayerChangeListener(new OnPlayerChangeListener() {

			@Override
			public void onCaptureChanged(int newCaptures) {
				whiteCaptures.setText(String.valueOf(newCaptures));
			}
			
		});

		return rootView;
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		Stone last = stoneMgr.getLastMove();
		updateComment(last == null ? null : last.comment);
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
	    inflater.inflate(R.menu.fragment_game, menu);
	    
	    itemUndo = menu.findItem(R.id.action_undo);
	    itemRedo = menu.findItem(R.id.action_redo);
	    
	    refreshMenuItems();
	    super.onCreateOptionsMenu(menu,inflater);
	}
	
	@Override
	public boolean onOptionsItemSelected (MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_undo:
			undo();
			break;
		case R.id.action_redo:
			redo();
			break;
		case R.id.action_info:
			if (gameTree != null) {
				GameInfoDialogFragment.newInstance(gameTree).show(getFragmentManager(), DIALOG_TAG_GAME_INFO);
			} else {
				new AlertDialogFragment.Builder()
				.setMessage(R.string.no_game_info)
				.create().show(getFragmentManager(), "d");
				
			}
			break;
		}
		return false;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putParcelable(EXTRA_STONE_MANAGER, stoneMgr);
		
		player[0].saveState(outState);
		player[1].saveState(outState);
		
		outState.putInt(EXTRA_STATE, state);
	}
	
	private void refreshMenuItems() {
		if (itemUndo == null) return;
		itemUndo.setEnabled(stoneMgr.canUndo());
		if (!itemUndo.isEnabled())
			itemUndo.getIcon().mutate().setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_IN);
		else
			itemUndo.getIcon().mutate().clearColorFilter();
		
		itemRedo.setEnabled(stoneMgr.canRedo());
		if (!itemRedo.isEnabled())
			itemRedo.getIcon().mutate().setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_IN);
		else
			itemRedo.getIcon().mutate().clearColorFilter();
	}

	private class CustomGoBoardAdapter extends GoBoardAdapter {

		@Override
		public void preparePoint(ImageButton view, int across, int down) {
			switch (stoneMgr.getStoneType(across, down)) {
			case StoneManager.STONE_BLACK:
				view.setImageResource(R.drawable.black_stone);
				break;
			case StoneManager.STONE_WHITE:
				view.setImageResource(R.drawable.white_stone);
				break;
			default:
				view.setImageDrawable(null);
				break;
			}
			
			Stone s = stoneMgr.getLastMove();
			if (s != null && s.across == across && s.down == down) {
				LayerDrawable d = new LayerDrawable(new Drawable[] {
						view.getDrawable(),
						getResources().getDrawable(R.drawable.stone_important_mark),
				});
				int size = view.getMeasuredWidth();
				int padding = (int)(size * 0.1f);
				d.setLayerInset(1, padding, padding, padding, padding);
				view.setImageDrawable(d);
			}
			
		}

	}

	@Override
	public boolean onBackPressed() {
		return undo();
	}
	
	private boolean undo() {
		Consequence c = stoneMgr.undoLastMove();
		if (c.type == Consequence.TYPE_MOVE) {
			adapter.notifyDataSetChanged();
			if (state == STATE_BLACK_TURN) {
				player[1].addCaptures(c.extra);
				state = STATE_WHITE_TURN;
			} else if (state == STATE_WHITE_TURN) {
				player[0].addCaptures(c.extra);
				state = STATE_BLACK_TURN;
			}

			Stone last = stoneMgr.getLastMove();
			updateComment(last == null ? null : last.comment);
			
			refreshMenuItems();
		}
		return c.type == Consequence.TYPE_MOVE;
	}
	
	private boolean redo() {
		Consequence c = stoneMgr.redoLastMove();
		if (c.type == Consequence.TYPE_MOVE) {
			adapter.notifyDataSetChanged();
			if (state == STATE_BLACK_TURN) {
				player[0].addCaptures(c.extra);
				state = STATE_WHITE_TURN;
			} else if (state == STATE_WHITE_TURN) {
				player[1].addCaptures(c.extra);
				state = STATE_BLACK_TURN;
			}
			
			Stone last = stoneMgr.getLastMove();
			updateComment(last == null ? null : last.comment);
			
			refreshMenuItems();
		}
		return c.type == Consequence.TYPE_MOVE;
	}
	
	private static class ExtrasInfo {
		boolean isHiding = false;
	}
	
	private void updateComment(String comment) {
		ExtrasInfo info = (ExtrasInfo) extras.getTag();
		if (info == null) {
			info = new ExtrasInfo();
			extras.setTag(info);
		}
		
		if (comment != null) {
			expandCollapseExtra.setText(R.string.comments);
			extraText.setText(comment);
			
			extra.getLayoutParams().height = LayoutParams.WRAP_CONTENT;
			
			extras.measure(MeasureSpec.makeMeasureSpec(rootView.getMeasuredWidth(), MeasureSpec.AT_MOST), 
					MeasureSpec.makeMeasureSpec(rootView.getMeasuredHeight(), MeasureSpec.AT_MOST));
			
			int maxHeight = rootView.getMeasuredHeight() / 2;
			
			if (extra.getMeasuredHeight() > maxHeight) {
				extra.getLayoutParams().height = maxHeight;
			}
			
			if (extras.getVisibility() == View.INVISIBLE) {
				extras.setVisibility(View.VISIBLE);
				TranslateAnimation ani = new TranslateAnimation(0, 0, extras.getMeasuredHeight(), 0);
				ani.setDuration(DEFAULT_ANIMATION_DURATION);
				ani.setFillAfter(true);
				extras.startAnimation(ani);
			}
		} else {
			if (extras.getVisibility() == View.VISIBLE && !info.isHiding) {
				TranslateAnimation ani = new TranslateAnimation(0, 0, 0, extras.getMeasuredHeight());
				ani.setDuration(DEFAULT_ANIMATION_DURATION);
				ani.setFillAfter(true);
				final ExtrasInfo i = info;
				ani.setAnimationListener(new AnimationListener() {

					@Override
					public void onAnimationStart(Animation animation) {
						// TODO Auto-generated method stub

					}

					@Override
					public void onAnimationEnd(Animation animation) {
						extras.setVisibility(View.INVISIBLE);
						extraText.setText("");
						i.isHiding = false;
					}

					@Override
					public void onAnimationRepeat(Animation animation) {
						// TODO Auto-generated method stub

					}

				});
				extras.startAnimation(ani);
				info.isHiding = true;
			}
		}
	}
	
	public void expandExtra() {
		extra.setVisibility(View.VISIBLE);
		extras.measure(MeasureSpec.makeMeasureSpec(rootView.getMeasuredWidth(), MeasureSpec.AT_MOST), 
				MeasureSpec.makeMeasureSpec(rootView.getMeasuredHeight(), MeasureSpec.AT_MOST));
		
		TranslateAnimation ani = new TranslateAnimation(0, 0, extra.getMeasuredHeight(), 0);
		ani.setDuration(DEFAULT_ANIMATION_DURATION);
		ani.setFillAfter(true);
		extras.startAnimation(ani);
	}
	
	public void collapseExtra() {
		TranslateAnimation ani = new TranslateAnimation(0, 0, 0, extra.getMeasuredHeight());
		ani.setDuration(DEFAULT_ANIMATION_DURATION);
		ani.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				extra.setVisibility(View.GONE);
				extras.clearAnimation();
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
				// TODO Auto-generated method stub
				
			}
			
		});
		extras.startAnimation(ani);		
	}
	
	public static class GameInfoDialogFragment extends DialogFragment {
		
		private static final String ARGS_NODE_MAP = "node_map";
		
		private View rootView;
		private TextView txtEventDetails;
		private TextView txtBlackPlayer;
		private TextView txtWhitePlayer;
		private TextView txtKomi;
		private TextView txtResult;
		
        private static GameInfoDialogFragment newInstance(Node detailsNode) {
        	GameInfoDialogFragment f = new GameInfoDialogFragment();
        	
        	Bundle args = new Bundle();
        	args.putSerializable(ARGS_NODE_MAP, (Serializable) detailsNode.getArgs());
        	
        	f.setArguments(args);
            return f;
        }
        
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
          Dialog dialog = super.onCreateDialog(savedInstanceState);

          // request a window without the title
          dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
          return dialog;
        }
        
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        	rootView = inflater.inflate(R.layout.dialog_game_info, container, false);
        	
        	txtEventDetails = (TextView) rootView.findViewById(R.id.txtEventDetails);
        	txtBlackPlayer = (TextView) rootView.findViewById(R.id.txtBlackPlayer);
        	txtWhitePlayer = (TextView) rootView.findViewById(R.id.txtWhitePlayer);
        	txtKomi = (TextView) rootView.findViewById(R.id.txtKomi);
        	txtResult = (TextView) rootView.findViewById(R.id.txtResult);
        	
        	HashMap<String, String> args = (HashMap<String, String>) getArguments().getSerializable(ARGS_NODE_MAP);
        	
        	SpannableStringBuilder builder = new SpannableStringBuilder(getString(R.string.detail_event_details));
        	builder.setSpan(new android.text.style.StyleSpan(android.graphics.Typeface.BOLD), 0, builder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        	builder.append(' ');
        	String event = args.get("EV");
        	if (event == null) {
        	} else {
        		builder.append(event);
        	}
        	
        	txtEventDetails.setText(builder);
        	
        	builder = new SpannableStringBuilder(getString(R.string.detail_black_player));
        	builder.setSpan(new android.text.style.StyleSpan(android.graphics.Typeface.BOLD), 0, builder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        	builder.append(' ');
        	String player = args.get("PB");
        	if (player == null) {
        	} else {
        		builder.append(player);
        	}
        	String rank = args.get("BR");
        	if (rank != null) {
        		builder.append('(');
        		builder.append(rank);
        		builder.append(')');
        	}
        	
        	txtBlackPlayer.setText(builder);
        	
        	builder = new SpannableStringBuilder(getString(R.string.detail_white_player));
        	builder.setSpan(new android.text.style.StyleSpan(android.graphics.Typeface.BOLD), 0, builder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        	builder.append(' ');
        	player = args.get("PW");
        	if (player == null) {
        	} else {
        		builder.append(player);
        	}
        	rank = args.get("WR");
        	if (rank != null) {
        		builder.append('(');
        		builder.append(rank);
        		builder.append(')');
        	}
        	
        	txtWhitePlayer.setText(builder);
        	
        	builder = new SpannableStringBuilder(getString(R.string.detail_komi));
        	builder.setSpan(new android.text.style.StyleSpan(android.graphics.Typeface.BOLD), 0, builder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        	builder.append(' ');
        	String komi = args.get("KM");
        	if (komi != null) {
        		builder.append(komi);
        	}
        	
        	txtKomi.setText(builder);
        	
        	builder = new SpannableStringBuilder(getString(R.string.detail_result));
        	builder.setSpan(new android.text.style.StyleSpan(android.graphics.Typeface.BOLD), 0, builder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        	builder.append(' ');
        	String result = getResult(args.get("RE"));
        	if (result != null) {
        		builder.append(result);
        	}
        	
        	txtResult.setText(builder);
        	
        	return rootView;
        }
        
        public String getResult(String result) {
        	StringBuilder builder = new StringBuilder();
        	
        	for (int i = 0; i < result.length(); i++) {
        		char c = result.charAt(i);
        		if (c == 'B') {
        			builder.append("Black won");
        		} else if (c == 'W') {
        			builder.append("White won");
        		} else if (c == 'R') {
        			builder.append("(resign)");
        		}
        	}
        	
        	return builder.toString();
        }
	}
}
