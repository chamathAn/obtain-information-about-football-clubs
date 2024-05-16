package com.example.footballclubs

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.footballclubs.data.Clubs
import com.example.footballclubs.data.Leagues
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class SearchForClubsByLeague : ComponentActivity() {
    var InforToSaveToDatabse = mutableListOf<Map<String, String>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MainContent()
        }
    }


    @Composable
    fun MainContent() {

        var leagueInfoDisplay by rememberSaveable {
            mutableStateOf("")
        }

        var inputText by rememberSaveable {
            mutableStateOf("")
        }

        val scope = rememberCoroutineScope()
        LazyColumn {
            item {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {


                    OutlinedTextField(

                        value = inputText,
                        onValueChange = {
                            inputText = it

                        },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            disabledContainerColor = Color.White,
                            focusedIndicatorColor = Color(0xFF2894F4),
                            unfocusedIndicatorColor = Color(0xFF2894F4),
                            focusedLabelColor = Color(0xFF2894F4),
                            unfocusedLabelColor = Color(0xFF2894F4),
                        ),

                        label = { Text(text = "Search") },
                        maxLines = 1,


                        trailingIcon = {
                            if (!inputText.equals("")) {
                                Icon(
                                    imageVector = Icons.Outlined.Clear,
                                    contentDescription = null,
                                    modifier = Modifier.clickable {
                                        inputText = "" // Make text field empty
                                    }
                                )
                            }
                        }
                    )
                    Row(
                        modifier = Modifier.padding(0.dp, 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Button(
                            onClick = {
                                scope.launch {
                                    leagueInfoDisplay = fetchLeagueClubs(inputText)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(Color(0xFF2894F4)),

                            ) {
                            Text(text = "Retrieve Clubs", color = Color.White)
                        }
                        Spacer(modifier = Modifier.width(10.dp))

                        Button(
                            onClick = {
                                dataClubsToDatabase(scope)
                            },
                            colors = ButtonDefaults.buttonColors(Color(0xFF2894F4)),
                        ) {
                            Text(text = "Save clubs to Database", color = Color.White)
                        }
                    }

                    Text(

                        text = leagueInfoDisplay
                    )
                }

            }

        }

    }

    private fun dataClubsToDatabase(scope: CoroutineScope) {
        InforToSaveToDatabse.forEach { e ->
            e.let {
                scope.launch {
                    leaguesDao.insertLeagues(
                        Leagues(
                            idLeague = e.get("idLeague").toString(),
                            strLeague = e.get("strLeague").toString(),
                            strSport = null,
                            strLeagueAlternate = null
                        )
                    )

                    clubsDao.insertClubs(
                        Clubs(
                            id = e.get("idTeam").toString(),
                            name = e.get("Name").toString(),
                            shortName = e.get("strTeamShort").toString(),
                            alternateNames = e.get("strAlternate").toString(),
                            formedYear = e.get("intFormedYear").toString(),
                            leagueId = e.get("idLeague").toString(),
                            stadium = e.get("strStadium").toString(),
                            keywords = e.get("strKeywords").toString(),
                            stadiumThumb = e.get("strStadiumThumb").toString(),
                            stadiumLocation = e.get("strStadiumLocation").toString(),
                            stadiumCapacity = e.get("intStadiumCapacity").toString(),
                            website = e.get("strWebsite").toString(),
                            teamJersey = e.get("strTeamJersey").toString(),
                            teamLogo = e.get("strTeamLogo").toString()
                        )
                    )
                }
            }
        }
        Toast.makeText(
            this,
            "Successfully data in database",
            Toast.LENGTH_SHORT
        ).show()
    }

    private suspend fun fetchLeagueClubs(inputText: String): String {
        var keyword = ""
        var allLeagueClubs =""
        val splitted_input = inputText.split(" ")
        for (i in 0 until splitted_input.size) {
            keyword += "${splitted_input[i]}%20"
        }
        try {
            val url_string =
                "https://www.thesportsdb.com/api/v1/json/3/search_all_teams.php?l=${keyword}"
            val url = URL(url_string)
            val con: HttpURLConnection = url.openConnection() as HttpURLConnection

            // collecting all the JSON string
            var stb = StringBuilder()

            // run the code of the launched coroutine in a new thread
            withContext(Dispatchers.IO) {
                var bf = BufferedReader(InputStreamReader(con.inputStream))
                var line: String? = bf.readLine()
                while (line != null) {
                    // keep reading until no more lines of text
                    stb.append(line + "\n")
                    line = bf.readLine()
                }
            }
            allLeagueClubs = parseJSON(stb)
        } catch (e: Exception){
            Toast.makeText(
                this,
                "Enter a valid input",
                Toast.LENGTH_SHORT
            ).show()
        }

        return allLeagueClubs
    }

    fun parseJSON(stb: StringBuilder): String {
        var mapOfExtractedKeyAndValues = mutableMapOf<String, String>()
        val keysToExtract = listOf(
            "idTeam", "strTeam", "strTeamShort", "strAlternate", "intFormedYear",
            "strLeague", "idLeague", "strStadium", "strKeywords", "strStadiumThumb",
            "strStadiumLocation", "intStadiumCapacity", "strWebsite", "strTeamJersey",
            "strTeamLogo"
        )
// this contains the full JSON returned by the Web Service
        val json = JSONObject(stb.toString())
// Information about all the clubs in this league extracted by this function
        var allLeagueClubs = StringBuilder()
        var jsonArray: JSONArray = json.getJSONArray("teams")
// extract all the leagues from the JSON array
        jsonArray.let {
            for (i in 0 until jsonArray.length()) {
                try {
                    val league: JSONObject = jsonArray[i] as JSONObject
                    keysToExtract.forEach { key ->
                        if (!key.equals("strTeam")) {
                            if (!league.isNull(key)) {
                                val value = league[key] as String
                                allLeagueClubs.append("$key : $value\n")
                                mapOfExtractedKeyAndValues.put(key, value)
                            }
                        } else {
                            if (!league.isNull(key)) {
                                val value = league[key] as String
                                allLeagueClubs.append("Name : $value\n")
                                mapOfExtractedKeyAndValues.put("Name", value)
                            }
                        }
                    }
                } catch (jen: JSONException) {
                    // Handle JSONException if needed
                }

                InforToSaveToDatabse.add(mapOfExtractedKeyAndValues)
                mapOfExtractedKeyAndValues = mutableMapOf()
                allLeagueClubs.append("\n\n")
            }

        }
        return allLeagueClubs.toString()
    }


}


