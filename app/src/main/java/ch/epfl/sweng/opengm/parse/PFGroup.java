package ch.epfl.sweng.opengm.parse;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.parse.GetCallback;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ch.epfl.sweng.opengm.OpenGMApplication;
import ch.epfl.sweng.opengm.identification.ImageOverview;
import ch.epfl.sweng.opengm.utils.Alert;

import static ch.epfl.sweng.opengm.parse.PFConstants.GROUP_TABLE_DESCRIPTION;
import static ch.epfl.sweng.opengm.parse.PFConstants.GROUP_TABLE_EVENTS;
import static ch.epfl.sweng.opengm.parse.PFConstants.GROUP_TABLE_ISPRIVATE;
import static ch.epfl.sweng.opengm.parse.PFConstants.GROUP_TABLE_NAME;
import static ch.epfl.sweng.opengm.parse.PFConstants.GROUP_TABLE_PICTURE;
import static ch.epfl.sweng.opengm.parse.PFConstants.GROUP_TABLE_ROLES;
import static ch.epfl.sweng.opengm.parse.PFConstants.GROUP_TABLE_SURNAMES;
import static ch.epfl.sweng.opengm.parse.PFConstants.GROUP_TABLE_TITLE;
import static ch.epfl.sweng.opengm.parse.PFConstants.GROUP_TABLE_USERS;
import static ch.epfl.sweng.opengm.parse.PFConstants.OBJECT_ID;
import static ch.epfl.sweng.opengm.parse.PFConstants.USER_TABLE_GROUPS;
import static ch.epfl.sweng.opengm.parse.PFConstants.USER_TABLE_PICTURE;
import static ch.epfl.sweng.opengm.parse.PFUtils.checkArguments;
import static ch.epfl.sweng.opengm.parse.PFUtils.checkNullArguments;
import static ch.epfl.sweng.opengm.parse.PFUtils.convertFromJSONArray;
import static ch.epfl.sweng.opengm.parse.PFUtils.listToArray;
import static ch.epfl.sweng.opengm.parse.PFUtils.objectToArray;
import static ch.epfl.sweng.opengm.parse.PFUtils.objectToString;
import static ch.epfl.sweng.opengm.parse.PFUtils.retrieveFileFromServer;

public class PFGroup extends PFEntity {

    private final static String PARSE_TABLE_GROUP = PFConstants.GROUP_TABLE_NAME;

    private final static int IDX_NAME = 0;
    private final static int IDX_USERS = 1;
    private final static int IDX_EVENTS = 2;
    private final static int IDX_DESCRIPTION = 3;
    private final static int IDX_PRIVACY = 4;
    private final static int IDX_PICTURE = 5;

    private final HashMap<String, PFMember> mMembers;
    private final List<PFEvent> mEvents;

    private int nOfUsers;
    private String mName;
    private String mDescription;
    private boolean mIsPrivate;
    private Bitmap mPicture;

    private PFGroup(String mId, String name, List<String> users, List<String> surnames, List<String[]> roles, List<String> events, boolean isPrivate, String description, Bitmap picture) {
        super(mId, PARSE_TABLE_GROUP);
        if (users == null || surnames == null || roles == null || events == null ||
                users.size() < 0 || surnames.size() < 0 || roles.size() < 0 || events.size() < 0) {
            throw new IllegalArgumentException("One of the array has a negative size or is null");
        }
        nOfUsers = users.size();
        mMembers = new HashMap<>();

        Log.v("DEBUT", "=======> PFGROUP NAME " + name);

        for (int i = 0; i < users.size(); i++) {
            try {
                //  TODO FIX ROLE
                PFMember member = new PFMember.Builder(users.get(i), surnames.get(i), new String[0]).build();
                mMembers.put(users.get(i), member);
            } catch (PFException e) {
                // TODO : what to do?
            }
        }
        mEvents = new ArrayList<>();
        for (String s : events) {
            mEvents.add(new PFEvent.Builder(s).build());
        }
        mName = name;
        mIsPrivate = isPrivate;
        mDescription = description;
        mPicture = picture;
    }

