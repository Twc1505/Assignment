/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.assignment1;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

/**
 *
 * @author Then Wei Cheng
 */
public abstract class BaseForm extends JFrame{
    public BaseForm() {
        // Set default behaviors for the form
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Center the form on screen
    }
    
    public abstract void initializeComponents();
    
    protected void exitApplication() {
        int response = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to exit?",
            "Confirm Exit",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );

        if (response == JOptionPane.YES_OPTION) {
            System.exit(0); // Exit the application
        }
    }
    
    protected void showMessage(String message) {
        JOptionPane.showMessageDialog(this, message);
    }
    
    protected void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
}

