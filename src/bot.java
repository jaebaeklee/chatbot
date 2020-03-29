import org.jibble.pircbot.PircBot;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import com.google.gson.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.text.DecimalFormat;

public class bot extends PircBot {
    public String defaultLocation = "75080"; //initialize location to default richardson
    protected Pattern regex = Pattern.compile("(\\d{5})");
    public bot() {
        this.setName("weatherBot39"); //username
    }

    public void onMessage(String channel, String sender, String login, String hostname, String input) {
        input = input.toLowerCase();
        if (input.contains("weather")){
            String location = defaultLocation;
            weather(input, channel, sender, location);
        }
        if (input.contains("symbol")){
            String symbol = "TXN";
            stock(input, channel, sender, symbol);
        }
    }
    public void stock(String input, String channel, String sender, String symbol) {
        String wordArray[] = input.split(" ");
        symbol = wordArray[1];
        if(wordArray.length > 2){
            String modifier = wordArray[2];
        }
        String symbolData = symbolSearch(symbol);
        sendMessage(channel, "Hey, " + sender + ", " + symbolData);

    }
    public void weather(String input, String channel, String sender, String location) {
        boolean zipcode = false;
        boolean goDefault = false;
        String wordArray[] = input.split(" ");
        Matcher matcher = regex.matcher(input);
        if (matcher.find()) {
            location = matcher.group();
            zipcode = true;
        }
        else {
            if (wordArray.length == 2) {
                if (wordArray[0].equals("weather")) {
                    location = wordArray[1];
                    zipcode = false;
                }
                else {
                    location = wordArray[0];
                    goDefault = true;
                }
            }
        }
        String temperature = "0";
        if(zipcode == true) {
            temperature = zipcodeSearch(location);
        }
        else if(zipcode == false){
            temperature = citySearch(location);
        }
        else{
            sendMessage(channel, "Hmm, couldn't find it. Defaulting to Richardson!");
        }
        sendMessage(channel, "Hey, " + sender + ", " + temperature);
    }

    static String symbolSearch(String symbol) {
        try {
            URL request = new URL("https://www.alphavantage.co/query?function=TIME_SERIES_INTRADAY&symbol="+symbol+"&interval=5min&apikey="); //insert api key
            HttpURLConnection connection = (HttpURLConnection) request.openConnection();
            InputStreamReader inputStream = new InputStreamReader(connection.getInputStream(), "UTF-8");
            BufferedReader bufferedReader = new BufferedReader(inputStream);
            StringBuilder responseBuilder = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != "        ") {
                responseBuilder.append(line);
            }
            bufferedReader.close();
            return parseStockJSON(responseBuilder.toString());
        }
        catch(Exception e) {
            return e.toString();
        }
    }

    static String zipcodeSearch(String zipcode) {
        StringBuilder result = new StringBuilder();
        try
        {
            URL request = new URL("http://api.openweathermap.org/data/2.5/weather?zip="+zipcode+"&appid="); //insert api key
            HttpURLConnection connection = (HttpURLConnection) request.openConnection();
            connection.setRequestMethod("GET");
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null)
            {
                result.append(line);
            }
            reader.close();
            return parseWeatherJSON(result.toString());
        }
        catch(Exception e) {
            return "Hmm. we couldn't find that zipcode. Try again!";
        }
    }

    static String citySearch(String city)
    {
        StringBuilder result = new StringBuilder();
        try
        {
            URL request = new URL("http://api.openweathermap.org/data/2.5/weather?q="+city+"&appid="); //insert api key
            HttpURLConnection connection = (HttpURLConnection) request.openConnection();
            connection.setRequestMethod("GET");
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null)
            {
                result.append(line);
            }
            reader.close();
            return parseWeatherJSON(result.toString());
        }
        catch(Exception e) {
            return "Hmm. we couldn't find that city. Try again!";
        }
    }

    static String parseWeatherJSON(String json) {
        JsonObject object = new JsonParser().parse(json).getAsJsonObject();
        String cityName = object.get("name").getAsString();
        JsonObject main = object.getAsJsonObject("main");
        double temp = main.get("temp").getAsDouble();
        temp = 32 + (temp-273.15) * 1.8;
        double tempMin = main.get("temp_min").getAsDouble();
        tempMin = 32 + (tempMin-273.15) * 1.8;
        double tempMax = main.get("temp_max").getAsDouble();
        tempMax = 32 + (tempMax-273.15) * 1.8;
        DecimalFormat df = new DecimalFormat("####0.0");
        return "the current temperature in " + cityName + " is " + df.format(temp) + "˚F with a high of " + df.format(tempMax) +
                "˚F and a low of " + df.format(tempMin) + "˚F." ;
    }

    static String parseStockJSON(String json) {
        JsonObject object = new JsonParser().parse(json).getAsJsonObject();
        JsonObject metaData = object.getAsJsonObject("Meta Data");
        String symbol = metaData.get("2. Symbol").getAsString();
        String information = metaData.get("1. Information").getAsString();
        JsonObject timeSeries = object.getAsJsonObject("Time Series (Daily)");
        double high = timeSeries.get("2. high").getAsDouble();
        return information + " for " + symbol + "\n The high for the day was " + high;
    }
}
