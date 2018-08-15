package com.idunnololz.igo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.idunnololz.igo.BoardHistoryManager.BoardDelta;
import com.idunnololz.igo.Parser.Node;
import com.idunnololz.utils.LogUtils;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

public class StoneManager implements Parcelable {
	private static final String TAG = StoneManager.class.getSimpleName();

	public static final int STONE_NONE = 0;
	public static final int STONE_BLACK = 1;
	public static final int STONE_WHITE = 2;

	private int boardSize;
	private PointInfo[][] points;
	private int[] typeCount = new int[4];

	private LinkedHashSet<BoardState> boardHistory = new LinkedHashSet<BoardState>();
	private List<StoneGroup> tempDead = new ArrayList<StoneGroup>();
	private BoardHistoryManager historyMgr = new BoardHistoryManager();
	
	private Stone lastMove;

	public StoneManager() {}

	public int getBoardSize() { return boardSize; }

	public void setBoardSize(int boardSize) {
		this.boardSize = boardSize;

		for (int i = 0; i < typeCount.length; i++) {
			typeCount[i] = 0;
		}

		points = new PointInfo[boardSize][];
		for (int i = 0; i < boardSize; i++) {
			points[i] = new PointInfo[boardSize];

			for (int j = 0; j < boardSize; j++) {
				points[i][j] = new PointInfo();
			}
		}
	}

	public Consequence placeStone(int across, int down, int stoneType) {
		return placeStone(across, down, stoneType, true, false);
	}
	
	public Consequence placeStone(int across, int down, int stoneType, boolean saveDelta, boolean buildKillDelta) {
		Stone s;
		PointInfo info = points[down][across];
		if (info.stone == null) {
			// make stone object if null
			s = new Stone();
			s.across = across;
			s.down = down;
		} else {
			s = info.stone;
		}
		s.type = stoneType;
		return placeStone(s, saveDelta, buildKillDelta);
	}

	public Consequence placeStone(Stone s, boolean saveDelta, boolean buildKillDelta) {
		LogUtils.d(TAG, "MoveIndex: " + (typeCount[STONE_BLACK] + typeCount[STONE_WHITE]));
		// Temp dead is cleared every move...
		tempDead.clear();

		final int stoneType = s.type;
		final int across = s.across;
		final int down = s.down;
		
		Consequence con = new Consequence();
		con.type = Consequence.TYPE_LEGAL_MOVE_NO_CAPTURE;

		PointInfo info = points[s.down][s.across];
		info.stone = s;

		// Don't worry. If this move is illegal, removeStone will be called
		// which will decrement this counter...
		typeCount[stoneType]++;

		StoneGroup group = info.group;
		if (group == null) {
			// lazy init group
			group = new StoneGroup();
			info.group = group;
		}
		group.stones.add(info.stone);

		// time to do some checks...
		for (int i = -1; i < 2; i++) {
			for (int j = -1; j < 2; j++) {
				int a = across + i;
				int d = down + j;

				if (!isValid(a, d)) continue;
				if (Math.abs(i) == Math.abs(j)) continue;

				PointInfo p = points[d][a];
				if (p.group == null) {
					info.group.liberties.add(compactBoardLocation(a, d));
				} else {
					StoneGroup g = p.group;
					g.liberties.remove(compactBoardLocation(across, down));

					if (g.getStoneType() == stoneType) {
						// merge the groups...
						mergeGroup(g, info.group);
						group = info.group;
					} else {
						// if this is the opponent's group, check if group is alive...
						if (g.liberties.size() == 0) {
							// dead...
							Log.d(TAG, "Group dead!");
							con.type = Consequence.TYPE_LEGAL_MOVE_CAPTURE;
							con.extra = g.stones.size();
							removeGroup(g);

							tempDead.add(g);
						}
					}
				}
			}
		}

		// lastly check if this is a legal move...
		if (group.liberties.size() == 0) {
			con.type = Consequence.TYPE_ILLEGAL_MOVE_SUICIDE;
		}

		if (con.isLegal()) {
			BoardState bs = new BoardState(boardSize);
			bs.writeState(points);
			if (!boardHistory.contains(bs)) {
				boardHistory.add(bs);
			} else {
				con.type = Consequence.TYPE_ILLEGAL_MOVE_KO;
			}
		}

		if (con.isLegal()) {
			for(StoneGroup g : tempDead) {
				if (saveDelta || buildKillDelta) {
					for (Stone stone : g.stones) {
						historyMgr.removeStoneFromCurrent(stone);
					}
				}

				g.stones.clear();
				g.liberties.clear();
			}
			
			lastMove = info.stone;

			if (saveDelta) {
				historyMgr.addStoneToCurrent(info.stone);
				historyMgr.pushDelta();
			}
		} else {
			for(StoneGroup g : tempDead) {
				addGroup(g);
			}

			group.stones.remove(info.stone);
			removeStone(info.stone);
		}

		LogUtils.d(TAG, "group liberties: " + getLibertyString(group.liberties) + " blks: " + typeCount[STONE_BLACK] + " whites: " + typeCount[STONE_WHITE]);

		return con;
	}

