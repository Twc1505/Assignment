/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.assignment1;

/**
 *
 * @author Then Wei Cheng
 */
public class Register_CheckPassword {
    // Regular expressions for password validation
    public static final String REG_NUMBER = ".*\\d.*";
    public static final String REG_UPPERCASE = ".*[A-Z].*";
    public static final String REG_LOWERCASE = ".*[a-z].*";
    public static final String REG_SYMBOL = ".*[~!@#$%^&*()_+|<>?{}\\[\\]\\\\/;:'\".,`=].*";

    public static boolean checkPasswordRule(String password) {
        // Return false if password is null, less than 8 characters, or contains a comma
        if (password == null || password.length() < 8 || password.contains(",")) {
            return false;
        }

        int count = 0;
        if (password.matches(REG_NUMBER)) count++;
        if (password.matches(REG_LOWERCASE)) count++;
        if (password.matches(REG_UPPERCASE)) count++;
        if (password.matches(REG_SYMBOL)) count++;

        return count >= 3; // At least three conditions must be met
    }
}
