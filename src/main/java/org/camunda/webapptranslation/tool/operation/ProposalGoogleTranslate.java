package org.camunda.webapptranslation.tool.operation;

import com.google.cloud.translate.Translate;
import com.google.cloud.translate.Translate.TranslateOption;
import com.google.cloud.translate.TranslateOptions;
import com.google.cloud.translate.Translation;
import org.camunda.webapptranslation.tool.app.AppDictionary;
import org.camunda.webapptranslation.tool.app.AppTimeTracker;
import org.camunda.webapptranslation.tool.report.ReportInt;

public class ProposalGoogleTranslate implements Proposal {

    private final String googleAPIKey;
    private final int limitNumberOfTranslations;
    private Translate translate;
    private AppDictionary appDictionary;
    private AppDictionary referenceDictionary;
    private int numberOfTranslations;
    private int numberOfTranslationsRequested;

    private long accumulateTimeSinceLastReportInMS = 0;

    public ProposalGoogleTranslate(String googleAPIKey, int limitNumberOfTranslations) {
        this.googleAPIKey = googleAPIKey;
        this.limitNumberOfTranslations = limitNumberOfTranslations;
    }

    @Override
    public String getName() {
        return "GoogleTranslation";
    }

    @Override
    public void setDictionaries(AppDictionary appDictionary, AppDictionary referenceDictionary, EncyclopediaUniversal encyclopediaUniversal) {
        this.appDictionary = appDictionary;
        this.referenceDictionary = referenceDictionary;
    }

    @Override
    public boolean begin(ReportInt report) {
        System.setProperty("GOOGLE_API_KEY", googleAPIKey);
        translate = TranslateOptions.newBuilder().setApiKey(googleAPIKey).build().getService();
        numberOfTranslations = 0;
        numberOfTranslationsRequested = 0;
        return true;
    }

    @Override
    public void end(ReportInt report) {
        AppTimeTracker timeTracker = AppTimeTracker.getTimeTracker("googleTranslation");

        report.info(ProposalGoogleTranslate.class, "GoogleTranslation: " + numberOfTranslationsRequested + " requested,  " + numberOfTranslations + " done in " + timeTracker.getSumOfTimeMs() + " ms ");
    }

    @Override
    public String calculateProposition(String key, ReportInt report) {
        if (this.googleAPIKey == null)
            return null;
        numberOfTranslationsRequested++;

        if (numberOfTranslations >= limitNumberOfTranslations)
            return null;

        AppTimeTracker timeTracker = AppTimeTracker.getTimeTracker("googleTranslation");

        try {
            timeTracker.start();
            TranslateOption sourceLanguageOption = Translate.TranslateOption.sourceLanguage(referenceDictionary.getLanguage());
            TranslateOption targetLanguageOption = Translate.TranslateOption.targetLanguage(appDictionary.getLanguage());

            Translation translation = translate.translate(
                    (String) referenceDictionary.getDictionary().get(key),
                    sourceLanguageOption,
                    targetLanguageOption);
            timeTracker.stop();

            numberOfTranslations++;
            accumulateTimeSinceLastReportInMS += timeTracker.getLastExecutionTime();

            if (accumulateTimeSinceLastReportInMS > 30000) {
                report.info(ProposalGoogleTranslate.class, "       (GoogleTranslation partial result): " + numberOfTranslations + " done in " + timeTracker.getSumOfTimeMs() + " ms ");
                accumulateTimeSinceLastReportInMS = 0;
            }
            return translation.getTranslatedText().replace("&#39;", "'");
        } catch (Exception e) {
            report.severe(ProposalGoogleTranslate.class, "Can't translate : " + e);
            return null;
        }
    }
}
