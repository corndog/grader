package grader;

class StudentCourse {
	public final String studentId;
	public final String course;

	public  StudentCourse(String id, String crs) {
		studentId = id;
		course = crs;
	}

	@Override
	public boolean equals(Object other) {
		return ((other instanceof StudentCourse) &&
			(studentId.equals(((StudentCourse)other).studentId)) &&
			(course.equals(((StudentCourse)other).course)));
	}

	@Override
	public int hashCode() {
		return studentId.hashCode() + course.hashCode();
	}

	@Override
	public String toString() {
		return studentId + " : " + course;
	}
}