package org.camunda.webapptranslation.tool;

import java.io.File;

public class WebApplication {
    /**
     * An application set reference:
     * - the name of the application
     * - the reference dictionary in the REFERENCE_FOLDER path
     * - the list of dictionary available in the TRANSLATION folder
     *
     */
    /**
     * Application name (cockpit, admin...)
     */
    public String applicationName;
    /**
     * Folder where all dictionaries are
     */
    public File translationFolder;
    /**
     * Folder where the reference dictionary is
     */
    public File referenceFolder;

}
