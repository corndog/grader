package grader;

class StudentCourse {
	public final String studentId;
	public final String course;

	public  StudentCourse(String id, String crs) throws Exception {
		if (id == null) 
			throw new Exception("student id should not be null");
		if (crs == null)
			throw new Exception("course should not be null");
		studentId = id;
		course = crs;
	}

	@Override
	public boolean equals(Object other) {
		return ((other instanceof StudentCourse) &&
			(studentId == ((StudentCourse)other).studentId) &&
			(course == ((StudentCourse)other).course));
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