/*
 * Chord Shift - Simple multiline editor for shifting your chords.
 * Copyright (C) 2015  Valerio Bozzolan and contributors
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

package it.reyboz.chordshift;

public class ChordShift {
	/**
	 * E.g.: La, La#, Si, Do..
	 */
	public final static int NAMING_CONVENTION_NEOLATIN = 0;

	/**
	 * E.g.: A, A#, B, C..
	 */
	public final static int NAMING_CONVENTION_ENGLISH = 1;

	/**
	 * For the "diesis" support.
	 */
	public final static boolean HAVE_DIESIS = true;

	/**
	 * For the "bemolle" support (Sib=La#, ..)
	 */
	public final static boolean HAVE_BEMOLLE = true;

	public final static String[][] DEFAULT_ENGLISH = {{"A", "A#", "B", "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#"}, {"A", "Bb", "B", "C", "Db", "D", "Eb", "E", "F", "Gb", "G", "Ab"}};
	public final static String[][] DEFAULT_NEOLATIN = {{"La", "La#", "Si", "Do", "Do#", "Re", "Re#", "Mi", "Fa", "Fa#", "Sol", "Sol#"}, {"La", "Sib", "Si", "Do", "Reb", "Re", "Mib", "Mi", "Fa", "Solb", "Sol", "Lab"}};
	public final static int DIESIS = 0;
	public final static int BEMOLLE = 1;

	private final static boolean DEFAULT_HAVE_DIESIS = HAVE_DIESIS; // As default, only diesis
	private final static boolean DEFAULT_HAVE_BEMOLLE = ! HAVE_BEMOLLE; // As default no bemolle
	private final static int DEFAULT_OUTPUT = DIESIS;

	private int namingConvention;
	private boolean haveDiesis;
	private boolean haveBemolle;

	/**
	 * Create a ChordShift object.
	 * @param namingConvention Choose the naming convention between ChordShift.DEFAULT_ENGLISH and ChordShift.DEFAULT_LATIN
	 * @param haveDiesis Enable the diesis support
	 * @param haveBemolle Enable the "bemolle" support (Sib=La#, ..)
	 */
	public ChordShift(int namingConvention, boolean haveDiesis, boolean haveBemolle) throws ExceptionInInitializerError {
		if(!isValidNamingConvention(namingConvention)) {
			throw new ExceptionInInitializerError("Naming convention unknown");
		}
		this.namingConvention = namingConvention;
		this.haveBemolle = haveBemolle;
		this.haveDiesis = haveDiesis;
	}

	public ChordShift(int namingConvention) throws ExceptionInInitializerError {
		this(namingConvention, DEFAULT_HAVE_DIESIS, DEFAULT_HAVE_BEMOLLE);
	}

	/**
	 * E.g:
	 * La#7 => index=1; suffix="7"
	 */
	public static class Note {

		private int index;
		private String suffix;

		/**
		 * From the note index.
		 * @param index Note index 0-12
		 * @param suffix
		 */
		public Note(int index, String suffix) {
			this.index = index;
			this.suffix = suffix;
		}

		public Note(int noteIndex) {
			this(noteIndex, "");
		}

		/**
		 * From the note name.
		 * @param noteName The note
		 * @param namingConvention Choose the naming convention between ChordShift.DEFAULT_ENGLISH and ChordShift.DEFAULT_LATIN
		 * @param haveDiesis Enable the diesis support
		 * @param haveBemolle Enable the bemolle support
		 * @throws Exception
		 */
		public Note(String noteName, int namingConvention, boolean haveDiesis, boolean haveBemolle) throws Exception {
			Note note = toNote(noteName, namingConvention, haveDiesis, haveBemolle);
			this.index = note.getIndex();
			this.suffix = note.getSuffix();
		}

		/**
		 * Get the note index.
		 * @param noteName The note
		 * @param namingConvention Choose the naming convention between ChordShift.DEFAULT_ENGLISH and ChordShift.DEFAULT_LATIN
		 * @return The note object
		 * @throws Exception
		 */
		public static Note toNote(String noteName, int namingConvention, boolean haveDiesis, boolean haveBemolle) throws Exception {
			noteName = getUCFirst(noteName);

			int noteNameLength = noteName.length();

			String[][] defaultNotes = getDefaultNotesFromNamingConvention(namingConvention);

			int defaultNotesLength = defaultNotes[DIESIS].length; // = defaultNotes[BEMOLLE].length

			if(haveDiesis) {
				for (int i = defaultNotesLength - 1; i >= 0; i--) {
					int defaultDiesisNoteLength = defaultNotes[DIESIS][i].length();

					// For DIESIS
					if (noteNameLength == defaultDiesisNoteLength) {
						if (noteName.equals(defaultNotes[DIESIS][i])) {
							return new Note(i);
						}
					} else if (noteNameLength > defaultDiesisNoteLength) {
						String notePrefix = noteName.substring(0, defaultDiesisNoteLength);
						if (notePrefix.equals(defaultNotes[DIESIS][i])) {
							return new Note(i, noteName.substring(defaultDiesisNoteLength));
						}
					}
				}
			}

			if(haveBemolle) {
				for (int i = 0; i < defaultNotesLength; i++) {
					int defaultBemolleNoteLength = defaultNotes[BEMOLLE][i].length();

					// For BEMOLLE
					if (noteNameLength == defaultBemolleNoteLength) {
						if (noteName.equals(defaultNotes[BEMOLLE][i])) {
							return new Note(i);
						}
					} else if (noteNameLength > defaultBemolleNoteLength) {
						String notePrefix = noteName.substring(0, defaultBemolleNoteLength);
						if (notePrefix.equals(defaultNotes[BEMOLLE][i])) {
							return new Note(i, noteName.substring(defaultBemolleNoteLength));
						}
					}
				}
			}

			throw new Exception("Note " + noteName + " not found");
		}

		/**
		 * Get the string in upper case first.
		 * See in PHP http://php.net/ucfirst
		 * @param s The string
		 * @return The string with the upper case first
		 */
		private static String getUCFirst(String s) {
			int length = s.length();
			if(s.length() > 0) {
				return Character.toUpperCase(s.charAt(0)) + s.substring(1);
			}
			return s;
		}

		public int getIndex() {
			return index;
		}

		public String getSuffix() {
			return suffix;
		}
	}

	/**
	 * Retrieve the default notes from a naming convention.
	 * @param namingConvention Choose the naming convention between ChordShift.DEFAULT_ENGLISH and ChordShift.DEFAULT_LATIN
	 * @return The notes
	 */
	private static String[][] getDefaultNotesFromNamingConvention(int namingConvention) throws IllegalArgumentException {
		switch (namingConvention) {
			case NAMING_CONVENTION_NEOLATIN:
				return DEFAULT_NEOLATIN;
			case NAMING_CONVENTION_ENGLISH:
				return DEFAULT_ENGLISH;
			default:
				throw new IllegalArgumentException("Unexpected naming convention");
		}
	}

	/**
	 * Retrieve the default notes.
	 * @return The notes
	 */
	private String[][] getDefaultNotesFromNamingConvention() {
		return getDefaultNotesFromNamingConvention(namingConvention);
	}

	/**
	 * Own exception to say where there is the error in the music sheet
	 */
	public static class RotationException extends Exception {
		private int wrongStartChr;
		private int wrongEndChr;
		private int wrongLine;
		private String wrongNote;

		public RotationException(int wrongStartChr, int wrongEndChr, int wrongLine, String wrongNote) {
			super("Unable to parse all notes");
			this.wrongStartChr = wrongStartChr;
			this.wrongEndChr = wrongEndChr;
			this.wrongLine = wrongLine;
			this.wrongNote = wrongNote;
		}

		public int getWrongStartChr() {
			return wrongStartChr;
		}

		public int getWrongEndChr() {
			return wrongEndChr;
		}

		public int getWrongLine() {
			return wrongLine;
		}

		public String getWrongNote() {
			return wrongNote;
		}
	}

	/**
	 * Converts from one naming convention to another shifting by a rotate factor and making a coffee.
	 * @param multiLineChords The music sheet
	 * @param namingConventionStart Initial naming convention
	 * @param namingConventionEnd Final naming convention
	 * @param rotateFactor semitones (0-12)
	 * @param haveDiesis Enable the diesis support
	 * @param haveBemolle Enable the bemolle support
	 * @param forceHavingDiesisOrBemolle Output with diesis or with bemolle
	 * @return The music sheet converted and shifted
	 */
	public static String getRotatedFromNamingConventionToAnother(String multiLineChords, int namingConventionStart, int namingConventionEnd, int rotateFactor, int forceHavingDiesisOrBemolle, boolean haveDiesis, boolean haveBemolle) throws Exception {
		if(forceHavingDiesisOrBemolle != DIESIS && forceHavingDiesisOrBemolle != BEMOLLE) {
			throw new IllegalArgumentException("Unexpected forceHavingDiesisOrBemolle");
		}

		String results = "";

		String[] lines = multiLineChords.split("\n", -1);

		String[][] chordsStart = getDefaultNotesFromNamingConvention(namingConventionStart);
		String[][] chordsEnd = getDefaultNotesFromNamingConvention(namingConventionEnd);
		int chordsLength = chordsStart[DIESIS].length; // = chordsEnd[DIESIS].length = chordsStart[DIESIS].length = chordsEnd[BEMOLLE].length = chordsStart[BEMOLLE]

		// For each line
		for (int i = 0; i < lines.length; i++) {
			String line = lines[i];
			int lineLength = line.length();

			// Skip comments
			if (lineLength > 0 && line.charAt(0) == '#') {
				results += line + "\n";
				continue;
			}

			// For each char in single line
			int j = 0;
			while(j<lineLength) {

				// Skip whitespaces
				if(line.charAt(j) == ' ') {
					results += " ";
					j++;
					continue;
				}

				String note = "";
				int startNote = j;
				while(j < lineLength && line.charAt(j) != ' ') {
					note += line.charAt(j);
					j++;
				}

				if(note.length() == 0) {
					continue;
				}

				int noteIndex = 0, noteIndexShifted = 0; // TMKH
				String noteSuffix = null; // TMKH

				boolean error = false;

				Note noteScraped = null;
				try {
					// Try parsing as the initial naming convention
					noteScraped = new Note(note, namingConventionStart, haveDiesis, haveBemolle); // Throws exception

					noteIndex = noteScraped.getIndex();
					noteSuffix = noteScraped.getSuffix();

					// WHY THE FUCK IN JAVA 5 % 12 = 0!??!!??!!?!??!?
					noteIndexShifted = (chordsLength + noteIndex + rotateFactor) % chordsLength;

					results += chordsEnd[forceHavingDiesisOrBemolle][noteIndexShifted] + noteSuffix;
				} catch (Exception e1) {
					if(namingConventionStart != namingConventionEnd) {
						try {
							// Try parsing as the final naming convention
							noteScraped = new Note(note, namingConventionEnd, haveDiesis, haveBemolle); // Throws exception

							noteIndex = noteScraped.getIndex();
							noteSuffix = noteScraped.getSuffix();

							// WHY THE FUCK IN JAVA 5 % 12 = 0!??!!??!!?!??!?
							noteIndexShifted = (chordsLength + noteIndex + rotateFactor) % chordsLength;

							results += chordsEnd[forceHavingDiesisOrBemolle][noteIndexShifted] + noteSuffix;
						} catch(Exception e2) {
							error = true;
						}
					} else {
						error = true;
					}
				}

				if(error) {
					// Generate the error content
					int parsedChars = 0;
					for(int k=0; k<i; k++) {
						parsedChars += lines[k].length() + 1; // +1 for newline
					}
					parsedChars += startNote;
					throw new RotationException(parsedChars, parsedChars + note.length(), i + 1, note);
				}
			}

			if(i != lines.length -1) {
				results += "\n";
			}
		}
		return results;
	}

	/**
	 * Converts to another shifting by a rotate factor
	 * @param multiLineChords The music sheet
	 * @param namingConventionEnd Final naming convention
	 * @param rotateFactor semitones (0-12)
	 * @param forceHavingDiesisOrBemolle Output with diesis or with bemolle
	 * @return The music sheet converted and shifted
	 */
	public String getRotatedToAnotherNamingConvention(String multiLineChords, int namingConventionEnd, int rotateFactor, int forceHavingDiesisOrBemolle) throws Exception {
		return getRotatedFromNamingConventionToAnother(multiLineChords, namingConvention, namingConventionEnd, rotateFactor, forceHavingDiesisOrBemolle, haveDiesis, haveBemolle);
	}

	/**
	 * Converts to another shifting by a rotate factor
	 * @param multiLineChords The music sheet
	 * @param namingConventionEnd Final naming convention
	 * @param rotateFactor semitones (0-12)
	 * @return The music sheet converted and shifted
	 */
	public String getRotatedToAnotherNamingConvention(String multiLineChords, int namingConventionEnd, int rotateFactor) throws Exception {
		return getRotatedToAnotherNamingConvention(multiLineChords, namingConventionEnd, rotateFactor, DEFAULT_OUTPUT);
	}

	/**
	 * Get shifted chords by a rotate factor
	 * @param multiLineChords The music sheet
	 * @param rotateFactor semitones (0-12)
	 * @param forceHavingDiesisOrBemolle Output with diesis or with bemolle
	 * @return The music sheet rotated
	 */
	public String getRotated(String multiLineChords, int rotateFactor, int forceHavingDiesisOrBemolle) throws Exception {
		return getRotatedFromNamingConventionToAnother(multiLineChords, namingConvention, namingConvention, rotateFactor, forceHavingDiesisOrBemolle, haveDiesis, haveBemolle);
	}

	/**
	 * Get shifted chords by a rotate factor
	 * @param multiLineChords The music sheet
	 * @param rotateFactor semitones (0-12)
	 * @return The music sheet rotated
	 */
	public String getRotated(String multiLineChords, int rotateFactor) throws Exception {
		return getRotated(multiLineChords, rotateFactor, DEFAULT_OUTPUT);
	}

	/**
	 * Get converted to a naming convention.
	 * @param multiLineChords The music sheet
	 * @param namingConventionEnd Final naming convention
	 * @param forceHavingDiesisOrBemolle Output with diesis or with bemolle
	 * @return The music sheet converted
	 */
	public String getConverted(String multiLineChords, int namingConventionEnd, int forceHavingDiesisOrBemolle) throws Exception {
		return getRotatedFromNamingConventionToAnother(multiLineChords, namingConvention, namingConventionEnd, 0, forceHavingDiesisOrBemolle, haveDiesis, haveBemolle);
	}

	/**
	 * Get converted to a naming convention.
	 * @param multiLineChords The music sheet
	 * @param namingConventionEnd Final naming convention
	 * @return The music sheet converted
	 */
	public String getConverted(String multiLineChords, int namingConventionEnd) throws Exception {
		return getConverted(multiLineChords, namingConventionEnd, DEFAULT_OUTPUT);
	}

	/**
	 * Check if a naming convention is valid.
	 * @param namingConvention Choose the naming convention between ChordShift.ENGLISH and ChordShift.LATIN
	 * @return If it's valid
	 */
	public static boolean isValidNamingConvention(int namingConvention) {
		switch(namingConvention) {
			case NAMING_CONVENTION_NEOLATIN:
			case NAMING_CONVENTION_ENGLISH:
				return true;
		}
		return false;
	}
}