package com.idunnololz.igo;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.idunnololz.utils.LogUtils;

public class Parser {
	private static final String TAG = Parser.class.getSimpleName();
	
	private static class Lexer {
		private static final char[][] FSM = new char[256][];
		
		private static final char STATE_ERROR = 0;
		private static final char STATE_START = 1;
		private static final char STATE_OUTER_GAME_TREE = 2;
		private static final char STATE_NODE = 3;
		private static final char STATE_PROP_ID = 4;
		private static final char STATE_PROP_VAL_START = 5;
		private static final char STATE_PROP_VAL = 6;
		private static final char STATE_PROP_VAL_ESCAPE = 7;
		private static final char STATE_PROP_VAL_END = 8;
		private static final char STATE_END = 255;
		
		private static final int TOKEN_LPAREN = 0;
		private static final int TOKEN_RPAREN = 1;
		private static final int TOKEN_SEMICOLON = 2;
		private static final int TOKEN_PROP_ID = 3;
		private static final int TOKEN_LBRACKET = 4;
		private static final int TOKEN_RBRACKET = 5;
		private static final int TOKEN_PROP_VAL = 6;
		
		static {
			// init...
			for (int i = 0; i < 256; i++) {
				FSM[i] = new char[256];
			}
			
			assignState(STATE_START, '(', STATE_OUTER_GAME_TREE);
			
			assignState(STATE_OUTER_GAME_TREE, ';', STATE_NODE);
			
			assignState(STATE_NODE, 'A', 'Z', STATE_PROP_ID);
			assignState(STATE_NODE, ')', STATE_END);
			
			assignState(STATE_PROP_ID, 'A', 'Z', STATE_PROP_ID);
			assignState(STATE_PROP_ID, '[', STATE_PROP_VAL_START);
			
			assignState(STATE_PROP_VAL_START, (char)0, (char)255, STATE_PROP_VAL);
			assignState(STATE_PROP_VAL_START, '\\', STATE_PROP_VAL_ESCAPE);
			assignState(STATE_PROP_VAL_START, ']', STATE_PROP_VAL_END);
			
			// STATE_PROP_VAL can be anything as long as it is not ],\,:. These must be escaped.
			assignState(STATE_PROP_VAL, (char)0, (char)255, STATE_PROP_VAL);
			assignState(STATE_PROP_VAL, '\\', STATE_PROP_VAL_ESCAPE);
			assignState(STATE_PROP_VAL, ']', STATE_PROP_VAL_END);
			
			assignState(STATE_PROP_VAL_ESCAPE, ']', STATE_PROP_VAL);
			assignState(STATE_PROP_VAL_ESCAPE, '\\', STATE_PROP_VAL);
			assignState(STATE_PROP_VAL_ESCAPE, ':', STATE_PROP_VAL);
			
			assignState(STATE_PROP_VAL_END, 'A', 'Z', STATE_PROP_ID);
			assignState(STATE_PROP_VAL_END, '[', STATE_PROP_VAL);
			assignState(STATE_PROP_VAL_END, ';', STATE_NODE);
			assignState(STATE_PROP_VAL_END, ')', STATE_END);
			
			assignState(STATE_END, '(', STATE_OUTER_GAME_TREE);
		}
		
		private List<Token> tokens;
		
		private static void assignState(char stateOld, char start, char end, char stateNew) {
			for (char i = start; i <= end; i++) {
				FSM[stateOld][i] = stateNew;
			}
		}
		
		private static void assignState(char stateOld, char c, char stateNew) {
			FSM[stateOld][c] = stateNew;
		}
		
		private void tokenize(InputStream in) throws Exception {
			List<Token> tokens = new ArrayList<Token>();
			
			boolean spaced = true;
			
			char c;
			StringBuilder sb = new StringBuilder();
			
			char lastState = 0;
			char state = STATE_START;
			
			while ((c = (char) in.read()) != -1) {
				if (state != STATE_PROP_VAL && Character.isWhitespace(c)) {
					spaced = true;
					continue;
				}
				
				lastState = state;
				
				// transition states...
				state = FSM[state][c];
				
				if (lastState == state) {
					sb.append(c);
				} else {
					if (sb.length() > 0) {
						Token t = new Token(sb.toString());
						switch (lastState) {
						case STATE_OUTER_GAME_TREE:
							t.type = TOKEN_LPAREN;
							break;
						case STATE_NODE:
							t.type = TOKEN_SEMICOLON;
							break;
						case STATE_PROP_ID:
							t.type = TOKEN_PROP_ID;
							break;
						case STATE_PROP_VAL_ESCAPE:
							t.token = t.token.substring(1);
						case STATE_PROP_VAL:
							Token last = tokens.get(tokens.size() - 1);
							if (last.type == TOKEN_PROP_VAL) {
								last.token += t.token;
							} else {
								t.type = TOKEN_PROP_VAL;
							}
							break;
						case STATE_PROP_VAL_START:
							t.type = TOKEN_LBRACKET;
							break;
						case STATE_PROP_VAL_END:
							t.type = TOKEN_RBRACKET;
							break;
						case STATE_END:
							t.type = TOKEN_RPAREN;
							break;
						default:
							throw new TokenException("Unknown token type before token '" + tokens.get(tokens.size() - 1) + "'");
						}
						if (t != null) {
							tokens.add(t);
						}
					}
					sb.setLength(0);
					sb.append(c);
				}
				
				if (state == STATE_ERROR) {
					LogUtils.e(TAG, "Token error! From state " + (int)lastState + " with arg " + (int)c);
					break;
				} else if (state == STATE_END) {
					// done!
					break;
				}
			}
			
			if (sb.length() > 0) {
				Token t = new Token(sb.toString());
				switch (state) {
				case STATE_OUTER_GAME_TREE:
					t.type = TOKEN_LPAREN;
					break;
				case STATE_NODE:
					t.type = TOKEN_SEMICOLON;
					break;
				case STATE_PROP_ID:
					t.type = TOKEN_PROP_ID;
					break;
				case STATE_PROP_VAL_ESCAPE:
					t.token = t.token.substring(1);
				case STATE_PROP_VAL:
					Token last = tokens.get(tokens.size() - 1);
					if (last.type == TOKEN_PROP_VAL) {
						last.token += t.token;
					} else {
						t.type = TOKEN_PROP_VAL;
					}
					break;
				case STATE_PROP_VAL_START:
					t.type = TOKEN_LBRACKET;
					break;
				case STATE_PROP_VAL_END:
					t.type = TOKEN_RBRACKET;
					break;
				case STATE_END:
					t.type = TOKEN_RPAREN;
					break;
				default:
					throw new TokenException("Unknown token type before token '" + tokens.get(tokens.size() - 1) + "'");
				}
				if (t != null) {
					tokens.add(t);
				}
			}

			sb.setLength(0);
			sb = sb.append("[\"");
			for (Token s : tokens) {
				sb.append(s.token);
				sb.append("\", \"");
			}
			sb.append("\"]");
			LogUtils.d(TAG, "Tokens processed: " + sb.toString());
			
			this.tokens = tokens;
		}
		
