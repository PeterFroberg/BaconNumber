import java.io.*;

/**
 * Enkel tolk för datat till Kevin Bacon-uppgiften. Inte på långa vägar testad
 * tillräckligt bra, så rapportera alla buggar ni hittar. Den har antagligen
 * problem med namn som börjar med konstiga tecken. Plocka bort dessa från
 * filen. De brukar ligga först.
 * 
 * Användning:
 * <ul>
 * <li>Skapa en instans av BaconReader och skicka med sökvägen till filen ni
 * vill läsa in.
 * <li>Anropa metoden getNextPart tills ni har fått in allt ni vill ha in, ni
 * tröttnar, eller den returerar null. Det sistnämnda betyder att filen är slut.
 * <li>Anropa close för att stänga filen när allt ni vill läsa in är klart.
 * </ul>
 * 
 * Tips: jobba inte med de fulla filerna när ni testar. Det är drygt 16 miljoner
 * rader i de bägge filerna actors.list och actresses.list. Kommandona (linux
 * edyl) head och tail kan vara till nytta för att skapa mer hanterliga
 * datamängder att testa med.
 * 
 * @author henrikbe
 */
public class BaconReader {

	public enum PartType {
		/**
		 * Name of actor or actress
		 */
		NAME,
		/**
		 * Title of movie or show
		 */
		TITLE,
		/**
		 * Year, remember to combine this with the title to get a reasonable ID
		 */
		YEAR,
		/**
		 * Id of a recurring show, might be a date, a name, or any other sort of
		 * ID. The most common one is probably on the format (#1.12) which means
		 * season 1 show 12. You <b>need</b> to use this value to distinguish
		 * between tv shows, otherwise the Bacon number of almost everyone will
		 * be far too low.
		 */
		ID,
		/**
		 * Any extra sort of information. Ignore this.
		 */
		INFO
	}

	public static class Part {
		public PartType type;
		public String text;

		public Part(PartType type, String text) {
			this.type = type;
			this.text = text;
		}

		public String toString() {
			return String.format("[%s, %s]", type, text);
		}
	}

	private boolean atBeginningOfLine;
	private BufferedReader reader;
	private int currentChar;
	private StringBuffer buffer;

	public BaconReader(String file) throws FileNotFoundException, IOException {
		this(new File(file));
	}

	public BaconReader(File file) throws FileNotFoundException, IOException {
		reader = new BufferedReader(new FileReader(file));
		skipToBeginningOfData();
		buffer = new StringBuffer();
	}

	public void close() throws IOException {
		reader.close();
	}

	public Part getNextPart() throws IOException {
		ignoreWhiteSpace();
		switch (currentChar) {
		case -1:
			return null;
		case '(':
			return yearOrExtraInfo();
		case '"':
			return tvTitle();
		case '<':
			return billingPosition();
		case '[':
			return characterName();
		case '{':
			return showId();
		default:
			return nameOrTitle();
		}
	}

	// ////////////////////////////////////////////////////////////////////////
	/*
	 * Alla metoder nedanför är privata och används för att tolka filen. Om du
	 * inte är intresserad av hur klassen jobbar (enkel recurisve descent) så
	 * kan allt nedanför denna rad ignoreras.
	 */
	// ////////////////////////////////////////////////////////////////////////

	private void skipToBeginningOfData() throws IOException {
		String line = reader.readLine();
		while (!line.startsWith("----			------")) {
			line = reader.readLine();
		}
		read();
		atBeginningOfLine = true;
	}

	private void accept() throws IOException {
		buffer.append((char) currentChar);
		read();
	}

	// private void accept(char ch) throws IOException {
	// if (currentChar == ch) {
	// accept();
	// } else {
	// throw new RuntimeException(
	// String
	// .format(
	// "Illegal character found, was %s (%d), but should have been %s (%d)",
	// (char) currentChar, currentChar, ch,
	// (int) ch));
	// }
	// }

	private void acceptTo(char ch) throws IOException {
		while (currentChar != ch) {
			accept();
		}
	}

	private void skip() throws IOException {
		read();
	}

	private void read() throws IOException {
		atBeginningOfLine = currentChar == 10 || currentChar == 13;
		currentChar = reader.read();
	}

	private void ignoreWhiteSpace() throws IOException {
		while (Character.isWhitespace(currentChar)) {
			read();
		}
	}

	private Part getPart(PartType type) {
		Part p = new Part(type, buffer.toString().trim());
		buffer = new StringBuffer();
		return p;
	}

	private Part yearOrExtraInfo() throws IOException {
		skip();
		while (Character.isDigit(currentChar)) {
			accept();
		}
		if (currentChar == ')') {
			skip();
			return getPart(PartType.YEAR);
		}
		acceptTo(')');
		skip();
		return getPart(PartType.INFO);
	}

	private Part tvTitle() throws IOException {
		skip();
		acceptTo('"');
		skip();
		if (buffer.toString().equals("Steff"))
			return name();
		return getPart(PartType.TITLE);
	}

	private Part billingPosition() throws IOException {
		skip();
		acceptTo('>');
		skip();
		return getPart(PartType.INFO);
	}

	private Part characterName() throws IOException {
		skip();
		acceptTo(']');
		skip();
		return getPart(PartType.INFO);
	}

	private Part showId() throws IOException {
		skip();
		acceptTo('}');
		skip();
		return getPart(PartType.ID);
	}

	private Part nameOrTitle() throws IOException {
		if (atBeginningOfLine)
			return name();
		else
			return title();
	}

	private Part name() throws IOException {
		while (currentChar == '-') {
			accept();
		}
		if (buffer.length() > 1) {
			currentChar = -1;
			return null;
		}
		acceptTo('\t');
		return getPart(PartType.NAME);
	}

	private Part title() throws IOException {
		acceptTo('(');
		return getPart(PartType.TITLE);
	}

}
