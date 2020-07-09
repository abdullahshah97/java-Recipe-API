/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package recipe;

import java.net.URLEncoder;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.JsonNode;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mashape.unirest.http.exceptions.UnirestException;
import java.awt.Desktop;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import javax.swing.JOptionPane;

/**
 *
 * @author abdullah
 */
public class RecipeAPI {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        String host = "https://spoonacular-recipe-food-nutrition-v1.p.rapidapi.com/";
        String charset = "UTF-8";
        // Headers for a request
        String x_rapidapi_host = "spoonacular-recipe-food-nutrition-v1.p.rapidapi.com";
        String x_rapidapi_key = "b9f954ec31msh89d7085bb800e27p1f52eajsn53b018e18614";//Type here your key - bc81e5768ab748e6bf725613c5c55ad2
        String choice = "";
        while (true) {
            choice = JOptionPane.showInputDialog("The following options are available to select:\n"
                    + "1) Ask a question to find out nutritional information about food.\n"
                    + "2) Get a meal plan with target calories and a time frame.\n"
                    + "3) Random food joke.\n"
                    + "4) Breakdown of nutritional value given your ingredients.\n"
                    + "5) Exit");
            try {
                decideOption(choice, host, charset, x_rapidapi_host, x_rapidapi_key);

            } catch (Exception e) {
                print(e);
            }
        }
    }

    public static void decideOption(String choice, String host, String charset, String x_rapidapi_host, String x_rapidapi_key) throws Exception {
        try {
            if (choice.equals("1")) {
                quickAnswer(host, charset, x_rapidapi_host, x_rapidapi_key);
            } else if (choice.equals("2")) {
                requestPlan(host, charset, x_rapidapi_host, x_rapidapi_key);
            } else if (choice.equals("3")) {
                randomJoke(host, x_rapidapi_host, x_rapidapi_key);
            } else if (choice.equals("4")) {
                visualiseIngredients(host, charset, x_rapidapi_host, x_rapidapi_key);
            } else if (choice.equals("5")) {
                System.exit(0);
            } else {
                JOptionPane.showMessageDialog(null, "Please enter a valid response from 1, 2, 3, 4 or 5");
            }

        } catch (UnirestException e) {

        }
    }

    public static void randomJoke(String host, String x_rapidapi_host, String x_rapidapi_key) throws Exception {
        HttpResponse<JsonNode> response = Unirest.get(host + "food/jokes/random")
                .header("x-rapidapi-host", x_rapidapi_host)
                .header("x-rapidapi-key", x_rapidapi_key)
                .asJson();
        JsonObject jsonObject = returnJson(response);
        String joke = jsonObject.get("text").getAsString();
        JOptionPane.showMessageDialog(null, joke);
    }

    public static void requestPlan(String host, String charset, String x_rapidapi_host, String x_rapidapi_key) throws Exception {
        // Host url
        String calories = JOptionPane.showInputDialog(null, "Input Target Calories per Day: ");//create check for integer value
        String timeFrame = JOptionPane.showInputDialog(null, "Input Time Frame (Day/Week): ");

        String query = "?targetCalories=" + calories + "&timeFrame=" + timeFrame;

        HttpResponse<JsonNode> response = Unirest.get(host + "recipes/mealplans/generate" + query)
                .header("x-rapidapi-host", x_rapidapi_host)
                .header("x-rapidapi-key", x_rapidapi_key)
                .asJson();

        JsonObject jsonObject = returnJson(response);

        String result = "";
        if (timeFrame.equalsIgnoreCase("day")) {
            JsonArray jsonArray = jsonObject.getAsJsonArray("meals");
            result = "The following meals will help to achieve a calorie intake of " + calories + " today";
            for (int i = 0; i < jsonArray.size(); i++) {
                String meal = jsonArray.get(i).getAsJsonObject().get("title").getAsString();
                result = result + "\nMeal " + (i + 1) + ":\n";
                result = result + meal;
            }
        } else if (timeFrame.equalsIgnoreCase("week")) {
            JsonArray jsonArray = jsonObject.getAsJsonArray("items");
            result = "The following meals will help to achieve a calorie intake of " + calories + " a week";
            for (int i = 0; i < jsonArray.size(); i++) {
                if (jsonArray.isJsonNull()) {
                    print("null");
                } else {
                    /*            JsonObject jsonObject2 = jsonArray.get(i).getAsJsonObject();
                print(jsonObject2.getAsString());
                jsonArray = jsonObject2.getAsJsonArray();
                String meal = jsonArray.getAsJsonObject().get("title").getAsString();
                String day = jsonArray.getAsJsonObject().get("day").getAsString();
                String slot = jsonArray.getAsJsonObject().get("slot").getAsString();
                result = result + "\nDay " + day + ":\nMeal " + slot + ":\n";
                result = result + meal;
                     */
                    String meal = jsonArray.get(i).getAsJsonObject().get("value").getAsString();
                    JsonObject jsonMeal = new JsonParser().parse(meal).getAsJsonObject();
                    String title = jsonMeal.get("title").getAsString();
                    String day = jsonArray.get(i).getAsJsonObject().get("day").getAsString();
                    String slot = jsonArray.get(i).getAsJsonObject().get("slot").getAsString();
                    result = result + "\nMeal Name: " + title + "\nDay: " + day + ":\nMeal No: " + slot + "\n";
                }
            }
        }
        JOptionPane.showMessageDialog(null, result);
    }

    public static void quickAnswer(String host, String charset, String x_rapidapi_host, String x_rapidapi_key) throws Exception {
        String s = JOptionPane.showInputDialog("What question would you like answered today?");
        // Format query for preventing encoding problems
        String query = String.format("?q=%s",
                URLEncoder.encode(s, charset));

        //Response
        HttpResponse<JsonNode> response = Unirest.get(host + "recipes/quickAnswer" + query)
                .header("x-rapidapi-host", x_rapidapi_host)
                .header("x-rapidapi-key", x_rapidapi_key)
                .asJson();

        //print for user Json --> string
        JsonObject jsonObject = returnJson(response);
        String answer = jsonObject.get("answer").getAsString();
        JOptionPane.showMessageDialog(null, s + "\n" + answer);
    }

    public static void visualiseIngredients(String host, String charset, String x_rapidapi_host, String x_rapidapi_key) throws UnirestException, IOException {
        /*String servings = JOptionPane.showInputDialog(null, "How many servings?");
        String countInput = JOptionPane.showInputDialog(null, "How many ingredients?");
        int count = Integer.parseInt(countInput);
        String[] ingredients = new String[count];
        count = 0;
        while (count < ingredients.length) {
            ingredients[count] = JOptionPane.showInputDialog("Enter ingredient no " + count + 1 + ":");
            count++;
        }
         */

        String ingredientsQuery = "";
        //catch int error 
        int servings = Integer.parseInt(JOptionPane.showInputDialog(null, "Enter the amount of servings."));
        int amount = Integer.parseInt(JOptionPane.showInputDialog(null, "How many ingredients in this recipe?"));
        int count = 0;
        while (count < amount) {
            String ingredient = JOptionPane.showInputDialog(null, "Please enter the amount followed by the ingredient. " + (count + 1)
                    + " of " + amount);
            ingredientsQuery += "\n" + ingredient;
            count++;
        }
        String query = String.format("ingredientList=%s",
                URLEncoder.encode(ingredientsQuery, charset));

        HttpResponse<String> response = Unirest.post("https://spoonacular-recipe-food-nutrition-v1.p.rapidapi.com/recipes/visualizeNutrition")
                .header("x-rapidapi-host", x_rapidapi_host)
                .header("x-rapidapi-key", x_rapidapi_key)
                .header("accept", "text/html")
                .header("content-type", "application/x-www-form-urlencoded")
                .body("defaultCss=true&" + query + "&servings=" + servings)
                .asString();
        print(response.getStatus());
        print(response.getBody());
        writeToHTML(response.getBody());

    }

    public static void writeToHTML(String html) throws IOException {
        FileWriter fWriter = null;
        BufferedWriter writer = null;
        String htmlFile = "visualiseIngredients.html";
        try {
            fWriter = new FileWriter(htmlFile);
            writer = new BufferedWriter(fWriter);
            writer.write(html);
            writer.close(); //make sure you close the writer object 
        } catch (Exception e) {
            //catch any exceptions here
        }
        File file = new File("visualiseIngredients.html");
        Desktop.getDesktop().browse(file.toURI());
    }

    //method to parse the body of response and return json object
    public static JsonObject returnJson(HttpResponse<JsonNode> response) {
        //Gson for prettifying the JSON
        /*  Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonParser jp = new JsonParser();
        JsonElement je = jp.parse(response.getBody().toString());
        String prettyJsonString = gson.toJson(je);
        return new JsonParser().parse(prettyJsonString).getAsJsonObject();
         */
        return new JsonParser().parse(response.getBody().toString()).getAsJsonObject();
    }

    public static <T> void print(T a) {
        System.out.println(a);
    }
}
