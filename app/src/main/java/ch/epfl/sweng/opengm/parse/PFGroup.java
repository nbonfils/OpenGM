package ch.epfl.sweng.opengm.parse;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ch.epfl.sweng.opengm.utils.Alert;

import static ch.epfl.sweng.opengm.parse.PFConstants.GROUP_TABLE_DESCRIPTION;
import static ch.epfl.sweng.opengm.parse.PFConstants.GROUP_TABLE_EVENTS;
import static ch.epfl.sweng.opengm.parse.PFConstants.GROUP_TABLE_ISPRIVATE;
import static ch.epfl.sweng.opengm.parse.PFConstants.GROUP_TABLE_PICTURE;
import static ch.epfl.sweng.opengm.parse.PFConstants.GROUP_TABLE_ROLES;
import static ch.epfl.sweng.opengm.parse.PFConstants.GROUP_TABLE_SURNAMES;
import static ch.epfl.sweng.opengm.parse.PFConstants.GROUP_TABLE_USERS;
import static ch.epfl.sweng.opengm.parse.PFConstants.OBJECT_ID;

import static ch.epfl.sweng.opengm.parse.PFUtils.*;

public class PFGroup extends PFEntity {

    private final static String PARSE_TABLE_GROUP = PFConstants.GROUP_TABLE_NAME;

    private final static int IDX_USERS = 0;
    private final static int IDX_SURNAMES = 1;
    private final static int IDX_ROLES = 2;
    private final static int IDX_EVENTS = 3;
    private final static int IDX_DESCRIPTION = 4;
    private final static int IDX_PRIVACY = 5;
    private final static int IDX_PICTURE = 6;


    private final List<PFUser> mUsers;
    private final List<String> mSurnames;
    private final List<String[]> mRoles;
    private final List<PFEvent> mEvents;

    private String mDescription;
    private boolean mIsPrivate;
    private Bitmap mPicture;

    public PFGroup(String mId, List<String> users, List<String> surnames, List<String[]> roles, List<String> events, boolean isPrivate, String description, Bitmap picture) {
        super(mId, PARSE_TABLE_GROUP);
        if (users == null || surnames == null || roles == null || events == null ||
                users.size() < 0 || surnames.size() < 0 || roles.size() < 0 || events.size() < 0) {
            throw new IllegalArgumentException("One of the array has a negative size or is null");
        }
        mUsers = new ArrayList<>();
        mSurnames = new ArrayList<>();
        mRoles = new ArrayList<>();

        for (int i = 0; i < users.size(); i++) {
            try {
                PFUser user = new PFUser.Builder(users.get(i)).build();
                mUsers.add(user);
                mSurnames.add(surnames.get(i));
                mRoles.add(roles.get(i));
            } catch (PFException e) {
                // TODO : what to do?
            }
        }
        mEvents = new ArrayList<>();
        for (String s : events) {
            mEvents.add(new PFEvent.Builder().build());
        }
        mIsPrivate = isPrivate;
        mDescription = description;
        mPicture = picture;

    }

    @Override
    protected void updateToServer(int idx) throws PFException {

    }

    public List<PFUser> getUsers() {
        return Collections.unmodifiableList(mUsers);
    }

    public String getSurnameForUser(String user) {
        int userIdx;
        if ((userIdx = containsUser(user)) != -1) {
            return mSurnames.get(userIdx);
        }
        return null;
    }

    public String[] getRolesForUser(String user) {
        int userIdx;
        if ((userIdx = containsUser(user)) != -1) {
            return mRoles.get(userIdx);
        }
        return null;

    }

    public List<PFEvent> getmEvents() {
        return Collections.unmodifiableList(mEvents);
    }

    public String getmDescription() {
        return mDescription;
    }

    public void setDescription(String description) {
        if (checkNullArguments(description, "Group's description")) {
            String oldFirstname = description;
            this.mDescription = description;
            try {
                updateToServer(IDX_DESCRIPTION);
            } catch (PFException e) {
                this.mDescription = oldFirstname;
                Alert.displayAlert("Error while updating the description to the server.");
            }

        }
    }

    public boolean isPrivate() {
        return mIsPrivate;
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

    public Bitmap getPicture() {
        return mPicture;
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

    private int containsUser(String user) {
        for (int i = 0; i < mUsers.size(); i++) {
            if (user.equals(mUsers.get(i).getId())) {
                return i;
            }
        }
        return -1;
    }

    public static class Builder extends PFEntity.Builder {

        private final List<String> mUsers;
        private final List<String> mSurnames;
        private final List<String[]> mRoles;
        private final List<String> mEvents;

        private String mDescription;
        private boolean isPrivate;
        private Bitmap mPicture;

        public Builder(String id) {
            super(id);
            mUsers = new ArrayList<>();
            mSurnames = new ArrayList<>();
            mRoles = new ArrayList<>();
            mEvents = new ArrayList<>();
        }

        private void setUsers(Object[] o) {
            for (Object obj : o) {
                mUsers.add((String) obj);
            }
        }

        private void setSurnames(Object[] o) {
            for (Object obj : o) {
                mSurnames.add((String) obj);
            }
        }

        private void setRoles(Object[] o) {
            for (Object obj : o) {
                try {
                    String[] roles = objectArrayToStringArray(objectToArray(obj));
                    mRoles.add(roles);
                } catch (PFException e) {
                    // TODO : what to do?
                }
            }
        }

        private void setEvents(Object[] o) {
            for (Object obj : o) {
                mEvents.add((String) obj);
            }
        }

        private void setPrivacy(boolean b) {
            this.isPrivate = b;
        }

        private void setDescription(String s) {
            this.mDescription = s;
        }

        @Override
        public void retrieveFromServer() throws PFException {
            ParseQuery<ParseObject> query = ParseQuery.getQuery(PARSE_TABLE_GROUP);
            query.whereEqualTo(OBJECT_ID, mId);
            try {
                ParseObject object = query.getFirst();
                if (object != null) {
                    setUsers(objectToArray(object.get(GROUP_TABLE_USERS)));
                    setSurnames(objectToArray(object.get(GROUP_TABLE_SURNAMES)));
                    setRoles(objectToArray(object.get(GROUP_TABLE_ROLES)));
                    setEvents(objectToArray(object.get(GROUP_TABLE_EVENTS)));
                    setPrivacy(object.getBoolean(GROUP_TABLE_ISPRIVATE));
                    setDescription(object.getString(GROUP_TABLE_DESCRIPTION));
                    ParseFile fileObject = (ParseFile) object
                            .get(GROUP_TABLE_PICTURE);
                    fileObject.getDataInBackground(new GetDataCallback() {
                        @Override
                        public void done(byte[] data, ParseException e) {
                            mPicture = (e == null ? null : BitmapFactory.decodeByteArray(data, 0, data.length));
                        }
                    });
                } else {
                    throw new PFException("Query failed");
                }
            } catch (ParseException e) {
                throw new PFException("Query failed");
            }
        }

        public PFGroup build() {
            return new PFGroup(mId, mUsers, mSurnames, mRoles, mEvents, isPrivate, mDescription, mPicture);
        }

        private String[] objectArrayToStringArray(Object[] o) {
            String[] out = new String[o.length];
            for (int i = 0; i < out.length; i++) {
                out[i] = (String) o[i];
            }
            return out;
        }

    }
}