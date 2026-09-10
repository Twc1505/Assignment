/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.assignment1;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 *
 * @author Then Wei Cheng
 */
public class UserAuthentication {
    private static final String DELIMITER = ",";
    
    public boolean authenticate(User user) {
    try (BufferedReader br = new BufferedReader(new FileReader("user_data.txt"))) {
        String line;
        while ((line = br.readLine()) != null) {
            String[] userData = line.split(DELIMITER);
            if (userData.length == 5) { // Ensure the line has all required fields
                String savedTP = userData[0];      // TP Number
                String savedPassword = userData[2]; // Password
                String savedRole = userData[4];     // Role

                // Check if TP Number and password match
                if (savedTP.equals(user.getUserID()) && savedPassword.equals(user.getPassword())) {
                    user.setRole(savedRole); // Update the user's role dynamically
                    return true; // Successful authentication
                }
            }
        }
    } catch (IOException e) {
        e.printStackTrace();
    }
    return false; // Authentication failed
}

}
