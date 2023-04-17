package org.camunda.webapptranslation.tool.app;

/* -------------------------------------------------------------------- */
/*                                                                      */
/* Manage, for an application (a directory) a language */
/*                                                                      */
/* -------------------------------------------------------------------- */

import org.camunda.webapptranslation.tool.report.ReportInt;

import java.io.File;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;


public class AppDictionary {


    public final static String PREFIX_PLEASE_TRANSLATE = "";
    private final File folder;
    private final String language;
    /**
     * Dictionary, JSON format is a hierarchy collection of String or list
     * Something like
     * {
     * "labels": {
     * "APP_VENDOR": "Camunda",
     * },
     * "monthsShort": [
     * "Jan",
     * "Feb",
     * "Mar"
     * ],
     * "week": {
     * "dow": 1,
     * "doy": 4
     * }
     * }
     */
    private Map<String, Object> dictionary;
    /**
     * marker to know if the dictionary is modified or not
     */
    private boolean dictionaryIsModified = false;

    /**
     * @param folder   folder where the dictionary is located
     * @param language language for this dictionary
     */
    public AppDictionary(File folder, String language) {
        this.folder = folder;
        this.language = language;
        this.dictionary = new LinkedHashMap<>();
    }


    /* -------------------------------------------------------------------- */
    /*                                                                      */
    /* Dictionary to file                                                   */
    /*                                                                      */
    /* -------------------------------------------------------------------- */

    /**
     * Check if the file relative to the dictionary exists
     *
     * @return true if the file exists
     */
    public boolean existFile() {
        File file = getFile();
        return file.exists();
    }


    /* -------------------------------------------------------------------- */
    /*                                                                      */
    /* Read/Write */
    /*                                                                      */
    /* -------------------------------------------------------------------- */
    public boolean read(ReportInt report) {
        AppDictionarySerialize serialize = new AppDictionarySerialize(this);
        dictionary = new LinkedHashMap<>();
        boolean status = serialize.read(report);
        dictionaryIsModified = false;
        return status;
    }

    /**
     * Write the dictionary
     *
     * @param report report used to report any error
     * @return true if the writing correctly took place
     */
    public boolean write(ReportInt report) {
        AppDictionarySerialize serialize = new AppDictionarySerialize(this);
        if (referenceDictionary!=null)
            serialize.setReferenceDictionary(referenceDictionary);
        boolean status = serialize.write(report);
        if (status)
            dictionaryIsModified = false;
        return status;
    }

    AppDictionary referenceDictionary;
    public void setReferenceDictionary(AppDictionary referenceDictionary) {
        this.referenceDictionary = referenceDictionary;
    }
    /**
     * isModified
     *
     * @return true if the dictionary change (keys added, removed)
     */
    public boolean isModified() {
        return dictionaryIsModified;
    }

    /**
     * Explicitaly move the modified option to true
     */
    public void setModified() {
        this.dictionaryIsModified = true;
    }



    /* -------------------------------------------------------------------- */
    /*                                                                      */
    /* Operation */
    /*                                                                      */
    /* -------------------------------------------------------------------- */


    /**
     * Add a Key in the dictionary
     *
     * @param key   key to add
     * @param value value to add
     */
    public void addKey(String key, Object value) {
        dictionary.put(key, value);
        dictionaryIsModified = true;
    }

    /**
     * Remove the key
     *
     * @param key key to remove
     */
    public void removeKey(String key) {
        dictionary.remove(key);
        dictionaryIsModified = true;
    }

    /* -------------------------------------------------------------------- */
    /*                                                                      */
    /* access function */
    /*                                                                      */
    /* -------------------------------------------------------------------- */

    public boolean exist(String key) {
        return (dictionary != null && dictionary.containsKey(key));
    }

    public Map<String, Object> getDictionary() {
        if (dictionary == null)
            return Collections.emptyMap();
        return dictionary;
    }

    /**
     * Return the file
     *
     * @return the file, path + language
     */
    public File getFile() {
        return new File(folder + File.separator + language + ".json");
    }

    /**
     * Return the language managed by this dictionary
     *
     * @return the language
     */
    public String getLanguage() {
        return language;
    }

}
