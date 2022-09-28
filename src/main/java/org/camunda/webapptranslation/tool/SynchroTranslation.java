package org.camunda.webapptranslation.tool;

/* -------------------------------------------------------------------- */
/*                                                                      */
/* SynchroTranslation                                                    */
/*                                                                      */
/*                                                                      */
/* -------------------------------------------------------------------- */

import org.camunda.webapptranslation.tool.app.AppPilot;
import org.camunda.webapptranslation.tool.operation.*;
import org.camunda.webapptranslation.tool.report.ReportInt;
import org.camunda.webapptranslation.tool.report.ReportLogger;
import org.camunda.webapptranslation.tool.report.ReportStdout;

import java.io.File;
import java.util.*;

public class SynchroTranslation {


    public static void main(String[] args) {

        SynchroParams synchroParams = new SynchroParams();
        synchroParams.explore(args);
        if (synchroParams.isError()) {
            synchroParams.printError();
            synchroParams.printUsage();
            return;
        }
        if (synchroParams.isUsage()) {
            synchroParams.printUsage();
            return;
        }

        synchroParams.printOptions();

        // get the report
        final ReportInt report;
        switch (synchroParams.getReport()) {
            case LOGGER:
                report = new ReportLogger();
                break;
            case STDOUT:
                report = new ReportStdout();
                break;
            default:
                report = new ReportStdout();
        }
        // Get all dictionary folders
        List<WebApplication> listApplications = getDictionaryFolder(synchroParams, report);
        if (listApplications.isEmpty()) {
            report.severe(SynchroTranslation.class, "No folder detected containing a file for the language [" + synchroParams.getReferenceLanguage() + "]");
            return;
        }
        // Build Application Pilot per folder
        List<AppPilot> listAppPilot = new ArrayList<>();
        listApplications.forEach(application -> listAppPilot.add(new AppPilot(application, synchroParams.getReferenceLanguage())));

        // Optimize is a pilot by itself
        WebApplication applicationOptimize = new WebApplication();
        applicationOptimize.applicationName = "Optimize";
        applicationOptimize.translationFolder = new File(synchroParams.getOptimizeFolder() + File.separator + "localisation");
        applicationOptimize.referenceFolder = new File(synchroParams.getOptimizeFolder() + File.separator + "localisation");
        listAppPilot.add(new AppPilot(applicationOptimize, synchroParams.getReferenceLanguage()));

        // Collect the list of expected languages
        Set<String> expectedLanguage = new HashSet<>();
        listAppPilot.forEach(pilot -> expectedLanguage.addAll(pilot.getLanguages()));
        listAppPilot.forEach(pilot -> pilot.setExpectedLanguage(expectedLanguage));

        // ---------- Detection
        if (synchroParams.getDetection() != SynchroParams.DETECTION.NO) {
            report.info(SynchroTranslation.class, "=================================== Detection ===================================");
            listAppPilot.forEach(pilot -> pilot.detection(synchroParams, report));
        }

        // ---------- Completion
        if (synchroParams.getCompletion() != SynchroParams.COMPLETION.NO) {
            report.info(SynchroTranslation.class, "=================================== Completion ===================================");
            EncyclopediaUniversal encyclopediaUniversal = new EncyclopediaUniversal(synchroParams.getReferenceLanguage());

            // Build the list of proposal objects
            List<Proposal> listProposals = new ArrayList<>();
            if (synchroParams.getCompletion() == SynchroParams.COMPLETION.TRANSLATION) {
                List<Proposal> listAllProposal = new ArrayList();
                listAllProposal.add(new ProposalSameKey());
                listAllProposal.add(new ProposalSameTranslation());
                if (synchroParams.getGoogleAPIKey() != null)
                    listAllProposal.add(new ProposalGoogleTranslate(synchroParams.getGoogleAPIKey(), synchroParams.getLimitNumberGoogleTranslation()));

                listAllProposal.forEach(proposal -> {
                    if (proposal.begin(report)) {
                        listProposals.add(proposal);
                    }
                });

            }

            listAppPilot.forEach(pilot -> pilot.completeEncyclopedia(encyclopediaUniversal, synchroParams, report));
            // Do the completion now
            listAppPilot.forEach(pilot -> pilot.completion(encyclopediaUniversal, listProposals, synchroParams, report));

            listProposals.forEach(proposal -> proposal.end(report));
        }

        System.out.println("The end");
    }

    /**
     * Check the disk and retrieve all the different folder
     *
     * @param synchroParams parameters to access the root folder
     * @param report        to report any error
     * @return the list of all folder where a dictionary is detected, based on the reference language
     */
    public static List<WebApplication> getDictionaryFolder(SynchroParams synchroParams, ReportInt report) {
        report.info(SynchroTranslation.class, "Explore from Translation Folder [" + synchroParams.getTranslationFolder() + "]");
        // We don't use the recursive exploration. Pattern is fixed now.
        // on the TRANSLATION_FOLDER, the path is  TRANSLATION_FOLDER/<application> like TRANSLATION_FOLDER/admin
        // on the REFERENCE_FOLDER, the path is REFERENCE_FOLDER/webapps/ui/<application>/client/en.json
        List<WebApplication> listApplications = new ArrayList<>();
        try {
            for (File file : Objects.requireNonNull(synchroParams.getTranslationFolder().listFiles())) {
                if (file.isDirectory()) {
                    // this is the application level
                    WebApplication application = new WebApplication();
                    application.applicationName = file.getName();
                    application.translationFolder = file;
                    // search if the same file exist on the reference
                    File referenceDictionary = new File(synchroParams.getReferenceFolder()
                            + File.separator + "webapps"
                            + File.separator + "ui"
                            + File.separator + application.applicationName
                            + File.separator + "client"
                            + File.separator + synchroParams.getReferenceLanguage() + ".json");
                    if (referenceDictionary.exists() && referenceDictionary.isFile()) {
                        application.referenceFolder = new File(synchroParams.getReferenceFolder()
                                + File.separator + "webapps"
                                + File.separator + "ui"
                                + File.separator + application.applicationName
                                + File.separator + "client");

                        listApplications.add(application);
                    }
                }
            }
        } catch (Exception e) {
            report.severe(SynchroTranslation.class, "Error parsing folder [" + synchroParams.getTranslationFolder() + "]", e);
        }
        return listApplications;
    }


    /**
     * Usefull to complete the recursive folder
     *
     * @param folder            source folder to explore
     * @param referenceLanguage reference language to detect if this folder is a dictionary folder
     * @param report            report any error
     * @return list of folders where translation are detected
     */
    private static List<File> completeRecursiveFolder(String folder, String referenceLanguage, ReportInt report) {
        // check the current folder and it's child
        List<File> listFolders = new ArrayList<>();
        try {
            for (File file : Objects.requireNonNull(new File(folder).listFiles())) {
                if (file.isDirectory()) {
                    listFolders.addAll(completeRecursiveFolder(file.getAbsolutePath(), referenceLanguage, report));
                }
                if (file.getName().contains(referenceLanguage + ".json")) {
                    listFolders.add(file.getParentFile());
                    report.info(SynchroTranslation.class, "Detect [" + file.getParentFile().getAbsolutePath() + "]");
                }
            }
        } catch (Exception e) {
            report.severe(SynchroTranslation.class, "Error parsing folder [" + folder + "]", e);
        }
        return listFolders;
    }
}