    @Override
    protected void updateToServer(final int idx) throws PFException {
        ParseQuery<ParseObject> query = ParseQuery.getQuery(GROUP_TABLE_NAME);
        query.getInBackground(getId(), new GetCallback<ParseObject>() {
            public void done(ParseObject object, ParseException e) {
                if (e == null) {
                    if (object != null) {
                        switch (idx) {
                            case IDX_NAME:
                                object.put(GROUP_TABLE_TITLE, mName);
                                break;
                            case IDX_USERS:
                                int idx = 0;
                                String[] users = new String[nOfUsers];
                                String[] surnames = new String[nOfUsers];
                                Object[] roles = new Object[nOfUsers];
                                for (PFMember member : mMembers.values()) {
                                    users[idx] = member.getId();
                                    surnames[idx] = member.getSurname();
                                    roles[idx++] = member.getRoles().toArray();
                                }
                                object.put(GROUP_TABLE_USERS, users);
                                object.put(GROUP_TABLE_SURNAMES, surnames);
                                object.put(GROUP_TABLE_ROLES, roles);
                                break;
                            case IDX_EVENTS:
                                object.put(GROUP_TABLE_EVENTS, listToArray(mEvents));
                                break;
                            case IDX_DESCRIPTION:
                                object.put(GROUP_TABLE_DESCRIPTION, mDescription);
                                break;
                            case IDX_PRIVACY:
                                object.put(GROUP_TABLE_ISPRIVATE, mIsPrivate);
                                break;
                            case IDX_PICTURE:
                                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                                mPicture.compress(Bitmap.CompressFormat.PNG, 100, stream);
                                byte[] image = stream.toByteArray();
                                ParseFile file = new ParseFile(String.format("group%s.png", getId()), image);
                                file.saveInBackground();
                                object.put(GROUP_TABLE_PICTURE, mPicture);
                                break;
                            default:
                                return;
                        }
                        object.saveInBackground(new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                if (e == null) {
                                    // throw new ParseException("No object for the selected id.");
                                }
                            }
                        });
                    } else {
                        // throw new ParseException("No object for the selected id.");
                    }
                } else {
                    // throw new ParseException("Error while sending the request to the server");
                }
            }
        });
    }

    public String getName() {
        return mName;
    }

    /*
        public List<PFUser> getUsers() {
            List<PFUser> users = new ArrayList<>();
            for (PFMember member : mMembers.values()) {
                users.add(member.getUser());
            }
            return users;
        }
    */
    public String getSurnameForUser(String user) {
        PFMember member = mMembers.get(user);
        if (member != null) {
            return member.getSurname();
        }
        return null;
    }

    public List<String> getRolesForUser(String user) {
        PFMember member = mMembers.get(user);
        if (member != null) {
            return member.getRoles();
        }
        return null;
    }

    public List<String> getRoles() {
        Set<String> roles = new HashSet<>();
        for (PFMember member : mMembers.values()) {
            roles.addAll(member.getRoles());
        }
        return new ArrayList<>(roles);
    }


    public void addUser(String user) {
        try {
            PFUser usr = new PFUser.Builder(user).build();
            addUser(usr);
        } catch (PFException e) {
            Alert.displayAlert("Error while adding the user to the server.");
        }
    }

    public void addUser(PFUser user) {
        if (mMembers.containsKey(user.getId())) {
            Alert.displayAlert("User already belongs to this group.");
        } else {
        /*      Member member = new Member(user);
                user.addToAGroup(getId());
                mMembers.put(user.getId(), member);
                */
            try {
                updateToServer(IDX_USERS);
                nOfUsers++;
            } catch (PFException e) {
                mMembers.remove(user.getId());
                Alert.displayAlert("Error while updating the user's groups to the server.");
            }
        }
    }


    public void removeUser(String user) {
        try {
            PFUser usr = new PFUser.Builder(user).build();
            removeUser(usr);
        } catch (PFException e) {
            Alert.displayAlert("Error while removing the user to the server.");
        }
    }

    public void removeUser(PFUser user) {
        if (!mMembers.containsKey(user.getId())) {
            Alert.displayAlert("User does not belong to this group.");
        } else {
            //                    Member oldMember = mMembers.remove(user.getId());
            user.removeFromGroup(getId());
            try {
                updateToServer(IDX_USERS);
                nOfUsers--;
            } catch (PFException e) {
                //                          mMembers.put(user.getId(), oldMember);
                Alert.displayAlert("Error while updating the user's groups to the server.");
            }
        }
    }

    /*
                    public void addRoleToUser(String role, PFUser user) {
                        if (checkNullArguments(role, "Role for user")) {
                            if (!mMembers.containsKey(user.getId())) {
                                Alert.displayAlert("User does not belong to this group.");
                            } else {
                                Member member = mMembers.get(user.getId());
                                member.addRole(role);
                                try {
                                    updateToServer(IDX_USERS);
                                } catch (PFException e) {
                                    member.removeRole(role);
                                    Alert.displayAlert("Error while updating the user's groups to the server.");
                                }
                            }
                        }
                    }

                    public void removeRoleToUser(String role, PFUser user) {
                        if (checkNullArguments(role, "Role for user")) {
                            if (!mMembers.containsKey(user.getId())) {
                                Alert.displayAlert("User does not belong to this group.");
                            } else {
                                Member member = mMembers.get(user.getId());
                                member.removeRole(role);
                                try {
                                    updateToServer(IDX_USERS);
                                } catch (PFException e) {
                                    member.addRole(role);
                                    Alert.displayAlert("Error while updating the user's groups to the server.");
                                }
                            }
                        }
                    }
                */
    public void setName(String name) {
        if (checkArguments(name, "Group's name")) {
            String oldTitle = mName;
            this.mName = name;
            try {
                updateToServer(IDX_NAME);
            } catch (PFException e) {
                this.mName = oldTitle;
                Alert.displayAlert("Error while updating the group's title to the server.");
            }
        }
    }

    public void setDescription(String description) {
        if (checkNullArguments(description, "Group's description")) {
            String oldDescription = description;
            this.mDescription = description;
            try {
                updateToServer(IDX_DESCRIPTION);
            } catch (PFException e) {
                this.mDescription = oldDescription;
                Alert.displayAlert("Error while updating the description to the server.");
            }

        }
    }

    public void setPrivacy(boolean isPrivate) {
        if (mIsPrivate != isPrivate) {
            this.mIsPrivate = !mIsPrivate;
            try {
                updateToServer(IDX_PRIVACY);
            } catch (PFException e) {
                this.mIsPrivate = !mIsPrivate;
                Alert.displayAlert("Error while changing the privacy to the server.");
            }
        }
    }

    public void setPicture(Bitmap picture) {
        if (!mPicture.equals(picture)) {
            Bitmap oldPicture = mPicture;
            this.mPicture = picture;
            try {
                updateToServer(IDX_PICTURE);
            } catch (PFException e) {
                this.mPicture = oldPicture;
                Alert.displayAlert("Error while updating the picture to the server.");
            }
        }
    }

    public static class Builder extends PFEntity.Builder implements ImageOverview {

        private final List<String> mUsers;
        private final List<String> mSurnames;
        private final List<String[]> mRoles;
        private final List<String> mEvents;

        private String mName;
        private String mDescription;
        private boolean mIsPrivate;
        private Bitmap mPicture;

        public Builder(PFUser user, String name) {
            super(null);
            if (name == null || name.isEmpty()) {
                throw new IllegalArgumentException("Group title should not be empty");
            }

            mUsers = new ArrayList<>();
            mSurnames = new ArrayList<>();
            mRoles = new ArrayList<>();

            mUsers.add(user.getId());
            mSurnames.add(user.getUsername());
            mRoles.add(new String[1]);
            mEvents = new ArrayList<>();
            mName = name;
            mIsPrivate = false;
            mDescription = "";
            mPicture = null;
        }

        public Builder(String id) {
            super(id);
            Log.v("DEBUT", "BUILDER GROUP" + id);
            mUsers = new ArrayList<>();
            mSurnames = new ArrayList<>();
            mRoles = new ArrayList<>();
            mEvents = new ArrayList<>();
        }

        private void setName(String name) {
            this.mName = name;
        }

        private void setUsers(String[] o) {
            this.mUsers.addAll(Arrays.asList(o));
        }

        private void setSurnames(String[] o) {
            this.mSurnames.addAll(Arrays.asList(o));
        }

        private void setRoles(String[] o) {
        }

        private void setEvents(Object[] o) {
            for (Object obj : o) {
                mEvents.add((String) obj);
            }
        }

        private void setPrivacy(boolean b) {
            this.mIsPrivate = b;
        }

        private void setDescription(String s) {
            this.mDescription = s;
        }

        @Override
        public void setImage(Bitmap bitmap) {
            this.mPicture = bitmap;
        }

        @Override
        public void retrieveFromServer() throws PFException {
            if (mId != null) {
                ParseQuery<ParseObject> query = ParseQuery.getQuery(PARSE_TABLE_GROUP);
                query.whereEqualTo(OBJECT_ID, mId);
                try {
                    ParseObject object = query.getFirst();
                    if (object != null) {
                        setName(object.getString(GROUP_TABLE_TITLE));
                        setPrivacy(object.getBoolean(GROUP_TABLE_ISPRIVATE));

                        String[] users = convertFromJSONArray(object.getJSONArray(GROUP_TABLE_USERS));
                        setUsers(users);

                        String[] surnames = convertFromJSONArray(object.getJSONArray(GROUP_TABLE_SURNAMES));
                        setSurnames(surnames);

                        // setRoles(objectToArray(object.get(GROUP_TABLE_ROLES)));

                        String[] events = convertFromJSONArray(object.getJSONArray(GROUP_TABLE_EVENTS));
                        setEvents(events);

                        setDescription(object.getString(GROUP_TABLE_DESCRIPTION));

                        retrieveFileFromServer(object, GROUP_TABLE_PICTURE, this);

                    } else {
                        throw new PFException("Query failed");
                    }
                } catch (ParseException e) {
                    throw new PFException("Query failed");
                }
            } else {
                final ParseObject object = new ParseObject(GROUP_TABLE_NAME);
                object.put(GROUP_TABLE_USERS, mUsers.toArray());
                object.put(GROUP_TABLE_SURNAMES, mSurnames.toArray());
                object.put(GROUP_TABLE_ROLES, mRoles.toArray());
                object.put(GROUP_TABLE_EVENTS, mEvents.toArray());
                object.put(GROUP_TABLE_TITLE, mName);
                object.put(GROUP_TABLE_DESCRIPTION, mDescription);
                object.put(GROUP_TABLE_ISPRIVATE, mIsPrivate);
                object.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e == null) {
                            setId(object.getObjectId());
                        } else {
                            // throw new PFException("Query failed");
                        }
                    }
                });
            }
        }

        public PFGroup build() throws PFException {
            retrieveFromServer();
            return new PFGroup(mId, mName, mUsers, mSurnames, mRoles, mEvents, mIsPrivate, mDescription, mPicture);
        }

    }
}