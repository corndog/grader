package grader


class Marks {
	val marks = mutableListOf<Int>()

	fun isComplete(): Boolean = marks.size == 2 || marks.size == 4

	fun add(mark: Int): Boolean = marks.add(mark)

	fun getFinalMark() : Int = kotlin.math.round(marks.sum().toDouble() / marks.size.toDouble()).toInt()
}