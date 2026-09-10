/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.assignment1;

/**
 *
 * @author Then Wei Cheng
 */
public class Consultation {
    private String slotID;
    private User user;
    private String timeSlot;
    
    public Consultation(String slotID, User user, String timeSlot) {
        this.slotID = slotID;
        this.user = user;
        this.timeSlot = timeSlot;
    }
    // Getters
    public String getSlotID() {
        return slotID;
    }

    public User getUser() { 
        return user; 
    }

    public String getLectureID() {
        return user.getUserID(); // Delegates to User object
    }

    public String getName() {
        return user.getName(); // Delegates to User object
    }

    public String getTimeSlot() {
        return timeSlot;
    }

    // Setters
    public void setTimeSlot(String timeSlot) {
        this.timeSlot = timeSlot;
    }

    // Convert to array for table usage
    public String[] toArray() {
        return new String[]{slotID, user.getUserID(), user.getName(),timeSlot};
    }
}
