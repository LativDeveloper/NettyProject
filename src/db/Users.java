package db;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class Users {

    private DBManager dbManager;

    public Users(DBManager dbManager) {
        this.dbManager = dbManager;
    }

    public ArrayList<JSONObject> get(String login, String password) {
        String query = "SELECT * FROM users WHERE login='"+login+"' AND password='"+password+"'";
        ArrayList<JSONObject> arrayList = new ArrayList<>();
        try {
            ResultSet resultSet = dbManager.executeQuery(query);
            while (resultSet.next()) {
                JSONObject jsonObject = new JSONObject();
                JSONArray victims = (JSONArray) new JSONParser().parse(resultSet.getString("victims"));
                jsonObject.put("id", resultSet.getInt("id"));
                jsonObject.put("login", resultSet.getString("login"));
                jsonObject.put("password", resultSet.getString("password"));
                jsonObject.put("victims", victims);
                arrayList.add(jsonObject);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return arrayList;
    }

}
