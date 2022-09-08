
# Camunda Webapp Translations Tool


The project contains a tool, "org.camunda.webapptranslation.SynchroTranslation" to facilitate the translation.
This tool contains two functions:
* Detect the missing keys in dictionaries
* Complete dictionaries to help the translator.

Propositions made by the tool are not visible by the application because all propositions are prefixed by different keys.

# How to use it
This section gives the different actions for translating a dictionary

## different repositories
Camunda 7 repository are https://github.com/camunda/camunda-webapp-translations

Camunda Optimize repository: in progress

Camunda 8 repository: in progress


## Fork the repository
First, FORK the repository where you want to contribute.
You can do that operation via www.github.com, clicking on the Fork button.

The second step is to download the repository locally on your machine.

For example, you do this operation for the Camunda 7 repository, and you download it on `c:\camunda\camunda-webapp-translations`.
We call the directory **<C7-TRANSLATION>**


## Clone the tool
Clone/download this repository locally, then you can execute the tool.

The GitHub project is
https://github.com/camunda-community-hub/camunda-webapp-translation-tool

You must have **Maven** + **Java** (**JDK 11** or upper installed).

Let's say that you cloned under `c:\camunda\camunda-webapp-translations-tool`.
We call this directory **<TRANSLATION-TOOL>**

Execute
```` 
> cd  <TRANSLATION-TOOL>
> mvn install
````

## Detection
Detection reads all dictionaries and does not modify them. The reference is the English dictionary.
A missing sentence means the key exists in the English dictionary but not in the target dictionary.

To detect the missing translations on a repository, execute
````
> cd <TRANSLATION-TOOL> 
> java -jar target/SynchroTranslation.jar <C7-TRANSLATION>
````

The result is something like
````
Folder to study: c:\camunda\camunda-webapp-translations
Reference language: en
Detection: SYNTHETIC
Completion: NO
Report: STDOUT
Explore from rootFolder [c:\camunda\camunda-webapp-translations]
Detect [c:\camunda\camunda-webapp-translations\camunda-webapp-translation/admin]
Detect [c:\camunda\camunda-webapp-translations\camunda-webapp-translation/cockpit]
Detect [c:\camunda\camunda-webapp-translations\camunda-webapp-translation/tasklist]
Detect [c:\camunda\camunda-webapp-translations\camunda-webapp-translation/welcome]
````

To have a complete explanation of how it works, check the **How detection works** chapter.

To execute the detection for only one language, use the `-l` parameter.

````
> cd <TRANSLATION-TOOL> 
> java -jar target/SynchroTranslation.jar -l fr <C7-TRANSLATION>
````

## Completion
To execute the completion, execute this command

````
> cd <TRANSLATION-TOOL> 
> java -jar target/SynchroTranslation.jar -d FULL -c TRANSLATION <C7-TRANSLATION>
````

To limit to one language.
````
> cd <TRANSLATION-TOOL> 
> java -jar target/SynchroTranslation.jar -d FULL -c TRANSLATION -l fr <C7-TRANSLATION>
````

The tool completes dictionaries. It will add in the target dictionary different keys:

* A key prefixed by `_PLEASETRANSLATETHESENTENCE` means that you have a key to translate. The content is the reference value (so, English)

* a couple of keys prefixed by `_PLEASEVERIFYTHESENTENCE` and a second one prefixed by `_PLEASEVERIFYTHESENTENCE_REFERENCE`

The first is a proposition in the target language. This proposition comes because the same sentence is already translated,
and then it can propose this value.
If multiple values are detected, they are all provided.

If you used a Google API Key, and the Google Translation service is called to get
the proposition. The `_REFERENCE` key is the reference value (so, English)

Example
````
"ACTIVITY_INSTANCE_TOOLTIP_CANCELED_PLEASETRANSLATETHESENTENCE": "Canceled Activity Instance",
"ABORT_PLEASEVERIFYTHESENTENCE": "Annuler ##;## Interrompre ##;## Abandonner",
"ABORT_PLEASEVERIFYTHESENTENCE_REFERENCE": "Abort",
````

