package com.technobit.repair_timer.ui.contact;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.snackbar.Snackbar;
import com.technobit.repair_timer.R;
import com.technobit.repair_timer.databinding.FragmentContactBinding;
import com.technobit.repair_timer.ui.customize.dialog.ConfirmChoiceDialog;
import com.technobit.repair_timer.ui.customize.dialog.ManageContactDialog;
import com.technobit.repair_timer.utils.Constants;
import com.technobit.repair_timer.utils.contact.Contact;
import com.technobit.repair_timer.utils.contact.ContactSingleton;

import java.io.IOException;
import java.util.ArrayList;

// todo: back arrow when item are selected
public class ContactFragment extends Fragment
        implements CardArrayAdapter.ItemLongClickListener, CardArrayAdapter.ItemClickListener {

    private static final String TAG = "ContactFragment";
    private CardArrayAdapter mCardArrayAdapter;
    private ContactSingleton mContactSingleton;
    private MenuItem mMenuDeleteItem;
    private MenuItem mMenuAddItem;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        // setto true le opzioni del menu, cosi posso visualizare le icone
        setHasOptionsMenu(true);
        FragmentContactBinding mBinding = FragmentContactBinding.inflate(inflater, container,false);
        View view = mBinding.getRoot();

        // save the singleton instance
        mContactSingleton = ContactSingleton.getInstance(getContext());


        // setting the recycle view for the fragment
        mBinding.contactListview.setHasFixedSize(true); // no change the layout size, better performance
        // setting layout
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        mBinding.contactListview.setLayoutManager(layoutManager);

        // adapter for the recycle view
        mCardArrayAdapter = new CardArrayAdapter();
        // add all item to the adapter
        addContactToAdapter();

        // set the long click listener and the click listener equal to my local listener
        mCardArrayAdapter.mySetLongClickListener(this);
        mCardArrayAdapter.mySetClickListener(this);

        // set the adapter for the listview
        mBinding.contactListview.setAdapter(mCardArrayAdapter);

        return view;
    }

    private void addContactToAdapter() {
        // get all contact from file
        ArrayList<Contact> allContact = mContactSingleton.getContactList();
        if(allContact != null) // if there is some contact
            mCardArrayAdapter.add(allContact); // add all list to adapter
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
        // Upload menu file with add/delete icons
        inflater.inflate(R.menu.menu_contact, menu); //.xml file name
        // get the item for delete contact
        mMenuDeleteItem = menu.findItem(R.id.icon_remove);
        mMenuAddItem = menu.findItem(R.id.icon_add);

        super.onCreateOptionsMenu(menu, inflater);
    }

    //Handling Action Bar button click
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.icon_remove:
                performRemovingContact();
                return true;
            case R.id.icon_add:
                displayAddContactDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // long click on recycle view item
    @Override
    public void onItemLongClick(View view, int position) {
        // save the selected position
        mCardArrayAdapter.savePositionToDelete(position);
        mCardArrayAdapter.notifyItemChanged(position); // update the card view (change background color)

        // remove delete icon
        if(mCardArrayAdapter.getPositionToDelete().isEmpty()) {
            mMenuDeleteItem.setVisible(false);
            mMenuAddItem.setVisible(true);
        }
        else {
            mMenuDeleteItem.setVisible(true);
            mMenuAddItem.setVisible(false);
        }
    }

    private void performRemovingContact(){
        SharedPreferences sharedPref = requireContext().getSharedPreferences(
                Constants.CHRONOMETER_SHARED_PREF_FILENAME, Context.MODE_PRIVATE);

        if (sharedPref.contains(Constants.CHRONOMETER_SHARED_PREF_TIME)){
            displaySnackbarError(R.string.snackbar_contact_no_delete_permitted);
            mCardArrayAdapter.clearSelectedCard();
            mMenuDeleteItem.setVisible(false); // remove delete icon
            mMenuAddItem.setVisible(true);
        }
        else {
            // Ask the user to confirm the action
            // get the message from the resource
            String message = getString(R.string.dialog_confirm_delete_message);
            // Create an instance of the dialog fragment and show it
            DialogFragment dialog = new ConfirmChoiceDialog(" ", message, confirmDeleteListener);
            dialog.show(getParentFragmentManager(), TAG);
        }


    }

    // listner for the dialog
    private ConfirmChoiceDialog.NoticeDialogListener confirmDeleteListener = new ConfirmChoiceDialog.NoticeDialogListener() {
        @Override
        public void onDialogPositiveClick() {

            try {
                // sort the array (descending) because I delete the item by position
                // so if the user select pos1, posN when I try to delete the posN
                // the position doesn't exist anymore
                mCardArrayAdapter.sortPosToDelete();
                // remove all items selected from file
                mContactSingleton.delete(mCardArrayAdapter.getPositionToDelete(),getContext());
                // remove all items from the listview
                mCardArrayAdapter.removeSelected();
            } catch (IOException e) {
                displaySnackbarError(R.string.snackbar_file_error);
            }
        }

        @Override
        public void onDialogCancel() {
            mCardArrayAdapter.clearSelectedCard();
            mMenuDeleteItem.setVisible(false); // remove delete icon
            mMenuAddItem.setVisible(true);
        }
    };


    // dialog to add new contact
    private void displayAddContactDialog() {
        // get the title for the dialog
        String title = getString(R.string.dialog_add_contact_title);
        // Create an instance of the dialog fragment and show it
        DialogFragment dialog = new ManageContactDialog(title,"","", -1,
                getContext(), mAddContactListener);
        dialog.show(getParentFragmentManager(), TAG);
    }

    // notice listener on positive button click for add contact
    private ManageContactDialog.NoticeDialogListener mAddContactListener = new ManageContactDialog.NoticeDialogListener() {
        @Override
        public void onDialogPositiveClick(Contact c, int position) {
            try {
                // Add the contact only if the name is not duplicate
                boolean added = mContactSingleton.addContact(c, getContext());
                if (added)
                    mCardArrayAdapter.add(new Card(c));
                else
                    displaySnackbarError(R.string.snackbar_duplicate_contact);
            } catch (IOException e) {
                displaySnackbarError(R.string.snackbar_file_error);
            }

        }
    };


    // click on recycle view item
    @Override
    public void onItemClick(View view, int position, String name, String email) {
        // get the title for the dialog
        String title = getString(R.string.dialog_update_contact_title);
        // Create an instance of the dialog fragment and show it
        DialogFragment dialog = new ManageContactDialog(title,name,email, position,
                getContext(), mUpdateContactListener);
        dialog.show(getParentFragmentManager(), TAG);
    }

    // notice listener on positive button click for contact update
    private ManageContactDialog.NoticeDialogListener mUpdateContactListener = new ManageContactDialog.NoticeDialogListener() {
        @Override
        public void onDialogPositiveClick(Contact c, int position) {
            Card temp = new Card(c);
            try {
                mContactSingleton.updateContact(c, position, getContext());
                mCardArrayAdapter.modify(temp,position);
            } catch (IOException e) {
                displaySnackbarError(R.string.snackbar_file_error);
            }

        }
    };

    private void displaySnackbarError(int stringId){
        // create a snackbar with a positive message
        Snackbar snackbar = Snackbar.make(requireView(), stringId, Snackbar.LENGTH_LONG);
        snackbar.setTextColor(Color.WHITE);
        snackbar.setActionTextColor(getResources().getColor(R.color.colorPrimary))
                .setAction(getString(R.string.snackbar_close_btn), new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                    }
                });
        snackbar.show();
    }
}