		private int position = 0;
		
		public Node makeTree() throws Exception {
			position = 0;
			
			if (tokens.get(position++).type == TOKEN_LPAREN) {
				return makeSequence(null);
			} else {
				throw new InvalidSyntaxException("Root of GAME_TREE must begin with '('");
			}
		}
		
		private Node makeSequence(Node parent) throws InvalidSyntaxException {
			if (tokens.get(position++).type == TOKEN_SEMICOLON) {
				return makeNode(parent);
			}
			throw new InvalidSyntaxException("Root of SEQUENCE must begin with ';'");
		}
		
		private Node makeNode(Node parent) throws InvalidSyntaxException {
			Node n = new Node();
			n.parent = parent;
			
			OUT:
			while (true) {
				if (position >= tokens.size()) {
					throw new InvalidSyntaxException("Unexpected end of tokens reached");
				}

				Token t = tokens.get(position++);
				
				switch (t.type) {
				case TOKEN_RPAREN:
					break OUT;
				case TOKEN_PROP_ID:
					addKvPair(n, t);
					break;
				case TOKEN_SEMICOLON:
					n.children.add(makeNode(n));
					break OUT;
				default:
					throw new InvalidSyntaxException("Unexpected token '" + t.token + "' in SEQUENCE");
				}
			}
		
			return n;
		}
		
		private void addKvPair(Node n, Token k) throws InvalidSyntaxException {
			Token v = getValueToken();
			
			n.args.put(k.token, v.token);
		}
		
		private Token getValueToken() throws InvalidSyntaxException {
			if (position + 3 >= tokens.size()) {
				throw new InvalidSyntaxException("Invalid expected value property");
			}
			
			Token vStart = tokens.get(position++);
			Token v = tokens.get(position++);
			Token vEnd = tokens.get(position++);
			
			if (vStart.type == TOKEN_LBRACKET && v.type == TOKEN_PROP_VAL && vEnd.type == TOKEN_RBRACKET) {
				return v;
			} else {
				throw new InvalidSyntaxException("Value property must be enclosed in brackets");
			}
		}
		
		public class TokenException extends Exception {
			/**
			 * 
			 */
			private static final long serialVersionUID = 3678434866994463407L;

			public TokenException(String message) {
				super(message);
			}
		}
		
		public class InvalidSyntaxException extends Exception {
			/**
			 * 
			 */
			private static final long serialVersionUID = 723279729407564527L;

			public InvalidSyntaxException(String message) {
				super(message);
			}
		}
		
		private class Token {
			String token;
			int type;
			
			public Token(String t) {
				token = t;
			}
		}
	}
	
	public static class Node {
		private Node parent;
		
		private Map<String, String> args = new HashMap<String, String>();
		private LinkedList<Node> children = new LinkedList<Node>();
		
		public Node getParent() {
			return parent;
		}
		
		public String get(String key) {
			return args.get(key);
		}
		
		public int getInt(String key, int def) {
			if (args.containsKey(key)) {
				return Integer.valueOf(args.get(key));
			}
			return def;
		}
		
		public Float getFloat(String key, float def) {
			if (args.containsKey(key)) {
				return Float.valueOf(args.get(key));
			}
			return def;
		}
		
		public Map<String, String> getArgs() {
			return args;
		}
		
		public boolean hasChild() {
			return children.size() > 0;
		}
		
		public Node getChild() {
			return children.getFirst();
		}
	}

	public Node load(String sgfFile) throws IOException {
		File file = new File(sgfFile);

		Node root = null;
		
		InputStream in = null;
		try {
			in = new BufferedInputStream(new FileInputStream(file));
			// We currently only support single branch game path...
			
			Lexer l = new Lexer();
			try {
				l.tokenize(in);
				root = l.makeTree();
			} catch (Exception e) {
				LogUtils.e(TAG, "", e);
			}
		} finally {
			if (in != null) {
				in.close();
			}
		}
		
		return root;
	}
}
