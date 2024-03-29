package org.camunda.webapptranslation.tool.app;

/* -------------------------------------------------------------------- */
/*                                                                      */
/* Read/Write a dictionary */
/*                                                                      */
/* -------------------------------------------------------------------- */

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.camunda.webapptranslation.tool.report.ReportInt;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class AppDictionarySerialize {

    private static final String UTF8_BOM = "\uFEFF";
    private final AppDictionary appDictionary;

    protected AppDictionarySerialize(AppDictionary appDictionary) {
        this.appDictionary = appDictionary;
    }

    /**
     * @return true if the dictionary was read without error
     */
    public boolean read(ReportInt report) {
        // clear the dictionary
        File file = appDictionary.getFile();

        try (FileInputStream fis = new FileInputStream(file);
             InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8);
             BufferedReader reader = new BufferedReader(isr)
        ) {

            // Some dictionaries are encoded in BOM UTF8, which is not correct.
            String str;
            StringBuilder readerStr = new StringBuilder();
            while ((str = reader.readLine()) != null) {
                readerStr.append(str);
            }

            String jsonComplete = readerStr.toString();
            if (jsonComplete.startsWith(UTF8_BOM))
                jsonComplete = jsonComplete.substring(1);
            Gson gson = new Gson();
            Map<String, Object> dictionaryJson = gson.fromJson(jsonComplete, LinkedHashMap.class);
            // a empty dictionary return a null JSON
            if (dictionaryJson != null)
                convertToFlatList(dictionaryJson, "");
            return true;
        } catch (Exception e) {
            report.severe(AppDictionary.class, String.format(" Error during reading dictionary [%s] file[%s]", appDictionary.getLanguage(), file.getAbsolutePath()), e);
            return false;
        }
    }


    /**
     * Write the dictionary
     *
     * @return true if the dictionary has been successfully written
     */
    public boolean write(ReportInt report) {
        File file = appDictionary.getFile();
        try {
            // file exist before ? Rename it to .bak
            if (file.exists()) {
                File backupDirectory = new File(file.getParentFile().getAbsolutePath());
                String fileName = file.getName();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("ddMMyyyy_HHmmss");

                fileName = fileName.replace(".json", "_" + LocalDateTime.now().format(formatter) + ".bak");
                File destFile = new File(backupDirectory.getAbsolutePath() + File.separator + fileName);

                file.renameTo(destFile);
            }
        } catch (Exception e) {
            report.severe(AppDictionary.class, String.format(" Error during writing dictionary [%s] file[%s]", appDictionary.getDictionary(), file.getName()), e);
            return false;
        }

        // we want to sort the dictionary

        try (FileWriter writer = new FileWriter(file.getAbsolutePath())) {
            //We can write any JSONArray or JSONObject instance to the file
            Map<String, Object> dicoJson = convertFromFlatList();

            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            writer.write(gson.toJson(dicoJson));

            writer.flush();

        } catch (Exception e) {
            report.severe(AppDictionary.class, String.format(" Error during writing dictionary [%s] file[%s]", appDictionary.getDictionary(), file.getName()), e);
            return false;
        }
        return true;
    }

    /* -------------------------------------------------------------------- */
    /*                                                                      */
    /* internal : transform the hierarchy JSON to a FLAT structure,         */
    /*  and inverse                                                         */
    /*                                                                      */
    /* -------------------------------------------------------------------- */

    /**
     * Get all keys. The dictionary is a hierarchy, like "{labels { APPVENDOR = ""}}. Returns this value as "labels.APPVENDOR"
     */
    private void convertToFlatList(Map<String, Object> jsonDico, String hierarchy) {
        for (Map.Entry<String, Object> entry : jsonDico.entrySet()) {
            if (entry.getValue() instanceof Map) {
                convertToFlatList((Map<String, Object>) entry.getValue(), hierarchy + entry.getKey() + ".");
            } else if (entry.getValue() instanceof List) {
                appDictionary.addKey(hierarchy + entry.getKey(), entry.getValue());
            } else if (entry.getValue() instanceof Long || entry.getValue() instanceof Integer) {
                appDictionary.addKey(hierarchy + entry.getKey(), entry.getValue());
            } else {
                appDictionary.addKey(hierarchy + entry.getKey(), entry.getValue());
            }
        }
    }

    /**
     * Convert the flat list stored in the dictionary, to a hierarchy, to save it in Json
     * a TreeMap is used to order alphabetically keys
     *
     * @return a hierarchy of Map / Keys
     */
    private Map<String, Object> convertFromFlatList() {
        // no reference dictionary? Then sort alphabetically
        if (providedReferenceDictionary == null)
            return concertFromFlatListOrdered();
        else {
            return concertFromFlatListReference();
        }
    }


    private Map<String, Object> concertFromFlatListOrdered() {
        Map<String, Object> dicoObject = new TreeMap<>();


        for (Map.Entry<String, Object> entry : appDictionary.getDictionary().entrySet()) {
            StringTokenizer st = new StringTokenizer(entry.getKey(), ".");
            List<String> tokens = new ArrayList<>();
            while (st.hasMoreTokens()) {
                tokens.add(st.nextToken()); // use already extracted value
            }
            Map<String, Object> container = getContainer(tokens, dicoObject);
            container.put(tokens.get(tokens.size() - 1), entry.getValue());
        }
        return dicoObject;
    }

    private Map<String, Object> concertFromFlatListReference() {
        Map<String, Object> dicoObject = new LinkedHashMap<>();
        // first, get all the value
        Map<String, Object> allEntry = concertFromFlatListOrdered();
        // Now, we have to fullfill the dictionnary from the reference order
        for (String key : providedReferenceDictionary.keySet()) {
            Object value = allEntry.get(key);
            if (value != null)
                dicoObject.put(key, value);
        }
        // now, all entry which are not in the reference (new keys)
        for (String key : allEntry.keySet()) {
            Object value = providedReferenceDictionary.get(key);
            if (value==null)
                dicoObject.put(key, allEntry.get(key));
        }
        return dicoObject;
    }

    Map<String,Object> providedReferenceDictionary = null;
    public void setReferenceDictionary(AppDictionary referenceDictionary) {
        this.providedReferenceDictionary = referenceDictionary.getDictionary();

    }
    /**
     * Get the container according the list of Tokens. Each token is a key in the map
     *
     * @param tokens     list of tokens to navigate inside containers
     * @param dicoObject the root object to navigate in
     * @return the local container according the tokens list
     */
    private Map<String, Object> getContainer(List<String> tokens, Map<String, Object> dicoObject) {
        Map<String, Object> currentContainer = dicoObject;
        for (int i = 0; i < tokens.size() - 1; i++) {
            if (currentContainer.containsKey(tokens.get(i)))
                currentContainer = (Map<String, Object>) currentContainer.get(tokens.get(i));
            else {
                Map<String, Object> subContainer = new TreeMap<>();
                currentContainer.put(tokens.get(i), subContainer);
                currentContainer = subContainer;
            }
        }
        return currentContainer;
    }

}
