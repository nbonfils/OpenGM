package ch.epfl.sweng.opengm.parse;

import android.graphics.BitmapFactory;

import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.List;

import ch.epfl.sweng.opengm.utils.Alert;

/**
 * This class contains some static methods that may be called for conversion, saving or checking purposes
 */
public final class PFUtils {

    /**
     * Converts a JSONArray into an array of String
     *
     * @param jsonArray A json array containing the information you want to extract
     * @return An array of strings with all the elements of the json array
     */
    public static String[] convertFromJSONArray(JSONArray jsonArray) {
        if (jsonArray == null) {
            return null;
        }
        String[] array = new String[jsonArray.length()];

        for (int i = 0; i < array.length; i++) {
            try {
                array[i] = objectToString(jsonArray.get(i));
            } catch (JSONException | PFException e) {
                // TODO : what to do?
            }
        }

        return array;
    }

    /**
     * Downloads in background the image of a Parse object and stores them in an object
     * that implements PFImageInterface
     *
     * @param object  A Parse object which contains the image we want to get
     * @param entry   A string whose value is the entry which contains the image in our table
     * @param builder An object which contains an image in which the downloaded data
     *                will be stored
     */
    public static void retrieveFileFromServer(ParseObject object, String entry, final PFImageInterface builder) {
        ParseFile fileObject = (ParseFile) object.get(entry);
        if (fileObject != null) {
            fileObject.getDataInBackground(new GetDataCallback() {
                @Override
                public void done(byte[] data, ParseException e) {
                    if (e == null) {
                        builder.setImage(BitmapFactory.decodeByteArray(data, 0, data.length));
                    }
                }
            });
        }
    }

    /**
     * Casts the object into a string
     *
     * @param o An object that we want to cast
     * @return A string of the object
     * @throws PFException If the object in parameter is null if it could not be cast into a string
     */
    public static String objectToString(Object o) throws PFException {
        if (o == null) {
            throw new PFException("Object was null");
        }
        try {
            return (String) o;
        } catch (Exception e) {
            throw new PFException("Error while casting the value to a string");
        }
    }

    /**
     * Converts a list of PFEntities into a JSonarray whose elements are the id of the entities
     *
     * @param entitiesList A list that contains some PFEntity we want to get the ids
     * @return A JSONArray whose elements are the ids of the parameter
     */
    public static JSONArray listToArray(List<? extends PFEntity> entitiesList) {
        JSONArray array = new JSONArray();
        for (int i = 0; i < entitiesList.size(); i++) {
            array.put(entitiesList.get(i).getId());
        }
        return array;
    }

    /**
     * Checks if the argument is not null and not empty. If it is, displays an Toast with the message given in parameter
     *
     * @param arg            The string argument to be checked
     * @param displayedError The message that will be displayed in case of error
     * @return True if the argument is correct, false otherwise
     */
    public static boolean checkArguments(String arg, String displayedError) {
        if (arg == null || arg.isEmpty()) {
            Alert.displayAlert(displayedError + " is null or empty.");
            return false;
        }
        return true;
    }

    /**
     * Checks if the argument is not null. If it is, displays an Toast with the message given in parameter
     *
     * @param arg            The string argument to be checked
     * @param displayedError The message that will be displayed in case of error
     * @return True if the argument is correct, false otherwise
     */
    public static boolean checkNullArguments(String arg, String displayedError) {
        if (arg == null) {
            Alert.displayAlert(displayedError + " is null.");
            return false;
        }
        return true;
    }


}