	/**
	 * Returns true if there exists a move to undo.
	 * @return
	 */
	public boolean canUndo() {
		return historyMgr.canUndo();
	}

	public boolean canRedo() {
		return historyMgr.canRedo();
	}

	public Consequence undoLastMove() {
		if (!canUndo()) return new Consequence(Consequence.TYPE_NO_MOVE);

		BoardDelta d = historyMgr.undo();

		BoardState bs = new BoardState(boardSize);
		bs.writeState(points);
		boardHistory.remove(bs);

		for (Stone s : d.added) {
			removeStone(s, true);
			rebuildGroupsAround(s);
		}

		int stonesRestored = d.removed.length;
		StoneGroup g = new StoneGroup();
		for (Stone s : d.removed) {
			addStone(s, g, true);
		}

		LogUtils.d(TAG, "UndoMoveIndex: " + (typeCount[STONE_BLACK] + typeCount[STONE_WHITE]));
		
		// Now we need to find the last move made...
		d = historyMgr.peek();
		if (d != null) {
			// There was a last move! Let's mark it!
			lastMove = d.added[0];
		} else {
			lastMove = null;
		}

		Log.d(TAG, "Restored group with " + g.stones.size() + " stones and  " + g.liberties.size() + " liberties: " + getLibertyString(g.liberties));
		return new Consequence(Consequence.TYPE_MOVE, -stonesRestored);
	}

	public Consequence redoLastMove() {
		if (!canRedo()) return new Consequence(Consequence.TYPE_NO_MOVE);

		BoardDelta d = historyMgr.redo();
		Stone s = d.added[0];
		
		Consequence c;
		if (!d.undoCreated) {
			// we need to do some extra work so that undo will work
			// properly...
			LogUtils.d(TAG, "Creating undo stack...");
			historyMgr.enterDeltaEditMode();
			c = placeStone(s, false, true);
			historyMgr.setDeltaUndoCreated(true);
			historyMgr.exitDeltaEditMode();
		} else {
			c = placeStone(s, false, false);
		}
		return new Consequence(Consequence.TYPE_MOVE, c.extra);
	}

	private void mergeGroup(StoneGroup g1, StoneGroup g2) {
		StoneGroup bigger;
		StoneGroup smaller;
		if (g1.stones.size() >= g2.stones.size()) {
			bigger = g1;
			smaller = g2;
		} else {
			bigger = g2;
			smaller = g1;
		}

		bigger.stones.addAll(smaller.stones);
		bigger.liberties.addAll(smaller.liberties);

		for (Stone s : smaller.stones) {
			PointInfo p = points[s.down][s.across];
			p.group = bigger;
		}

		LogUtils.d(TAG, "New group size: " + bigger.stones.size());
	}

	private void addGroup(StoneGroup g) {
		for (Stone s : g.stones) {
			addStone(s, g);
		}
	}

	private void addStone(Stone s, StoneGroup g) {
		addStone(s, g, false);
	}

