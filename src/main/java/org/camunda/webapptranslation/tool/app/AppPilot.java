package org.camunda.webapptranslation.tool.app;


/* -------------------------------------------------------------------- */
/*                                                                      */
/* Manage an application (a directory)                                  */
/* AppPilot exist for ONE folder and manage ALL languages in this folder */
/*                                                                      */
/* The class detects  all languages present (and missing), compare each  */
/* dictionary with the referential  language. It can just play to detect */
/* missing sentences, or complete it                                    */
/*                                                                      */
/* -------------------------------------------------------------------- */

import org.camunda.webapptranslation.tool.SynchroParams;
import org.camunda.webapptranslation.tool.operation.DictionaryCompletion;
import org.camunda.webapptranslation.tool.operation.DictionaryDetection;
import org.camunda.webapptranslation.tool.operation.EncyclopediaUniversal;
import org.camunda.webapptranslation.tool.operation.Proposal;
import org.camunda.webapptranslation.tool.report.ReportInt;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class AppPilot {

    File folder;
    String referenceLanguage;
    Set<String> languages = new HashSet<>();
    Set<String> expectedLanguages;


    Set<AppTimeTracker> allTrackers = new HashSet<>();

    public AppPilot(File folder, String referenceLanguage) {
        this.folder = folder;
        this.referenceLanguage = referenceLanguage;
        for (File file : Objects.requireNonNull(new File(folder.getAbsolutePath()).listFiles())) {
            if (file.getName().endsWith(".json"))
                languages.add(file.getName().substring(0, file.getName().length() - 5));
        }
    }

    public Set<String> getLanguages() {
        return languages;
    }

    /**
     * set the expected language for the application
     *
     * @param expectedLanguages list of expected languages
     */
    public void setExpectedLanguage(Set<String> expectedLanguages) {
        this.expectedLanguages = expectedLanguages;
    }

    /**
     * Detection
     * Check dictionary in the application
     * 1/ all dictionary exists
     * 2/ the language is complete
     *
     * @param synchroParams access to parameters
     * @param report        report the status
     */
    public void detection(SynchroParams synchroParams, ReportInt report) {

        AppDictionary referenceDictionary = new AppDictionary(folder, referenceLanguage);
        AppTimeTracker timeTrackerRead = AppTimeTracker.getTimeTracker("dictionaryRead");
        timeTrackerRead.start();
        referenceDictionary.read(report);
        timeTrackerRead.stop();

        DictionaryDetection appDetection = new DictionaryDetection();
        AppTimeTracker timeTrackerDetection = AppTimeTracker.getTimeTracker("detection");

        timeTrackerDetection.start();
        appDetection.detection(expectedLanguages, folder, referenceDictionary, synchroParams, report);
        timeTrackerDetection.stop();

    }

    /**
     * Do the completion on each dictionary
     *
     * @param encyclopediaUniversal, Encyclopedia universal to get propositions
     * @param synchroParams          parameter object
     * @param report                 report object
     */
    public void completion(EncyclopediaUniversal encyclopediaUniversal, List<Proposal> listProposals, SynchroParams synchroParams, ReportInt report) {
        AppDictionary referenceDictionary = new AppDictionary(folder, referenceLanguage);
        AppTimeTracker timeTracker = AppTimeTracker.getTimeTracker("readDictionary");

        timeTracker.start();
        referenceDictionary.read(report);
        timeTracker.stop();

        DictionaryCompletion appCompletion = new DictionaryCompletion();

        appCompletion.completion(expectedLanguages, folder, referenceDictionary, encyclopediaUniversal, listProposals, synchroParams, report);
    }

    /**
     * Build and complete all encyclopedia
     *
     * @param encyclopediaUniversal the encyclopedia universal object
     * @param synchroParams         synchronisation parameters
     * @param report                report object
     */
    public void completeEncyclopedia(EncyclopediaUniversal encyclopediaUniversal, SynchroParams synchroParams, ReportInt report) {
        AppTimeTracker timeTracker = AppTimeTracker.getTimeTracker("completeEncyclopedia");
        timeTracker.start();

        AppDictionary referenceDictionary = new AppDictionary(folder, referenceLanguage);
        if (referenceDictionary.existFile() && referenceDictionary.read(report))
            encyclopediaUniversal.registerDictionary(referenceDictionary);

        for (String language : expectedLanguages) {
            if (synchroParams.getOnlyCompleteOneLanguage() != null
                    && !synchroParams.getOnlyCompleteOneLanguage().equals(language))
                continue;
            AppDictionary dictionary = new AppDictionary(folder, language);
            if (dictionary.existFile() && dictionary.read(report)) {
                encyclopediaUniversal.registerDictionary(dictionary);
            }
        }
        timeTracker.stop();

    }


}
