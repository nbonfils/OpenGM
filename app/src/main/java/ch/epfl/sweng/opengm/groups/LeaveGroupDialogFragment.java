package ch.epfl.sweng.opengm.groups;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import ch.epfl.sweng.opengm.R;
import ch.epfl.sweng.opengm.parse.PFGroup;

import static ch.epfl.sweng.opengm.OpenGMApplication.getCurrentUser;
import static ch.epfl.sweng.opengm.groups.MyGroupsActivity.RELOAD_USER_KEY;

public class LeaveGroupDialogFragment extends DialogFragment {

    private PFGroup groupToLeave;

    public LeaveGroupDialogFragment(){
        groupToLeave = null;
    }

    /*public LeaveGroupDialogFragment(PFGroup groupToLeave) {
        this.groupToLeave = groupToLeave;
    }*/

    public void setGroupToLeave(PFGroup groupToLeave){
        this.groupToLeave = groupToLeave;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if(groupToLeave == null){
            throw new UnsupportedOperationException();
        }
        String leaveThisGroupWarning = getString(R.string.leaveGroupWarning);
        leaveThisGroupWarning = leaveThisGroupWarning.replace("[group]", groupToLeave.getName());

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(leaveThisGroupWarning)
                .setPositiveButton(R.string.leaveTheGroup, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Remove the user from this group
                        getCurrentUser().removeFromGroup(groupToLeave.getId());
                        // Go back to MyGroupsActivity
                        Intent intent = new Intent(getActivity(), MyGroupsActivity.class);
                        intent.putExtra(RELOAD_USER_KEY, false);
                        startActivity(intent);
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                });
        return builder.create();
    }
}