	private void addStone(Stone s, StoneGroup g, boolean makeGroup) {
		PointInfo p = points[s.down][s.across];
		p.stone = s;
		p.group = g;
		typeCount[s.type]++;

		if (makeGroup) {
			g.stones.add(s);
			lastMove = s;
		}

		// deduct liberties from all surrounding enemy groups...
		for (int i = -1; i < 2; i++) {
			for (int j = -1; j < 2; j++) {
				int a = s.across + i;
				int d = s.down + j;

				if (!isValid(a, d)) continue;
				if (Math.abs(i) == Math.abs(j)) continue;

				PointInfo info = points[d][a];
				if (info.group != null) {
					info.group.liberties.remove(compactBoardLocation(s.across, s.down));
				} else if (info.group == null) {
					if (makeGroup)
						g.liberties.add(compactBoardLocation(a, d));
				}
			}
		}
	}

	private void removeGroup(StoneGroup g) {
		for (Stone s : g.stones) {
			removeStone(s);
		}
	}

	private void removeStone(Stone s) {
		removeStone(s, false);
	}

	private void removeStone(Stone s, boolean removeCompletely) {
		LogUtils.d(TAG, "removing stone " + s.down + "," + s.across);
		// remove this stone...
		PointInfo p = points[s.down][s.across];
		typeCount[p.stone.type]--;

		if (removeCompletely) {
			p.group.stones.remove(s);
		}

		// remember to add this as a liberty to all adjacent groups...
		for (int i = -1; i < 2; i++) {
			for (int j = -1; j < 2; j++) {
				int a = s.across + i;
				int d = s.down + j;

				if (!isValid(a, d)) continue;
				if (Math.abs(i) == Math.abs(j)) continue;

				PointInfo info = points[d][a];
				if (info.group != null) {
					info.group.liberties.add(compactBoardLocation(s.across, s.down));
				}

				if (removeCompletely) {
					p.group.liberties.remove(compactBoardLocation(a, d));
				}
			}
		}

		p.stone = null;
		p.group = null;
	}

	public void rebuildGroupsAround(Stone s) {
		LogUtils.d(TAG, "Rebuilding groups...");
		for (int i = -1; i < 2; i++) {
			for (int j = -1; j < 2; j++) {
				int a = s.across + i;
				int d = s.down + j;

				if (!isValid(a, d)) continue;
				if (Math.abs(i) == Math.abs(j)) continue;

				PointInfo info = points[d][a];
				if (info.stone != null) {
					rebuildGroup(info.stone);
				}
			}
		}
		LogUtils.d(TAG, "Done rebuild...");
	}


	private List<Stone> unvisited = new ArrayList<Stone>();
	/**
	 * Traverses the group of a given stone and rebuilds the group
	 * object from scratch, ensuring correct group values.
	 * @param s A stone in a group to rebuild
	 */
	public void rebuildGroup(Stone s) {
		StoneGroup g = new StoneGroup();
		g.stones.add(s);

		unvisited.clear();
		unvisited.add(s);

		for (int x = 0; x < unvisited.size(); x++) {
			s = unvisited.get(x);
			for (int i = -1; i < 2; i++) {
				for (int j = -1; j < 2; j++) {
					int a = s.across + i;
					int d = s.down + j;

					if (!isValid(a, d)) continue;
					if (Math.abs(i) == Math.abs(j)) continue;

					PointInfo p = points[d][a];
					if (p.group == null) {
						g.liberties.add(compactBoardLocation(a, d));
					} else {
						if (s.type == p.stone.type) {
							// same group...
							g.stones.add(p.stone);
							if (!unvisited.contains(p.stone)) {
								unvisited.add(p.stone);
							}
						}
					}
				}
			}
		}

		for (Stone stone : g.stones) {
			PointInfo p = points[stone.down][stone.across];
			p.group = g;
		}
		printInfoRegardingGroup(s.across, s.down);
	}

