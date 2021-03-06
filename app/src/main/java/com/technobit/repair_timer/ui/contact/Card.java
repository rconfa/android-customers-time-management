package com.technobit.repair_timer.ui.contact;

import com.technobit.repair_timer.repositories.contact.Contact;

// Class that represent a single contact like a card to be added in the recycle view.
public class Card extends Contact {
    private Boolean isCardSelected; // Save if the card is selected, if true background = green

    public Card(String company_name, String email) {
        super(company_name,email);

        this.isCardSelected = false;
    }

    public Card(Contact contact) {
        super(contact);

        this.isCardSelected = false;
    }

    // get the company name
    public String getCompanyName() {
        return super.getCompany_name();
    }

    // get the email
    @Override
    public String getEmail() {
        return super.getEmail();
    }

    // get and set if the card is selected
    public boolean isCardSelected(){
        return isCardSelected;
    }

    public void setCardSelection(boolean sel){
        this.isCardSelected = sel;
    }
}
