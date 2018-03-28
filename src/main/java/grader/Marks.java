// package grader;

// import java.util.ArrayList;

// // some simplistic checking I guess

// class Marks {
// 	//private final int requiredNumberOfMarks;
// 	public final ArrayList<Integer> marks;

// 	public Marks() {
// 		//requiredNumberOfMarks = numMarks;
// 		marks = new ArrayList<Integer>(); 
// 	}

// 	public boolean isComplete() {
// 		return marks.size() == 2 || marks.size() == 4;
// 	}

// 	public void add(Integer mark) {
// 		marks.add(mark);
// 	}

// 	public int getFinalMark() {
// 		return (int) Math.round((double) marks.stream().reduce(0, Integer::sum) / marks.size());
// 	}
// }