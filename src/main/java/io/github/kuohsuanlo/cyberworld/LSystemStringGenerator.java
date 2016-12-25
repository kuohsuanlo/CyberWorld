package io.github.kuohsuanlo.cyberworld;

public class LSystemStringGenerator {
	/** Default set of available characters. */
	protected	char []		m_defAlphabet	= {'F', '+', '-', '[', ']'};
	/** Set of available characters. */
	protected	char []		m_alphabet;
	/** Default initial configuration. */
	protected	String		m_defAxiom		= "F";
	/** Initial configuration of L-system. */
	protected	String		m_axiom;
	/** Default production rule. */
	protected	String []	m_defRule		= {"F[+F]F[-F]F", "+", "-", "[", "]"};
	/**
	 * Production rule of each character of the alphabet. There
	 * must exist one rule per character.
	 */
	protected	String []	m_rule;
	/** Container to hold the current state of the L-system. */
	protected	String		m_tree;
	
	/** Constructor. */
	public LSystemStringGenerator() {
		if (getClass() == LSystemStringGenerator.class)
		  init();
	}
	
	public void init() {
		m_alphabet = m_defAlphabet;
		m_axiom		= m_defAxiom;
		int numLetters = m_defRule.length;
		m_rule = new String[numLetters];
		for (int i=0; i<numLetters; i++)
			m_rule[i] = m_defRule[i];
		m_tree = "";
	}
	
	/**
	 * Generate the tree by applying the rules until the
	 * description has a given length.
	 * @param		maxLength		maximal length of tree until we continue to iterate.
	 */
	public String iterate(int maxLength) {
		m_tree = new String(m_axiom);
		int [] ruleLen = new int[m_alphabet.length];
		for (int j=0; j<m_alphabet.length; j++)
			ruleLen[j] = m_rule[j].length();
		for (int num=0; num<maxLength; num++) {
			int len = m_tree.length();
			int newLen = 0;
			for (int i=0; i<len; i++) {
				char c = m_tree.charAt(i);
				for (int j=0; j<m_alphabet.length; j++) {
					if (c == m_alphabet[j]) {
						newLen += ruleLen[j];
						break;
					}
				}
			}

			StringBuffer newTree = new StringBuffer(newLen);
			for (int i=0; i<len; i++) {
				char c = m_tree.charAt(i);
				for (int j=0; j<m_alphabet.length; j++) {
					if (c == m_alphabet[j]) {
						newTree.append(m_rule[j]);
						break;
					}
				}
			}
			m_tree = newTree.toString();
		}
		return m_tree;
	}
	/**
	 * Make the current state of the L-system available, e.g. for
	 * interpreting the string using turtle graphics commands.
	 */
	public String getTree() {
		return m_tree;
	}
	
	public static void main(String[] args) {
		LSystemStringGenerator ls = new LSystemStringGenerator();
		System.out.print(ls.iterate(2));

	}
	
}