Your work then is to check the file, then check translation or translate, and remove the prefix.

````
"ACTIVITY_INSTANCE_TOOLTIP_CANCELED": "Annuler les instances d'activité",
"ABORT": "Annuler",
````

## Using a Google API

To use a google API, run the completion with the `-googleAPIKey` parameter.

````
> cd <TRANSLATION-TOOL> 
> java -jar target/SynchroTranslation.jar -d FULL -c TRANSLATION -l fr --googleAPIKey <GOOGLEAPI> --limiteGoogleAPIKey 500 <C7-TRANSLATION>
````

Attention: each time you run the translation, it first removes all the prefixed keys. So, the tool will re-ask the same key, which impacts your cost.
We recommend that you check keys after each iteration and remove prefixed, as not to translate the same sentence again.

## Commit and do a pull request
Commit your change, and make a Pull Request to update the main Camunda Repository.


# Explanation
## How the detection work
By default, the tool does a Detection only. Detection reads all dictionaries and does not modify them.


1. First pass

All subdirectories are read recursively. When, in a folder, a reference dictionary is detected (`en.json`), then the other `*.json` are read.
It gives the list of all expected languages. The folder is registered as a **"language folder"**

For example, at the end of this pass, we detected the language [*fr*, *de*, *bp*, *ru*, *pt-BR*].

The list of **"language folder"** are [ *admin*, *cockpit*, *tasklist*, *welcome* ]

2. Second pass

This is executed **"language folder"** per **"language folder"**,

For each language detected:

The file related to the language does not exist. It is marked as *Not exist*

Else, the dictionary is compared to the reference dictionary. For example, the detection compares `admin/fr.json` and `admin/en.json`.
* if a key exists in the reference, this is a "**MISSING KEY**"
* if a key exists in the dictionary but not in the reference, this is a "**TOO MUCH KEY**"
* if a key exists in the dictionary and  the reference but is not the same type, this is a "**INCORRECT CLASSE**""

Example in the reference dictionary:
``` 
"weekdays": [
      "Sunday",
      "Monday",
      "Tuesday",
      "Wednesday",
      "Thursday",
      "Friday",
      "Saturday"
    ],
 ```
In a dictionary:
````
"weekdays":"Dimanche,Lundi,Mardi,Mercredi,Jeudi,Vendredi,Samedi",
```` 

