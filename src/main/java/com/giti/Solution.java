package com.giti;

import java.util.Objects;

public class Solution {
class Course{
    int label;
    boolean visited;

    Course(int label, boolean visited){
        this.label = label;
        this.visited = visited;
    }

    public String toString(){
        return String.valueOf(label);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Course)) return false;
        Course course = (Course) o;
        return label == course.label;
    }

    @Override
    public int hashCode() {
        return Objects.hash(label);
    }
}
    public int[] findOrder(int numCourses, int[][] prerequisites) {
        int l =prerequisites.length;
        return new int[l];
    }
}