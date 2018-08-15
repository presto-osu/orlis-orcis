package com.idunnololz.igo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Stack;

import android.os.Parcel;

import com.idunnololz.igo.Parser.Node;
import com.idunnololz.igo.StoneManager.Stone;
import com.idunnololz.utils.LogUtils;

public class BoardHistoryManager {
	private static final String TAG = BoardHistoryManager.class.getSimpleName();
	
	private Stack<Stone> stoneAdded = new Stack<Stone>();
	private Stack<Stone> stoneRemoved = new Stack<Stone>();
	
	private List<Delta> deltas = new ArrayList<Delta>();
	
	private Delta currentDelta = new Delta();
	
	private int currentPosition = -1;
	private int curPosAdded = -1;
	private int curPosRemoved = -1;
	
	private boolean isDirty;
	private boolean deltaEditMode = false;
	
	public BoardHistoryManager() {}
	
	private void clean() {
		if (isDirty && !deltaEditMode) {
			while (stoneAdded.size() > curPosAdded + 1) {
				stoneAdded.pop();
			}
			while (stoneRemoved.size() > curPosRemoved + 1) {
				stoneRemoved.pop();
			}
			while (deltas.size() > currentPosition + 1) {
				deltas.remove(deltas.size() - 1);
			}
			
			isDirty = false;
		}
	}
	
	/**
	 * Resets all variables...
	 */
	private void reset() {
		stoneAdded.clear();
		stoneRemoved.clear();
		deltas.clear();
		currentDelta = new Delta();
		currentPosition = -1;
		curPosAdded = -1;
		curPosRemoved = -1;
	}
	
	public void addStoneToCurrent(Stone s) {
		clean();
		currentDelta.addSize++;
		curPosAdded++;
		stoneAdded.add(s);
	}
	
	public void removeStoneFromCurrent(Stone s) {
		clean();
		currentDelta.removeSize++;
		curPosRemoved++;
		stoneRemoved.add(s);
	}
	
	/**
	 * Sets whether the current delta's undo stack has been created before.
	 * @param created
	 */
	public void setDeltaUndoCreated(boolean created) {
		currentDelta.undoCreated = created;
	}
	
	public void pushDelta() {
		deltas.add(currentDelta);
		currentDelta = new Delta();
		currentPosition++;
	}
	
	public boolean canUndo() {
		return currentPosition >= 0;
	}
	
	public boolean canRedo() {
		return currentPosition < deltas.size() - 1;
	}
	
	public void read(Parcel in) {
		// clean object
		clean();
		reset();
		
		int added = in.readInt();
		for (int i = 0; i < added; i++) {
			stoneAdded.add(new Stone(in));
			LogUtils.d(TAG, "read" + stoneAdded.get(stoneAdded.size() - 1));
		}
		
		int removed = in.readInt();
		for (int i = 0; i < removed; i++) {
			stoneRemoved.add(new Stone(in));
		}
		
		int ds = in.readInt();
		for (int i = 0; i < ds; i++) {
			deltas.add(new Delta(in));
		}
		
		currentPosition = in.readInt();
		curPosAdded = in.readInt();
		curPosRemoved = in.readInt();
	}
	
	public void write(Parcel out) {
		out.writeInt(stoneAdded.size());
		for (Stone s : stoneAdded) {
			s.write(out);
			LogUtils.d(TAG, "write" + s);
		}
		
		out.writeInt(stoneRemoved.size());
		for (Stone s : stoneRemoved) {
			s.write(out);
		}
		
		out.writeInt(deltas.size());
		for (Delta d : deltas) {
			d.write(out);
		}
		
		out.writeInt(currentPosition);
		out.writeInt(curPosAdded);
		out.writeInt(curPosRemoved);
	}
	
	public BoardDelta undo() {
		isDirty = true;
		Delta d = deltas.get(currentPosition--);
		BoardDelta bd = new BoardDelta(d);
		for (int i = 0; i < d.addSize; i++) {
			bd.added[i] = stoneAdded.get(curPosAdded--);
		}
		for (int i = 0; i < d.removeSize; i++) {
			bd.removed[i] = stoneRemoved.get(curPosRemoved--);
		}
		
		LogUtils.d(TAG, "Undo: " + bd.added.length + " added and " + bd.removed.length + " removed.");
		return bd;
	}
	
	public BoardDelta peek() {
		if (currentPosition == -1) return null;
		
		Delta d = deltas.get(currentPosition);
		BoardDelta bd = new BoardDelta(d);
		for (int i = 0; i < d.addSize; i++) {
			bd.added[i] = stoneAdded.get(curPosAdded - i);
		}
		for (int i = 0; i < d.removeSize; i++) {
			bd.removed[i] = stoneRemoved.get(curPosRemoved - i);
		}
		return bd;
	}
	
	public BoardDelta redo() {
		isDirty = currentPosition != deltas.size() - 1;
		Delta d = deltas.get(++currentPosition);
		BoardDelta bd = new BoardDelta(d);
		for (int i = 0; i < d.addSize; i++) {
			bd.added[i] = stoneAdded.get(++curPosAdded);
		}
		for (int i = 0; i < d.removeSize; i++) {
			bd.removed[i] = stoneRemoved.get(++curPosRemoved);
		}
		bd.undoCreated = d.undoCreated;
		
		LogUtils.d(TAG, "Redo: " + bd.added.length + " added and " + bd.removed.length + " removed.");
		return bd;
	}
	
	public static class BoardDelta {
		public Stone[] added;
		public Stone[] removed;
		public boolean undoCreated = true;
		
		public BoardDelta(Delta d) {
			added = new Stone[d.addSize];
			removed = new Stone[d.removeSize];
		}
	}
	
	private class Delta {
		int addSize = 0;
		int removeSize = 0;
		boolean undoCreated = true;
		
		public Delta() {}
		
		public Delta(Parcel in) {
			addSize = in.readInt();
			removeSize = in.readInt();
			undoCreated = in.readInt() == 1;
		}
		
		public void write(Parcel out) {
			out.writeInt(addSize);
			out.writeInt(removeSize);
			out.writeInt(undoCreated ? 1 : 0);
		}
	}

	public void createHistoryFromGameTree(Node n) {
		for (; n.hasChild(); n = n.getChild()) {
			Stone s = new Stone();
			
			for (Entry<String, String> e : n.getArgs().entrySet()) {
				if (e.getKey().equals("B")) {
					String v = e.getValue();
					s.across = v.charAt(0) - 'a';
					s.down = v.charAt(1) - 'a';
					s.type = StoneManager.STONE_BLACK;
				} else if (e.getKey().equals("W")) {
					String v = e.getValue();
					s.across = v.charAt(0) - 'a';
					s.down = v.charAt(1) - 'a';
					s.type = StoneManager.STONE_WHITE;
					
				} else if (e.getKey().equals("C")) {
					// comment
					s.comment = e.getValue();
				}
			}
			
			if (s.type != StoneManager.STONE_NONE) {
				addStoneToCurrent(s);
				setDeltaUndoCreated(false);
				pushDelta();
			}
		}
		
		currentPosition = -1;
		curPosAdded = -1;
	}

	public void enterDeltaEditMode() {
		deltaEditMode = true;
		currentDelta = deltas.get(currentPosition);
	}
	
	public void exitDeltaEditMode() {
		currentDelta = new Delta();
		deltaEditMode = false;
	}
}
