package grader;

import java.util.ArrayList;

class Marks {
	private final ArrayList<Integer> marks;

	public Marks(int numberOfMarks) {
		marks = new ArrayList<Integer>(); // should be 4 for our excercize...??
	}

	public void add(int mark) {
		marks.add(mark);
	}

	public int getFinalMark() {
		return (int) Math.round(marks.stream().reduce(0, Integer::sum) / marks.size());
	}

	public ArrayList<Integer> getMarks() {
		return marks;
	}
}