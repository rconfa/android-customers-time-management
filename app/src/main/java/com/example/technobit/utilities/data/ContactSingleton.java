package com.example.technobit.utilities.data;

import android.content.Context;

import java.io.IOException;
import java.util.ArrayList;

// Class that implement Singleton pattern
// store all the contact list that can be used in all class.
public class ContactSingleton {
    // TODO: in catch give back an error to the user.

    private static ContactSingleton instance;

    // Global variable
    private  ArrayList<Contact> clienti;
    private FileContact dc;

    // Restrict the constructor from being instantiated
    private ContactSingleton(Context c){
        dc = new FileContact();

        // Read all contact from file
        try {
            clienti = dc.readFile(c);
        } catch (IOException e) {
            clienti = null;
        }
    }

    public ArrayList<Contact> getContactList(){
        return this.clienti;
    }

    // return an array list with all company_name
    public ArrayList<String> getContactNameList(){

        ArrayList<String> name = new ArrayList<String>();
        if(this.clienti != null)
            for(Contact s:this.clienti)
                name.add(s.getCompany_name());

        return name;
    }

    public void delete(ArrayList<Integer> pos, Context c){
        if(clienti == null)
            return;
        // Remove the client from the file
        try {
            dc.delete(pos,c);
            // If no errors I delete The client also from the list
            for(int index:pos)
                clienti.remove(index);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addContact(Contact c_temp, Context c){
        if(clienti == null)
            clienti = new  ArrayList<Contact>();
        try {
            dc.writeToFile(c_temp, c); // Add the new contact to file
            // if no errors I add the contact to list
            clienti.add(c_temp);
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }

    public void updateContact(Contact toUpdate, int pos, Context c){
        if(clienti == null)
            return;
        // update the contact on file
        try {
            dc.update(toUpdate, pos,c);
            // If no errors I update it also in the list
            clienti.set(pos, toUpdate); // update into client list

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static synchronized ContactSingleton getInstance(Context c){
        if(instance==null){
            instance=new ContactSingleton(c);
        }
        return instance;
    }
}