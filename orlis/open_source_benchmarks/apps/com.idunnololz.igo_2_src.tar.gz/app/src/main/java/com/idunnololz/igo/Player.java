package com.idunnololz.igo;

import android.os.Bundle;

public class Player {
	private static final String EXTRA_CAPTURES = "captures";
	private static final String EXTRA_BLACK_CAPTURES = "b_captures";
	
	private int captures;
	private boolean isBlack;
	
	private OnPlayerChangeListener onPlayerChangeListener = null;
	
	public Player(boolean isBlack) {
		this.isBlack = isBlack;
	}
	
	public void saveState(Bundle b) {
		if (isBlack) {
			b.putInt(EXTRA_BLACK_CAPTURES, captures);
		} else {
			b.putInt(EXTRA_CAPTURES, captures);
		}
	}
	
	public void loadState(Bundle b) {
		if (isBlack) {
			captures = b.getInt(EXTRA_BLACK_CAPTURES, 0);
		} else {
			captures = b.getInt(EXTRA_CAPTURES, 0);
		}
	}
	
	public void addCaptures(int captures) {
		if (captures == 0) return;
		this.captures += captures;
		
		if (onPlayerChangeListener != null) {
			onPlayerChangeListener.onCaptureChanged(this.captures);
		}
	}
	
	public void removeCaptures(int captures) {
		if (captures == 0) return;
		this.captures -= captures;
		
		if (onPlayerChangeListener != null) {
			onPlayerChangeListener.onCaptureChanged(this.captures);
		}
	}
	
	public int getCaptures() {
		return captures;
	}
	
	public void setOnPlayerChangeListener(OnPlayerChangeListener listener) {
		this.onPlayerChangeListener = listener;
		
		if (onPlayerChangeListener != null) {
			onPlayerChangeListener.onCaptureChanged(captures);
		}
	}
	
	public static interface OnPlayerChangeListener {
		void onCaptureChanged(int newCaptures);
	}
}
