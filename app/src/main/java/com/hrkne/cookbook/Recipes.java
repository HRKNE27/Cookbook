package com.hrkne.cookbook;

import java.util.List;

public class Recipes {
    //TODO: Need to add tags, ingredients, and steps later on
    String Date;
    String Time;
    String Title;
    String Description;
    String Fullname;
    String RecipeImage;
    String ProfileImage;
    String UID;
    String Ingredients;
    String Steps;

    public Recipes(){

    }
    public Recipes(String Date,String Time,String Title,String Description,
                   String Fullname,String ProfileImage,String UID,String RecipeImage,
                   String Ingredients,String Steps){
        this.Date = Date;
        this.Time = Time;
        this.Title = Title;
        this.Description = Description;
        this.Fullname = Fullname;
        this.ProfileImage = ProfileImage;
        this.UID = UID;
        this.RecipeImage = RecipeImage;
        this.Ingredients = Ingredients;
        this.Steps = Steps;
    }

    public String getDate() {
        return Date;
    }

    public void setDate(String Date) {
        this.Date = Date;
    }

    public String getTime() {
        return Time;
    }

    public void setTime(String Time) {
        this.Time = Time;
    }

    public String getTitle() {
        return Title;
    }

    public void setTitle(String Title) {
        this.Title = Title;
    }

    public String getDescription() {
        return Description;
    }

    public void setDescription(String Description) {
        this.Description = Description;
    }

    public String getFullname() {
        return Fullname;
    }

    public void setFullname(String Fullname) { this.Fullname = Fullname; }

    public String getProfileimage() {
        return ProfileImage;
    }

    public void setProfileimage(String ProfileImage) {
        this.ProfileImage = ProfileImage;
    }

    public String getUID() {
        return UID;
    }

    public void setUID(String UID) {
        this.UID = UID;
    }

    public String getRecipeImage(){return RecipeImage;}
    public void setRecipeImage(String RecipeImage){this.RecipeImage = RecipeImage;}
    public String getIngredients(){return Ingredients;}
    public void setIngredients(String Ingredients){this.Ingredients = Ingredients;}
    public String getSteps(){return Steps;}
    public void setSteps(String Steps){this.Steps = Steps;}
}
