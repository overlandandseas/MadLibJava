//JAVA IS FUCKING GARBAGE

import java.util.*; // I   SHOULD
import java.net.*; //  NOT   HAVE
import java.io.*; //   TO DO THIS

public class MadLib_Legacy {

	ArrayList<String> wordsArray;
	ArrayList<Boolean> boolArray;
	short rating;

	// default constructor
	public MadLib_Legacy() {
		System.out.println("You fucked up.");
		// LOOK AT ALL THIS HOT GARBAGE
		System.out.println("Script Verification Failed (Error Code: 332)");
		System.out.println("This was never meant to be written in java.");
		// I SHOULD JUST BE ABLE TO TYPE print "hi" BUT NOOOOO
		System.exit(1);
		return;
	}

	// constructor
	public MadLib_Legacy(String entered) {
		wordsArray = new ArrayList<>();
		// THE FUCK? YOU KNOW THESE ARE CONSTRUCTORS
		boolArray = new ArrayList<>();
		// THEY DON'T NEED <>();
		short count = 0;
		// NO UNSIGNED INTEGERS? FUCK MAN
		StringBuilder wordtemp = new StringBuilder();
		// THE FUCK IS A STRING BUILDER ANYWAY
		for (short c = 0; c <= entered.length(); c++) {
			// WHY DOESN'T for(char c : entered) WORK?
			// I THOUGHT JAVA WAS MEANT FOR THAT
			if (c != entered.length() && entered.charAt(c) != ' ') {
				wordtemp.append(entered.charAt(c));
				// HEAVEN FORBID WE USE += AND [] TO MANIPULATE STRINGS
			} else {
				wordsArray.add(count, wordtemp.toString());
				// wordsArray[cout] = wordtemp DOESN'T WORK WTF
				boolArray.add(count, wordtemp.charAt(0) == '^');
				count++;
				wordtemp = new StringBuilder("");
			}
		} // DO YOU INDENT 2 OR 4 SPACES?
	} // DOESNT'T MATTER AS LONG AS IT LOOKS FUCK UGLY

	public void playNprint() {
		System.out.println(play());
	}

	public String play() {
		StringBuilder temp = new StringBuilder();
		Scanner sc = new Scanner(System.in);

		for (short c = 0; c < wordsArray.size(); c++) {
			if (boolArray.get(c)) {
				System.out.printf("Please enter a "
						+ wordsArray.get(c).substring(1) + ":  ");
				temp.append(sc.nextLine());
				// APPEND
			} else {
				temp.append(wordsArray.get(c));
				// APPEND
			}
			temp.append(" ");
			// APPEND KNIFE TO WRIST

		}
		return temp.toString();
		// .toString()??? YOU KIDDING ME?
	}

	public static void main(String[] args) {
		MadLib a = new MadLib(
				"I don't think that the ^noun is the future to our ^emotion and we should probably invest more time in the ^noun before ^pronoun die.");
		// System.out.println(a.play());
		// MadLib b = new MadLib();
		a.playNprint();
	}
	// HOW MUCH DO YOU LIKE {}{}{B}}}R{{A}}{C}{}K{{{E{}}T}}}S}
}