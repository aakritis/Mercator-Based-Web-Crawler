package edu.upenn.cis455.storage;

import java.util.ArrayList;
import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;

@Entity
public class Users {

    @PrimaryKey
    private String username;
    private String name;
    private String password;
    ArrayList<String> channels;

    public void setUsername(String data) {
        username = data;
    }

    public String getUsername() {
        return username;
    }
    
    public void setName(String data) {
        name = data;
    }

    public String getName() {
        return name;
    }
    
    public void setPassword(String data) {
        password = data;
    }

    public String getPassword() {
        return password;
    }
    
    public void setChannels(ArrayList<String> c) {
    	channels = c;
    }
    
    public ArrayList<String> getChannels() {
    	return channels;
    }
} 

