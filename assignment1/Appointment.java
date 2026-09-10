/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.assignment1;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 *
 * @author Then Wei Cheng
 */
public class Appointment {
    private String appointmentID;
    private User student; // Represents the student as a User object
    private User lecturer; // Represents the lecturer as a User object
    private Consultation consultation; // Represents the consultation details
    private String bookingDate;
    private LocalDateTime bookingTimestamp;
    private String status; // e.g., Scheduled, Completed, Cancelled
    private String notes;
    private String studentFeedback;
    private String lecturerFeedback;
    private String studentRating;
    private String lecturerRating;


    // Constructor
    public Appointment(String appointmentID, User student, User lecturer, Consultation consultation,
                       String bookingDate, LocalDateTime bookingTimestamp, String status,
                       String notes, String studentFeedback, String studentRating, String lecturerFeedback, String lecturerRating) {
        this.appointmentID = appointmentID;
        this.student = student;
        this.lecturer = lecturer;
        this.consultation = consultation;
        this.bookingDate = bookingDate;
        this.bookingTimestamp = bookingTimestamp;
        this.status = status;
        this.notes = notes;
        this.studentFeedback = studentFeedback;
        this.studentRating = studentRating;
        this.lecturerFeedback = lecturerFeedback;
        this.lecturerRating = lecturerRating;

    }

    // Getters
    public String getAppointmentID() {
        return appointmentID;
    }

    public User getStudent() {
        return student;
    }

    public User getLecturer() {
        return lecturer;
    }

    public Consultation getConsultation() {
        return consultation;
    }

    public String getBookingDate() {
        return bookingDate;
    }

    public LocalDateTime getBookingTimestamp() {
        return bookingTimestamp;
    }

    public String getStatus() {
        return status;
    }

    public String getNotes() {
        return notes;
    }

    public String getStudentFeedback() {
        return studentFeedback;
    }

    public String getLecturerFeedback() {
        return lecturerFeedback;
    }
    
    public String getStudentRating() {
        return studentRating;
    }
    
    public String getLecturerRating() {
        return lecturerRating;
    }

    // Setters
    public void setStatus(String status) {
        this.status = status;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public void setStudentFeedback(String studentFeedback) {
        this.studentFeedback = studentFeedback;
    }

    public void setLecturerFeedback(String lecturerFeedback) {
        this.lecturerFeedback = lecturerFeedback;
    }

    public void setStudentRating(String studentRating) {
        this.studentRating = studentRating;
    }
    
    public void setLecturerRating(String lecturerRating) {
        this.lecturerRating = lecturerRating;
    }
    
    // Convert to array for table usage
    public String[] toArray() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        String formattedBookingTimestamp = bookingTimestamp.format(formatter);

        return new String[]{
            appointmentID,
            student.getUserID(), student.getName(),
            consultation.getSlotID(), lecturer.getUserID(),
            lecturer.getName(), consultation.getTimeSlot(),
            bookingDate, formattedBookingTimestamp, // Use formatted timestamp here
            status, notes, studentFeedback, studentRating, lecturerFeedback, lecturerRating
        };
    }
}