	private String getLibertyString(Set<Integer> l) {
		StringBuilder builder = new StringBuilder("{ ");
		for (Integer i : l) {
			builder.append('[')
			.append(extractAcross(i))
			.append(',')
			.append(extractDown(i))
			.append("], ");
		}
		return builder.append(" }").toString();
	}

	public boolean isValid(int across, int down) {
		return (across >= 0 && across < boardSize) &&
				(down >= 0 && down < boardSize);
	}

	public int getStoneType(int across, int down) {
		if (isPointEmpty(across, down)) return 0;
		return points[down][across].stone.type;
	}

	public boolean isPointEmpty(int across, int down) {
		return points[down][across].stone == null;
	}

	private int compactBoardLocation(int across, int down) {
		return (across << 8) + down;
	}

	private int extractAcross(int compact) {
		return ((compact >> 8) & 0xFF);
	}

	private int extractDown(int compact) {
		return (compact & 0xFF);
	}
	
	public Stone getLastMove() {
		return lastMove;
	}

	private static class PointInfo {
		Stone stone;
		StoneGroup group;
	}

	static class Stone {
		int type;
		int across, down;
		
		String comment;

		public Stone() {}

		public Stone(Parcel in) {
			type = in.readInt();
			across = in.readInt();
			down = in.readInt();
			if (in.readInt() == 1) {
				comment = in.readString();
			}
		}

		public void write(Parcel out) {
			out.writeInt(type);
			out.writeInt(across);
			out.writeInt(down);
			if (comment != null) {
				out.writeInt(1);
				out.writeString(comment);
			} else {
				out.writeInt(0);
			}
		}

		@Override
		public String toString() {
			return "[across: " + across + ", down: " + down + ", type: " + type + "]";
		}
	}

	private static class StoneGroup {
		Set<Integer> liberties = new HashSet<Integer>();

		Set<Stone> stones = new HashSet<Stone>();

		public int getStoneType() {
			return stones.iterator().next().type;
		}
	}

	public static class Consequence {
		private static final int MASK_LEGAL_MODE = 0x1000;

		public static final int TYPE_ILLEGAL_MOVE_SUICIDE = 0x0001;
		public static final int TYPE_ILLEGAL_MOVE_KO = 0x0002;

		public static final int TYPE_LEGAL_MOVE_NO_CAPTURE = 0x1000;
		public static final int TYPE_LEGAL_MOVE_CAPTURE = 0x1001;
		
		public static final int TYPE_NO_MOVE = 0x2000;
		public static final int TYPE_MOVE = 0x2001;

		int type = 0;
		int extra = 0;
		
		public Consequence() {}
		
		public Consequence(int type) { this.type = type; }
		
		public Consequence(int type, int extra) { this.type = type; this.extra = extra; }

		public boolean isLegal() {
			return (type & MASK_LEGAL_MODE) != 0;
		}
	}

	private static class BoardState {
		/**
		 * The board state stores the current board state as an array of
		 * bytes where each intersection takes 2 bits for state:
		 * 00 = empty
		 * 01 = white stone
		 * 10 = black stone
		 * 11 = marked
		 */

		/**
		 * Size of a byte in bits
		 */
		private static final int BYTE_SIZE = 8;
		/**
		 * Size of the state of a point in bits
		 */
		private static final int POINT_STATE_SIZE = 2;

		byte[] rawState;

		int boardSize;

		public BoardState(int boardSize) {
			// add one for rounding errors...
			int stateSize = ((boardSize * boardSize) / (BYTE_SIZE / POINT_STATE_SIZE)) + 1;
			rawState = new byte[stateSize];

			this.boardSize = boardSize;
		}

		public BoardState(int boardSize, Parcel p) {
			this(boardSize);

			p.readByteArray(rawState);
		}

		public void writeState(PointInfo[][] points) {
			int bytePos = 0;
			int bitPos = 0;
			for (int i = 0; i < boardSize; i++) {
				for (int j = 0; j < boardSize; j++) {
					PointInfo p = points[i][j];

					// only keep 2 bits...
					byte stoneN = (byte) ((p.stone == null ? 0 : p.stone.type) & 0x3);
					rawState[bytePos] |= stoneN << bitPos;

					bitPos += 2;
					if (bitPos == 8) {
						bytePos++;
						bitPos = 0;
					}
				}
			}
		}

