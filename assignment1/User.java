/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.mycompany.assignment1;

/**
 *
 * @author Then Wei Cheng
 */
public class User {
    private String userid;
    private String username;
    private String password;
    private String email;
    private String role;
    
    public User(String userid, String username, String password, String email, String role){
        this.userid = userid;
        this.username = username;
        this.password = password;
        this.email = email;
        this.role = role;
    }
    
    public String getUserID() {
        return userid;
    }
    
    public String getName() {
        return username;
    }
    
    public String getPassword() {
        return password;
    }
    
    public String getEmail() {
        return email;
    }
    
    public String getRole() {
        return role;
    }
    
    public void setUserID(String userid) {
        this.userid = userid;
    }
    
    public void setName(String username){
        this.username = username;
    }
    
    public void setPassword(String password){
        this.password = password;
    }
    
    public void setEmail(String email){
        this.email = email;
    }
    
    public void setRole(String role){
        this.role = role;
    }
}
