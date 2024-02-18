package de.hysky.skyblocker.skyblock.chat;

import de.hysky.skyblocker.utils.Utils;
import net.minecraft.sound.SoundEvent;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Data class to contain all the settings for a chat rule
 */
public class ChatRule {

    private String name;

    //inputs
    private Boolean enabled;
    private Boolean isPartialMatch;
    private Boolean isRegex;
    private Boolean isIgnoreCase;
    private String filter;
    private String validLocations;

    //output
    private Boolean hideMessage;
    private Boolean showActionBar;
    private Boolean showAnnouncement;
    private String replaceMessage;
    private SoundEvent customSound;
    /**
     * Creates a chat rule with default options.
     */
    public ChatRule(){
        this.name = "New Rule";

        this.enabled = true;
        this.isPartialMatch = false;
        this.isRegex = false;
        this.isIgnoreCase = true;
        this.filter = "";
        this.validLocations = "";

        this.hideMessage = true;
        this.showActionBar = false;
        this.showAnnouncement = false;
        this.replaceMessage = null;
        this.customSound = null;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public Boolean getPartialMatch() {
        return isPartialMatch;
    }

    public void setPartialMatch(Boolean partialMatch) {
        isPartialMatch = partialMatch;
    }

    public Boolean getRegex() {
        return isRegex;
    }

    public void setRegex(Boolean regex) {
        isRegex = regex;
    }

    public Boolean getIgnoreCase() {
        return isIgnoreCase;
    }

    public void setIgnoreCase(Boolean ignoreCase) {
        isIgnoreCase = ignoreCase;
    }

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    public Boolean getHideMessage() {
        return hideMessage;
    }

    public void setHideMessage(Boolean hideMessage) {
        this.hideMessage = hideMessage;
    }

    public Boolean getShowActionBar() {
        return showActionBar;
    }

    public void setShowActionBar(Boolean showActionBar) {
        this.showActionBar = showActionBar;
    }

    public Boolean getShowAnnouncement() {
        return showAnnouncement;
    }

    public void setShowAnnouncement(Boolean showAnnouncement) {
        this.showAnnouncement = showAnnouncement;
    }

    public String getReplaceMessage() {
        return replaceMessage;
    }

    public void setReplaceMessage(String replaceMessage) {
        this.replaceMessage = replaceMessage;
    }

    public SoundEvent getCustomSound() {
       return customSound;
    }

    public void setCustomSound(SoundEvent customSound) {
        this.customSound = customSound;
    }

    public String getValidLocations() {
        return validLocations;
    }

    public void setValidLocations(String validLocations) {
        this.validLocations = validLocations;
    }

    /**
     * checks every input option and if the games state and the inputted str matches them returns true.
     * @param inputString the chat message to check if fits
     * @return if the inputs are all true and the outputs should be performed
     */
    public Boolean isMatch(String inputString){
        //enabled
        if (!enabled) return false;

        //ignore case
        String testString;
        String testFilter;
        if (isIgnoreCase){
            testString = inputString.toLowerCase();
            testFilter = filter.toLowerCase();
        }else {
            testString = inputString;
            testFilter = filter;
        }

        //filter
        if (testFilter.isBlank()) return false;
        if(isRegex) {
            if (isPartialMatch) {
               if (! Pattern.compile(testFilter).matcher(testString).find()) return false;
            }else {
                if (!testString.matches(testFilter)) return false;
            }
        } else{
            if (isPartialMatch) {
                if (!testString.contains(testFilter)) return false;
            }else {
                if (!testFilter.equals(testString)) return false;
            }
        }

        //location
        if (validLocations.isBlank()){ //if no locations do not check
            return true;
        }
        String rawLocation = Utils.getLocationRaw();
        Boolean isLocationValid = null;
        for (String validLocation : validLocations.replace(" ", "").toLowerCase().split(",")) {//the locations are raw locations split by "," and start with ! if not locations
            String rawValidLocation = ChatRulesHandler.locations.get(validLocation.replace("!",""));
            if (rawValidLocation == null) continue;
            if (validLocation.startsWith("!")) {//not location
                if (Objects.equals(rawValidLocation, rawLocation.toLowerCase())) {
                    isLocationValid = false;
                    break;
                }
            }
            else {
                if (Objects.equals(rawValidLocation, rawLocation.toLowerCase())) { //normal location
                    isLocationValid = true;
                    break;
                }
            }
        }
        if (isLocationValid != null && isLocationValid){//if location is not in the list at all and is a not a "!" location or and is a normal location
            return true;
        }

        return false;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}