		@Override
		public boolean equals(Object o) {
			return Arrays.equals(((BoardState) o).rawState, rawState);
		}

		@Override
		public int hashCode() {
			return Arrays.hashCode(rawState);
		}
	}

	public void printInfoRegardingGroup(int across, int down) {
		PointInfo p = points[down][across];
		if (p.group != null) {
			LogUtils.d(TAG, "GroupInfo: size " + p.group.stones.size() + " liberties " + getLibertyString(p.group.liberties));
		}
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel p, int arg1) {
		LogUtils.d(TAG, "writeToParcel");

		// Write all info about this manager into the parcel...

		p.writeInt(boardSize);

		int[] blacks = new int[typeCount[STONE_BLACK]];
		int[] whites = new int[typeCount[STONE_WHITE]];

		int blackCount = 0, whiteCount = 0;

		for (int i = 0; i < boardSize; i++) {
			for (int j = 0; j < boardSize; j++) {
				PointInfo info = points[j][i];
				if (info.stone == null) continue;
				switch (info.stone.type) {
				case STONE_BLACK:
					blacks[blackCount++] = compactBoardLocation(i, j);
					break;
				case STONE_WHITE:
					whites[whiteCount++] = compactBoardLocation(i, j);
					break;
				default:
					break;
				}
			}
		}

		p.writeIntArray(blacks);
		p.writeIntArray(whites);

		p.writeInt(boardHistory.size());
		if (boardHistory.size() > 0) {
			for (BoardState bs : boardHistory) {
				p.writeByteArray(bs.rawState);
			}
		}
		
		p.writeInt(lastMove == null ? 0 : 1);
		if (lastMove != null) {
			p.writeInt(lastMove.across);
			p.writeInt(lastMove.down);
			p.writeInt(lastMove.type);
		}

		historyMgr.write(p);
	}

	public static final Parcelable.Creator<StoneManager> CREATOR = new Parcelable.Creator<StoneManager>() {

		@Override
		public StoneManager createFromParcel(Parcel p) {
			LogUtils.d(TAG, "createFromParcel");

			int boardSize = p.readInt();

			StoneManager stoneMgr = new StoneManager();
			stoneMgr.setBoardSize(boardSize);

			int[] blacks = p.createIntArray();
			int[] whites = p.createIntArray();

			LogUtils.d(TAG, "restored: " + blacks.length + " black stones and " + whites.length + " white stones");

			for (int i = 0; i < blacks.length; i++) {
				int across = stoneMgr.extractAcross(blacks[i]);
				int down = stoneMgr.extractDown(blacks[i]);
				stoneMgr.placeStone(across, down, STONE_BLACK);
			}

			for (int i = 0; i < whites.length; i++) {
				int across = stoneMgr.extractAcross(whites[i]);
				int down = stoneMgr.extractDown(whites[i]);
				stoneMgr.placeStone(across, down, STONE_WHITE);
			}

			int boardHistorySize = p.readInt();
			if (boardHistorySize > 0) {
				for (int i = 0; i < boardHistorySize; i++) {
					stoneMgr.boardHistory.add(new BoardState(boardSize, p));
				}
			}

			boolean hasLastMove = p.readInt() == 1;
			if (hasLastMove) {
				stoneMgr.lastMove = new Stone();
				stoneMgr.lastMove.across = p.readInt();
				stoneMgr.lastMove.down = p.readInt();
				stoneMgr.lastMove.type = p.readInt();
			}
			
			stoneMgr.historyMgr.read(p);

			return stoneMgr;
		}

		@Override
		public StoneManager[] newArray(int args) {
			throw new UnsupportedOperationException("No. Just no.");
		}

	};

	public void ingestGameTree(Node n) {
		historyMgr.createHistoryFromGameTree(n);
	}
}