Example of execution:
```
=================================== Detection ===================================
----- Folder D:\dev\intellij\py-camunda-webapp-translations\admin
[de]       ... OK
[np]       ... Not exist (559 missing keys)
[ru]       ... Not exist (559 missing keys)
[pt-BR]    ... OK
[en]       ... Referential
[it]       ... Not exist (559 missing keys)
[fr]       ... OK
[es]       ... Missing 40 keys
[cs]       ... Not exist (559 missing keys)
[zh-Hans]  ... Missing 18 keys
[uk]       ... Not exist (559 missing keys)
[ja]       ... Not exist (559 missing keys)
[pl]       ... Not exist (559 missing keys)
[da]       ... Not exist (559 missing keys)
[nl]       ... Not exist (559 missing keys)
[tr]       ... Not exist (559 missing keys)
----- Folder D:\dev\intellij\py-camunda-webapp-translations\cockpit
[de]       ... Missing 17 keys,Incorrect class 1 keys
[np]       ... Not exist (2371 missing keys)
[ru]       ... Not exist (2371 missing keys)
[pt-BR]    ... Missing 38 keys,Incorrect class 1 keys
[en]       ... Referential
[it]       ... Not exist (2371 missing keys)
[fr]       ... Not exist (2371 missing keys)
[es]       ... Missing 147 keys,Incorrect class 1 keys
[cs]       ... Not exist (2371 missing keys)
[zh-Hans]  ... Not exist (2371 missing keys)
[uk]       ... Not exist (2371 missing keys)
[ja]       ... Not exist (2371 missing keys)
[pl]       ... Not exist (2371 missing keys)
[da]       ... Not exist (2371 missing keys)
[nl]       ... Not exist (2371 missing keys)
[tr]       ... Not exist (2371 missing keys)
----- Folder D:\dev\intellij\py-camunda-webapp-translations\tasklist
[de]       ... Missing 4 keys
[np]       ... Missing 17 keys,Too much 24 keys
[ru]       ... Missing 4 keys
[pt-BR]    ... Missing 4 keys
[en]       ... Referential
[it]       ... Missing 20 keys
[fr]       ... Missing 131 keys,Too much 13 keys
[es]       ... Missing 22 keys
[cs]       ... Missing 118 keys,Too much 19 keys
[zh-Hans]  ... Missing 20 keys
[uk]       ... Missing 185 keys,Too much 13 keys
[ja]       ... Missing 123 keys,Too much 13 keys
[pl]       ... Missing 29 keys
[da]       ... Missing 122 keys,Too much 13 keys
[nl]       ... Missing 123 keys,Too much 13 keys
[tr]       ... Missing 20 keys
----- Folder D:\dev\intellij\py-camunda-webapp-translations\welcome
[de]       ... OK
[np]       ... Not exist (178 missing keys)
[ru]       ... Not exist (178 missing keys)
[pt-BR]    ... OK
[en]       ... Referential
[it]       ... Not exist (178 missing keys)
[fr]       ... OK
[es]       ... Missing 18 keys
[cs]       ... Not exist (178 missing keys)
[zh-Hans]  ... Missing 15 keys
[uk]       ... Not exist (178 missing keys)
[ja]       ... Not exist (178 missing keys)
[pl]       ... Not exist (178 missing keys)
[da]       ... Not exist (178 missing keys)
[nl]       ... Not exist (178 missing keys)
[tr]       ... Not exist (178 missing keys)
The end
```

## Parameters
`-f <folder>` gives a different root folder

`-l <language>` execution for only one language

`-d <NO|SYNTHETIC|FULL>` default is *SYNTHETIC*. If the detection is *NO*, the function is not executed. With* FULL*, all missing or in trouble keys are listed.

`-r <STDOUT|LOGGER >` Default is *STDOUT*. With *LOGGER*, the result is sent to the Java Logger.

## Completion
The completion removed all non **"TOO MUCH"** keys and added a key for each missing. It does not add the final key but a prefixed key.
For example, when the key `AUTH_DAY_CONTEXT_EVENING` is missing, completion adds a key `AUTH_DAY_CONTEXT_EVENING_ PLEASETRANSLATETHESENTENCE`.

There are two situations:

* Completion is not able to calculate a proposition:

Then a key prefixed by `_PLEASETRANSLATETHESENTENCE` is added.

* Completion can calculate a proposition:

Then two keys are added: One prefixed by `_PLEASEVERIFYTHESENTENCE`, and a second prefixed by `_PLEASEVERIFYTHESENTENCE_REFERENCE`
Then, it is possible to see the proposition and the original sentence.

**The reviewer has to check sentences and rename keys. Executing the software again will purge all prefixed keys.**

For example, the result may be in the French dictionary after the execution.
````
"ACTIVITY_INSTANCE_TOOLTIP_CANCELED_PLEASETRANSLATETHESENTENCE": "Canceled Activity Instance",
"ABORT_PLEASEVERIFYTHESENTENCE": "Annuler ##;## Interrompre ##;## Abandonner",
"ABORT_PLEASEVERIFYTHESENTENCE_REFERENCE": "Abort",
````

The different completion strategies are :
* Same key
  The translation may exist in another dictionary for the same language.
  For example, in the Cockpit dictionary, the key `ABORT` is missing.
  In the taskList dictionary, the key `ABORT` exists with a translation:
 ```
 ABORT = 'annuler'
 ```
