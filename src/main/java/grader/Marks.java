package grader;

import java.util.ArrayList;

// some simplistic checking I guess

class Marks {
	public final Integer[] marks;

	public Marks() {
		marks = new Integer[4]; 
	}

	private boolean allFour() {
		return marks[0] != null && marks[1] != null && marks[2] != null && marks[3] != null;
	}

	private boolean firstTwo() {
		return marks[0] != null && marks[1] != null && marks[2] == null && marks[3] == null;
	}

	private boolean lastTwo() {
		return (marks[0] == null && marks[1] == null && marks[2] != null && marks[3] != null);
	}	

	public boolean isComplete() {
		return allFour() || firstTwo() || lastTwo();

	}

	private Integer size() {
		if (allFour())
			return 4;
		else if (firstTwo() || lastTwo())
			return 2;
		else
			return 0;
	}

	// ix goind to be 1 - 4
	public void add(Integer ix, Integer mark) {
		marks[ix -1] = mark;
	}

	// should only call this after isComplete
	public Integer getFinalMark() {
		Integer sum = 0;
		for (int i = 0; i < 4; i++) {
			if (marks[i] != null)
				sum += marks[i];
		}
		Double avg = sum.doubleValue() / size().doubleValue();
		return (int) Math.round(avg);
	}
}