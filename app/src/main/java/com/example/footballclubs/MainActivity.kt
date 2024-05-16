package com.example.footballclubs
//https://drive.google.com/file/d/1yyWEdhxqv5t3QWkVdiJYMhCL2Igv9isA/view?usp=sharing

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import androidx.room.Room
import com.example.footballclubs.data.ClubsDao
import com.example.footballclubs.data.LeagueDatabase
import com.example.footballclubs.data.Leagues
import com.example.footballclubs.data.LeaguesDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

lateinit var db: LeagueDatabase
lateinit var leaguesDao: LeaguesDao
lateinit var clubsDao: ClubsDao

class MainActivity : ComponentActivity() {
    var ClubInfoList = mutableListOf<Map<String, Any>>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        db = Room.databaseBuilder(this, LeagueDatabase::class.java, "leagueDatabase").build()
        leaguesDao = db.leaguesDao()
        clubsDao = db.clubsDao()
        setContent {
            GUI()
        }
    }

    @Composable
    fun GUI() {
        mainContentColumn()

    }

    @Composable
    fun mainContentColumn() {
        var inputText: String by rememberSaveable {
            mutableStateOf("")
        }
        var isClubInfoShowing by rememberSaveable {
            mutableStateOf(false)
        }

        val scope = rememberCoroutineScope()
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            item {
                Column {

                    Button(
                        onClick = { addLeaguesToDb(scope) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        colors = ButtonDefaults.buttonColors(Color(0xFF2894F4))
                    ) {
                        Text(
                            text = "Add Leagues to DB",
                            color = Color.White
                        )
                    }
                    Button(
                        onClick = {
                            Intent(applicationContext, SearchForClubsByLeague::class.java).also {
                                startActivity(it)
                            }
                        }, modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        colors = ButtonDefaults.buttonColors(Color(0xFF2894F4))
                    ) {
                        Text(text = "Search for Clubs By League", color = Color.White)
                    }
                    Button(
                        onClick = {
                            Intent(applicationContext, SearchForClubs::class.java).also {
                                startActivity(it)
                            }
                        }, modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        colors = ButtonDefaults.buttonColors(Color(0xFF2894F4))
                    ) {
                        Text(text = "Search for Clubs", color = Color.White)
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceAround

                    ) {
                        OutlinedTextField(
                            modifier = Modifier.width(200.dp),
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
                        Spacer(modifier = Modifier.width(10.dp))
                        Button(

                            onClick = {
                                isClubInfoShowing = false
                                ClubInfoList.clear()
                                scope.launch(Dispatchers.IO) {
                                    fetchClubsFromUrl(inputText, scope)
                                    inputText = ""
                                    isClubInfoShowing = true

                                }
                            },
                            colors = ButtonDefaults.buttonColors(Color(0xFF2894F4)),

                            ) {
                            Text(text = "Search ", color = Color.White)
                        }
                    }
                    if (isClubInfoShowing) {

                        processImages()


                    }
                }

            }
        }
    }


    @Composable
    private fun processImages() {
        ClubInfoList.forEach { e ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp, 10.dp)
                    .wrapContentHeight(align = Alignment.CenterVertically)
                    .shadow(ambientColor = Color.DarkGray, elevation = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White, //Card background color
                    contentColor = Color.Black  //Card content color
                ),

                shape = RoundedCornerShape(8.dp),
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = e["strTeam"].toString())
                    val jerseyList = runBlocking { (e["Jersey"] as Deferred<List<String>>).await() }

                    jerseyList.forEach { jerseyUrl ->
                        var bitmap by rememberSaveable { mutableStateOf<Bitmap?>(null) }
                        LaunchedEffect(Unit) {
                            val url = URL(jerseyUrl)
                            val bmp: Bitmap = withContext(Dispatchers.IO) {
                                BitmapFactory.decodeStream(url.openConnection().getInputStream())
                            }
                            bitmap = bmp
                        }

                        Text(text = jerseyUrl)
                        bitmap?.let {
                            Image(
                                bitmap = it.asImageBitmap(),
                                contentDescription = "Club Logo",
                                modifier = Modifier.size(100.dp)
                            )
                        }
                    }
                }
            }
        }
    }


    fun addLeaguesToDb(scope: CoroutineScope) {

        var leaguesJsonObj = JSONObject(readJsonFile()) //  Get json content as its obj
        var array = leaguesJsonObj.getJSONArray("leagues")
        for (i in 0 until array.length()) {
            val leagueObj = array.getJSONObject(i)
            val idLeague = leagueObj.getString("idLeague")
            val strLeague = leagueObj.getString("strLeague")
            val strSport = leagueObj.getString("strSport")
            val strLeagueAlternate = leagueObj.getString("strLeagueAlternate")

            try {
                scope.launch {
                    leaguesDao.insertLeagues(
                        Leagues(
                            idLeague = idLeague,
                            strLeague = strLeague,
                            strSport = strSport,
                            strLeagueAlternate = strLeagueAlternate
                        )
                    )
                }

            } catch (e: Exception) {
                Toast.makeText(
                    this,
                    "Unexpected error occurred",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        Toast.makeText(
            this,
            "Successfully data in database",
            Toast.LENGTH_SHORT
        ).show()

    }

    fun readJsonFile(): String {
        return try {
            var inputStream: InputStream = assets.open("football_leagues.json") // Open JSON file
            inputStream.bufferedReader().use { it.readText() } // Read JSON file
        } catch (e: IOException) {
            e.printStackTrace()
            ""
        }
    }

    //  API Section
    private suspend fun fetchClubsFromUrl(inputText: String, scope: CoroutineScope) {
        var keyword = ""
        val splitted_input = inputText.split(" ")
        for (i in 0 until splitted_input.size) {
            keyword += "${splitted_input[i]}%20"
        }
        val url_string =
            "https://www.thesportsdb.com/api/v1/json/3/searchteams.php?t=\${inputText"
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
        parseJSON(stb, scope)

    }

    fun parseJSON(stb: StringBuilder, scope: CoroutineScope) {
        var mapOfExtractedKeyAndValues = mutableMapOf<String, Any>()
        val keysToExtract = listOf(
            "idTeam", "strTeam"
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
                    val teamJsonObj: JSONObject = jsonArray[i] as JSONObject
                    keysToExtract.forEach { key ->
                        if (key.equals("idTeam")) {
                            if (!teamJsonObj.isNull(key)) {
                                val value = teamJsonObj[key] as String
                                mapOfExtractedKeyAndValues.put(
                                    "Jersey",
                                    scope.async(Dispatchers.IO) { fetchJerseyUrls(value) }
                                )
                            }
                        } else {
                            if (!teamJsonObj.isNull(key)) {
                                val value = teamJsonObj[key] as String

                                mapOfExtractedKeyAndValues.put(key, value)
                            }
                        }

                    }
                } catch (jen: JSONException) {
                    // Handle JSONException if needed
                }

                ClubInfoList.add(mapOfExtractedKeyAndValues)
                mapOfExtractedKeyAndValues = mutableMapOf()

            }

        }

    }

    //  Jersey URL Fetch
    private suspend fun fetchJerseyUrls(teamId: String): List<String> {

        val url_string =
            "https://www.thesportsdb.com/api/v1/json/3/lookupequipment.php?id=${teamId}"
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

        return parseJSONJersey(stb)
    }

    fun parseJSONJersey(stb: StringBuilder): List<String> {
        var list = mutableListOf<String>()

// this contains the full JSON returned by the Web Service
        val json = JSONObject(stb.toString())
// Information about all the clubs in this league extracted by this function
        var allLeagueClubs = StringBuilder()
        var jsonArray: JSONArray = json.getJSONArray("equipment")
// extract all the leagues from the JSON array
        jsonArray.let {
            for (i in 0 until jsonArray.length()) {
                try {
                    val teamJsonObj: JSONObject = jsonArray[i] as JSONObject


                    if (!teamJsonObj.isNull("strEquipment")) {
                        val value = teamJsonObj["strEquipment"] as String
                        if (value.contains(","))
                            list.addAll(value.split(","))
                        else list.add(value)

                    }


                } catch (jen: JSONException) {
                    // Handle JSONException if needed
                }


            }

        }
        return list
    }
}