So the tool can propose 'annuler'.
When the same keys exist in different dictionaries but with different values,  all the possibilities are joined in the value, separate by `##;##` .
The reviewer has to keep one.
````
"ABORT_PLEASEVERIFYTHESENTENCE": "Annuler ##;## Interrompre ##;## Abandonner",
````
The value in the reference dictionary is added, too, to help the reviewer.
````
"ABORT_PLEASEVERIFYTHESENTENCE_REFERENCE": "Abort",
````
* Same translation

This strategy comes in second.
The idea is to search if the same content already exists in a different key.

For example, we search this key in the French cockpit dictionary:
```
  ==>  cockpit[fr]: 
  "BULK_OVERRIDE_SUCCESSFUL"
  ```
In the reference (English) dictionary, the value is
```
  ===>  cockpit[en]:  
  "BULK_OVERRIDE_SUCCESSFUL": "Successful",
  ```
We search if the same **CONTENT** (`Successful`) exists in the reference dictionary.
We found in the
```
===>  taskList[en] : "OPERATION_SUCCESSFUL": "Successful",
===>  taskList[en] : "REMOVED_OK": "Successful",
===>  taskList[en] : "CREATED_SUCCESS": "Successful",
  ```
So, now we can check if a translation exists for these keys.
We detect (assuming the CREATED_SUCCESS does not exist in the French dictionary):
```
===>  taskList[fr] :  "OPERATION_SUCCESSFUL": "Avec succès",
===>  taskList[fr] :  "REMOVED_OK": "opération réussie",
  ```
So, we have two propositions:

```
  "BULK_OVERRIDE_SUCCESSFUL": = "Avec succès  ##;## opération réussie"
```

## Google Translation

### Usage
The Google Translation API is used to give a proposition. This requires providing an API Key.
To get a Google API Key, visit Google. It's possible to get an account with an initial three-month budget of $300, which is enough to translate millions of sentences.

Two parameters exist then:

`--googleAPIKey <GoogleKey>` to provide your key.

`--limiteGoogleAPIKey <Number>`  to limit the number of translations.

By default, the limitation is 100 translations.

**Attention, each time you restart the software, all prefixed keys are purged. So, when you translate X sentences, you have to review them and remove the prefix, or else the translation will ask again for the same key.**

Example of execution:

```
=================================== Completion ===================================
----- Folder D:\dev\intellij\py-camunda-webapp-translations\admin
[fr]       ... Nothing done.
----- Folder D:\dev\intellij\py-camunda-webapp-translations\cockpit
[fr]       ... Add 2369 keys / proposition ( GoogleTranslation:1, SameKey:234, SameTranslation:454), Check in the dictionary keys ( _PLEASEVERIFYTHESENTENCE:689, _PLEASETRANSLATETHESENTENCE:1680, _PLEASEVERIFYTHESENTENCE_REFERENCE:689)
Dictionary written with success.
----- Folder D:\dev\intellij\py-camunda-webapp-translations\tasklist
[fr]       ... Add 131 keys / proposition ( SameKey:105, SameTranslation:9), Check in the dictionary keys ( _PLEASEVERIFYTHESENTENCE:114, _PLEASETRANSLATETHESENTENCE:17, _PLEASEVERIFYTHESENTENCE_REFERENCE:114)
Dictionary written with success.
----- Folder D:\dev\intellij\py-camunda-webapp-translations\welcome
[fr]       ... Nothing done.
Google translation: 1693 requested, one done in 2774 ms
The end
```

The reviewer needs to choose and remove the prefixed:
````
"ACTIVITY_INSTANCE_TOOLTIP_CANCELED": "Annuler les instances d'activitée",
"ABORT": "Annuler",
"ABORT_PLEASEVERIFYTHESENTENCE_REFERENCE": "Abort",
````
Tip: don't take care of `_REFERENCE` key. Just execute the software again to purge all the prefixed keys.

### parameters

`-c <NO|KEYS|TRANSLATION`>`Default is NO. With KEYS, only the two first strategies are used. With TRANSLATION, keys and translation are used.

`--googleAPIKey <GoogleKey>` to provide your key.

`--limiteGoogleAPIKey <Number>`  to limit the number of translations.
