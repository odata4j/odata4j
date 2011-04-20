package org.odata4j.producer.jpa.oneoff03;

import java.io.Serializable;
import java.util.HashSet;

import javax.persistence.*;

@Entity
@Table(name = "Student")
public class Student implements Serializable {
	private static final long serialVersionUID = 1L;
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Basic(optional = false)
	@Column(name = "StudentID")
	private Integer StudentID;
	
	@ManyToMany(targetEntity=Course.class, cascade=CascadeType.ALL)
	@JoinTable(name="Student_Course",joinColumns = @JoinColumn(name="StudentID"), inverseJoinColumns = @JoinColumn(name = "CourseID")) 
    private java.util.Collection<Course> courses = new HashSet<Course>();
	
	@Basic(optional = false)
	@Column(name = "StudentName")
	private String StudentName;
	
	public String getStudentName() {
		return StudentName;
	}

	public void setStudentName(String studentName) {
		StudentName = studentName;
	}

	public Integer getStudentID() {
		return StudentID;
	}

	public void setStudentID(Integer studentID) {
		StudentID = studentID;
	}

	public java.util.Collection<Course> getCourses() {
		return courses;
	}

	public void setCourses(java.util.Collection<Course> courses) {
		this.courses = courses;
	}

	
}
